package com.peakstamina.network.packets.walljump;

import com.peakstamina.compat.walljump.WallJumpCompat;
import com.peakstamina.peakStaminaMod;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PacketSyncWallJumpState(boolean isClinging, boolean isSpeedBoosting) implements CustomPacketPayload {

    public static final Type<PacketSyncWallJumpState> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "sync_wall_jump_state"));

    public static final StreamCodec<FriendlyByteBuf, PacketSyncWallJumpState> STREAM_CODEC = StreamCodec.ofMember(
        PacketSyncWallJumpState::encode,
        PacketSyncWallJumpState::decode
    );

    public static PacketSyncWallJumpState decode(FriendlyByteBuf buf) {
        return new PacketSyncWallJumpState(buf.readBoolean(), buf.readBoolean());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isClinging);
        buf.writeBoolean(this.isSpeedBoosting);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // System.out.println("[PEAK DEBUG - SERVER] Syncing State -> Player: " + player.getName().getString() + " | Clinging: " + this.isClinging + " | Speeding: " + this.isSpeedBoosting);
                WallJumpCompat.IS_CLINGING.put(player.getUUID(), this.isClinging);
                WallJumpCompat.IS_SPEED_BOOSTING.put(player.getUUID(), this.isSpeedBoosting);
            }
        });
    }
}