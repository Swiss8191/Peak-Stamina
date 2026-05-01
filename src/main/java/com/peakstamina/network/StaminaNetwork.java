package com.peakstamina.network;

import com.peakstamina.PeakStaminaMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = PeakStaminaMod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class StaminaNetwork {

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Create a registrar for your mod
        final PayloadRegistrar registrar = event.registrar(PeakStaminaMod.MODID).versioned("1.0");

        // Register Server -> Client payload
        registrar.playToClient(
            SyncStaminaPayload.TYPE, 
            SyncStaminaPayload.STREAM_CODEC, 
            SyncStaminaPayload::handle
        );

        // Register Client -> Server payload
        registrar.playToServer(
            MissedAttackPayload.TYPE, 
            MissedAttackPayload.STREAM_CODEC, 
            MissedAttackPayload::handle
        );
    }
}