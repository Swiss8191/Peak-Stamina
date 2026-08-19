package com.peakstamina.compat.combatroll;

import com.mojang.logging.LogUtils;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

public class CombatRollCompat {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isCombatRollLoaded = false;
    private static final UUID EXHAUSTED_ROLL_UUID = UUID.fromString("c00ba740-1111-2222-3333-ba5e57a314a0");

    public static void init() {
        if (ModList.get().isLoaded("combatroll")) {
            isCombatRollLoaded = true;
            MinecraftForge.EVENT_BUS.register(CombatRollCompat.class);

            try {
                LOGGER.info("Peak Stamina: Attempting to hook into CombatRoll events...");
                Class<?> eventsClass = Class.forName("net.combatroll.api.event.ServerSideRollEvents");
                Field eventField = eventsClass.getField("PLAYER_START_ROLLING");
                Object eventInstance = eventField.get(null);
                Class<?> listenerInterface = Class.forName("net.combatroll.api.event.ServerSideRollEvents$PlayerStartRolling");

                Object proxyListener = Proxy.newProxyInstance(
                        CombatRollCompat.class.getClassLoader(),
                        new Class<?>[]{listenerInterface},
                        (proxy, method, args) -> {
                            if (method.getName().equals("onPlayerStartedRolling") && args.length >= 1) {
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
                        LOGGER.info("Peak Stamina: Successfully hooked CombatRoll.");
                        break;
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Peak Stamina: Failed to initialize CombatRoll compat reflection.", e);
                isCombatRollLoaded = false;
            }
        }
    }

    public static boolean isLoaded() {
        return isCombatRollLoaded;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!isCombatRollLoaded || event.phase != TickEvent.Phase.END || event.side.isClient()) return;

        double cost = StaminaLists.LISTS.combatRollCost.get();
        if (cost <= 0) return;

        Attribute countAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("combatroll", "count"));
        if (countAttr == null) return;

        AttributeInstance instance = event.player.getAttribute(countAttr);
        if (instance == null) return;

        double usageMult = 1.0;
        AttributeInstance usageAttr = event.player.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE.get());
        if (usageAttr != null) usageMult = usageAttr.getValue();

        double combatRollMult = 1.0;
        AttributeInstance combatRollAttr = event.player.getAttribute(StaminaAttributes.COMBATROLL_COST_MULTIPLIER.get());
        if (combatRollAttr != null) combatRollMult = combatRollAttr.getValue();

        double finalCost = cost * usageMult * combatRollMult;

        event.player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            boolean hasStamina = cap.stamina >= finalCost; 
            boolean hasModifier = instance.getModifier(EXHAUSTED_ROLL_UUID) != null;

            if (!hasStamina && !hasModifier) {
                instance.addTransientModifier(new AttributeModifier(
                        EXHAUSTED_ROLL_UUID, "PeakStamina Roll Block", -100.0, AttributeModifier.Operation.ADDITION));
            } 
            else if (hasStamina && hasModifier) {
                instance.removeModifier(EXHAUSTED_ROLL_UUID);
            }
        });
    }

    private static void handleRollDeduction(ServerPlayer serverPlayer) {
        if (!isCombatRollLoaded) return;

        double cost = StaminaLists.LISTS.combatRollCost.get();
        if (cost <= 0) return;

        serverPlayer.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            double finalCost = cost;
            
            double usageMult = 1.0;
            AttributeInstance usageAttr = serverPlayer.getAttribute(StaminaAttributes.GLOBAL_STAMINA_USAGE.get());
            if (usageAttr != null) usageMult = usageAttr.getValue();
            
            double combatRollMult = 1.0;
            AttributeInstance combatRollAttr = serverPlayer.getAttribute(StaminaAttributes.COMBATROLL_COST_MULTIPLIER.get());
            if (combatRollAttr != null) combatRollMult = combatRollAttr.getValue();

            finalCost *= (usageMult * combatRollMult);

            ServerStaminaHandler.consumeStamina(cap, (float) finalCost);

            if (cap.stamina < 0) cap.stamina = 0;
            if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;

            int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
            double delayMult = 1.0;
            AttributeInstance delayAttr = serverPlayer.getAttribute(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get());
            if (delayAttr != null) delayMult = delayAttr.getValue();
            cap.staminaRegenDelay = (int) (baseDelay * delayMult);

            StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                    new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues, cap.activeBuffs));
        });
    }
}