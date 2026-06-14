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

public record PacketSyncStamina(float stamina, float maxStamina, float fatiguePenalty, float hungerPenalty, float poisonPenalty, float weightPenalty, int exhaustionCooldown, float bonusStamina, float[] penaltyValues) implements CustomPacketPayload {

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
        return new PacketSyncStamina(s, m, f, h, p, w, e, b, pv);
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
                }
            }
        }
    }
}