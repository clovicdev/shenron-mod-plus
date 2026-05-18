package com.clovic.shenron.registry;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.entity.DragonBallEntity;
import com.clovic.shenron.entity.ShenronEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ShenronMod.MOD_ID);

    public static final RegistryObject<EntityType<DragonBallEntity>> DRAGON_BALL = ENTITIES.register(
            "dragon_ball",
            () -> EntityType.Builder.<DragonBallEntity>of(DragonBallEntity::new, MobCategory.MISC)
                    .sized(0.55F, 0.55F)
                    .clientTrackingRange(32)
                    .updateInterval(2)
                    .build(ShenronMod.id("dragon_ball").toString())
    );

    public static final RegistryObject<EntityType<ShenronEntity>> SHENRON = ENTITIES.register(
            "shenron",
            () -> EntityType.Builder.of(ShenronEntity::new, MobCategory.CREATURE)
                    .sized(17.0F, 28.0F)
                    .clientTrackingRange(256)
                    .updateInterval(2)
                    .build(ShenronMod.id("shenron").toString())
    );

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
