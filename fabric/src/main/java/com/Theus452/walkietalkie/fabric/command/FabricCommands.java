package com.Theus452.walkietalkie.fabric.command;

import com.Theus452.walkietalkie.fabric.config.FabricModConfigs;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class FabricCommands {
    public static void register() {
        
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("walkietalkie")
                    .then(Commands.literal("setrange")
                            .requires(source -> source.hasPermission(2))
                            .then(Commands.argument("distance", DoubleArgumentType.doubleArg(0.0))
                                    .executes(command -> {
                                        double distance = DoubleArgumentType.getDouble(command, "distance");
                                        FabricModConfigs.setChatRange(distance); 
                                        command.getSource().sendSuccess(() -> Component.translatable("command.walkietalkie.setrange", distance), true);
                                        return 1;
                                    })
                            )
                    )
            );
        });
    }
}