package com.peakstamina.network;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.network.packets.PacketMissedAttack;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.network.packets.walljump.PacketSyncWallJumpState;
import com.peakstamina.network.packets.walljump.PacketWallJumpAction;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class StaminaNetwork {

    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(peakStaminaMod.MODID, "stamina_net"),
            () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, PacketSyncStamina.class, PacketSyncStamina::encode, PacketSyncStamina::decode, PacketSyncStamina::handle);
        CHANNEL.registerMessage(id++, PacketMissedAttack.class, PacketMissedAttack::encode, PacketMissedAttack::decode, PacketMissedAttack::handle);
        CHANNEL.messageBuilder(PacketWallJumpAction.class, id++, NetworkDirection.PLAY_TO_SERVER).encoder(PacketWallJumpAction::toBytes).decoder(PacketWallJumpAction::new).consumerMainThread(PacketWallJumpAction::handle).add();
        CHANNEL.messageBuilder(PacketSyncWallJumpState.class, id++, NetworkDirection.PLAY_TO_SERVER).encoder(PacketSyncWallJumpState::toBytes).decoder(PacketSyncWallJumpState::new).consumerMainThread(PacketSyncWallJumpState::handle).add();
    }
}
