package com.peakstamina.network;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.network.packets.PacketMissedAttack;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.network.packets.walljump.PacketSyncWallJumpState;
import com.peakstamina.network.packets.walljump.PacketWallJumpAction;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = peakStaminaMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class StaminaNetwork {
    private static final String PROTOCOL_VERSION = "1";

    @SubscribeEvent
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
    }
}