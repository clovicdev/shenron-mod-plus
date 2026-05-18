package com.clovic.shenron.entity;

import com.clovic.shenron.registry.ModItems;
import com.clovic.shenron.world.DragonBallSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

public class DragonBallEntity extends Entity {
    private static final DustParticleOptions GOLD_TRAIL = new DustParticleOptions(new Vector3f(1.0F, 0.74F, 0.12F), 1.35F);
    private static final EntityDataAccessor<Integer> STAR = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SCATTERING = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CEREMONIAL = SynchedEntityData.defineId(DragonBallEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int SCATTER_TICKS = 82;

    private BlockPos scatterTarget = BlockPos.ZERO;
    private Vec3 scatterVelocity = Vec3.ZERO;
    private int scatterTicks;

    public DragonBallEntity(EntityType<? extends DragonBallEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(STAR, 1);
        entityData.define(SCATTERING, false);
        entityData.define(CEREMONIAL, false);
    }

    public int getStar() {
        return entityData.get(STAR);
    }

    public void setStar(int star) {
        entityData.set(STAR, Math.max(1, Math.min(7, star)));
    }

    public boolean isScattering() {
        return entityData.get(SCATTERING);
    }

    public boolean isCeremonial() {
        return entityData.get(CEREMONIAL);
    }

    public void setCeremonial(boolean ceremonial) {
        entityData.set(CEREMONIAL, ceremonial);
    }

    public void startScatter(BlockPos target, Vec3 velocity) {
        scatterTarget = target;
        scatterVelocity = velocity;
        scatterTicks = 0;
        entityData.set(SCATTERING, true);
        setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();
        if (isScattering()) {
            tickScatter();
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (isScattering() || isCeremonial()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = new ItemStack(ModItems.dragonBall(getStar()));
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }

        if (level() instanceof ServerLevel serverLevel) {
            DragonBallSavedData.get(serverLevel).markCollected(getStar());
        }
        level().playSound(null, blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.7F, 1.3F);
        discard();
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setStar(tag.getInt("Star"));
        entityData.set(SCATTERING, tag.getBoolean("Scattering"));
        entityData.set(CEREMONIAL, tag.getBoolean("Ceremonial"));
        scatterTicks = tag.getInt("ScatterTicks");
        scatterTarget = new BlockPos(tag.getInt("TargetX"), tag.getInt("TargetY"), tag.getInt("TargetZ"));
        scatterVelocity = new Vec3(tag.getDouble("VelocityX"), tag.getDouble("VelocityY"), tag.getDouble("VelocityZ"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Star", getStar());
        tag.putBoolean("Scattering", isScattering());
        tag.putBoolean("Ceremonial", isCeremonial());
        tag.putInt("ScatterTicks", scatterTicks);
        tag.putInt("TargetX", scatterTarget.getX());
        tag.putInt("TargetY", scatterTarget.getY());
        tag.putInt("TargetZ", scatterTarget.getZ());
        tag.putDouble("VelocityX", scatterVelocity.x);
        tag.putDouble("VelocityY", scatterVelocity.y);
        tag.putDouble("VelocityZ", scatterVelocity.z);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 4096.0D;
    }

    private void tickScatter() {
        if (level().isClientSide) {
            return;
        }

        scatterTicks++;
        Vec3 previous = position();
        Vec3 drift = scatterVelocity.add(0.0D, scatterTicks * 0.004D, 0.0D);
        setDeltaMovement(drift);
        move(MoverType.SELF, drift);
        emitScatterTrail(previous);

        if (scatterTicks >= SCATTER_TICKS && level() instanceof ServerLevel serverLevel) {
            serverLevel.getChunk(scatterTarget);
            moveTo(scatterTarget.getX() + 0.5D, scatterTarget.getY() + 0.65D, scatterTarget.getZ() + 0.5D, getYRot(), getXRot());
            setDeltaMovement(Vec3.ZERO);
            entityData.set(SCATTERING, false);
        }
    }

    private void emitScatterTrail(Vec3 previous) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 current = position().add(0.0D, 0.35D, 0.0D);
        Vec3 start = previous.add(0.0D, 0.35D, 0.0D);
        for (int i = 0; i <= 5; i++) {
            Vec3 point = current.lerp(start, i / 5.0D);
            serverLevel.sendParticles(GOLD_TRAIL, point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
        }
        if (scatterTicks % 3 == 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, current.x, current.y, current.z, 2, 0.04D, 0.04D, 0.04D, 0.0D);
        }
    }
}
