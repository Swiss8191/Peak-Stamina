package com.peakstamina.compat.walljump;

import com.peakstamina.data.StaminaData;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

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
            NeoForge.EVENT_BUS.register(WallJumpCompat.class);
        }
    }

    public static boolean isLoaded() { return isWallJumpLoaded; }

    public static void refreshCache() {
        startCostCache.clear();
        continueCostCache.clear();
        if (!isWallJumpLoaded) return;

        try {
            List<? extends String> currentConfig = StaminaLists.LISTS.wallJumpActionCosts.get();
            for (String entry : currentConfig) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length < 3) continue;
                    
                    String actionName = parts[0].trim();
                    for (int i = 1; i < parts.length - 1; i++) {
                        String token = parts[i].trim().toUpperCase();
                        if (token.equals("START")) {
                            startCostCache.put(actionName, Float.parseFloat(parts[i + 1].trim()));
                        } else if (token.equals("CONTINUE")) {
                            continueCostCache.put(actionName, Float.parseFloat(parts[i + 1].trim()));
                        }
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
    }

    public static float getStartCost(String actionName) {
        return startCostCache.getOrDefault(actionName, 0.0f);
    }

    public static float getContinueCost(String actionName) {
        return continueCostCache.getOrDefault(actionName, 0.0f);
    }

    private static int getSpeedBoostLevel(Player player) {
        net.minecraft.world.item.ItemStack stack = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
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

    public static void tick(Player player, StaminaData cap) {
        if (!isWallJumpLoaded) return;

        UUID id = player.getUUID();
        boolean clinging = IS_CLINGING.getOrDefault(id, false);
        boolean speeding = IS_SPEED_BOOSTING.getOrDefault(id, false);

        if (!clinging && !speeding) return;

        double usageMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE, 1.0);
        double wallJumpMult = ServerStaminaHandler.getAttributeValue(player, StaminaAttributes.WALLJUMPTXF_COST_MULTIPLIER, 1.0);
        float totalDrain = 0f;

        if (clinging) totalDrain += getContinueCost("WallCling");
        
        if (speeding) {
            float baseSpeedDrain = getContinueCost("SpeedBoost");
            float extraDrain = 0.0f;

            int level = getSpeedBoostLevel(player);
            if (level == 1) {
                extraDrain = StaminaLists.LISTS.speedBoostExtraLvl1.get().floatValue();
            } else if (level == 2) {
                extraDrain = StaminaLists.LISTS.speedBoostExtraLvl2.get().floatValue();
            } else if (level >= 3) {
                extraDrain = StaminaLists.LISTS.speedBoostExtraLvl3.get().floatValue();
            }
            
            totalDrain += (baseSpeedDrain + extraDrain);
        }

        if (totalDrain > 0) {
            double finalCost = totalDrain * usageMult * wallJumpMult;
            ServerStaminaHandler.consumeStamina(cap, (float) finalCost);
            cap.staminaRegenDelay = StaminaConfig.COMMON.recoveryDelay.get(); 
            
            if (cap.stamina < 0) cap.stamina = 0;
            if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.getEntity().getUUID();
        IS_CLINGING.remove(id);
        IS_SPEED_BOOSTING.remove(id);
    }
}