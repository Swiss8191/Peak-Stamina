package com.peakstamina.compat.walljump;

import com.mojang.logging.LogUtils;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.network.packets.walljump.PacketSyncWallJumpState;
import com.peakstamina.network.packets.walljump.PacketWallJumpAction;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public class WallJumpClientCompat {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isWallJumpLoaded = false;

    private static Field wjWallJumpCountField;
    private static Field wjTicksWallClingedField;
    private static Field djJumpCountField;

    private static int prevWallJumpCount = 0;
    private static int prevDoubleJumpCount = 0;
    
    private static boolean wasClinging = false;
    private static boolean wasSpeedBoosting = false;
    
    private static double prevY = 0;

    public static void init() {
        if (ModList.get().isLoaded("walljump")) {
            try {
                Class<?> wjLogicClass = Class.forName("com.jahirtrap.walljump.logic.WallJumpLogic");
                wjWallJumpCountField = wjLogicClass.getDeclaredField("wallJumpCount");
                wjWallJumpCountField.setAccessible(true);
                
                wjTicksWallClingedField = wjLogicClass.getDeclaredField("ticksWallClinged");
                wjTicksWallClingedField.setAccessible(true);

                Class<?> djLogicClass = Class.forName("com.jahirtrap.walljump.logic.DoubleJumpLogic");
                djJumpCountField = djLogicClass.getDeclaredField("jumpCount");
                djJumpCountField.setAccessible(true);

                isWallJumpLoaded = true;
                MinecraftForge.EVENT_BUS.register(WallJumpClientCompat.class);
            } catch (Exception e) {
                LOGGER.error("Peak Stamina: Failed to initialize Wall-Jump client reflection.", e);
                e.printStackTrace(); 
                isWallJumpLoaded = false;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (!isWallJumpLoaded || event.phase != TickEvent.Phase.END) return;

        net.minecraft.client.player.LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        try {
            int currentWallJumps = wjWallJumpCountField.getInt(null);
            int currentDoubleJumps = djJumpCountField.getInt(null);
            int currentTicksClinged = wjTicksWallClingedField.getInt(null);

            if (currentWallJumps > prevWallJumpCount) {
                System.out.println(">>> PEAK DEBUG [CLIENT]: WallJump detected! Sending packet to server...");
                StaminaNetwork.CHANNEL.sendToServer(new PacketWallJumpAction("WallJump"));
            }
            if (currentDoubleJumps < prevDoubleJumpCount) {
                StaminaNetwork.CHANNEL.sendToServer(new PacketWallJumpAction("DoubleJump"));
            }

            if (player.horizontalCollision && !player.input.jumping) {
                if (player.getY() - prevY >= 0.5) {
                    StaminaNetwork.CHANNEL.sendToServer(new PacketWallJumpAction("StepAssist"));
                }
            }

            boolean isClinging = currentTicksClinged > 0;
            boolean isSpeedBoosting = player.isSprinting() && hasSpeedBoostEnchant(player);

            if (isClinging != wasClinging || isSpeedBoosting != wasSpeedBoosting) {
                StaminaNetwork.CHANNEL.sendToServer(new PacketSyncWallJumpState(isClinging, isSpeedBoosting));
                wasClinging = isClinging;
                wasSpeedBoosting = isSpeedBoosting;
            }

            if (isClinging) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    if (cap.stamina <= 0 && com.peakstamina.config.StaminaLists.LISTS.dropOnEmptyWallCling.get()) {
                        try {
                            wjTicksWallClingedField.set(null, 0); // Forces player to drop
                        } catch (Exception ignored) {}
                    }
                });
            }

            prevWallJumpCount = currentWallJumps;
            prevDoubleJumpCount = currentDoubleJumps;
            prevY = player.getY();

        } catch (Exception e) {
            // System.out.println("PEAK DEBUG: TICK REFLECTION CRASHED!"); 
            e.printStackTrace();
        }
    }

    private static boolean hasSpeedBoostEnchant(Player player) {
        Enchantment speedBoost = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation("walljump", "speed_boost"));
        if (speedBoost != null) {
            return EnchantmentHelper.getItemEnchantmentLevel(speedBoost, player.getItemBySlot(EquipmentSlot.FEET)) > 0;
        }
        return false;
    }
}