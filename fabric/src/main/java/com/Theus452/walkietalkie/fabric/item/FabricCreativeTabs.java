package com.Theus452.walkietalkie.fabric.item;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import com.Theus452.walkietalkie.item.ModCreativeModTabs;
import com.Theus452.walkietalkie.item.ModItems;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

import net.minecraft.world.item.ItemStack;

public class FabricCreativeTabs {
    public static void register() {
        
        CreativeModeTab walkieTalkieTab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
                new ResourceLocation(WalkieTalkieMod.MOD_ID, "walkietalkie_tab"),
                FabricItemGroup.builder()
                        .title(Component.translatable("creativetab.walkietalkie_tab"))
                        .icon(() -> new ItemStack(ModItems.WALKIETALKIE.get()))
                        .displayItems((displayContext, output) -> {
                            output.accept(ModItems.WALKIETALKIE.get());
                        })
                        .build());

        
        ModCreativeModTabs.WALKIETALKIE_TAB = () -> walkieTalkieTab;
    }
}