package com.clovic.shenron.client.renderer;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.entity.DragonBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DragonBallEntityRenderer extends EntityRenderer<DragonBallEntity> {
    private static final ResourceLocation TEXTURE = ShenronMod.id("textures/entity/dragon_ball.png");
    private static final int SPHERE_SLICES = 40;
    private static final int SPHERE_STACKS = 20;
    private static final float RADIUS = 0.40F;

    public DragonBallEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DragonBallEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float bob = Mth.sin((entity.tickCount + partialTick) * 0.11F) * 0.08F;
        float scale = entity.isScattering() ? 1.35F : 1.0F;

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.46D + bob, 0.0D);
        poseStack.scale(scale, scale, scale);
        if (entity.isCeremonial()) {
            float pulse = 1.0F + Mth.sin((entity.tickCount + partialTick) * 0.22F) * 0.08F;
            poseStack.scale(pulse, pulse, pulse);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 1.8F));
        renderSphere(poseStack, buffer.getBuffer(RenderType.entitySolid(TEXTURE)), packedLight);
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.46D + bob, 0.0D);
        poseStack.scale(scale, scale, scale);
        if (entity.isCeremonial()) {
            float pulse = 1.0F + Mth.sin((entity.tickCount + partialTick) * 0.22F) * 0.08F;
            poseStack.scale(pulse, pulse, pulse);
        }
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        renderStars(entity.getStar(), poseStack, buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)), packedLight);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DragonBallEntity entity) {
        return TEXTURE;
    }

    private static void renderSphere(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        for (int stack = 0; stack < SPHERE_STACKS; stack++) {
            float theta1 = -Mth.HALF_PI + Mth.PI * stack / SPHERE_STACKS;
            float theta2 = -Mth.HALF_PI + Mth.PI * (stack + 1) / SPHERE_STACKS;

            for (int slice = 0; slice < SPHERE_SLICES; slice++) {
                float phi1 = Mth.TWO_PI * slice / SPHERE_SLICES;
                float phi2 = Mth.TWO_PI * (slice + 1) / SPHERE_SLICES;

                SpherePoint a = spherePoint(theta1, phi1);
                SpherePoint b = spherePoint(theta1, phi2);
                SpherePoint c = spherePoint(theta2, phi2);
                SpherePoint d = spherePoint(theta2, phi1);

                vertex(poseStack, consumer, a, 0.0F, 0.0F, packedLight);
                vertex(poseStack, consumer, b, 1.0F, 0.0F, packedLight);
                vertex(poseStack, consumer, c, 1.0F, 1.0F, packedLight);
                vertex(poseStack, consumer, d, 0.0F, 1.0F, packedLight);
            }
        }
    }

    private static void renderStars(int stars, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        float[][] layouts = switch (stars) {
            case 1 -> new float[][]{{0.0F, 0.0F}};
            case 2 -> new float[][]{{-0.10F, -0.10F}, {0.10F, 0.10F}};
            case 3 -> new float[][]{{-0.12F, 0.10F}, {0.0F, -0.12F}, {0.12F, 0.10F}};
            case 4 -> new float[][]{{-0.12F, -0.12F}, {0.12F, -0.12F}, {-0.12F, 0.12F}, {0.12F, 0.12F}};
            case 5 -> new float[][]{{-0.13F, -0.13F}, {0.13F, -0.13F}, {0.0F, 0.0F}, {-0.13F, 0.13F}, {0.13F, 0.13F}};
            case 6 -> new float[][]{{-0.13F, -0.14F}, {0.13F, -0.14F}, {-0.13F, 0.0F}, {0.13F, 0.0F}, {-0.13F, 0.14F}, {0.13F, 0.14F}};
            default -> new float[][]{{-0.14F, -0.14F}, {0.0F, -0.15F}, {0.14F, -0.14F}, {-0.09F, 0.0F}, {0.09F, 0.0F}, {-0.11F, 0.15F}, {0.11F, 0.15F}};
        };

        for (float[] point : layouts) {
            renderStar(point[0], point[1], 0.063F, 0.027F, 96, 19, 10, poseStack, consumer, packedLight);
            renderStar(point[0], point[1], 0.052F, 0.022F, 191, 22, 15, poseStack, consumer, packedLight);
        }
    }

    private static void renderStar(float x, float y, float outer, float inner, int red, int green, int blue, PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        float z = -0.423F;
        for (int i = 0; i < 10; i++) {
            float angleA = -Mth.HALF_PI + i * Mth.PI / 5.0F;
            float angleB = -Mth.HALF_PI + (i + 1) * Mth.PI / 5.0F;
            float radiusA = i % 2 == 0 ? outer : inner;
            float radiusB = (i + 1) % 2 == 0 ? outer : inner;
            flatVertex(poseStack, consumer, x, y, z, red, green, blue, packedLight);
            flatVertex(poseStack, consumer, x + Mth.cos(angleA) * radiusA, y + Mth.sin(angleA) * radiusA, z, red, green, blue, packedLight);
            flatVertex(poseStack, consumer, x + Mth.cos(angleB) * radiusB, y + Mth.sin(angleB) * radiusB, z, red, green, blue, packedLight);
        }
    }

    private static SpherePoint spherePoint(float theta, float phi) {
        float cosTheta = Mth.cos(theta);
        float x = cosTheta * Mth.cos(phi);
        float y = Mth.sin(theta);
        float z = cosTheta * Mth.sin(phi);
        return new SpherePoint(x * RADIUS, y * RADIUS, z * RADIUS, x, y, z);
    }

    private static void vertex(PoseStack poseStack, VertexConsumer consumer, SpherePoint point, float u, float v, int packedLight) {
        float highlight = Math.max(0.0F, point.nx() * -0.35F + point.ny() * 0.58F + point.nz() * -0.45F);
        float shade = Mth.clamp(0.70F + point.ny() * 0.17F + point.nz() * 0.13F + highlight * 0.24F, 0.58F, 1.0F);
        int red = (int) (255 * shade);
        int green = (int) (142 * shade);
        int blue = (int) (18 * shade);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        consumer.vertex(matrix, point.x(), point.y(), point.z())
                .color(red, green, blue, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normal, point.nx(), point.ny(), point.nz())
                .endVertex();
    }

    private static void flatVertex(PoseStack poseStack, VertexConsumer consumer, float x, float y, float z, int red, int green, int blue, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        consumer.vertex(pose.pose(), x, y, z)
                .color(red, green, blue, 255)
                .uv(0.5F, 0.5F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(pose.normal(), 0.0F, 0.0F, -1.0F)
                .endVertex();
    }

    private record SpherePoint(float x, float y, float z, float nx, float ny, float nz) {
    }
}
