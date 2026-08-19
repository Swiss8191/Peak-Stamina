package com.peakstamina.handlers.experimental;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.ExperimentalConfig;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = peakStaminaMod.MODID)
public class CustomActionHandler {

    private static class ActionRule {
        String type;
        String arg;
        double cost;

        ActionRule(String type, String arg, double cost) {
            this.type = type;
            this.arg = arg;
            this.cost = cost;
        }
    }

    private static final List<ActionRule> RULES_CACHE = new ArrayList<>();

    public static void refreshCache() {
        RULES_CACHE.clear();
        List<? extends String> configs = ExperimentalConfig.EXPERIMENTAL.customActionHooks.get();
        for (String entry : configs) {
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 3) {
                    RULES_CACHE.add(new ActionRule(
                            parts[0].trim().toUpperCase(),
                            parts[1].trim(),
                            Double.parseDouble(parts[2].trim())
                    ));
                }
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (!event.isMounting() || !(event.getEntityMounting() instanceof ServerPlayer player)) return;

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntityBeingMounted().getType()).toString();
        if (!processAction(player, "MOUNT", entityId)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(event.getTarget().getType()).toString();
        if (!processAction(player, "INTERACT_ENTITY", entityId)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String damageType = event.getSource().type().msgId(); 
        processAction(player, "HURT", damageType);
    }

    @SubscribeEvent
    public static void onFish(ItemFishedEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (!processAction(player, "FISH", "ANY")) {
            event.setCanceled(true);
        }
    }

    private static boolean processAction(ServerPlayer player, String type, String currentArg) {
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return true;
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return true;

        for (ActionRule rule : RULES_CACHE) {
            if (rule.type.equals(type) && (rule.arg.equalsIgnoreCase("ANY") || rule.arg.equalsIgnoreCase(currentArg))) {

                double baseCost = rule.cost;
                if (baseCost == 0) return true;

                var cap = player.getData(StaminaCapability.STAMINA);
                
                double finalCost;
                if (baseCost > 0) {
                    double usageMult = 1.0;
                    AttributeInstance usageAttr = player.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE);
                    if (usageAttr != null) usageMult = usageAttr.getValue();
                    finalCost = baseCost * usageMult;
                } else {
                    double actionRecoveryMult = 1.0;
                    AttributeInstance recAttr = player.getAttribute(StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER);
                    if (recAttr != null) actionRecoveryMult = recAttr.getValue();
                    finalCost = baseCost * actionRecoveryMult;
                }

                if (baseCost > 0 && (cap.stamina + cap.bonusStamina) < finalCost) {
                    return false;
                }

                if (baseCost > 0) {
                    ServerStaminaHandler.consumeStamina(cap, (float) finalCost);

                    int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
                    double delayMult = 1.0;
                    AttributeInstance delayAttr = player.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER);
                    if (delayAttr != null) delayMult = delayAttr.getValue();

                    cap.staminaRegenDelay = (int) (baseDelay * delayMult);
                } else {
                    cap.stamina -= (float) finalCost;
                }

                if (cap.stamina < 0) cap.stamina = 0;
                if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

                PacketDistributor.sendToPlayer(player, new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues, cap.activeBuffs));
                return true;
            }
        }
        return true;
    }
}