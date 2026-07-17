package com.Theus452.walkietalkie.neoforge.item;

import com.Theus452.walkietalkie.WalkieTalkieMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class NeoForgeCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, WalkieTalkieMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> WALKIETALKIE_TAB = CREATIVE_MODE_TABS.register("walkietalkie_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(NeoForgeItems.WALKIETALKIE_REG_OBJ.get()))
                    .title(Component.translatable("creativetab.walkietalkie_tab"))
                    .displayItems((displayContext, output) -> {
                        output.accept(NeoForgeItems.WALKIETALKIE_REG_OBJ.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
