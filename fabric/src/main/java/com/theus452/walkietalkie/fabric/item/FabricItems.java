package com.theus452.walkietalkie.fabric.item;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.item.ModCreativeModTabs;
import com.theus452.walkietalkie.item.ModItems;
import com.theus452.walkietalkie.item.WalkieTalkieItem;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class FabricItems {
    public static void register() {
        Item walkieTalkie = Registry.register(Registry.ITEM, new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkie_talkie"),
                new WalkieTalkieItem(new FabricItemSettings().group(CreativeModeTab.TAB_TOOLS).stacksTo(1).tab(ModCreativeModTabs.WALKIETALKIE_TAB.get())));

        ModItems.WALKIETALKIE = () -> walkieTalkie;
    }
}