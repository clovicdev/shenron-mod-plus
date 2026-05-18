package com.clovic.shenron.client.renderer;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.client.model.ShenronModel;
import com.clovic.shenron.entity.ShenronEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ShenronRenderer extends MobRenderer<ShenronEntity, ShenronModel<ShenronEntity>> {
    private static final ResourceLocation TEXTURE = ShenronMod.id("textures/entity/shenron.png");

    public ShenronRenderer(EntityRendererProvider.Context context) {
        super(context, new ShenronModel<>(context.bakeLayer(ShenronModel.LAYER_LOCATION)), 7.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(ShenronEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(ShenronEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(8.0F, 8.0F, 8.0F);
    }
}
