package com.peakstamina.compat.elenaidodge2;

import com.elenai.elenaidodge2.client.KeyBinding;
import com.elenai.elenaidodge2.event.ClientEvents;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.network.packets.elenaidodge2.PacketElenaiDodge;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ElenaiDodgeClientCompat {
    
    private static int previousDodgeCooldown = 0;
    private static boolean wasOnGroundAtDodge = false;

    public static void init() {
        MinecraftForge.EVENT_BUS.register(ElenaiDodgeClientCompat.class);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (KeyBinding.DODGE_KEY.isDown()) {
            boolean onGround = mc.player.onGround();
            
            mc.player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                double cost = onGround ? StaminaLists.LISTS.elenaiDodgeGroundCost.get() : StaminaLists.LISTS.elenaiDodgeAirCost.get();
                
                if (cap.stamina < cost) {
                    while (KeyBinding.DODGE_KEY.consumeClick()) {
                        // Eating the input to prevent dodging when out of stamina
                    }
                } else {
                    wasOnGroundAtDodge = onGround;
                }
            });
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Listen to ED2's internal cooldown to know exactly when a dodge successfully executes
        int currentDodgeCooldown = ClientEvents.currentCooldown;
        
        if (previousDodgeCooldown == 0 && currentDodgeCooldown > 0) {
            System.out.println("[PEAK DEBUG] Client detected dodge! Sending packet to server...");
            StaminaNetwork.CHANNEL.sendToServer(new PacketElenaiDodge(!wasOnGroundAtDodge));
        }
        
        previousDodgeCooldown = currentDodgeCooldown;
    }
}