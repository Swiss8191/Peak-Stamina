package com.peakstamina.compat.elenaidodge2;

import com.elenai.elenaidodge2.config.ED2CommonConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiDodgeCompat {

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ElenaiDodgeCompat.class);
    }

    @SubscribeEvent
    public static void onServerStart(ServerStartingEvent event) {
        try {

            ED2CommonConfig.DODGE_COST.set(0);
        } catch (Exception e) {
            System.out.println("[PeakStamina] Failed to override Elenai Dodge 2 dodge cost config: " + e.getMessage());
        }
    }
    
}