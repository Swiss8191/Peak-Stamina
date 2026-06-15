package com.peakstamina.network.packets.walljump;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.compat.walljump.WallJumpCompat;
import com.peakstamina.data.StaminaData;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.peakStaminaMod;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketWallJumpAction(String actionType) implements CustomPacketPayload {

    public static final Type<PacketWallJumpAction> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "wall_jump_action"));

    public static final StreamCodec<FriendlyByteBuf, PacketWallJumpAction> STREAM_CODEC = StreamCodec.ofMember(
        PacketWallJumpAction::encode,
        PacketWallJumpAction::decode
    );

    public static PacketWallJumpAction decode(FriendlyByteBuf buf) {
        return new PacketWallJumpAction(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.actionType);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                StaminaData cap = player.getData(StaminaCapability.STAMINA);
                if (cap != null) {
                    double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.STAMINA_USAGE, 1.0);
                    float cost = WallJumpCompat.getStartCost(this.actionType);
                    
                    System.out.println("[PEAK DEBUG - SERVER] Received Action: '" + this.actionType + "' | Config Cost: " + cost);

                    if (cost > 0) {
                        ServerStaminaHandler.consumeStamina(cap, (float)(cost * usageMult));
                        cap.staminaRegenDelay = ServerStaminaHandler.getRecoveryDelay(player);
                        
                        PacketDistributor.sendToPlayer(player, new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
                    }
                }
            }
        });
    }
}