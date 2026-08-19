package com.peakstamina.network.packets;

import com.peakstamina.capabilities.StaminaCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketSyncStamina {
    private final float stamina, maxStamina, fatiguePenalty, hungerPenalty, poisonPenalty, weightPenalty;
    private final int exhaustionCooldown;
    private final float bonusStamina; 
    private final float[] penaltyValues;
    private final List<StaminaCapability.BuffInstance> activeBuffs;

    public PacketSyncStamina(float stamina, float maxStamina, float fatiguePenalty, float hungerPenalty, float poisonPenalty, float weightPenalty, int exhaustionCooldown, float bonusStamina, float[] penaltyValues, List<StaminaCapability.BuffInstance> activeBuffs) {
        this.stamina = stamina;
        this.maxStamina = maxStamina;
        this.fatiguePenalty = fatiguePenalty;
        this.hungerPenalty = hungerPenalty;
        this.poisonPenalty = poisonPenalty;
        this.weightPenalty = weightPenalty;
        this.exhaustionCooldown = exhaustionCooldown;
        this.bonusStamina = bonusStamina;
        this.penaltyValues = penaltyValues;
        this.activeBuffs = activeBuffs != null ? activeBuffs : new ArrayList<>();
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

        // Write Buffs
        buf.writeVarInt(msg.activeBuffs.size());
        for (StaminaCapability.BuffInstance buff : msg.activeBuffs) {
            buf.writeUtf(buff.attributeName);
            buf.writeDouble(buff.amount);
            buf.writeVarInt(buff.operation);
            buf.writeVarInt(buff.durationTicks);
            buf.writeUtf(buff.sourceItem != null ? buff.sourceItem : "");
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

        // Read Buffs
        int buffCount = buf.readVarInt();
        List<StaminaCapability.BuffInstance> buffs = new ArrayList<>();
        for (int i = 0; i < buffCount; i++) {
            buffs.add(new StaminaCapability.BuffInstance(
                buf.readUtf(),
                buf.readDouble(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readUtf()
            ));
        }

        return new PacketSyncStamina(s, m, f, h, p, w, e, b, pv, buffs);
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
                    
                    // Sync active buffs to client
                    cap.activeBuffs.clear();
                    cap.activeBuffs.addAll(msg.activeBuffs);
                });
            }
        }
    }
}