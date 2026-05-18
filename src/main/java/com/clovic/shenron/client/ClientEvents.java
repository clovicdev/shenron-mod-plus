package com.clovic.shenron.client;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.client.model.ShenronModel;
import com.clovic.shenron.client.renderer.DragonBallEntityRenderer;
import com.clovic.shenron.client.renderer.ShenronRenderer;
import com.clovic.shenron.item.DragonRadarItem;
import com.clovic.shenron.registry.ModEntities;
import com.clovic.shenron.registry.ModItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = ShenronMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DRAGON_BALL.get(), DragonBallEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SHENRON.get(), ShenronRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ShenronModel.LAYER_LOCATION, ShenronModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.DRAGON_RADAR.get(), ShenronMod.id("radar_direction"), ClientEvents::radarDirection));
    }

    private static float radarDirection(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        CompoundTag tag = stack.getTag();
        if (entity == null || tag == null || !tag.getBoolean(DragonRadarItem.HAS_TARGET)) {
            return 0.0F;
        }

        double dx = tag.getInt(DragonRadarItem.TARGET_X) + 0.5D - entity.getX();
        double dz = tag.getInt(DragonRadarItem.TARGET_Z) + 0.5D - entity.getZ();
        double distanceSqr = dx * dx + dz * dz;
        if (distanceSqr < 36.0D) {
            return 0.9F;
        }

        double targetAngle = Math.atan2(dz, dx);
        double yaw = Math.toRadians(entity.getYRot());
        double relative = targetAngle - yaw + Math.PI / 2.0D;
        float wrapped = Mth.wrapDegrees((float) Math.toDegrees(relative));
        int sector = Mth.floor((wrapped + 180.0F) / 45.0F) & 7;
        return 0.1F + sector * 0.1F;
    }
}
