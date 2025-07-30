package com.hepdd.gtmthings.common.event;

import com.hepdd.gtmthings.GTMThings;
import com.hepdd.gtmthings.common.command.GTMThingsCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 处理命令相关事件
 */
@Mod.EventBusSubscriber(modid = GTMThings.MOD_ID)
public class CommandEvents {

    /**
     * 注册命令
     *
     * @param event 注册命令事件
     */
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        GTMThingsCommand.register(event.getDispatcher());
    }
}
