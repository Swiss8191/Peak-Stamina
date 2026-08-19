package com.peakstamina.network;

import com.peakstamina.network.packets.PacketMissedAttack;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.network.packets.parcool.PacketParCoolAction;
import com.peakstamina.network.packets.walljump.PacketSyncWallJumpState;
import com.peakstamina.network.packets.walljump.PacketWallJumpAction;
import com.peakstamina.peakStaminaMod;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class StaminaNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(peakStaminaMod.MODID)
                .versioned(PROTOCOL_VERSION);

        // Server -> Client
        registrar.playToClient(
                PacketSyncStamina.TYPE,
                PacketSyncStamina.STREAM_CODEC,
                PacketSyncStamina::handle
        );
        
        // Client -> Server
        registrar.playToServer(
                PacketMissedAttack.TYPE,
                PacketMissedAttack.STREAM_CODEC,
                PacketMissedAttack::handle
        );
        registrar.playToServer(
                PacketWallJumpAction.TYPE,
                PacketWallJumpAction.STREAM_CODEC,
                PacketWallJumpAction::handle
        );
        registrar.playToServer(
                PacketSyncWallJumpState.TYPE,
                PacketSyncWallJumpState.STREAM_CODEC,
                PacketSyncWallJumpState::handle
        );
        registrar.playToServer(
                PacketParCoolAction.TYPE,
                PacketParCoolAction.STREAM_CODEC,
                PacketParCoolAction::handle
        );
    }
}
