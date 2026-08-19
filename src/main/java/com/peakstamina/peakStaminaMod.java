package com.peakstamina;

import com.peakstamina.commands.StaminaCommand;
import com.peakstamina.compat.packedup.PackedUpCompat;
import com.peakstamina.compat.parcool.ParCoolClientCompat;
import com.peakstamina.compat.parcool.ParCoolCompat;
import com.peakstamina.compat.combatroll.CombatRollCompat;
import com.peakstamina.config.ExperimentalConfig;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;

import com.peakstamina.capabilities.StaminaCapability;

@Mod(peakStaminaMod.MODID)
public class peakStaminaMod {

    public static final String MODID = "peakstamina";

    public peakStaminaMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, StaminaConfig.COMMON_SPEC, "peakstamina/common.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, StaminaConfig.CLIENT_SPEC, "peakstamina/client.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, StaminaLists.LISTS_SPEC, "peakstamina/lists.toml");
        modContainer.registerConfig(ModConfig.Type.COMMON, ExperimentalConfig.EXPERIMENTAL_SPEC, "peakstamina/experimental.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.peakstamina.client.ClientConfigSetup.register(modContainer);

            modEventBus.addListener((net.neoforged.fml.event.config.ModConfigEvent.Loading e) -> {
                if (e.getConfig().getType() == net.neoforged.fml.config.ModConfig.Type.CLIENT) {
                    com.peakstamina.client.gui.CustomIconRegistry.reload();
                }
            });
        }

        StaminaAttributes.ATTRIBUTES.register(modEventBus);
        StaminaCapability.ATTACHMENT_TYPES.register(modEventBus); 
        
        modEventBus.addListener(this::attachAttributes);
        modEventBus.addListener(com.peakstamina.network.StaminaNetwork::register);

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
            CombatRollCompat.register();
        }

        if (ModList.get().isLoaded("shieldexp")) {
            com.peakstamina.compat.shieldexp.ShieldExpansionCompat.init();
        }

        if (ModList.get().isLoaded("walljump")) {
            com.peakstamina.compat.walljump.WallJumpCompat.init();
            if (FMLEnvironment.dist == Dist.CLIENT) {
                com.peakstamina.compat.walljump.WallJumpClientCompat.init();
            }
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
        if (!event.has(EntityType.PLAYER, StaminaAttributes.GLOBAL_STAMINA_USAGE)) {
            event.add(EntityType.PLAYER, StaminaAttributes.GLOBAL_STAMINA_USAGE);
        }
        if (!event.has(EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER)) {
            event.add(EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER);
        }

        event.add(EntityType.PLAYER, StaminaAttributes.CURRENT_STAMINA);
        event.add(EntityType.PLAYER, StaminaAttributes.PENALTY_GAIN_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.PENALTY_DECAY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MAX_GLOBAL_PENALTY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.EXHAUSTED_SPRINT_SPEED);
        event.add(EntityType.PLAYER, StaminaAttributes.WEIGHT_LIMIT);
        event.add(EntityType.PLAYER, StaminaAttributes.CURRENT_WEIGHT);
        event.add(EntityType.PLAYER, StaminaAttributes.JUMP_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SPRINT_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.ATTACK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SHIELD_BLOCK_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.ITEM_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.USE_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.TICK_COST_MULTIPLIER);
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
        
        event.add(EntityType.PLAYER, StaminaAttributes.COMBATROLL_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.PARCOOL_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SHIELDEXP_BONUS_GAIN_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.SHIELDEXP_PARRY_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.WALLJUMPTXF_COST_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MAX_POISON_PENALTY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MAX_FATIGUE_PENALTY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MAX_HUNGER_PENALTY_MULTIPLIER);
        event.add(EntityType.PLAYER, StaminaAttributes.MAX_WEIGHT_PENALTY_MULTIPLIER);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StaminaCommand.register(event.getDispatcher());
    }

}