package com.clovic.shenron.command;

import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.entity.ShenronEntity;
import com.clovic.shenron.registry.ModEntities;
import com.clovic.shenron.registry.ModItems;
import com.clovic.shenron.world.DragonBallSavedData;
import com.clovic.shenron.world.DragonBallSpawner;
import com.clovic.shenron.world.SummonRituals;
import com.clovic.shenron.world.WishSettingsSavedData;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class AdminCommands {
    private static final int PERMISSION_LEVEL = 2;

    private AdminCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("shenron_test")
                .requires(source -> source.hasPermission(PERMISSION_LEVEL))
                .then(Commands.literal("give_balls").executes(context -> giveBalls(context.getSource())))
                .then(Commands.literal("give_radar").executes(context -> giveRadar(context.getSource())))
                .then(Commands.literal("summon").executes(context -> summon(context.getSource())))
                .then(Commands.literal("ritual").executes(context -> ritual(context.getSource())))
                .then(Commands.literal("wish_limit")
                        .executes(context -> showWishLimit(context.getSource()))
                        .then(Commands.argument("stacks", IntegerArgumentType.integer(1, 54))
                                .executes(context -> setWishLimit(context.getSource(), IntegerArgumentType.getInteger(context, "stacks")))))
                .then(Commands.literal("scatter").executes(context -> scatter(context.getSource())))
                .then(Commands.literal("locate").executes(context -> locate(context.getSource())))
        );
    }

    private static int giveBalls(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        for (int star = 1; star <= 7; star++) {
            give(player, new ItemStack(ModItems.dragonBall(star)));
        }
        source.sendSuccess(() -> Component.literal("Gave all seven Dragon Balls.").withStyle(ChatFormatting.GREEN), true);
        return 7;
    }

    private static int giveRadar(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        give(player, new ItemStack(ModItems.DRAGON_RADAR.get()));
        source.sendSuccess(() -> Component.literal("Gave Dragon Radar.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int ritual(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (player.serverLevel().dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("The ritual animation can only run in the Overworld."));
            return 0;
        }

        SummonRituals.begin(player);
        source.sendSuccess(() -> Component.literal("Started Shenron's Dragon Ball ritual animation.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int summon(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.serverLevel();
        ShenronEntity shenron = ModEntities.SHENRON.get().create(level);
        if (shenron == null) {
            source.sendFailure(Component.literal("Unable to create Shenron."));
            return 0;
        }

        shenron.moveTo(player.getX(), player.getY() + 3.2D, player.getZ(), player.getYRot(), 0.0F);
        level.addFreshEntity(shenron);
        source.sendSuccess(() -> Component.literal("Summoned Shenron for testing.").withStyle(ChatFormatting.GREEN), true);
        return 1;
    }

    private static int showWishLimit(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int stacks = WishSettingsSavedData.get(player.serverLevel()).maxWishStacks();
        source.sendSuccess(() -> Component.literal("Shenron wish limit is " + stacks + " stack(s).").withStyle(ChatFormatting.GREEN), false);
        return stacks;
    }

    private static int setWishLimit(CommandSourceStack source, int stacks) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        WishSettingsSavedData settings = WishSettingsSavedData.get(player.serverLevel());
        settings.setMaxWishStacks(stacks);
        source.sendSuccess(() -> Component.literal("Set Shenron wish limit to " + settings.maxWishStacks() + " stack(s).").withStyle(ChatFormatting.GREEN), true);
        return settings.maxWishStacks();
    }

    private static int scatter(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (player.serverLevel().dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Dragon Balls scatter only in the Overworld."));
            return 0;
        }

        clearLoadedBalls(player.serverLevel(), player.blockPosition());
        DragonBallSpawner.scatterFrom(player.serverLevel(), player.position().add(0.0D, 1.0D, 0.0D), player);
        source.sendSuccess(() -> Component.literal("Scattered a fresh Dragon Ball set from your position.").withStyle(ChatFormatting.GREEN), true);
        return 7;
    }

    private static int locate(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (player.serverLevel().dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.literal("Dragon Balls are tracked in the Overworld."));
            return 0;
        }

        DragonBallSpawner.ensureInitialized(player);
        Optional<DragonBallSavedData.BallLocation> nearest = DragonBallSavedData.get(player.serverLevel()).nearestInWorld(player.blockPosition());
        if (nearest.isEmpty()) {
            source.sendFailure(Component.literal("No active Dragon Ball locations are available."));
            return 0;
        }

        DragonBallSavedData.BallLocation location = nearest.get();
        BlockPos pos = location.pos();
        int distance = (int) Math.sqrt(pos.distSqr(player.blockPosition()));
        source.sendSuccess(() -> Component.literal(location.star() + "-Star Dragon Ball: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + " (" + distance + " blocks)").withStyle(ChatFormatting.GOLD), false);
        return 1;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void clearLoadedBalls(ServerLevel level, BlockPos origin) {
        AABB area = AABB.ofSize(Vec3.atCenterOf(origin), 256.0D, 384.0D, 256.0D);
        level.getEntitiesOfClass(DragonBallEntity.class, area).forEach(DragonBallEntity::discard);
    }
}
