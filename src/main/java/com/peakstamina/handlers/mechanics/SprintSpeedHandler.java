package com.peakstamina.handlers.mechanics;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = peakStaminaMod.MODID)
public class SprintSpeedHandler {

    @SubscribeEvent
    public static void onLivingUpdate(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        
        // Sprint logic
        if (player.isSprinting()) {
            var attrInstance = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.SPRINT_SPEED.get()));
            if (attrInstance != null) {
                double sprintEffectiveness = attrInstance.getValue();
                if (sprintEffectiveness < 1.0) {
                    double vanillaSprintMultiplier = 1.3;
                    double targetMultiplier = 1.0 + (0.3 * sprintEffectiveness);
                    double reductionScale = targetMultiplier / vanillaSprintMultiplier;
                    player.setDeltaMovement(player.getDeltaMovement().multiply(
                        reductionScale, 1.0, reductionScale
                    ));
                }
            }
        }

        // Slow climb speed logic
        if (player.onClimbable() && player.isShiftKeyDown() && StaminaConfig.COMMON.enableSlowClimb.get()) {
            var climbAttrInstance = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.SLOW_CLIMB_SPEED.get()));
            double baseClimbMult = StaminaConfig.COMMON.slowClimbSpeed.get();
            double attrClimbMult = climbAttrInstance != null ? climbAttrInstance.getValue() : 1.0;
            
            double finalClimbSpeed = baseClimbMult * attrClimbMult;
            
            // Only scale if the multiplier isn't exactly 1.0
            if (Math.abs(finalClimbSpeed - 1.0) > 0.001) {
                player.setDeltaMovement(player.getDeltaMovement().scale(finalClimbSpeed));
            }
        }
    }
}