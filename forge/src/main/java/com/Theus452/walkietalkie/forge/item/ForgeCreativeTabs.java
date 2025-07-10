package com.Theus452.walkietalkie.forge.item;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ForgeCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WalkieTalkieMod.MOD_ID);

    
    public static final RegistryObject<CreativeModeTab> WALKIETALKIE_TAB = CREATIVE_MODE_TABS.register("walkietalkie_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ForgeItems.WALKIETALKIE_REG_OBJ.get()))
                    .title(Component.translatable("creativetab.walkietalkie_tab"))
                    .displayItems((displayContext, output) -> {
                        output.accept(ForgeItems.WALKIETALKIE_REG_OBJ.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}