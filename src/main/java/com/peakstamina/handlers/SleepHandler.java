package com.peakstamina.handlers;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.network.PacketSyncStamina;
import com.peakstamina.network.StaminaNetwork;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.SleepFinishedTimeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID)
public class SleepHandler {

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        for (ServerPlayer player : level.players()) {
            if (player.isSleeping()) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    
                    float reduction = StaminaConfig.COMMON.sleepFatigueReduction.get().floatValue();
                    cap.fatiguePenalty = Math.max(0.0f, cap.fatiguePenalty - reduction);
                    
                    if (cap.fatiguePenalty <= 0) {
                        cap.fatigueTimer = 0;
                    }

                    StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
                });
            }
        }
    }
}