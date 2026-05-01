package com.peakstamina.network;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.data.StaminaData;
import com.peakstamina.registry.StaminaAttachments;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncStaminaPayload(float stamina, float maxStamina, float fatiguePenalty, float hungerPenalty, float poisonPenalty, float weightPenalty, int exhaustionCooldown, float bonusStamina, float[] penaltyValues) implements CustomPacketPayload {
    
    public static final Type<SyncStaminaPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PeakStaminaMod.MODID, "sync_stamina"));
    public static final StreamCodec<FriendlyByteBuf, SyncStaminaPayload> STREAM_CODEC = StreamCodec.ofMember(SyncStaminaPayload::write, SyncStaminaPayload::new);

    public SyncStaminaPayload(FriendlyByteBuf buf) {
        this(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readVarInt(), buf.readFloat(), readFloatArray(buf));
    }

    private static float[] readFloatArray(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        float[] pv = new float[length];
        for (int i = 0; i < length; i++) pv[i] = buf.readFloat();
        return pv;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeFloat(stamina);
        buf.writeFloat(maxStamina);
        buf.writeFloat(fatiguePenalty);
        buf.writeFloat(hungerPenalty);
        buf.writeFloat(poisonPenalty);
        buf.writeFloat(weightPenalty);
        buf.writeVarInt(exhaustionCooldown);
        buf.writeFloat(bonusStamina);
        buf.writeVarInt(penaltyValues.length);
        for (float f : penaltyValues) buf.writeFloat(f);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.flow().isClientbound()) {
                net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    StaminaData cap = player.getData(StaminaAttachments.STAMINA);
                    cap.stamina = this.stamina;
                    cap.maxStamina = this.maxStamina;
                    cap.fatiguePenalty = this.fatiguePenalty;
                    cap.currentHungerPenalty = this.hungerPenalty;
                    cap.poisonPenalty = this.poisonPenalty;
                    cap.exhaustionCooldown = this.exhaustionCooldown;
                    cap.weightPenalty = this.weightPenalty;
                    cap.bonusStamina = this.bonusStamina;
                    cap.penaltyValues = this.penaltyValues;
                }
            }
        });
    }
}