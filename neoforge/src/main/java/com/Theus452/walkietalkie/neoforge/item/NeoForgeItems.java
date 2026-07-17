package com.Theus452.walkietalkie.neoforge.item;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;

public class NeoForgeItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(WalkieTalkieMod.MOD_ID);

    public static final DeferredItem<Item> WALKIETALKIE_REG_OBJ = ITEMS.register("walkie_talkie",
            () -> new WalkieTalkieItem(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
