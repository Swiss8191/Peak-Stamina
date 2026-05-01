package com.peakstamina.handlers;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.compat.ParCoolCompat;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.config.ExperimentalConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;

@EventBusSubscriber(modid = PeakStaminaMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ConfigReloadHandler {

    @SubscribeEvent
    public static void onConfigLoad(ModConfigEvent.Loading event) {
        handleConfigEvent(event);
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        handleConfigEvent(event);
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