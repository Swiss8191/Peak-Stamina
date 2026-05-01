package com.peakstamina.compat;

import com.alrex.parcool.api.client.gui.ParCoolHUDEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ParCoolClientCompat {

    public static void init() {
        NeoForge.EVENT_BUS.register(ParCoolClientCompat.class);
    }

    @SubscribeEvent
    public static void onParCoolHUDRender(ParCoolHUDEvent.RenderEvent event) {
        event.setCanceled(true);
    }
}