package com.clovic.shenron.registry;

import com.clovic.shenron.ShenronMod;
import com.clovic.shenron.item.DragonBallItem;
import com.clovic.shenron.item.DragonRadarItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class ModItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShenronMod.MOD_ID);

    public static final RegistryObject<Item> ONE_STAR_DRAGON_BALL = registerDragonBall(1);
    public static final RegistryObject<Item> TWO_STAR_DRAGON_BALL = registerDragonBall(2);
    public static final RegistryObject<Item> THREE_STAR_DRAGON_BALL = registerDragonBall(3);
    public static final RegistryObject<Item> FOUR_STAR_DRAGON_BALL = registerDragonBall(4);
    public static final RegistryObject<Item> FIVE_STAR_DRAGON_BALL = registerDragonBall(5);
    public static final RegistryObject<Item> SIX_STAR_DRAGON_BALL = registerDragonBall(6);
    public static final RegistryObject<Item> SEVEN_STAR_DRAGON_BALL = registerDragonBall(7);

    public static final RegistryObject<Item> DRAGON_RADAR = ITEMS.register(
            "dragon_radar",
            () -> new DragonRadarItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON))
    );

    public static final List<RegistryObject<Item>> DRAGON_BALLS = List.of(
            ONE_STAR_DRAGON_BALL,
            TWO_STAR_DRAGON_BALL,
            THREE_STAR_DRAGON_BALL,
            FOUR_STAR_DRAGON_BALL,
            FIVE_STAR_DRAGON_BALL,
            SIX_STAR_DRAGON_BALL,
            SEVEN_STAR_DRAGON_BALL
    );

    private ModItems() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static Item dragonBall(int stars) {
        return switch (stars) {
            case 1 -> ONE_STAR_DRAGON_BALL.get();
            case 2 -> TWO_STAR_DRAGON_BALL.get();
            case 3 -> THREE_STAR_DRAGON_BALL.get();
            case 4 -> FOUR_STAR_DRAGON_BALL.get();
            case 5 -> FIVE_STAR_DRAGON_BALL.get();
            case 6 -> SIX_STAR_DRAGON_BALL.get();
            case 7 -> SEVEN_STAR_DRAGON_BALL.get();
            default -> throw new IllegalArgumentException("Dragon Ball star count must be between 1 and 7.");
        };
    }

    private static RegistryObject<Item> registerDragonBall(int stars) {
        return ITEMS.register("dragon_ball_" + stars, () -> new DragonBallItem(stars, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    }
}
