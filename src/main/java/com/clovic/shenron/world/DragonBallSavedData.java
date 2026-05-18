package com.clovic.shenron.world;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DragonBallSavedData extends SavedData {
    private static final String DATA_NAME = "shenron_dragon_balls";
    private final Map<Integer, BallLocation> locations = new HashMap<>();
    private boolean initialized;

    public static DragonBallSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(DragonBallSavedData::load, DragonBallSavedData::new, DATA_NAME);
    }

    public static DragonBallSavedData load(CompoundTag tag) {
        DragonBallSavedData data = new DragonBallSavedData();
        data.initialized = tag.getBoolean("Initialized");
        ListTag list = tag.getList("Balls", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            int star = entry.getInt("Star");
            if (star >= 1 && star <= 7) {
                data.locations.put(star, new BallLocation(
                        star,
                        new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")),
                        entry.getBoolean("InWorld")
                ));
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("Initialized", initialized);
        ListTag list = new ListTag();
        locations.values().stream()
                .sorted((left, right) -> Integer.compare(left.star(), right.star()))
                .forEach(location -> {
                    CompoundTag entry = new CompoundTag();
                    entry.putInt("Star", location.star());
                    entry.putInt("X", location.pos().getX());
                    entry.putInt("Y", location.pos().getY());
                    entry.putInt("Z", location.pos().getZ());
                    entry.putBoolean("InWorld", location.inWorld());
                    list.add(entry);
                });
        tag.put("Balls", list);
        return tag;
    }

    public boolean initialized() {
        return initialized;
    }

    public void setInitialized() {
        initialized = true;
        setDirty();
    }

    public Optional<BallLocation> nearestInWorld(BlockPos origin) {
        return locations.values().stream()
                .filter(BallLocation::inWorld)
                .min((left, right) -> Double.compare(left.pos().distSqr(origin), right.pos().distSqr(origin)));
    }

    public void setInWorld(int star, BlockPos pos) {
        if (star >= 1 && star <= 7) {
            locations.put(star, new BallLocation(star, pos, true));
            setDirty();
        }
    }

    public void markCollected(int star) {
        BallLocation location = locations.get(star);
        if (location != null) {
            locations.put(star, new BallLocation(star, location.pos(), false));
            setDirty();
        }
    }

    public record BallLocation(int star, BlockPos pos, boolean inWorld) {
    }
}
