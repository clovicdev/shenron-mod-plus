package com.clovic.shenron.world;

import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.entity.ShenronEntity;
import com.clovic.shenron.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SummonRituals {
    private static final DustParticleOptions GOLD_LIGHT = new DustParticleOptions(new Vector3f(1.0F, 0.78F, 0.12F), 1.45F);
    private static final int SHENRON_RISE_TICK = 62;
    private static final int FINISH_TICK = 92;
    private static final double[][] BALL_PATTERN = {
            {-0.58D, -0.78D},
            {0.58D, -0.78D},
            {-1.16D, 0.0D},
            {0.0D, 0.0D},
            {1.16D, 0.0D},
            {-0.58D, 0.78D},
            {0.58D, 0.78D}
    };

    private static final List<Ritual> ACTIVE = new ArrayList<>();

    private SummonRituals() {
    }

    public static void begin(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Facing facing = Facing.from(player.getYRot());
        Vec3 projectedCenter = player.position().add(facing.forward().scale(3.2D));
        BlockPos center = surfaceAt(level, projectedCenter.x, projectedCenter.z);

        clearCeremonialBalls(level, Vec3.atCenterOf(center), 10.0D);
        for (int star = 1; star <= 7; star++) {
            Vec3 offset = facing.right().scale(BALL_PATTERN[star - 1][0]).add(facing.forward().scale(BALL_PATTERN[star - 1][1]));
            BlockPos pos = surfaceAt(level, center.getX() + 0.5D + offset.x, center.getZ() + 0.5D + offset.z);
            DragonBallEntity ball = ModEntities.DRAGON_BALL.get().create(level);
            if (ball != null) {
                ball.setStar(star);
                ball.setCeremonial(true);
                ball.moveTo(pos.getX() + 0.5D, pos.getY() + 0.18D, pos.getZ() + 0.5D, player.getYRot(), 0.0F);
                level.addFreshEntity(ball);
            }
        }

        ACTIVE.add(new Ritual(level, center, player.getYRot(), Mth.wrapDegrees(player.getYRot() + 180.0F)));
        level.playSound(null, center, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 0.75F);
        level.playSound(null, center, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 0.55F);
        level.sendParticles(ParticleTypes.END_ROD, center.getX() + 0.5D, center.getY() + 0.35D, center.getZ() + 0.5D, 80, 1.6D, 0.2D, 1.6D, 0.035D);
        player.displayClientMessage(Component.translatable("message.shenron.ritual_started").withStyle(ChatFormatting.GREEN), false);
    }

    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }

        Iterator<Ritual> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Ritual ritual = iterator.next();
            if (ritual.tick()) {
                iterator.remove();
            }
        }
    }

    public static void clearCeremonialBalls(ServerLevel level, Vec3 center, double radius) {
        AABB area = AABB.ofSize(center, radius * 2.0D, radius * 2.0D, radius * 2.0D);
        level.getEntitiesOfClass(DragonBallEntity.class, area, DragonBallEntity::isCeremonial)
                .forEach(DragonBallEntity::discard);
    }

    private static BlockPos surfaceAt(ServerLevel level, double x, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int blockY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockX, blockZ);
        return new BlockPos(blockX, blockY, blockZ);
    }

    private record Facing(Vec3 forward, Vec3 right) {
        static Facing from(float yawDegrees) {
            double yaw = Math.toRadians(yawDegrees);
            Vec3 forward = new Vec3(-Mth.sin((float) yaw), 0.0D, Mth.cos((float) yaw)).normalize();
            Vec3 right = new Vec3(forward.z, 0.0D, -forward.x).normalize();
            return new Facing(forward, right);
        }
    }

    private static final class Ritual {
        private final ServerLevel level;
        private final BlockPos center;
        private final float patternYaw;
        private final float shenronYaw;
        private int age;
        private boolean spawned;

        private Ritual(ServerLevel level, BlockPos center, float patternYaw, float shenronYaw) {
            this.level = level;
            this.center = center;
            this.patternYaw = patternYaw;
            this.shenronYaw = shenronYaw;
        }

        private boolean tick() {
            age++;

            double x = center.getX() + 0.5D;
            double y = center.getY() + 0.25D;
            double z = center.getZ() + 0.5D;
            double radius = 0.7D + Math.min(age, SHENRON_RISE_TICK) * 0.025D;
            level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y + 0.25D, z, 4, radius, 0.05D, radius, 0.01D);

            if (age % 5 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, x, y + 0.1D, z, 14, 1.55D, 0.12D, 1.55D, 0.025D);
            }
            emitDragonBallGlow();

            if (!spawned && age >= SHENRON_RISE_TICK) {
                spawned = true;
                spawnShenron();
            }

            return age > FINISH_TICK;
        }

        private void spawnShenron() {
            ShenronEntity shenron = ModEntities.SHENRON.get().create(level);
            if (shenron == null) {
                return;
            }

            double x = center.getX() + 0.5D;
            double startY = center.getY() - 10.0D;
            double targetY = center.getY() + 15.5D;
            double z = center.getZ() + 0.5D;
            shenron.moveTo(x, startY, z, shenronYaw, 0.0F);
            shenron.startSummoning(targetY);
            level.addFreshEntity(shenron);
            level.playSound(null, center, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0F, 0.68F);
            level.sendParticles(ParticleTypes.FLASH, x, center.getY() + 0.7D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            level.sendParticles(ParticleTypes.CLOUD, x, center.getY() + 0.4D, z, 90, 1.9D, 0.4D, 1.9D, 0.08D);
        }

        private void emitDragonBallGlow() {
            Facing facing = Facing.from(patternYaw);
            double shenronY = center.getY() - 7.0D + Math.min(age, SHENRON_RISE_TICK) / (double) SHENRON_RISE_TICK * 18.0D;
            Vec3 shenronPoint = new Vec3(center.getX() + 0.5D, shenronY, center.getZ() + 0.5D);

            for (double[] pattern : BALL_PATTERN) {
                Vec3 offset = facing.right().scale(pattern[0]).add(facing.forward().scale(pattern[1]));
                Vec3 ballPoint = new Vec3(center.getX() + 0.5D + offset.x, center.getY() + 0.42D, center.getZ() + 0.5D + offset.z);
                level.sendParticles(GOLD_LIGHT, ballPoint.x, ballPoint.y, ballPoint.z, 2, 0.16D, 0.08D, 0.16D, 0.0D);
                if (age % 2 == 0) {
                    drawLine(ballPoint, shenronPoint, 9);
                }
            }
        }

        private void drawLine(Vec3 start, Vec3 end, int steps) {
            for (int i = 0; i <= steps; i++) {
                Vec3 point = start.lerp(end, i / (double) steps);
                level.sendParticles(GOLD_LIGHT, point.x, point.y, point.z, 1, 0.025D, 0.025D, 0.025D, 0.0D);
            }
        }
    }
}
