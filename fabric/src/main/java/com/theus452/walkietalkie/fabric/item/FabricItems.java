package com.theus452.walkietalkie.fabric.item;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.item.ModItems;
import com.theus452.walkietalkie.item.WalkieTalkieItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class FabricItems {
    public static void register() {
        
        Item walkieTalkie = Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie"),
                new WalkieTalkieItem(new Item.Properties().stacksTo(1)));

        ModItems.WALKIETALKIE = () -> walkieTalkie;
    }
}