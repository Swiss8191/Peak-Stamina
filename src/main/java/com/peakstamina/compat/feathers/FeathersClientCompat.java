package com.peakstamina.compat.feathers;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FeathersClientCompat {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(FeathersClientCompat.class);
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        // Intercepts and blocks the Feathers mod from rendering its UI elements
        if (event.getOverlay().id().getNamespace().equals("feathers")) {
            event.setCanceled(true);
        }
    }
}