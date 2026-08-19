package com.peakstamina.compat.parcool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.alrex.parcool.api.Stamina;
import com.alrex.parcool.api.unstable.action.ParCoolActionEvent;
import com.alrex.parcool.common.action.Action;
import com.mojang.logging.LogUtils;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.data.StaminaData;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class ParCoolCompat {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isParCoolLoaded = false;
    private static Map<String, Float> startCostCache = null;
    private static Map<String, Float> continueCostCache = null;
    private static Field staminaInstanceField;
    private static final Map<Class<?>, Method> methodCache = new HashMap<>();

    private static boolean hasLoggedRefillError = false;
    private static final int REFILL_INTERVAL = 20;

    public static void init() {
        if (ModList.get().isLoaded("parcool")) {
            isParCoolLoaded = true;
            NeoForge.EVENT_BUS.register(ParCoolCompat.class);

            try {
                LOGGER.info("Peak Stamina: Attempting to reflect ParCool staminaInstance...");
                Field field = Stamina.class.getDeclaredField("staminaInstance");
                field.setAccessible(true);
                staminaInstanceField = field;
                LOGGER.info("Peak Stamina: Successfully accessed ParCool staminaInstance field.");
            } catch (Exception e) {
                LOGGER.error("Peak Stamina: Failed to reflect ParCool staminaInstance field!", e);
            }
        }
    }

    public static boolean isLoaded() {
        return isParCoolLoaded;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (!isParCoolLoaded) return;
        if (event.getEntity().tickCount % REFILL_INTERVAL == 0) {
            refillParCoolStamina(event.getEntity());
        }
    }

    public static void refreshCache() {
        List<? extends String> currentConfig = StaminaLists.LISTS.parCoolActionCosts.get();
        startCostCache = new HashMap<>();
        continueCostCache = new HashMap<>();
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

    private static float getStartCost(Action action) {
        String name = action.getClass().getSimpleName();
        if (startCostCache != null && startCostCache.containsKey(name)) {
            return startCostCache.get(name);
        }
        
        short index = com.alrex.parcool.common.action.Actions.getIndexOf(action.getClass());
        if (index >= 0 && index < com.alrex.parcool.common.action.Actions.ACTION_REGISTRIES.size()) {
            int nativeCost = com.alrex.parcool.common.action.Actions.ACTION_REGISTRIES.get(index).getDefaultStaminaConsumption();
            if (nativeCost == 0) return 0.0f;
        }
        return 0.0f; 
    }

    private static float getContinueCost(Action action) {
        String name = action.getClass().getSimpleName();
        if (continueCostCache != null && continueCostCache.containsKey(name)) {
            return continueCostCache.get(name);
        }
        
        short index = com.alrex.parcool.common.action.Actions.getIndexOf(action.getClass());
        if (index >= 0 && index < com.alrex.parcool.common.action.Actions.ACTION_REGISTRIES.size()) {
            int nativeCost = com.alrex.parcool.common.action.Actions.ACTION_REGISTRIES.get(index).getDefaultStaminaConsumption();
            if (nativeCost == 0) return 0.0f;
        }
        return 0.0f; 
    }

    @SubscribeEvent
    public static void onParCoolTryStart(ParCoolActionEvent.TryToStart event) {
        Player player = event.getPlayer();
        refillParCoolStamina(player);
        if (hasInfiniteStamina(player)) return;

        StaminaData cap = player.getData(StaminaCapability.STAMINA);
        if (cap.stamina <= 0 && getStartCost(event.getAction()) > 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onParCoolTryContinue(ParCoolActionEvent.TryToContinue event) {
        Player player = event.getPlayer();
        if (hasInfiniteStamina(player)) return;

        StaminaData cap = player.getData(StaminaCapability.STAMINA);
        float continueCost = getContinueCost(event.getAction());
        
        if (continueCost != 0) {
            if (player.level().isClientSide) {
                double finalCost;
                double parcoolMult = getAttributeValue(player, StaminaAttributes.PARCOOL_COST_MULTIPLIER, 1.0);
                
                if (continueCost > 0) {
                    double usageMult = getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE, 1.0);
                    finalCost = continueCost * usageMult * parcoolMult;
                } else {
                    double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER, 1.0);
                    finalCost = continueCost * actionRecoveryMult;
                }

                // Local Prediction
                if (finalCost > 0) {
                    if (cap.bonusStamina > 0 && cap.bonusStamina >= finalCost) {
                        cap.bonusStamina -= (float) finalCost;
                    } else if (cap.bonusStamina > 0) {
                        float remainder = (float) (finalCost - cap.bonusStamina);
                        cap.bonusStamina = 0;
                        cap.stamina -= remainder;
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                } else {
                    cap.stamina -= (float) finalCost;
                }

                if (cap.stamina < 0) cap.stamina = 0;
                if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                if (continueCost >= 0) {
                    cap.staminaRegenDelay = com.peakstamina.handlers.core.ServerStaminaHandler.getRecoveryDelay(player);
                }

                // Send sync to Server
                net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                    new com.peakstamina.network.packets.parcool.PacketParCoolAction(continueCost)
                );
            }
        }

        if (cap.stamina <= 0 && continueCost > 0) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onParCoolStart(ParCoolActionEvent.Start.Post event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        StaminaData cap = player.getData(StaminaCapability.STAMINA);

        if (!hasInfiniteStamina(player)) {
            float cost = getStartCost(action);
            if (cost != 0) {
                if (player.level().isClientSide) {
                    double finalCost;
                    double parcoolMult = getAttributeValue(player, StaminaAttributes.PARCOOL_COST_MULTIPLIER, 1.0);
                    
                    if (cost > 0) {
                        double usageMult = getAttributeValue(player, StaminaAttributes.GLOBAL_STAMINA_USAGE, 1.0);
                        finalCost = cost * usageMult * parcoolMult;
                    } else {
                        double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER, 1.0);
                        finalCost = cost * actionRecoveryMult;
                    }

                    // Local Prediction
                    if (finalCost > 0) {
                        if (cap.bonusStamina > 0 && cap.bonusStamina >= finalCost) {
                            cap.bonusStamina -= (float) finalCost;
                        } else if (cap.bonusStamina > 0) {
                            float remainder = (float) (finalCost - cap.bonusStamina);
                            cap.bonusStamina = 0;
                            cap.stamina -= remainder;
                        } else {
                            cap.stamina -= (float) finalCost;
                        }
                    } else {
                        cap.stamina -= (float) finalCost;
                    }

                    if (cap.stamina < 0) cap.stamina = 0;
                    if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                    if (cost >= 0) {
                        cap.staminaRegenDelay = com.peakstamina.handlers.core.ServerStaminaHandler.getRecoveryDelay(player);
                    }

                    // Send sync to Server
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                        new com.peakstamina.network.packets.parcool.PacketParCoolAction(cost)
                    );
                }
            }
        }
    }

    private static void refillParCoolStamina(Player player) {
        try {
            if (staminaInstanceField == null) return;

            Stamina parCoolWrapper = Stamina.get(player);
            if (parCoolWrapper == null) return;

            Object internalStamina = staminaInstanceField.get(parCoolWrapper);
            if (internalStamina == null) return;

            Class<?> clazz = internalStamina.getClass();
            Method setMethod = methodCache.get(clazz);
            if (setMethod == null) {
                setMethod = clazz.getMethod("set", int.class);
                methodCache.put(clazz, setMethod);

                if (hasLoggedRefillError) {
                    LOGGER.info("Peak Stamina: ParCool reflection recovered successfully for " + clazz.getSimpleName());
                    hasLoggedRefillError = false;
                }
            }

            setMethod.invoke(internalStamina, 20000);
        } catch (Exception e) {
            if (!hasLoggedRefillError) {
                LOGGER.error("Peak Stamina: Error refilling ParCool stamina!", e);
                hasLoggedRefillError = true;
            }
        }
    }

    private static boolean hasInfiniteStamina(LivingEntity player) {
        List<? extends String> effects = StaminaLists.LISTS.infiniteStaminaEffects.get();
        for (String id : effects) {
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null && BuiltInRegistries.MOB_EFFECT.containsKey(loc)) {
                var effect = BuiltInRegistries.MOB_EFFECT.getHolder(loc).orElse(null);
                if (effect != null && player.hasEffect(effect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double getAttributeValue(Player player, java.util.function.Supplier<Attribute> attrSupplier, double fallback) {
        AttributeInstance instance = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attrSupplier.get()));
        return (instance != null) ? instance.getValue() : fallback;
    }
}