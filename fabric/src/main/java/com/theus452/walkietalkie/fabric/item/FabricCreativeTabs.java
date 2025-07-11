package com.theus452.walkietalkie.fabric.item;

import com.theus452.walkietalkie.WalkieTalkieMod;
import com.theus452.walkietalkie.item.ModCreativeModTabs;
import com.theus452.walkietalkie.item.ModItems;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;


public class FabricCreativeTabs {


    private static CreativeModeTab WALKIETALKIE_TAB;

    public static void register() {

        WALKIETALKIE_TAB = FabricItemGroupBuilder.create(
                        new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkietalkie_tab"))
                .icon(() -> new ItemStack(ModItems.WALKIETALKIE.get()))
                .appendItems(stacks -> stacks.add(new ItemStack(ModItems.WALKIETALKIE.get()))
                ).build();

        ModCreativeModTabs.WALKIETALKIE_TAB = () -> WALKIETALKIE_TAB;
    }
}