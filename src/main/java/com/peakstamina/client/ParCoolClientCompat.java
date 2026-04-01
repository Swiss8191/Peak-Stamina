package com.peakstamina.compat;

import com.alrex.parcool.api.client.gui.ParCoolHUDEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ParCoolClientCompat {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ParCoolClientCompat.class);
    }

    @SubscribeEvent
    public static void onParCoolHUDRender(ParCoolHUDEvent.RenderEvent event) {
        if (event.isCancelable()) {
            event.setCanceled(true);
        }
    }
}