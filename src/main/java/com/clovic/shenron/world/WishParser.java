package com.clovic.shenron.world;

import com.clovic.shenron.ShenronMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WishParser {
    private static final Pattern NUMBER = Pattern.compile("(\\d{1,5})\\s*$");
    private static final int MAX_MATCHES = 12;

    private WishParser() {
    }

    public static List<ItemStack> parseRequestedItems(String rawMessage, int maxRewardStacks) {
        String message = normalize(rawMessage);
        if (message.isBlank()) {
            return List.of();
        }

        List<ItemMatch> matches = new ArrayList<>();
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
            if (key == null || item == Items.AIR || ShenronMod.MOD_ID.equals(key.getNamespace())) {
                continue;
            }

            String phrase = key.getPath().replace('_', ' ');
            addMatch(message, item, phrase, matches);
            addMatch(message, item, phrase + "s", matches);
        }

        boolean[] occupied = new boolean[message.length()];
        List<ItemMatch> selected = matches.stream()
                .sorted(Comparator.comparingInt((ItemMatch match) -> match.end() - match.start()).reversed())
                .filter(match -> reserve(match, occupied))
                .limit(MAX_MATCHES)
                .toList();

        List<ItemStack> rewards = new ArrayList<>();
        int remainingStacks = Math.max(1, maxRewardStacks);
        for (ItemMatch match : selected) {
            if (remainingStacks <= 0) {
                break;
            }

            int maxStack = Math.max(1, new ItemStack(match.item()).getMaxStackSize());
            int requested = requestedCount(message, match);
            int allowedByStackLimit = remainingStacks * maxStack;
            int remainingItems = Math.min(requested, allowedByStackLimit);

            while (remainingItems > 0 && remainingStacks > 0) {
                int count = Math.min(maxStack, remainingItems);
                rewards.add(new ItemStack(match.item(), count));
                remainingItems -= count;
                remainingStacks--;
            }
        }

        return rewards;
    }

    private static void addMatch(String message, Item item, String phrase, List<ItemMatch> matches) {
        if (phrase.length() < 3) {
            return;
        }

        int index = message.indexOf(phrase);
        while (index >= 0) {
            int end = index + phrase.length();
            if (hasBoundary(message, index - 1) && hasBoundary(message, end)) {
                matches.add(new ItemMatch(item, phrase, index, end));
            }
            index = message.indexOf(phrase, end);
        }
    }

    private static boolean reserve(ItemMatch match, boolean[] occupied) {
        for (int i = match.start(); i < match.end(); i++) {
            if (occupied[i]) {
                return false;
            }
        }
        for (int i = match.start(); i < match.end(); i++) {
            occupied[i] = true;
        }
        return true;
    }

    private static int requestedCount(String message, ItemMatch match) {
        OptionalInt explicit = numberBefore(message, match.start());
        if (explicit.isPresent()) {
            return clamp(explicit.getAsInt(), 1, 99999);
        }
        return 1;
    }

    private static OptionalInt numberBefore(String message, int start) {
        int from = Math.max(0, start - 12);
        Matcher matcher = NUMBER.matcher(message.substring(from, start).trim());
        if (matcher.find()) {
            return OptionalInt.of(Integer.parseInt(matcher.group(1)));
        }
        return OptionalInt.empty();
    }

    private static boolean hasBoundary(String message, int index) {
        return index < 0 || index >= message.length() || !Character.isLetterOrDigit(message.charAt(index));
    }

    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record ItemMatch(Item item, String phrase, int start, int end) {
    }
}
