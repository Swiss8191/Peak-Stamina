package com.peakstamina.handlers;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID)
public class SprintSpeedHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (player.isSprinting()) {
                double sprintEffectiveness = player.getAttributeValue(StaminaAttributes.SPRINT_SPEED.get());

                if (sprintEffectiveness < 1.0) {
                    
                    double vanillaSprintMultiplier = 1.3;
                    double targetMultiplier = 1.0 + (0.3 * sprintEffectiveness);

                    double reductionScale = targetMultiplier / vanillaSprintMultiplier;

                    player.setDeltaMovement(player.getDeltaMovement().multiply(
                        reductionScale, 
                        1.0, 
                        reductionScale
                    ));
                }
            }
        }
    }
}