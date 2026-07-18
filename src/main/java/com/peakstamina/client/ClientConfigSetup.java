package com.peakstamina.client;

import com.peakstamina.client.gui.PeakConfigMenu;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

public class ClientConfigSetup {

    public static void register() {
        ModLoadingContext.get().registerExtensionPoint(
            ConfigScreenHandler.ConfigScreenFactory.class,
            () -> new ConfigScreenHandler.ConfigScreenFactory(
                (minecraft, parentScreen) -> PeakConfigMenu.createScreen(parentScreen)
            )
        );
    }
}