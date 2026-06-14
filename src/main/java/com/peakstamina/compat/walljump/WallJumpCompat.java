package com.peakstamina.compat.walljump;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class WallJumpCompat {

    private static boolean isWallJumpLoaded = false;

    public static final Map<UUID, Boolean> IS_CLINGING = new HashMap<>();
    public static final Map<UUID, Boolean> IS_SPEED_BOOSTING = new HashMap<>();

    private static final Map<String, Float> startCostCache = new HashMap<>();
    private static final Map<String, Float> continueCostCache = new HashMap<>();

    public static void init() {
        if (ModList.get().isLoaded("walljump")) {
            isWallJumpLoaded = true;
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(WallJumpCompat.class);
        }
    }

    public static boolean isLoaded() { return isWallJumpLoaded; }

    public static void refreshCache() {
        startCostCache.clear();
        continueCostCache.clear();
        if (!isWallJumpLoaded) return;

        List<? extends String> currentConfig = StaminaLists.LISTS.wallJumpActionCosts.get();
        for (String entry : currentConfig) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 2) continue;
                String actionName = parts[0].trim();
                for (int i = 1; i < parts.length - 1; i++) {
                    String type = parts[i].trim().toUpperCase();
                    if (type.equals("START") && i + 1 < parts.length) {
                        startCostCache.put(actionName, Float.parseFloat(parts[++i].trim()));
                    } else if (type.equals("CONTINUE") && i + 1 < parts.length) {
                        continueCostCache.put(actionName, Float.parseFloat(parts[++i].trim()));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    public static float getStartCost(String actionName) {
        return startCostCache.getOrDefault(actionName, 0.0f);
    }

    public static float getContinueCost(String actionName) {
        return continueCostCache.getOrDefault(actionName, 0.0f);
    }

    public static void tick(Player player, StaminaCapability cap) {
        if (!isWallJumpLoaded) return;

        UUID id = player.getUUID();
        boolean clinging = IS_CLINGING.getOrDefault(id, false);
        boolean speeding = IS_SPEED_BOOSTING.getOrDefault(id, false);

        if (!clinging && !speeding) return;

        double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
        float totalDrain = 0f;

        if (clinging) totalDrain += getContinueCost("WallCling");
        if (speeding) {
            float baseSpeedDrain = getContinueCost("SpeedBoost");
            float extraDrain = 0.0f;

            net.minecraft.world.item.enchantment.Enchantment speedBoost = net.minecraftforge.registries.ForgeRegistries.ENCHANTMENTS.getValue(new net.minecraft.resources.ResourceLocation("walljump:speed_boost"));
            
            if (speedBoost != null) {
                int level = net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(speedBoost, player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET));

                if (level == 1) {
                    extraDrain = StaminaLists.LISTS.speedBoostExtraLvl1.get().floatValue();
                } else if (level == 2) {
                    extraDrain = StaminaLists.LISTS.speedBoostExtraLvl2.get().floatValue();
                } else if (level >= 3) {
                    extraDrain = StaminaLists.LISTS.speedBoostExtraLvl3.get().floatValue();
                }
            }
            
            totalDrain += (baseSpeedDrain + extraDrain);
        }

        if (totalDrain > 0) {
            ServerStaminaHandler.consumeStamina(cap, (float) (totalDrain * usageMult));
            cap.staminaRegenDelay = StaminaConfig.COMMON.recoveryDelay.get();
            
            if (cap.stamina < 0) cap.stamina = 0;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        IS_CLINGING.remove(id);
        IS_SPEED_BOOSTING.remove(id);
    }
}