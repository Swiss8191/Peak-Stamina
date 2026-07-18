package com.peakstamina.handlers.core;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.compat.parcool.ParCoolCompat;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.experimental.CustomActionHandler;
import com.peakstamina.handlers.experimental.MobStaminaHandler;
import com.peakstamina.handlers.mechanics.WeightHandler;
import com.peakstamina.config.ExperimentalConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = peakStaminaMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigReloadHandler {

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        handleConfigEvent(event);
    }

    @SubscribeEvent
        public static void onConfigReload(ModConfigEvent.Reloading event) {
            if (event.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.COMMON) {
                com.peakstamina.handlers.core.ServerStaminaHandler.refreshAllCaches(); 
                if (com.peakstamina.compat.walljump.WallJumpCompat.isLoaded()) {
                    com.peakstamina.compat.walljump.WallJumpCompat.refreshCache();
                }
            }
            if (event.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.CLIENT) {
                com.peakstamina.client.events.ClientStaminaEvents.invalidateCache();
            }
        }

    private static void handleConfigEvent(ModConfigEvent event) {
        if (event.getConfig().getSpec() == StaminaLists.LISTS_SPEC) {
            ServerStaminaHandler.refreshAllCaches();
            WeightHandler.validateCache();

            if (ParCoolCompat.isLoaded()) {
                ParCoolCompat.refreshCache();
            }
        }
        
        if (event.getConfig().getSpec() == ExperimentalConfig.EXPERIMENTAL_SPEC) {
            MobStaminaHandler.refreshCache(); 
            CustomActionHandler.refreshCache();
        }
    }
}