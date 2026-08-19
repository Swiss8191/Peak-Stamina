package com.peakstamina.network.packets;

import java.util.function.Supplier;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.handlers.mechanics.WeightHandler;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class PacketMissedAttack {

    public PacketMissedAttack() {}

    public static void encode(PacketMissedAttack msg, FriendlyByteBuf buf) {}

    public static PacketMissedAttack decode(FriendlyByteBuf buf) {
        return new PacketMissedAttack();
    }

    public static void handle(PacketMissedAttack msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            
            if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
            if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;

            net.minecraft.world.item.Item weaponItem = player.getMainHandItem().getItem();
            if (player.getCooldowns().isOnCooldown(weaponItem)) return;

            float cost = StaminaConfig.COMMON.depletionMissedAttack.get().floatValue();

            if (StaminaConfig.COMMON.missedAttackCostScalesWithWeight.get()) {
                ItemStack weapon = player.getMainHandItem();
                double wepWeight = WeightHandler.getItemWeight(weapon, StaminaConfig.COMMON.autoWeightBase.get()) / Math.max(1, weapon.getCount());
                
                double normalizer = StaminaConfig.COMMON.missedAttackWeightNormalizer.get();
                double scaleFactor = StaminaConfig.COMMON.missedAttackWeightScaleFactor.get();
                double minMult = StaminaConfig.COMMON.missedAttackWeightMinMultiplier.get();
                double maxMult = StaminaConfig.COMMON.missedAttackWeightMaxMultiplier.get();

                double weightMult = 1.0 + (((wepWeight - normalizer) / normalizer) * scaleFactor);
                weightMult = Math.max(minMult, Math.min(maxMult, weightMult)); 
                cost *= (float) weightMult;
            }
            
            cost = com.peakstamina.handlers.core.ServerStaminaHandler.applyTirelessDiscount(player.getMainHandItem(), cost);

            final float finalBaseCost = cost;
            
            if (finalBaseCost > 0) {
                 player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                     
                    double usageMult = 1.0;
                    AttributeInstance usageAttr = player.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE.get());
                    if (usageAttr != null) usageMult = usageAttr.getValue();

                    double missedMult = 1.0;
                    AttributeInstance missedAttr = player.getAttribute(StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get());
                    if (missedAttr != null) missedMult = missedAttr.getValue();
                    
                    double finalCalculatedCost = finalBaseCost * usageMult * missedMult;

                    // Consume stamina
                    ServerStaminaHandler.consumeStamina(cap, (float) finalCalculatedCost);
                    
                    if (cap.stamina < 0) cap.stamina = 0;
                    if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                    int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
                    double delayMult = 1.0;
                    AttributeInstance delayAttr = player.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get());
                    if (delayAttr != null) delayMult = delayAttr.getValue();
                    cap.staminaRegenDelay = (int) (baseDelay * delayMult);

                    // Apply cooldown if enabled and they are now out of stamina
                    if (cap.stamina <= 0 && StaminaConfig.COMMON.enableAttackCooldownWhenExhausted.get()) {
                        int cd = StaminaConfig.COMMON.exhaustedAttackCooldownDuration.get();
                        if (cd > 0) {
                            player.getCooldowns().addCooldown(weaponItem, cd);
                        }
                    }
                     
                    StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues, cap.activeBuffs));
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}