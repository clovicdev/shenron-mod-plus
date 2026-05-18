package com.clovic.shenron.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class WishSettingsSavedData extends SavedData {
    public static final int DEFAULT_MAX_WISH_STACKS = 2;
    private static final int MAX_WISH_STACKS = 54;
    private static final String DATA_NAME = "shenron_wish_settings";

    private int maxWishStacks = DEFAULT_MAX_WISH_STACKS;

    public static WishSettingsSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(WishSettingsSavedData::load, WishSettingsSavedData::new, DATA_NAME);
    }

    public static WishSettingsSavedData load(CompoundTag tag) {
        WishSettingsSavedData data = new WishSettingsSavedData();
        data.maxWishStacks = clamp(tag.contains("MaxWishStacks") ? tag.getInt("MaxWishStacks") : DEFAULT_MAX_WISH_STACKS);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("MaxWishStacks", maxWishStacks);
        return tag;
    }

    public int maxWishStacks() {
        return maxWishStacks;
    }

    public void setMaxWishStacks(int maxWishStacks) {
        int clamped = clamp(maxWishStacks);
        if (this.maxWishStacks != clamped) {
            this.maxWishStacks = clamped;
            setDirty();
        }
    }

    private static int clamp(int value) {
        return Math.max(1, Math.min(MAX_WISH_STACKS, value));
    }
}
