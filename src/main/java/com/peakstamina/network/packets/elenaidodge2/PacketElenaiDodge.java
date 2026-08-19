package com.peakstamina.network.packets.elenaidodge2;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public class PacketElenaiDodge {

    private final boolean isMidAir;

    public PacketElenaiDodge(boolean isMidAir) {
        this.isMidAir = isMidAir;
    }

    public PacketElenaiDodge(FriendlyByteBuf buf) {
        this.isMidAir = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isMidAir);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double cost = this.isMidAir ? StaminaLists.LISTS.elenaiDodgeAirCost.get() : StaminaLists.LISTS.elenaiDodgeGroundCost.get();
                    
                    double usageMult = 1.0;
                    if (player.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE.get()) != null) {
                        usageMult = player.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE.get()).getValue();
                    }
                    
                    double dodgeMult = 1.0;
                    if (player.getAttribute(StaminaAttributes.ELENAIDODGE2_COST_MULTIPLIER.get()) != null) {
                        dodgeMult = player.getAttribute(StaminaAttributes.ELENAIDODGE2_COST_MULTIPLIER.get()).getValue();
                    }
                    
                    float finalCost = (float) (cost * usageMult * dodgeMult);
                    
                    if (finalCost > 0) {
                        ServerStaminaHandler.consumeStamina(cap, finalCost);
                        
                        int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
                        double delayMult = 1.0;
                        if (player.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get()) != null) {
                            delayMult = player.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get()).getValue();
                        }
                        cap.staminaRegenDelay = (int) (baseDelay * delayMult);
                        
                        if (cap.stamina < 0) cap.stamina = 0;
                        
                        com.peakstamina.network.StaminaNetwork.CHANNEL.send(
                            PacketDistributor.PLAYER.with(() -> player),
                            new PacketSyncStamina(
                                cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, 
                                cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, 
                                cap.bonusStamina, cap.penaltyValues, cap.activeBuffs
                            )
                        );
                    }
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}