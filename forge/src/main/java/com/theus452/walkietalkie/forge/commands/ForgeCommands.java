package com.theus452.walkietalkie.forge.commands;

import com.theus452.walkietalkie.forge.config.ForgeModConfigs;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ForgeCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("walkietalkie")
                .then(Commands.literal("setrange")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("distance", DoubleArgumentType.doubleArg(0.0))
                                .executes(command -> {
                                    double distance = DoubleArgumentType.getDouble(command, "distance");

                                    ForgeModConfigs.CHAT_RANGE.set(distance);
                                    ForgeModConfigs.SPEC.save();

                                    command.getSource().sendSuccess(Component.translatable("command.walkietalkie.setrange", distance), true);
                                    return 1;
                                })
                        )
                )
        );
    }
}