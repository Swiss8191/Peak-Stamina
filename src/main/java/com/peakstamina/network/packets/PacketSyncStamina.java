package com.peakstamina.network.packets;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.data.StaminaData;
import com.peakstamina.peakStaminaMod;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncStamina(float stamina, float maxStamina, float fatiguePenalty, float hungerPenalty, float poisonPenalty, float weightPenalty, int exhaustionCooldown, float bonusStamina, float[] penaltyValues, java.util.List<StaminaData.BuffInstance> activeBuffs) implements CustomPacketPayload {

    public PacketSyncStamina {
        if (activeBuffs == null) activeBuffs = java.util.List.of();
        if (penaltyValues == null) penaltyValues = new float[0];
    }

    public static final CustomPacketPayload.Type<PacketSyncStamina> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "sync_stamina"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncStamina> STREAM_CODEC = StreamCodec.ofMember(
        PacketSyncStamina::encode,
        PacketSyncStamina::decode
    );

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

        int buffCount = buf.readVarInt();
        java.util.List<StaminaData.BuffInstance> buffs = new java.util.ArrayList<>(buffCount);
        for (int i = 0; i < buffCount; i++) {
            String attr = buf.readUtf();
            double amount = buf.readDouble();
            int operation = buf.readVarInt();
            int duration = buf.readVarInt();
            String sourceItemId = buf.readBoolean() ? buf.readUtf() : null;
            buffs.add(new StaminaData.BuffInstance(attr, amount, operation, duration, sourceItemId));
        }

        return new PacketSyncStamina(s, m, f, h, p, w, e, b, pv, buffs);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.stamina);
        buf.writeFloat(this.maxStamina);
        buf.writeFloat(this.fatiguePenalty);
        buf.writeFloat(this.hungerPenalty);
        buf.writeFloat(this.poisonPenalty);
        buf.writeVarInt(this.exhaustionCooldown);
        buf.writeFloat(this.weightPenalty);
        buf.writeFloat(this.bonusStamina);

        buf.writeVarInt(this.penaltyValues.length);
        for (float f : this.penaltyValues) {
            buf.writeFloat(f);
        }

        buf.writeVarInt(this.activeBuffs.size());
        for (StaminaData.BuffInstance buff : this.activeBuffs) {
            buf.writeUtf(buff.attributeName);
            buf.writeDouble(buff.amount);
            buf.writeVarInt(buff.operation);
            buf.writeVarInt(buff.durationTicks);
            buf.writeBoolean(buff.sourceItemId != null);
            if (buff.sourceItemId != null) {
                buf.writeUtf(buff.sourceItemId);
            }
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientHandler.handlePacket(this);
            }
        });
    }

    private static class ClientHandler {
        private static void handlePacket(PacketSyncStamina msg) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                StaminaData cap = mc.player.getData(StaminaCapability.STAMINA);
                if (cap != null) {
                    cap.stamina = msg.stamina();
                    cap.maxStamina = msg.maxStamina();
                    cap.fatiguePenalty = msg.fatiguePenalty();
                    cap.currentHungerPenalty = msg.hungerPenalty();
                    cap.poisonPenalty = msg.poisonPenalty();
                    cap.exhaustionCooldown = msg.exhaustionCooldown();
                    cap.weightPenalty = msg.weightPenalty();
                    cap.bonusStamina = msg.bonusStamina();
                    cap.penaltyValues = msg.penaltyValues();
                    cap.activeBuffs.clear();
                    cap.activeBuffs.addAll(msg.activeBuffs());
                }
            }
        }
    }
}