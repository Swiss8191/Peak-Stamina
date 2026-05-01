package com.peakstamina;

import com.peakstamina.commands.StaminaCommand;
import com.peakstamina.compat.PackedUpCompat;
import com.peakstamina.compat.ParCoolClientCompat;
import com.peakstamina.compat.ParCoolCompat;
import com.peakstamina.config.ExperimentalConfig;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.registry.StaminaAttributes;
import com.peakstamina.registry.StaminaAttachments;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

@Mod(PeakStaminaMod.MODID)
public class PeakStaminaMod {

    public static final String MODID = "peakstamina";

    public PeakStaminaMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, StaminaConfig.COMMON_SPEC, "peakstamina/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, StaminaConfig.CLIENT_SPEC, "peakstamina/client.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, StaminaLists.LISTS_SPEC, "peakstamina/lists.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, ExperimentalConfig.EXPERIMENTAL_SPEC, "peakstamina/experimental.toml");

        StaminaAttributes.ATTRIBUTES.register(modEventBus);
        StaminaAttachments.ATTACHMENT_TYPES.register(modEventBus); 

        modEventBus.addListener(this::attachAttributes);

        NeoForge.EVENT_BUS.register(this);

        if (ModList.get().isLoaded("parcool")) {
            ParCoolCompat.init();
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ParCoolClientCompat.init();
            }
        }

        if (ModList.get().isLoaded("packedup")) {
            PackedUpCompat.init();
        }

        if (ModList.get().isLoaded("combatroll") || ModList.get().isLoaded("combat_roll")) {
            com.peakstamina.compat.CombatRollCompat.register();
        }
    } 

    private void attachAttributes(EntityAttributeModificationEvent event) {
        if (!event.has(EntityType.PLAYER, StaminaAttributes.MAX_STAMINA)) {
            event.add(EntityType.PLAYER, StaminaAttributes.MAX_STAMINA);
        }
        if (!event.has(EntityType.PLAYER, StaminaAttributes.SLOW_CLIMB_SPEED)) {
            event.add(EntityType.PLAYER, StaminaAttributes.SLOW_CLIMB_SPEED);
        }
        if (!event.has(EntityType.PLAYER, StaminaAttributes.STAMINA_REGEN)) {
            event.add(EntityType.PLAYER, StaminaAttributes.STAMINA_REGEN);
        }
        if (!event.has(EntityType.PLAYER, StaminaAttributes.STAMINA_USAGE)) {
            event.add(EntityType.PLAYER, StaminaAttributes.STAMINA_USAGE);
        }
        if (!event.has(EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY)) {
            event.add(EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY);
        }

        event.add(EntityType.PLAYER, StaminaAttributes.CURRENT_STAMINA);
        event.add(EntityType.PLAYER, StaminaAttributes.PENALTY_GAIN_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.PENALTY_DECAY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SPRINT_SPEED);
        event.add(EntityType.PLAYER, StaminaAttributes.WEIGHT_LIMIT);
        event.add(EntityType.PLAYER, StaminaAttributes.CURRENT_WEIGHT);
        event.add(EntityType.PLAYER, StaminaAttributes.JUMP_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SPRINT_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.ATTACK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SHIELD_BLOCK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.ITEM_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.BLOCK_BREAK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.BLOCK_PLACE_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SWIM_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.CLIMB_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.ELYTRA_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.REGEN_DELAY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.WEIGHT_CALC_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_CAPACITY);
        event.add(EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_DECAY_RATE);
        event.add(EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_DECAY_DELAY);
        event.add(EntityType.PLAYER, StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StaminaCommand.register(event.getDispatcher());
    }
}