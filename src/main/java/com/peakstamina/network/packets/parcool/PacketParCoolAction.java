package com.peakstamina.network.packets.parcool;

import java.util.function.Supplier;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class PacketParCoolAction {
    private final float baseCost;

    public PacketParCoolAction(float baseCost) {
        this.baseCost = baseCost;
    }

    public PacketParCoolAction(FriendlyByteBuf buf) {
        this.baseCost = buf.readFloat();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeFloat(this.baseCost);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double finalCost;
                    double parcoolMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.PARCOOL_COST_MULTIPLIER.get(), 1.0);
                    
                    if (this.baseCost > 0) {
                        double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE.get(), 1.0);
                        finalCost = this.baseCost * usageMult * parcoolMult;
                    } else {
                        double actionRecoveryMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER.get(), 1.0);
                        finalCost = this.baseCost * actionRecoveryMult;
                    }

                    // Apply server-side stamina drain
                    if (finalCost > 0) {
                        ServerStaminaHandler.consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                    
                    if (cap.stamina < 0) cap.stamina = 0;
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    // Reset regeneration delay on the server
                    if (this.baseCost >= 0) {
                        cap.staminaRegenDelay = ServerStaminaHandler.getRecoveryDelay(player);
                    }
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}