package com.peakstamina.network.packets.walljump;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.compat.walljump.WallJumpCompat;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import java.util.function.Supplier;

public class PacketWallJumpAction {
    private final String actionType;

    public PacketWallJumpAction(String actionType) { this.actionType = actionType; }
    public PacketWallJumpAction(FriendlyByteBuf buf) { this.actionType = buf.readUtf(); }
    public void toBytes(FriendlyByteBuf buf) { buf.writeUtf(this.actionType); }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                    float cost = WallJumpCompat.getStartCost(actionType);
                    
                    // System.out.println("PEAK DEBUG [SERVER]: Received action '" + actionType + "'. Calculated Config Cost: " + cost);

                    if (cost > 0) {
                        ServerStaminaHandler.consumeStamina(cap, (float)(cost * usageMult));
                        cap.staminaRegenDelay = ServerStaminaHandler.getRecoveryDelay(player);
                        
                        StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                            new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
                    }
                });
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}