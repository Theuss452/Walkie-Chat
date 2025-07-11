package com.theus452.walkietalkie.forge.item;

import com.theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ForgeCreativeTabs {

    public static final CreativeModeTab WALKIETALKIE_TAB =
            new CreativeModeTab(WalkieTalkieMod.MOD_ID) {
        @Override
        public Component getDisplayName() {
            return Component.translatable("creativetab.walkietalkie_tab");
        }

        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ForgeItems.WALKIETALKIE_REG_OBJ.get());
        }
    };
}
