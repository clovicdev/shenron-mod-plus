package com.clovic.shenron.world;

import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.item.DragonRadarItem;
import com.clovic.shenron.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public final class DragonBallSpawner {
    private static final int INITIAL_MIN_DISTANCE = 96;
    private static final int INITIAL_MAX_DISTANCE = 640;
    private static final int SCATTER_MIN_DISTANCE = 160;
    private static final int SCATTER_MAX_DISTANCE = 960;

    private DragonBallSpawner() {
    }

    public static void ensureInitialized(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(Level.OVERWORLD);
        if (level == null) {
            return;
        }

        DragonBallSavedData data = DragonBallSavedData.get(level);
        if (data.initialized()) {
            return;
        }

        RandomSource random = RandomSource.create(level.getSeed() ^ player.getUUID().getMostSignificantBits());
        BlockPos origin = player.blockPosition();
        for (int star = 1; star <= 7; star++) {
            BlockPos pos = findSurfacePosition(level, origin, random, INITIAL_MIN_DISTANCE, INITIAL_MAX_DISTANCE);
            spawnRestingBall(level, star, pos);
            data.setInWorld(star, pos);
        }
        data.setInitialized();
    }

    public static void updateRadar(net.minecraft.world.item.ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        ServerLevel level = player.serverLevel();
        if (level.dimension() != Level.OVERWORLD) {
            clearRadar(tag);
            return;
        }

        Optional<DragonBallSavedData.BallLocation> target = DragonBallSavedData.get(level).nearestInWorld(player.blockPosition());
        if (target.isEmpty()) {
            clearRadar(tag);
            return;
        }

        BlockPos pos = target.get().pos();
        tag.putBoolean(DragonRadarItem.HAS_TARGET, true);
        tag.putInt(DragonRadarItem.TARGET_X, pos.getX());
        tag.putInt(DragonRadarItem.TARGET_Y, pos.getY());
        tag.putInt(DragonRadarItem.TARGET_Z, pos.getZ());
        tag.putInt(DragonRadarItem.TARGET_STAR, target.get().star());
        tag.putInt(DragonRadarItem.TARGET_DISTANCE, Mth.floor(Math.sqrt(pos.distSqr(player.blockPosition()))));
    }

    public static void scatterFrom(ServerLevel level, Vec3 origin, ServerPlayer player) {
        DragonBallSavedData data = DragonBallSavedData.get(level);
        RandomSource random = RandomSource.create(level.getSeed() ^ level.getGameTime() ^ player.getUUID().getLeastSignificantBits());
        BlockPos center = BlockPos.containing(origin);

        for (int star = 1; star <= 7; star++) {
            BlockPos restingPos = findSurfacePosition(level, center, random, SCATTER_MIN_DISTANCE, SCATTER_MAX_DISTANCE);
            data.setInWorld(star, restingPos);

            DragonBallEntity entity = ModEntities.DRAGON_BALL.get().create(level);
            if (entity != null) {
                double angle = (Math.PI * 2.0D / 7.0D) * (star - 1);
                Vec3 launch = new Vec3(Math.cos(angle) * 0.35D, 0.55D + random.nextDouble() * 0.12D, Math.sin(angle) * 0.35D);
                entity.setStar(star);
                entity.moveTo(origin.x, origin.y + 1.2D, origin.z, random.nextFloat() * 360.0F, 0.0F);
                entity.startScatter(restingPos, launch);
                level.addFreshEntity(entity);
            }
        }

        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 1.35F);
    }

    public static void scatterExistingFrom(ServerLevel level, Vec3 origin, ServerPlayer player, List<DragonBallEntity> balls) {
        DragonBallSavedData data = DragonBallSavedData.get(level);
        RandomSource random = RandomSource.create(level.getSeed() ^ level.getGameTime() ^ player.getUUID().getMostSignificantBits());
        BlockPos center = BlockPos.containing(origin);

        for (DragonBallEntity entity : balls) {
            if (entity == null || !entity.isAlive()) {
                continue;
            }

            int star = entity.getStar();
            BlockPos restingPos = findSurfacePosition(level, center, random, SCATTER_MIN_DISTANCE, SCATTER_MAX_DISTANCE);
            data.setInWorld(star, restingPos);

            double angle = (Math.PI * 2.0D / 7.0D) * (star - 1);
            Vec3 launch = new Vec3(Math.cos(angle) * 0.48D, 0.82D + random.nextDouble() * 0.16D, Math.sin(angle) * 0.48D);
            entity.moveTo(entity.getX(), entity.getY() + 0.18D, entity.getZ(), random.nextFloat() * 360.0F, 0.0F);
            entity.startScatter(restingPos, launch);
        }

        level.playSound(null, origin.x, origin.y, origin.z, SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0F, 1.55F);
    }

    private static void clearRadar(CompoundTag tag) {
        tag.putBoolean(DragonRadarItem.HAS_TARGET, false);
        tag.remove(DragonRadarItem.TARGET_X);
        tag.remove(DragonRadarItem.TARGET_Y);
        tag.remove(DragonRadarItem.TARGET_Z);
        tag.remove(DragonRadarItem.TARGET_STAR);
        tag.remove(DragonRadarItem.TARGET_DISTANCE);
    }

    private static void spawnRestingBall(ServerLevel level, int star, BlockPos pos) {
        DragonBallEntity entity = ModEntities.DRAGON_BALL.get().create(level);
        if (entity == null) {
            return;
        }
        entity.setStar(star);
        entity.moveTo(pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D, level.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(entity);
    }

    private static BlockPos findSurfacePosition(ServerLevel level, BlockPos origin, RandomSource random, int minDistance, int maxDistance) {
        for (int attempt = 0; attempt < 80; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = minDistance + random.nextInt(Math.max(1, maxDistance - minDistance));
            int x = origin.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = origin.getZ() + Mth.floor(Math.sin(angle) * distance);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState floor = level.getBlockState(pos.below());
            if (!floor.isAir() && floor.getFluidState().isEmpty()) {
                return pos;
            }
        }

        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        return new BlockPos(origin.getX(), y, origin.getZ());
    }
}
