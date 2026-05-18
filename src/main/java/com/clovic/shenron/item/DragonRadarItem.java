package com.clovic.shenron.item;

import com.clovic.shenron.world.DragonBallSpawner;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class DragonRadarItem extends Item {
    public static final String HAS_TARGET = "HasTarget";
    public static final String TARGET_X = "TargetX";
    public static final String TARGET_Y = "TargetY";
    public static final String TARGET_Z = "TargetZ";
    public static final String TARGET_STAR = "TargetStar";
    public static final String TARGET_DISTANCE = "TargetDistance";

    public DragonRadarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack stack, float partialTick, float equipProcess, float swingProcess) {
                float lookDown = Mth.clamp((player.getXRot() - 12.0F) / 58.0F, 0.0F, 1.0F);
                if (lookDown <= 0.0F) {
                    return false;
                }

                int side = arm == HumanoidArm.RIGHT ? 1 : -1;
                poseStack.translate(side * -0.055F * lookDown, -0.075F * lookDown, -0.43F * lookDown);
                poseStack.mulPose(Axis.XP.rotationDegrees(8.0F * lookDown));
                float scale = 1.0F + 0.18F * lookDown;
                poseStack.scale(scale, scale, scale);
                return false;
            }
        });
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player && player.tickCount % 10 == 0) {
            DragonBallSpawner.updateRadar(stack, player);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            DragonBallSpawner.updateRadar(stack, serverPlayer);
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getBoolean(HAS_TARGET)) {
                int star = tag.getInt(TARGET_STAR);
                int distance = tag.getInt(TARGET_DISTANCE);
                serverPlayer.displayClientMessage(Component.translatable("item.shenron.dragon_radar.signal", star, distance).withStyle(ChatFormatting.GREEN), true);
            } else {
                serverPlayer.displayClientMessage(Component.translatable("item.shenron.dragon_radar.no_signal").withStyle(ChatFormatting.GRAY), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getBoolean(HAS_TARGET)) {
            tooltip.add(Component.translatable("item.shenron.dragon_radar.tooltip.signal", tag.getInt(TARGET_STAR), tag.getInt(TARGET_DISTANCE)).withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("item.shenron.dragon_radar.tooltip.no_signal").withStyle(ChatFormatting.GRAY));
        }
    }
}
