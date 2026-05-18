package com.clovic.shenron.entity;

import com.clovic.shenron.world.SummonRituals;
import com.clovic.shenron.world.WishParser;
import com.clovic.shenron.world.WishResolutionEffects;
import com.clovic.shenron.world.WishSettingsSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ShenronEntity extends PathfinderMob {
    private static final double WISH_RANGE = 96.0D;
    private static final int SUMMON_ANIMATION_TICKS = 160;
    private static final EntityDataAccessor<Integer> SUMMON_TICKS = SynchedEntityData.defineId(ShenronEntity.class, EntityDataSerializers.INT);

    private int summonTicks;
    private double summonStartY;
    private double summonTargetY;

    public ShenronEntity(EntityType<? extends ShenronEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoGravity(true);
        setInvulnerable(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.FOLLOW_RANGE, 128.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SUMMON_TICKS, 0);
    }

    public static boolean handleChat(ServerPlayer player, String message) {
        ServerLevel level = player.serverLevel();
        Optional<ShenronEntity> shenron = level.getEntitiesOfClass(
                        ShenronEntity.class,
                        player.getBoundingBox().inflate(WISH_RANGE),
                        dragon -> dragon.isAlive() && dragon.distanceToSqr(player) <= WISH_RANGE * WISH_RANGE
                )
                .stream()
                .min(Comparator.comparingDouble(dragon -> dragon.distanceToSqr(player)));

        if (shenron.isEmpty() || !shenron.get().isClosestPlayer(player)) {
            return false;
        }

        return shenron.get().grantWish(player, message);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 96.0F));
        goalSelector.addGoal(1, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);
        setDeltaMovement(Vec3.ZERO);

        if (level() instanceof ServerLevel serverLevel) {
            tickSummonAnimation(serverLevel);
            if (tickCount % 24 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 2.6D, getZ(), 10, 1.4D, 1.1D, 1.4D, 0.01D);
            }
            Player nearest = level().getNearestPlayer(this, WISH_RANGE);
            if (nearest != null) {
                getLookControl().setLookAt(nearest, 20.0F, 20.0F);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    public void startSummoning(double targetY) {
        summonTicks = SUMMON_ANIMATION_TICKS;
        summonStartY = getY();
        summonTargetY = targetY;
        entityData.set(SUMMON_TICKS, summonTicks);
    }

    public float summonProgress() {
        int ticks = entityData.get(SUMMON_TICKS);
        if (ticks <= 0) {
            return 1.0F;
        }
        return Mth.clamp(1.0F - ticks / (float) SUMMON_ANIMATION_TICKS, 0.0F, 1.0F);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SummonTicks", entityData.get(SUMMON_TICKS));
        tag.putDouble("SummonStartY", summonStartY);
        tag.putDouble("SummonTargetY", summonTargetY);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summonTicks = tag.getInt("SummonTicks");
        summonStartY = tag.getDouble("SummonStartY");
        summonTargetY = tag.getDouble("SummonTargetY");
        entityData.set(SUMMON_TICKS, summonTicks);
    }

    private boolean grantWish(ServerPlayer player, String message) {
        int maxWishStacks = level() instanceof ServerLevel serverLevel
                ? WishSettingsSavedData.get(serverLevel).maxWishStacks()
                : WishSettingsSavedData.DEFAULT_MAX_WISH_STACKS;
        List<ItemStack> rewards = WishParser.parseRequestedItems(message, maxWishStacks);
        if (rewards.isEmpty()) {
            player.displayClientMessage(Component.translatable("entity.shenron.shenron.unclear_wish").withStyle(ChatFormatting.GREEN), false);
            return true;
        }

        for (ItemStack reward : rewards) {
            if (!player.getInventory().add(reward.copy())) {
                player.drop(reward.copy(), false);
            }
        }

        if (level() instanceof ServerLevel serverLevel) {
            finishWish(serverLevel, player);
        }

        player.displayClientMessage(Component.translatable("entity.shenron.shenron.wish_granted").withStyle(ChatFormatting.GREEN), false);
        return true;
    }

    private void finishWish(ServerLevel serverLevel, ServerPlayer player) {
        serverLevel.sendParticles(ParticleTypes.FLASH, getX(), getY() + 2.0D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.5D, getZ(), 80, 3.4D, 1.6D, 3.4D, 0.07D);
        level().playSound(null, blockPosition(), SoundEvents.ENDER_DRAGON_DEATH, SoundSource.PLAYERS, 0.45F, 1.65F);
        SummonRituals.clearCeremonialBalls(serverLevel, position(), 10.0D);
        WishResolutionEffects.begin(serverLevel, position().add(0.0D, 2.0D, 0.0D), player);

        AABB cleanupArea = getBoundingBox().inflate(32.0D);
        serverLevel.getEntitiesOfClass(ShenronEntity.class, cleanupArea, dragon -> dragon.distanceToSqr(this) < 2048.0D)
                .forEach(ShenronEntity::removeAfterWish);
    }

    private void removeAfterWish() {
        ejectPassengers();
        stopRiding();
        setHealth(0.0F);
        remove(RemovalReason.KILLED);
    }

    private void tickSummonAnimation(ServerLevel level) {
        summonTicks = entityData.get(SUMMON_TICKS);
        if (summonTicks <= 0) {
            return;
        }

        int elapsed = SUMMON_ANIMATION_TICKS - summonTicks;
        double progress = elapsed / (double) SUMMON_ANIMATION_TICKS;
        double peakY = summonTargetY + 16.0D;
        double nextY;
        if (progress < 0.66D) {
            double climb = progress / 0.66D;
            double eased = 1.0D - Math.pow(1.0D - climb, 2.0D);
            nextY = summonStartY + (peakY - summonStartY) * eased;
        } else {
            double settle = (progress - 0.66D) / 0.34D;
            double eased = settle * settle * (3.0D - 2.0D * settle);
            nextY = peakY + (summonTargetY - peakY) * eased;
        }
        moveTo(getX(), nextY, getZ(), getYRot(), getXRot());

        double ring = 2.2D + progress * 4.2D;
        level.sendParticles(ParticleTypes.END_ROD, getX(), summonTargetY - 9.5D, getZ(), 7, ring, 0.12D, ring, 0.012D);
        if (summonTicks % 6 == 0) {
            level.sendParticles(ParticleTypes.CLOUD, getX(), nextY + 0.55D, getZ(), 18, 1.8D, 0.85D, 1.8D, 0.05D);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, getX(), summonTargetY - 7.5D, getZ(), 12, ring * 0.45D, 0.35D, ring * 0.45D, 0.02D);
        }

        summonTicks--;
        entityData.set(SUMMON_TICKS, summonTicks);
        if (summonTicks == 0) {
            moveTo(getX(), summonTargetY, getZ(), getYRot(), getXRot());
            level.sendParticles(ParticleTypes.FLASH, getX(), getY() + 1.7D, getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private boolean isClosestPlayer(ServerPlayer player) {
        AABB range = getBoundingBox().inflate(WISH_RANGE);
        return level().getEntitiesOfClass(ServerPlayer.class, range).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr))
                .map(closest -> closest.getUUID().equals(player.getUUID()))
                .orElse(false);
    }
}
