package com.peakstamina.handlers.mechanics;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.data.StaminaData;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.config.StaminaConfig;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = peakStaminaMod.MODID)
public class SleepHandler {

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            if (player.isSleeping()) {
                StaminaData cap = player.getData(StaminaCapability.STAMINA);
                
                float reduction = StaminaConfig.COMMON.sleepFatigueReduction.get().floatValue();
                cap.fatiguePenalty = Math.max(0.0f, cap.fatiguePenalty - reduction);
                
                if (cap.fatiguePenalty <= 0) {
                    cap.fatigueTimer = 0;
                }

                ServerStaminaHandler.sync(player, cap);
            }
        }
    }
}