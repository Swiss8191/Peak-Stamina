package com.peakstamina.compat.walljump;

import com.mojang.logging.LogUtils;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.data.StaminaData;
import com.peakstamina.network.packets.walljump.PacketSyncWallJumpState;
import com.peakstamina.network.packets.walljump.PacketWallJumpAction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
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
                NeoForge.EVENT_BUS.register(WallJumpClientCompat.class);
            } catch (Exception e) {
                LOGGER.error("Peak Stamina: Failed to initialize Wall-Jump client reflection.", e);
                e.printStackTrace(); 
                isWallJumpLoaded = false;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (!isWallJumpLoaded) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        try {
            int currentWallJumps = wjWallJumpCountField.getInt(null);
            int currentDoubleJumps = djJumpCountField.getInt(null);
            int currentTicksClinged = wjTicksWallClingedField.getInt(null);

            if (currentWallJumps > prevWallJumpCount) {
                PacketDistributor.sendToServer(new PacketWallJumpAction("WallJump"));
            }
            if (currentDoubleJumps < prevDoubleJumpCount) {
                PacketDistributor.sendToServer(new PacketWallJumpAction("DoubleJump"));
            }

            if (player.horizontalCollision && !player.input.jumping) {
                if (player.getY() - prevY >= 0.5) {
                    PacketDistributor.sendToServer(new PacketWallJumpAction("StepAssist"));
                }
            }

            boolean isClinging = currentTicksClinged > 0;
            boolean isSpeedBoosting = player.isSprinting() && getSpeedBoostLevel(player) > 0;

            if (isClinging != wasClinging || isSpeedBoosting != wasSpeedBoosting) {
                PacketDistributor.sendToServer(new PacketSyncWallJumpState(isClinging, isSpeedBoosting));

                if (isClinging && !wasClinging) {
                    PacketDistributor.sendToServer(new PacketWallJumpAction("WallCling"));
                }

                if (isSpeedBoosting && !wasSpeedBoosting) {
                    PacketDistributor.sendToServer(new PacketWallJumpAction("SpeedBoost"));
                }

                wasClinging = isClinging;
                wasSpeedBoosting = isSpeedBoosting;
            }

            if (isClinging) {
                StaminaData cap = player.getData(StaminaCapability.STAMINA); 
                if (cap != null && cap.stamina <= 0 && com.peakstamina.config.StaminaLists.LISTS.dropOnEmptyWallCling.get()) {
                    try {
                        wjTicksWallClingedField.set(null, 0); 
                    } catch (Exception ignored) {}
                }
            }

            prevWallJumpCount = currentWallJumps;
            prevDoubleJumpCount = currentDoubleJumps;
            prevY = player.getY();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int getSpeedBoostLevel(Player player) {
        net.minecraft.world.item.ItemStack stack = player.getItemBySlot(EquipmentSlot.FEET);
        if (stack.isEmpty()) return 0;
        try {
            net.minecraft.world.item.enchantment.ItemEnchantments enchants = net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentsForCrafting(stack);
            net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> spHolder = player.level().registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                    .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("walljump", "speed_boost")));
            return enchants.getLevel(spHolder);
        } catch (Exception e) {
            return 0;
        }
    }
}