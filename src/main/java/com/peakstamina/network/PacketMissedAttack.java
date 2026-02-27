package com.peakstamina.network;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.handlers.ServerStaminaHandler; 
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

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

            float cost = StaminaConfig.COMMON.depletionMissedAttack.get().floatValue();
            
            if (cost > 0) {
                 player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                     
                    double usageMult = 1.0;
                    AttributeInstance usageAttr = player.getAttribute(StaminaAttributes.STAMINA_USAGE.get());
                    if (usageAttr != null) usageMult = usageAttr.getValue();

                    double missedMult = 1.0;
                    AttributeInstance missedAttr = player.getAttribute(StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get());
                    if (missedAttr != null) missedMult = missedAttr.getValue();
                    
                    double finalCost = cost * usageMult * missedMult;

                    ServerStaminaHandler.consumeStamina(cap, (float) finalCost);
                    
                    if (cap.stamina < 0) cap.stamina = 0;
                    if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                    int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
                    double delayMult = 1.0;
                    AttributeInstance delayAttr = player.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get());
                    if (delayAttr != null) delayMult = delayAttr.getValue();
                    
                    cap.staminaRegenDelay = (int) (baseDelay * delayMult);
                     
                    StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
                 });
            }
        });
        ctx.get().setPacketHandled(true);
    }
}