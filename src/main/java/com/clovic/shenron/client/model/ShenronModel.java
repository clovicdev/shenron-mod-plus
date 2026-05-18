package com.clovic.shenron.client.model;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.entity.ShenronEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ShenronModel<T extends ShenronEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ShenronMod.id("shenron"), "main");
    private static final int NECK_SEGMENTS = 12;
    private static final int SEGMENTS = 42;

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart[] neck = new ModelPart[NECK_SEGMENTS];
    private final ModelPart[] body = new ModelPart[SEGMENTS];
    private final ModelPart[] legs = new ModelPart[4];

    public ShenronModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.jaw = head.getChild("jaw");
        for (int i = 0; i < NECK_SEGMENTS; i++) {
            neck[i] = root.getChild("neck_" + i);
        }
        for (int i = 0; i < SEGMENTS; i++) {
            body[i] = root.getChild("body_" + i);
        }
        legs[0] = root.getChild("front_left_leg");
        legs[1] = root.getChild("front_right_leg");
        legs[2] = root.getChild("rear_left_leg");
        legs[3] = root.getChild("rear_right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.8F, -5.8F, -8.0F, 13.6F, 10.8F, 10.6F, new CubeDeformation(0.18F))
                        .texOffs(0, 18).addBox(-4.9F, -2.7F, -19.0F, 9.8F, 4.6F, 11.6F, new CubeDeformation(0.08F))
                        .texOffs(42, 0).addBox(-7.3F, -6.9F, -7.1F, 3.6F, 3.4F, 4.8F)
                        .texOffs(58, 0).addBox(3.7F, -6.9F, -7.1F, 3.6F, 3.4F, 4.8F)
                        .texOffs(38, 8).addBox(-6.9F, -1.1F, -9.9F, 2.6F, 3.5F, 4.7F)
                        .texOffs(52, 8).addBox(4.3F, -1.1F, -9.9F, 2.6F, 3.5F, 4.7F)
                        .texOffs(0, 76).addBox(-3.25F, -2.9F, -19.45F, 1.55F, 1.55F, 0.7F)
                        .texOffs(0, 76).addBox(1.7F, -2.9F, -19.45F, 1.55F, 1.55F, 0.7F)
                        .texOffs(116, 76).addBox(-0.2F, -0.15F, -19.65F, 0.4F, 0.45F, 0.55F),
                PartPose.offset(0.0F, -3.6F, -48.0F)
        );

        head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-4.1F, 0.7F, -18.0F, 8.2F, 3.2F, 10.2F)
                        .texOffs(28, 32).addBox(-2.25F, 3.4F, -14.4F, 4.5F, 2.3F, 4.6F),
                PartPose.ZERO
        );
        head.addOrReplaceChild(
                "left_brow",
                CubeListBuilder.create().texOffs(38, 12).addBox(0.85F, -4.15F, -19.85F, 4.35F, 0.95F, 1.05F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.16F)
        );
        head.addOrReplaceChild(
                "right_brow",
                CubeListBuilder.create().texOffs(50, 12).addBox(-5.2F, -4.15F, -19.85F, 4.35F, 0.95F, 1.05F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.16F)
        );
        head.addOrReplaceChild(
                "left_horn_base",
                CubeListBuilder.create().texOffs(62, 0).addBox(-1.05F, -8.8F, -1.1F, 2.1F, 8.8F, 2.1F),
                PartPose.offsetAndRotation(3.05F, -4.2F, -4.1F, -0.44F, -0.24F, 0.28F)
        );
        head.addOrReplaceChild(
                "right_horn_base",
                CubeListBuilder.create().texOffs(72, 0).addBox(-1.05F, -8.8F, -1.1F, 2.1F, 8.8F, 2.1F),
                PartPose.offsetAndRotation(-3.05F, -4.2F, -4.1F, -0.44F, 0.24F, -0.28F)
        );
        head.addOrReplaceChild(
                "left_horn_tip",
                CubeListBuilder.create().texOffs(62, 0).addBox(-0.75F, -8.0F, -0.75F, 1.5F, 8.0F, 1.5F),
                PartPose.offsetAndRotation(4.6F, -11.4F, -7.4F, -0.76F, -0.34F, 0.18F)
        );
        head.addOrReplaceChild(
                "right_horn_tip",
                CubeListBuilder.create().texOffs(72, 0).addBox(-0.75F, -8.0F, -0.75F, 1.5F, 8.0F, 1.5F),
                PartPose.offsetAndRotation(-4.6F, -11.4F, -7.4F, -0.76F, 0.34F, -0.18F)
        );
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(84, 26).addBox(0.0F, -2.6F, -1.2F, 6.4F, 5.2F, 2.4F)
                        .texOffs(104, 76).addBox(4.9F, -4.4F, -0.95F, 3.2F, 5.4F, 1.9F),
                PartPose.offsetAndRotation(6.1F, -2.6F, -3.7F, -0.22F, -0.34F, 0.42F)
        );
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(84, 26).addBox(-6.4F, -2.6F, -1.2F, 6.4F, 5.2F, 2.4F)
                        .texOffs(104, 76).addBox(-8.1F, -4.4F, -0.95F, 3.2F, 5.4F, 1.9F),
                PartPose.offsetAndRotation(-6.1F, -2.6F, -3.7F, -0.22F, 0.34F, -0.42F)
        );
        head.addOrReplaceChild(
                "crest",
                CubeListBuilder.create()
                        .texOffs(84, 26).addBox(-1.7F, -7.8F, -4.6F, 3.4F, 7.8F, 8.6F)
                        .texOffs(100, 26).addBox(-1.25F, -5.8F, 3.5F, 2.5F, 5.9F, 5.5F),
                PartPose.offsetAndRotation(0.0F, -5.0F, -1.3F, -0.18F, 0.0F, 0.0F)
        );
        head.addOrReplaceChild(
                "left_upper_whisker",
                CubeListBuilder.create().texOffs(82, 0).addBox(0.0F, -0.25F, -42.0F, 0.72F, 0.72F, 42.0F),
                PartPose.offsetAndRotation(4.2F, -0.7F, -16.2F, 0.0F, -0.66F, 0.18F)
        );
        head.addOrReplaceChild(
                "right_upper_whisker",
                CubeListBuilder.create().texOffs(82, 0).addBox(-0.72F, -0.25F, -42.0F, 0.72F, 0.72F, 42.0F),
                PartPose.offsetAndRotation(-4.2F, -0.7F, -16.2F, 0.0F, 0.66F, -0.18F)
        );
        head.addOrReplaceChild(
                "left_lower_whisker",
                CubeListBuilder.create().texOffs(82, 0).addBox(0.0F, -0.2F, -32.0F, 0.55F, 0.55F, 32.0F),
                PartPose.offsetAndRotation(2.7F, 1.65F, -17.1F, 0.34F, -0.48F, -0.1F)
        );
        head.addOrReplaceChild(
                "right_lower_whisker",
                CubeListBuilder.create().texOffs(82, 0).addBox(-0.55F, -0.2F, -32.0F, 0.55F, 0.55F, 32.0F),
                PartPose.offsetAndRotation(-2.7F, 1.65F, -17.1F, 0.34F, 0.48F, 0.1F)
        );

        for (int i = 0; i < NECK_SEGMENTS; i++) {
            float width = neckWidth(i);
            PartDefinition neckSegment = root.addOrReplaceChild(
                    "neck_" + i,
                    CubeListBuilder.create()
                            .texOffs(0, 44 + (i % 4) * 8)
                            .addBox(-width / 2.0F, -width / 2.0F, -4.2F, width, width, 8.4F, new CubeDeformation(0.18F)),
                    PartPose.ZERO
            );
            addScaledBodyDetails(neckSegment, "neck_belly_" + i, "neck_spine_" + i, width, i, 8.8F, 3.2F);
        }

        for (int i = 0; i < SEGMENTS; i++) {
            float width = bodyWidth(i);
            PartDefinition segment = root.addOrReplaceChild(
                    "body_" + i,
                    CubeListBuilder.create()
                            .texOffs(0, 44 + (i % 4) * 8)
                            .addBox(-width / 2.0F, -width / 2.0F, -4.1F, width, width, 8.2F, new CubeDeformation(0.2F)),
                    PartPose.ZERO
            );
            addScaledBodyDetails(segment, "belly_" + i, "spine_" + i, width, i, 8.5F, 3.55F);
        }

        addLeg(root, "front_left_leg", -1.0F);
        addLeg(root, "front_right_leg", 1.0F);
        addLeg(root, "rear_left_leg", -1.0F);
        addLeg(root, "rear_right_leg", 1.0F);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float summon = smooth(entity.summonProgress());
        float revealCursor = summon * (SEGMENTS + 7.0F);
        float wave = ageInTicks * 0.018F;

        head.visible = summon > 0.015F;
        head.x = 0.0F;
        head.y = -5.0F + Mth.sin(ageInTicks * 0.035F) * 0.55F + (1.0F - summon) * 36.0F;
        head.z = -48.0F + (1.0F - summon) * 5.5F;
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.34F + Mth.sin(ageInTicks * 0.021F) * 0.075F;
        head.xRot = 0.17F + headPitch * Mth.DEG_TO_RAD * 0.2F + Mth.sin(ageInTicks * 0.03F) * 0.028F;
        head.zRot = Mth.sin(ageInTicks * 0.018F) * 0.025F;
        jaw.xRot = 0.14F + Mth.sin(ageInTicks * 0.075F) * 0.055F;

        for (int i = 0; i < body.length; i++) {
            float visibleAmount = Mth.clamp(revealCursor - i, 0.0F, 1.0F);
            body[i].visible = visibleAmount > 0.02F;

            float t = i / (float) (body.length - 1);
            float nextT = Math.min(1.0F, (i + 1.0F) / (body.length - 1));
            float x = bodyX(t, wave);
            float y = bodyY(t, ageInTicks);
            float z = bodyZ(t, wave);
            float nextX = bodyX(nextT, wave);
            float nextY = bodyY(nextT, ageInTicks);
            float nextZ = bodyZ(nextT, wave);

            body[i].x = x;
            body[i].y = y + (1.0F - visibleAmount) * 44.0F;
            body[i].z = z + (1.0F - visibleAmount) * 8.0F;
            aimSegment(body[i], x, y, z, nextX, nextY, nextZ);
            body[i].zRot += Mth.sin(wave * 3.1F + i * 0.28F) * 0.08F;
        }

        for (int i = 0; i < neck.length; i++) {
            float t = (i + 1.0F) / (neck.length + 1.0F);
            float visibleAmount = Mth.clamp(revealCursor + 2.0F - i * 0.55F, 0.0F, 1.0F);
            neck[i].visible = head.visible && visibleAmount > 0.02F;

            float bodyX = bodyX(0.0F, wave);
            float bodyY = bodyY(0.0F, ageInTicks);
            float bodyZ = bodyZ(0.0F, wave);
            float x = lerp(t, head.x * 0.55F, bodyX);
            float y = lerp(t, head.y + 7.8F, bodyY) + Mth.sin(ageInTicks * 0.036F + i * 0.44F) * 0.18F;
            float z = lerp(t, head.z + 8.0F, bodyZ - 0.8F);
            float nextT = Math.min(1.0F, (i + 2.0F) / (neck.length + 1.0F));
            float nextX = lerp(nextT, head.x * 0.55F, bodyX);
            float nextY = lerp(nextT, head.y + 7.8F, bodyY);
            float nextZ = lerp(nextT, head.z + 8.0F, bodyZ - 0.8F);

            neck[i].x = x;
            neck[i].y = y + (1.0F - visibleAmount) * 27.0F;
            neck[i].z = z;
            aimSegment(neck[i], x, y, z, nextX, nextY, nextZ);
        }

        placeLeg(legs[0], body[8], -6.6F, -1.0F, summon, ageInTicks);
        placeLeg(legs[1], body[9], 6.6F, 1.0F, summon, ageInTicks + 9.0F);
        placeLeg(legs[2], body[28], -5.8F, -1.0F, summon, ageInTicks + 18.0F);
        placeLeg(legs[3], body[29], 5.8F, 1.0F, summon, ageInTicks + 27.0F);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    private static void addScaledBodyDetails(PartDefinition segment, String bellyName, String spineName, float width, int index, float length, float spineHeight) {
        segment.addOrReplaceChild(
                bellyName,
                CubeListBuilder.create()
                        .texOffs(48, 42 + (index % 4) * 7)
                        .addBox(-width * 0.35F, width * 0.19F, -length / 2.0F - 0.1F, width * 0.7F, 1.55F, length + 0.2F),
                PartPose.ZERO
        );
        segment.addOrReplaceChild(
                spineName,
                CubeListBuilder.create()
                        .texOffs(88, 48)
                        .addBox(-0.75F, -width / 2.0F - spineHeight, -1.5F, 1.5F, spineHeight + 0.35F, 3.0F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.12F, 0.0F, 0.0F)
        );
        segment.addOrReplaceChild(
                spineName + "_rear",
                CubeListBuilder.create()
                        .texOffs(88, 48)
                        .addBox(-0.58F, -width / 2.0F - spineHeight * 0.72F, 1.7F, 1.16F, spineHeight * 0.72F, 2.2F),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.08F, 0.0F, 0.0F)
        );
    }

    private static float bodyX(float t, float wave) {
        float coil = Mth.clamp(t * 3.4F, 0.0F, 1.0F);
        float radius = (8.8F + Mth.sin(t * Mth.PI) * 5.8F) * coil;
        float angle = t * Mth.TWO_PI * 2.72F + wave;
        return Mth.sin(angle) * radius;
    }

    private static float bodyY(float t, float ageInTicks) {
        float skyArch = Mth.sin(t * Mth.PI) * -39.0F;
        float tailDrop = t * 45.0F;
        float idle = Mth.sin(ageInTicks * 0.036F + t * 9.0F) * 0.72F;
        return 7.6F + skyArch + tailDrop + idle;
    }

    private static float bodyZ(float t, float wave) {
        float coil = Mth.clamp(t * 3.4F, 0.0F, 1.0F);
        float radius = (8.8F + Mth.sin(t * Mth.PI) * 5.8F) * coil;
        float angle = t * Mth.TWO_PI * 2.72F + wave;
        return -34.0F + t * 92.0F + Mth.cos(angle) * radius * 0.58F;
    }

    private static void aimSegment(ModelPart part, float x, float y, float z, float nextX, float nextY, float nextZ) {
        float dx = nextX - x;
        float dy = nextY - y;
        float dz = nextZ - z;
        float flat = Mth.sqrt(dx * dx + dz * dz);
        part.yRot = (float) Math.atan2(dx, dz);
        part.xRot = -(float) Math.atan2(dy, flat);
        part.zRot = Mth.clamp(dx * 0.018F, -0.18F, 0.18F);
    }

    private static float bodyWidth(int index) {
        float t = index / (float) Math.max(1, SEGMENTS - 1);
        if (t < 0.18F) {
            return 8.6F - t * 8.0F;
        }
        if (t > 0.72F) {
            return 7.0F - (t - 0.72F) * 13.5F;
        }
        return 7.15F + Mth.sin(t * Mth.PI) * 0.6F;
    }

    private static float neckWidth(int index) {
        float t = index / (float) Math.max(1, NECK_SEGMENTS - 1);
        return 6.0F + t * 2.9F;
    }

    private static void addLeg(PartDefinition root, String name, float side) {
        PartDefinition leg = root.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(70, 42).addBox(-1.45F, 0.0F, -1.45F, 2.9F, 7.6F, 2.9F)
                        .texOffs(80, 42).addBox(-1.8F, 6.7F, -4.0F, 3.6F, 1.75F, 4.9F)
                        .texOffs(70, 54).addBox(-1.15F, 3.7F, -2.35F, 2.3F, 5.4F, 2.3F),
                PartPose.offsetAndRotation(side * 6.0F, 15.0F, -20.0F, 0.35F, side * 0.5F, side * 0.3F)
        );
        leg.addOrReplaceChild(
                name + "_claws",
                CubeListBuilder.create()
                        .texOffs(96, 42).addBox(-2.55F, 7.7F, -5.45F, 0.9F, 0.9F, 3.15F)
                        .texOffs(96, 42).addBox(-0.45F, 7.7F, -5.75F, 0.9F, 0.9F, 3.45F)
                        .texOffs(96, 42).addBox(1.65F, 7.7F, -5.45F, 0.9F, 0.9F, 3.15F),
                PartPose.ZERO
        );
    }

    private static void placeLeg(ModelPart leg, ModelPart anchor, float sideOffset, float side, float summon, float time) {
        leg.visible = anchor.visible && summon > 0.38F;
        leg.x = anchor.x + sideOffset;
        leg.y = anchor.y + 2.6F;
        leg.z = anchor.z + 0.4F;
        leg.xRot = anchor.xRot + 0.62F + Mth.sin(time * 0.04F) * 0.05F;
        leg.yRot = anchor.yRot + side * 0.82F;
        leg.zRot = side * 0.28F;
    }

    private static float smooth(float value) {
        float clamped = Mth.clamp(value, 0.0F, 1.0F);
        return clamped * clamped * (3.0F - 2.0F * clamped);
    }

    private static float lerp(float t, float start, float end) {
        return start + (end - start) * t;
    }
}
