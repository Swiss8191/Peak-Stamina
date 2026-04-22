package com.peakstamina.handlers;

import org.checkerframework.common.returnsreceiver.qual.This;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.compat.ParCoolCompat;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.config.ExperimentalConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
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
