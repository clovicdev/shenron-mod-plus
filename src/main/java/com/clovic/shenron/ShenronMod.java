package com.clovic.shenron;

import com.clovic.shenron.entity.ShenronEntity;
import com.clovic.shenron.registry.ModCreativeTabs;
import com.clovic.shenron.registry.ModEntities;
import com.clovic.shenron.registry.ModItems;
import com.clovic.shenron.world.ServerEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShenronMod.MOD_ID)
public final class ShenronMod {
    public static final String MOD_ID = "shenron";

    public ShenronMod(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();

        ModItems.register(modBus);
        ModEntities.register(modBus);
        ModCreativeTabs.register(modBus);

        modBus.addListener(this::registerAttributes);
        MinecraftForge.EVENT_BUS.register(ServerEvents.class);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SHENRON.get(), ShenronEntity.createAttributes().build());
    }
}
