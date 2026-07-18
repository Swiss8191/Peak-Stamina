package com.peakstamina.network.packets.walljump;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.data.StaminaData;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.compat.walljump.WallJumpCompat;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.peakStaminaMod;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketWallJumpAction implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PacketWallJumpAction> TYPE = 
        new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "wall_jump_action"));

    public static final StreamCodec<FriendlyByteBuf, PacketWallJumpAction> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8,
        msg -> msg.actionType,
        PacketWallJumpAction::new
    );

    private final String actionType;

    public PacketWallJumpAction(String actionType) { 
        this.actionType = actionType; 
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            
            StaminaData cap = player.getData(StaminaCapability.STAMINA);
            
            double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE, 1.0);
            double wallJumpMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.WALLJUMPTXF_COST_MULTIPLIER, 1.0);
            
            float cost = WallJumpCompat.getStartCost(actionType);

            if (cost > 0) {
                ServerStaminaHandler.consumeStamina(cap, (float)(cost * usageMult * wallJumpMult));
                cap.staminaRegenDelay = ServerStaminaHandler.getRecoveryDelay(player);
                
                if (cap.stamina < 0) cap.stamina = 0;
                if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                PacketDistributor.sendToPlayer(player,
                    new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
            }
        });
    }
}