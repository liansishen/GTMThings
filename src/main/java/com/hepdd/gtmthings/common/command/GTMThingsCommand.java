package com.hepdd.gtmthings.common.command;

import com.hepdd.gtmthings.client.screen.ScreenManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * GTMThings模组的命令类
 */
public class GTMThingsCommand {

    /**
     * 注册所有命令
     *
     * @param dispatcher 命令调度器
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> gtmthingsCommand = Commands.literal("gtmthings")
            .requires(source -> source.hasPermission(0)) // 所有玩家都可以使用
            .then(Commands.literal("demo_ui")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    // 在客户端打开UI
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                        ScreenManager.openDemoWidgetScreen(player);
                    });

                    context.getSource().sendSuccess(() -> Component.translatable("command.gtmthings.demo_ui.success"), false);
                    return 1;
                })
            );

        dispatcher.register(gtmthingsCommand);
    }
}
