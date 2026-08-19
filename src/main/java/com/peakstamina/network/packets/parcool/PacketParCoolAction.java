package com.peakstamina.network.packets.parcool;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.data.StaminaData;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.peakStaminaMod;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketParCoolAction(float baseCost) implements CustomPacketPayload {

    public static final Type<PacketParCoolAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "parcool_action"));

    public static final StreamCodec<FriendlyByteBuf, PacketParCoolAction> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        PacketParCoolAction::baseCost,
        PacketParCoolAction::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.flow().isServerbound() && ctx.player() instanceof ServerPlayer player) {
                StaminaData cap = player.getData(StaminaCapability.STAMINA);
                double finalCost;
                double parcoolMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.PARCOOL_COST_MULTIPLIER, 1.0);
                
                if (this.baseCost > 0) {
                    double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE, 1.0);
                    finalCost = this.baseCost * usageMult * parcoolMult;
                } else {
                    double actionRecoveryMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER, 1.0);
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
            }
        });
    }
}