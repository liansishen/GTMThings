package com.hepdd.gtmthings.data;

import com.hepdd.gtmthings.client.menu.ExampleMenu;
import net.minecraft.client.gui.screens.MenuScreens;
import com.hepdd.gtmthings.client.widget.WidgetScreen;

import static com.hepdd.gtmthings.data.GTMTMenuTypes.EXAMPLE_MENU;

public class GTMTScreens {

    public static void init() {
        MenuScreens.register(EXAMPLE_MENU.get(), WidgetScreen::new);
    }
}
