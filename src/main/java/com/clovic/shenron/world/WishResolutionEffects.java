package com.clovic.shenron.world;

import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.registry.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public final class WishResolutionEffects {
    private static final DustParticleOptions GOLD_LIGHT = new DustParticleOptions(new Vector3f(1.0F, 0.78F, 0.12F), 1.55F);
    private static final int FORM_TICK = 42;
    private static final int LAUNCH_TICK = 72;
    private static final int FINISH_TICK = 96;
    private static final double[][] BALL_PATTERN = {
            {-0.58D, -0.78D},
            {0.58D, -0.78D},
            {-1.16D, 0.0D},
            {0.0D, 0.0D},
            {1.16D, 0.0D},
            {-0.58D, 0.78D},
            {0.58D, 0.78D}
    };

    private static final List<Resolution> ACTIVE = new ArrayList<>();

    private WishResolutionEffects() {
    }

    public static void begin(ServerLevel level, Vec3 shenronCenter, ServerPlayer player) {
        ACTIVE.add(new Resolution(level, shenronCenter, player.getUUID()));
        level.playSound(null, shenronCenter.x, shenronCenter.y, shenronCenter.z, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.1F, 1.45F);
        level.sendParticles(ParticleTypes.FLASH, shenronCenter.x, shenronCenter.y + 1.5D, shenronCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(GOLD_LIGHT, shenronCenter.x, shenronCenter.y + 1.5D, shenronCenter.z, 120, 4.0D, 5.0D, 4.0D, 0.14D);
    }

    public static void tick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || ACTIVE.isEmpty()) {
            return;
        }

        Iterator<Resolution> iterator = ACTIVE.iterator();
        while (iterator.hasNext()) {
            Resolution resolution = iterator.next();
            if (resolution.tick()) {
                iterator.remove();
            }
        }
    }

    private static final class Resolution {
        private final ServerLevel level;
        private final Vec3 shenronCenter;
        private final Vec3 gatherCenter;
        private final UUID playerId;
        private final List<DragonBallEntity> formedBalls = new ArrayList<>();
        private int age;
        private boolean formed;
        private boolean launched;

        private Resolution(ServerLevel level, Vec3 shenronCenter, UUID playerId) {
            this.level = level;
            this.shenronCenter = shenronCenter;
            this.gatherCenter = shenronCenter.add(0.0D, -8.5D, 0.0D);
            this.playerId = playerId;
        }

        private boolean tick() {
            age++;
            emitDissolveLight();

            if (!formed && age >= FORM_TICK) {
                formed = true;
                formDragonBalls();
            }

            if (formed && !launched) {
                pulseFormedBalls();
            }

            if (!launched && age >= LAUNCH_TICK) {
                launched = true;
                launchDragonBalls();
            }

            return age > FINISH_TICK;
        }

        private void emitDissolveLight() {
            double progress = Mth.clamp(age / (double) FORM_TICK, 0.0D, 1.0D);
            Vec3 streamStart = shenronCenter.add(0.0D, 2.0D + Mth.sin(age * 0.18F) * 1.2D, 0.0D);
            int beams = 7;
            for (int i = 0; i < beams; i++) {
                double angle = (Math.PI * 2.0D / beams) * i + age * 0.08D;
                Vec3 start = streamStart.add(Math.cos(angle) * 3.2D, Math.sin(age * 0.13D + i) * 1.4D, Math.sin(angle) * 3.2D);
                Vec3 end = gatherCenter.add(BALL_PATTERN[i][0], 0.35D, BALL_PATTERN[i][1]);
                Vec3 point = start.lerp(end, progress);
                level.sendParticles(GOLD_LIGHT, point.x, point.y, point.z, 3, 0.18D, 0.18D, 0.18D, 0.02D);
                if (age % 3 == 0) {
                    drawLine(start.lerp(end, Math.max(0.0D, progress - 0.25D)), point, 4);
                }
            }

            level.sendParticles(ParticleTypes.END_ROD, shenronCenter.x, shenronCenter.y + 1.4D, shenronCenter.z, 12, 3.2D, 4.6D, 3.2D, 0.04D);
            level.sendParticles(GOLD_LIGHT, gatherCenter.x, gatherCenter.y + 0.45D, gatherCenter.z, 10, 1.3D, 0.35D, 1.3D, 0.025D);
        }

        private void formDragonBalls() {
            for (int star = 1; star <= 7; star++) {
                DragonBallEntity ball = ModEntities.DRAGON_BALL.get().create(level);
                if (ball == null) {
                    continue;
                }

                Vec3 pos = gatherCenter.add(BALL_PATTERN[star - 1][0], 0.0D, BALL_PATTERN[star - 1][1]);
                ball.setStar(star);
                ball.setCeremonial(true);
                ball.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(ball);
                formedBalls.add(ball);
            }
            level.playSound(null, gatherCenter.x, gatherCenter.y, gatherCenter.z, SoundEvents.AMETHYST_CLUSTER_HIT, SoundSource.PLAYERS, 1.0F, 0.55F);
            level.sendParticles(ParticleTypes.FLASH, gatherCenter.x, gatherCenter.y + 0.3D, gatherCenter.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }

        private void pulseFormedBalls() {
            for (DragonBallEntity ball : formedBalls) {
                if (!ball.isAlive()) {
                    continue;
                }
                level.sendParticles(GOLD_LIGHT, ball.getX(), ball.getY() + 0.35D, ball.getZ(), 3, 0.18D, 0.18D, 0.18D, 0.01D);
                drawLine(shenronCenter.add(0.0D, 1.2D, 0.0D), ball.position().add(0.0D, 0.28D, 0.0D), 7);
            }
        }

        private void launchDragonBalls() {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(playerId);
            if (player == null) {
                formedBalls.forEach(DragonBallEntity::discard);
                return;
            }

            formedBalls.forEach(ball -> ball.setCeremonial(false));
            DragonBallSpawner.scatterExistingFrom(level, gatherCenter.add(0.0D, 0.6D, 0.0D), player, formedBalls);
            level.playSound(null, gatherCenter.x, gatherCenter.y, gatherCenter.z, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 1.0F, 0.8F);
            level.sendParticles(GOLD_LIGHT, gatherCenter.x, gatherCenter.y + 0.5D, gatherCenter.z, 90, 1.8D, 0.7D, 1.8D, 0.08D);
        }

        private void drawLine(Vec3 start, Vec3 end, int steps) {
            for (int i = 0; i <= steps; i++) {
                double t = i / (double) steps;
                Vec3 point = start.lerp(end, t);
                level.sendParticles(GOLD_LIGHT, point.x, point.y, point.z, 1, 0.035D, 0.035D, 0.035D, 0.0D);
            }
        }
    }
}
