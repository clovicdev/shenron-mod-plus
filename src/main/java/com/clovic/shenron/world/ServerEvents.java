package com.clovic.shenron.world;

import com.clovic.shenron.command.AdminCommands;
import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.entity.ShenronEntity;
import com.clovic.shenron.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DragonBallSpawner.ensureInitialized(player);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        AdminCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        SummonRituals.tick(event);
        WishResolutionEffects.tick(event);
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String message = event.getRawText();

        if (ShenronEntity.handleChat(player, message)) {
            event.setCanceled(true);
            return;
        }

        if (!isSummonPhrase(message)) {
            return;
        }

        if (player.serverLevel().dimension() != Level.OVERWORLD) {
            player.displayClientMessage(Component.translatable("message.shenron.overworld_only").withStyle(ChatFormatting.GOLD), false);
            return;
        }

        DragonBallSpawner.ensureInitialized(player);
        if (!hasCompleteSet(player)) {
            player.displayClientMessage(Component.translatable("message.shenron.missing_balls").withStyle(ChatFormatting.GOLD), false);
            return;
        }

        consumeCompleteSet(player);
        SummonRituals.begin(player);
        event.setCanceled(true);
    }

    private static boolean isSummonPhrase(String message) {
        String normalized = message.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return normalized.equals("arise shenron")
                || normalized.equals("come forth shenron")
                || normalized.equals("shenron grant my wish")
                || normalized.equals("eternal dragon arise")
                || normalized.equals("eternal dragon come forth");
    }

    private static boolean hasCompleteSet(ServerPlayer player) {
        Map<Integer, DragonBallEntity> nearby = nearbyBalls(player);
        for (int star = 1; star <= 7; star++) {
            if (!hasInventoryBall(player, star) && !nearby.containsKey(star)) {
                return false;
            }
        }
        return true;
    }

    private static void consumeCompleteSet(ServerPlayer player) {
        Map<Integer, DragonBallEntity> nearby = nearbyBalls(player);
        for (int star = 1; star <= 7; star++) {
            if (!removeInventoryBall(player, star)) {
                DragonBallEntity entity = nearby.get(star);
                if (entity != null) {
                    entity.discard();
                    if (player.level() instanceof ServerLevel serverLevel) {
                        DragonBallSavedData.get(serverLevel).markCollected(star);
                    }
                }
            }
        }
    }

    private static Map<Integer, DragonBallEntity> nearbyBalls(ServerPlayer player) {
        Map<Integer, DragonBallEntity> result = new HashMap<>();
        AABB area = player.getBoundingBox().inflate(8.0D);
        player.level().getEntitiesOfClass(DragonBallEntity.class, area, ball -> !ball.isScattering() && !ball.isCeremonial())
                .forEach(ball -> result.putIfAbsent(ball.getStar(), ball));
        return result;
    }

    private static boolean hasInventoryBall(ServerPlayer player, int star) {
        Item item = ModItems.dragonBall(star);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            if (player.getInventory().getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean removeInventoryBall(ServerPlayer player, int star) {
        Item item = ModItems.dragonBall(star);
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                stack.shrink(1);
                player.getInventory().setChanged();
                return true;
            }
        }
        return false;
    }

}
