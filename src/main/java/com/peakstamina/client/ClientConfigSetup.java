package com.peakstamina.client;

import com.peakstamina.client.gui.PeakConfigMenu;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ClientConfigSetup {
    public static void register(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, screen) -> PeakConfigMenu.createScreen(screen));
    }
}