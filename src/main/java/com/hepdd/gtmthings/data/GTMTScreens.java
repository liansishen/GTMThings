package com.hepdd.gtmthings.data;

import com.hepdd.gtmthings.client.menu.EnergyDisplayMenu;
import com.hepdd.gtmthings.client.menu.ExampleMenu;
import com.hepdd.gtmthings.client.screen.EnergyDisplayScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import com.hepdd.gtmthings.client.widget.WidgetScreen;

import static com.hepdd.gtmthings.data.GTMTMenuTypes.ENERGY_DISPLAY_MENU;
import static com.hepdd.gtmthings.data.GTMTMenuTypes.EXAMPLE_MENU;

public class GTMTScreens {

    public static void init() {
        MenuScreens.register(EXAMPLE_MENU.get(), WidgetScreen::new);
        MenuScreens.register(ENERGY_DISPLAY_MENU.get(), EnergyDisplayScreen::new);
    }
}
