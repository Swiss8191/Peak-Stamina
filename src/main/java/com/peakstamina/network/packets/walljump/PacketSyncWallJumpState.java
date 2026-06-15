package com.peakstamina.network.packets.walljump;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import java.util.function.Supplier;

import com.peakstamina.compat.walljump.WallJumpCompat;

public class PacketSyncWallJumpState {
    private final boolean isClinging;
    private final boolean isSpeedBoosting;

    public PacketSyncWallJumpState(boolean isClinging, boolean isSpeedBoosting) {
        this.isClinging = isClinging;
        this.isSpeedBoosting = isSpeedBoosting;
    }

    public PacketSyncWallJumpState(FriendlyByteBuf buf) {
        this.isClinging = buf.readBoolean();
        this.isSpeedBoosting = buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(this.isClinging);
        buf.writeBoolean(this.isSpeedBoosting);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Update the server's knowledge of what the client is doing
                WallJumpCompat.IS_CLINGING.put(player.getUUID(), this.isClinging);
                WallJumpCompat.IS_SPEED_BOOSTING.put(player.getUUID(), this.isSpeedBoosting);
            }
        });
        context.setPacketHandled(true);
        return true;
    }
}