package com.peakstamina.compat;

import com.alrex.parcool.api.Stamina;
import com.alrex.parcool.api.unstable.action.ParCoolActionEvent;
import com.alrex.parcool.common.action.Action;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists; 
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParCoolCompat {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isParCoolLoaded = false;
    private static Map<String, Float> startCostCache = null;
    private static Map<String, Float> continueCostCache = null;
    private static List<? extends String> lastConfigRef = null;

    private static Field staminaInstanceField;
    private static final Map<Class<?>, Method> methodCache = new HashMap<>();

    private static boolean hasLoggedRefillError = false;
    private static final int REFILL_INTERVAL = 20;

    public static void init() {
        if (ModList.get().isLoaded("parcool")) {
            isParCoolLoaded = true;
            MinecraftForge.EVENT_BUS.register(ParCoolCompat.class);

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
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!isParCoolLoaded) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            if (event.player.tickCount % REFILL_INTERVAL == 0) {
                refillParCoolStamina(event.player);
            }
        }
    }

    private static void refreshCache() {
        List<? extends String> currentConfig = StaminaLists.LISTS.parCoolActionCosts.get();
        if (startCostCache != null && currentConfig == lastConfigRef) {
            return;
        }

        startCostCache = new HashMap<>();
        continueCostCache = new HashMap<>();
        lastConfigRef = currentConfig;
        for (String entry : currentConfig) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 2) {
                    continue;
                }
                String actionName = parts[0].trim();
                for (int i = 1; i < parts.length - 1; i++) {
                    String type = parts[i].trim().toUpperCase();
                    if (type.equals("START") && i + 1 < parts.length) {
                        startCostCache.put(actionName, Float.parseFloat(parts[++i].trim()));
                    } else if (type.equals("CONTINUE") && i + 1 < parts.length) {
                        continueCostCache.put(actionName, Float.parseFloat(parts[++i].trim()));
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static float getStartCost(Action action) {
        refreshCache();
        return startCostCache.getOrDefault(action.getClass().getSimpleName(), 5.0f);
    }

    private static float getContinueCost(String actionName) {
        refreshCache();
        return continueCostCache.getOrDefault(actionName, 0.2f);
    }

    @SubscribeEvent
    public static void onParCoolTryStart(ParCoolActionEvent.TryToStartEvent event) {
        Player player = event.getPlayer();
        refillParCoolStamina(player);

        if (hasInfiniteStamina(player)) {
            return;
        }

        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            if (cap.stamina <= 0) {
                event.setCanceled(true);
                return;
            }
        });
    }

    @SubscribeEvent
    public static void onParCoolTryContinue(ParCoolActionEvent.TryToContinueEvent event) {
        Player player = event.getPlayer();
        if (hasInfiniteStamina(player)) {
            return;
        }

        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            if (cap.stamina <= 0) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onParCoolStart(ParCoolActionEvent.StartEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            if (!hasInfiniteStamina(player)) {
                float cost = getStartCost(action);
                if (cost != 0) {
                    double finalCost;
                    
                    if (cost > 0) {
                        double usageMult = getAttributeValue(player, com.peakstamina.registry.StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                        finalCost = cost * usageMult;
                    } else {
                        double actionRecoveryMult = getAttributeValue(player, com.peakstamina.registry.StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);
                        finalCost = cost * actionRecoveryMult;
                    }

                    cap.stamina -= (float) finalCost;

                    if (cap.stamina < 0) {
                        cap.stamina = 0;
                    }

                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }
                    if (cost > 0) {
                        cap.staminaRegenDelay = StaminaConfig.COMMON.recoveryDelay.get();
                    }
                }
            }
            cap.currentParCoolAction = action.getClass().getSimpleName();
        });
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onParCoolStop(ParCoolActionEvent.StopEvent event) {
        Player player = event.getPlayer();
        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            if (event.getAction().getClass().getSimpleName().equals(cap.currentParCoolAction)) {
                cap.currentParCoolAction = null;
            }
        });
    }

    public static void tick(Player player, StaminaCapability cap) {
        if (!isParCoolLoaded) {
            return;
        }
        if (player.tickCount % REFILL_INTERVAL == 0) {
            refillParCoolStamina(player);
        }

        if (cap.currentParCoolAction == null) {
            return;
        }
        if (hasInfiniteStamina(player)) {
            return;
        }

        float continueCost = getContinueCost(cap.currentParCoolAction);
        if (continueCost != 0) {
            double finalCost;
            if (continueCost > 0) {
                double usageMult = getAttributeValue(player, com.peakstamina.registry.StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                finalCost = continueCost * usageMult;
            } else {
                double actionRecoveryMult = getAttributeValue(player, com.peakstamina.registry.StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);
                finalCost = continueCost * actionRecoveryMult;
            }

            cap.stamina -= (float) finalCost;
            if (cap.stamina < 0) {
                cap.stamina = 0;
            }
            if (cap.stamina > cap.maxStamina) {
                cap.stamina = cap.maxStamina;
            }

            if (continueCost > 0) {
                cap.staminaRegenDelay = StaminaConfig.COMMON.recoveryDelay.get();
            }
        }
    }

    private static void refillParCoolStamina(Player player) {
        try {
            if (staminaInstanceField == null) {
                return;
            }

            Stamina parCoolWrapper = Stamina.get(player);
            if (parCoolWrapper == null) {
                return;
            }

            Object internalStamina = staminaInstanceField.get(parCoolWrapper);
            if (internalStamina == null) {
                return;
            }

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

    private static boolean hasInfiniteStamina(net.minecraft.world.entity.LivingEntity player) {
        List<? extends String> effects = StaminaLists.LISTS.infiniteStaminaEffects.get();
        for (String id : effects) {
            net.minecraft.resources.ResourceLocation loc = net.minecraft.resources.ResourceLocation.tryParse(id);
            if (loc != null && net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.containsKey(loc)) {
                net.minecraft.world.effect.MobEffect effect = net.minecraftforge.registries.ForgeRegistries.MOB_EFFECTS.getValue(loc);
                if (effect != null && player.hasEffect(effect)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double getAttributeValue(Player player, net.minecraft.world.entity.ai.attributes.Attribute attr, double fallback) {
        net.minecraft.world.entity.ai.attributes.AttributeInstance instance = player.getAttribute(attr);
        return (instance != null) ? instance.getValue() : fallback;
    }
}