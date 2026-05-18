package com.clovic.shenron.item;

import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.registry.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonBallItem extends Item {
    private final int stars;

    public DragonBallItem(int stars, Properties properties) {
        super(properties);
        this.stars = stars;
    }

    public int stars() {
        return stars;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        DragonBallEntity ball = ModEntities.DRAGON_BALL.get().create(level);
        if (ball == null) {
            return InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        ball.setStar(stars);
        ball.moveTo(pos.getX() + 0.5D, pos.getY() + 0.18D, pos.getZ() + 0.5D, context.getRotation(), 0.0F);
        level.addFreshEntity(ball);
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.75F, 1.25F);

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.shenron.dragon_ball.tooltip", stars).withStyle(ChatFormatting.GOLD));
    }
}
