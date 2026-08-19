package com.peakstamina.compat.combatroll;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.peakStaminaMod;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class CombatRollCompat {

    private static final ResourceLocation EXHAUSTED_ROLL_ID = ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "exhausted_roll");

    public static void register() {
        NeoForge.EVENT_BUS.register(CombatRollCompat.class);

        try {
            Class<?> eventsClass;
            Class<?> listenerInterface;
            try {
                eventsClass = Class.forName("net.combat_roll.api.event.ServerSideRollEvents");
                listenerInterface = Class.forName("net.combat_roll.api.event.ServerSideRollEvents$PlayerStartRolling");
            } catch (ClassNotFoundException e) {
                eventsClass = Class.forName("net.combatroll.api.event.ServerSideRollEvents");
                listenerInterface = Class.forName("net.combatroll.api.event.ServerSideRollEvents$PlayerStartRolling");
            }

            Field eventField = eventsClass.getField("PLAYER_START_ROLLING");
            Object eventInstance = eventField.get(null);

            Object proxyListener = Proxy.newProxyInstance(
                    CombatRollCompat.class.getClassLoader(),
                    new Class<?>[]{listenerInterface},
                    (proxy, method, args) -> {
                        if (method.getName().equals("onPlayerStartedRolling") && args != null && args.length >= 1) {
                            if (args[0] instanceof ServerPlayer serverPlayer) {
                                handleRollDeduction(serverPlayer);
                            }
                        }
                        return null;
                    }
            );

            for (Method m : eventInstance.getClass().getMethods()) {
                if (m.getName().equals("register") && m.getParameterCount() == 1) {
                    m.invoke(eventInstance, proxyListener);
                    System.out.println("[PeakStamina] Successfully hooked into Combat Roll via Dynamic Proxy!");
                    break;
                }
            }
        } catch (Exception ignored) {
            System.err.println("[PeakStamina] Combat Roll API not found or failed to hook.");
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        double cost = StaminaLists.LISTS.combatRollCost.get();
        if (cost <= 0) return;

        var countAttrOpt = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("combat_roll:count"));
        if (countAttrOpt.isEmpty()) {
            countAttrOpt = BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse("combatroll:count"));
        }
        if (countAttrOpt.isEmpty()) return;

        AttributeInstance instance = event.getEntity().getAttribute(countAttrOpt.get());
        if (instance == null) return;

        double usageMult = 1.0;
        AttributeInstance usageAttr = event.getEntity().getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE);
        if (usageAttr != null) usageMult = usageAttr.getValue();

        double combatRollMult = 1.0;
        AttributeInstance combatRollAttr = event.getEntity().getAttribute(StaminaAttributes.COMBATROLL_COST_MULTIPLIER);
        if (combatRollAttr != null) combatRollMult = combatRollAttr.getValue();

        double finalCost = cost * usageMult * combatRollMult;

        var cap = event.getEntity().getData(StaminaCapability.STAMINA);
        boolean hasStamina = (cap.stamina + cap.bonusStamina) >= finalCost;
        boolean hasModifier = instance.hasModifier(EXHAUSTED_ROLL_ID);

        if (!hasStamina && !hasModifier) {
            instance.addTransientModifier(new AttributeModifier(
                    EXHAUSTED_ROLL_ID, -100.0, AttributeModifier.Operation.ADD_VALUE));
        } 
        else if (hasStamina && hasModifier) {
            instance.removeModifier(EXHAUSTED_ROLL_ID);
        }
    }

    private static void handleRollDeduction(ServerPlayer serverPlayer) {
        double cost = StaminaLists.LISTS.combatRollCost.get();
        if (cost <= 0) return;

        var cap = serverPlayer.getData(StaminaCapability.STAMINA);

        double finalCost = cost;
        double usageMult = 1.0;
        
        AttributeInstance usageAttr = serverPlayer.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE);
        if (usageAttr != null) usageMult = usageAttr.getValue();

        double combatRollMult = 1.0;
        AttributeInstance combatRollAttr = serverPlayer.getAttribute(StaminaAttributes.COMBATROLL_COST_MULTIPLIER);
        if (combatRollAttr != null) combatRollMult = combatRollAttr.getValue();
        
        finalCost *= (usageMult * combatRollMult);

        ServerStaminaHandler.consumeStamina(cap, (float) finalCost);

        if (cap.stamina < 0) cap.stamina = 0;
        if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

        int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
        double delayMult = 1.0;
        AttributeInstance delayAttr = serverPlayer.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER);
        if (delayAttr != null) delayMult = delayAttr.getValue();
        cap.staminaRegenDelay = (int) (baseDelay * delayMult);

        PacketDistributor.sendToPlayer(serverPlayer, new PacketSyncStamina(
            cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, 
            cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, 
            cap.bonusStamina, cap.penaltyValues, cap.activeBuffs
        ));
    }
}