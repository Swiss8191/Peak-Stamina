package com.peakstamina.network;

import com.peakstamina.capabilities.StaminaCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

public class PacketSyncStamina {
    private final float stamina, maxStamina, fatiguePenalty, hungerPenalty, poisonPenalty, weightPenalty;
    private final int exhaustionCooldown;
    private final float bonusStamina; 
    private final float[] penaltyValues;

    public PacketSyncStamina(float stamina, float maxStamina, float fatiguePenalty, float hungerPenalty, float poisonPenalty, float weightPenalty, int exhaustionCooldown, float bonusStamina, float[] penaltyValues) {
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.fatiguePenalty = fatiguePenalty;
        this.hungerPenalty = hungerPenalty;
        this.poisonPenalty = poisonPenalty;
        this.weightPenalty = weightPenalty;
        this.exhaustionCooldown = exhaustionCooldown;
        this.bonusStamina = bonusStamina;
        this.penaltyValues = penaltyValues;
    }

    public static void encode(PacketSyncStamina msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.stamina);
        buf.writeFloat(msg.maxStamina);
        buf.writeFloat(msg.fatiguePenalty);
        buf.writeFloat(msg.hungerPenalty);
        buf.writeFloat(msg.poisonPenalty);
        buf.writeVarInt(msg.exhaustionCooldown);
        buf.writeFloat(msg.weightPenalty);
        buf.writeFloat(msg.bonusStamina); 
        
        buf.writeVarInt(msg.penaltyValues.length);
        for (float f : msg.penaltyValues) {
            buf.writeFloat(f);
        }
    }

    public static PacketSyncStamina decode(FriendlyByteBuf buf) {
        float s = buf.readFloat();
        float m = buf.readFloat();
        float f = buf.readFloat();
        float h = buf.readFloat();
        float p = buf.readFloat();
        int e = buf.readVarInt();
        float w = buf.readFloat();
        float b = buf.readFloat();
        
        int length = buf.readVarInt();
        float[] pv = new float[length];
        for (int i = 0; i < length; i++) {
            pv[i] = buf.readFloat();
        }
        return new PacketSyncStamina(s, m, f, h, p, w, e, b, pv);
    }

    public static void handle(PacketSyncStamina msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientHandler.handlePacket(msg));
        });
        ctx.get().setPacketHandled(true);
    }

    private static class ClientHandler {
        private static void handlePacket(PacketSyncStamina msg) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player != null) {
                mc.player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    cap.stamina = msg.stamina;
                    cap.maxStamina = msg.maxStamina;
                    cap.fatiguePenalty = msg.fatiguePenalty;
                    cap.currentHungerPenalty = msg.hungerPenalty;
                    cap.poisonPenalty = msg.poisonPenalty;
                    cap.exhaustionCooldown = msg.exhaustionCooldown; 
                    cap.weightPenalty = msg.weightPenalty;
                    cap.bonusStamina = msg.bonusStamina;
                    cap.penaltyValues = msg.penaltyValues;
                });
            }
        }
    }
}