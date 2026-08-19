package com.peakstamina;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.commands.StaminaCommand;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import com.peakstamina.compat.combatroll.CombatRollCompat;
import com.peakstamina.compat.packedup.PackedUpCompat;
import com.peakstamina.compat.parcool.ParCoolCompat;
import com.peakstamina.compat.parcool.ParCoolClientCompat;
import com.peakstamina.compat.shieldexp.ShieldExpansionCompat;
import com.peakstamina.compat.walljump.WallJumpCompat;
import com.peakstamina.compat.walljump.WallJumpClientCompat;
import com.peakstamina.compat.elenaidodge2.ElenaiDodgeCompat;
import com.peakstamina.compat.elenaidodge2.ElenaiDodgeClientCompat;
import com.peakstamina.compat.feathers.FeathersCompat;
import com.peakstamina.compat.feathers.FeathersClientCompat;

import com.peakstamina.config.ExperimentalConfig;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod(peakStaminaMod.MODID)
public class peakStaminaMod {

    public static final String MODID = "peakstamina";
    public peakStaminaMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StaminaConfig.COMMON_SPEC, "peakstamina/common.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, StaminaConfig.CLIENT_SPEC, "peakstamina/client.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, StaminaLists.LISTS_SPEC, "peakstamina/lists.toml");
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExperimentalConfig.EXPERIMENTAL_SPEC, "peakstamina/experimental.toml");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            com.peakstamina.client.ClientConfigSetup.register();

            modEventBus.addListener((net.minecraftforge.fml.event.config.ModConfigEvent.Loading e) -> {
                if (e.getConfig().getType() == net.minecraftforge.fml.config.ModConfig.Type.CLIENT) {
                    com.peakstamina.client.gui.CustomIconRegistry.reload();
                }
            });
        }

        StaminaAttributes.ATTRIBUTES.register(modEventBus);
        com.peakstamina.registry.StaminaEnchantments.ENCHANTMENTS.register(modEventBus);
        modEventBus.addListener(this::attachAttributes);
        modEventBus.addListener(this::registerCaps);
        
        StaminaNetwork.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge"); 
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(net.minecraft.world.entity.Entity.class, this::attachEntityCaps);

        if (net.minecraftforge.fml.ModList.get().isLoaded("parcool")) {
            ParCoolCompat.init();
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("walljump")) {
            WallJumpCompat.init(); 
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("packedup")) {
            PackedUpCompat.init();
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("combatroll")) {
            CombatRollCompat.init();
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("shieldexp")) {
            ShieldExpansionCompat.init();
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("elenaidodge2")) {
            ElenaiDodgeCompat.init();
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("feathers")) {
            FeathersCompat.init();
        }

        if (FMLEnvironment.dist == Dist.CLIENT) {
            if (net.minecraftforge.fml.ModList.get().isLoaded("parcool")) {
                ParCoolClientCompat.init();
            }

            if (net.minecraftforge.fml.ModList.get().isLoaded("walljump")) {
                WallJumpClientCompat.init(); 
            }

            if (net.minecraftforge.fml.ModList.get().isLoaded("elenaidodge2")) {
                ElenaiDodgeClientCompat.init();
            }

            if (net.minecraftforge.fml.ModList.get().isLoaded("feathers")) {
                FeathersClientCompat.init();
            }
        }
    }


    private void attachAttributes(EntityAttributeModificationEvent event) {
        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_STAMINA.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_STAMINA.get());
        }

        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SLOW_CLIMB_SPEED.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SLOW_CLIMB_SPEED.get());
        }

        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_REGEN.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_REGEN.get());
        }

        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.GLOBAL_STAMINA_USAGE.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.GLOBAL_STAMINA_USAGE.get());
        }

        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER.get());
        }

        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.CURRENT_STAMINA.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PENALTY_GAIN_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PENALTY_DECAY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_GLOBAL_PENALTY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.EXHAUSTED_SPRINT_SPEED.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.WEIGHT_LIMIT.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.CURRENT_WEIGHT.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.JUMP_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SPRINT_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.ATTACK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SHIELD_BLOCK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.ITEM_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.USE_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.TICK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.BLOCK_BREAK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.BLOCK_PLACE_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SWIM_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.CLIMB_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.ELYTRA_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.REGEN_DELAY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.WEIGHT_CALC_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_CAPACITY.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_DECAY_RATE.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.BONUS_STAMINA_DECAY_DELAY.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER.get());

        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_POISON_PENALTY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_HUNGER_PENALTY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_FATIGUE_PENALTY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MAX_WEIGHT_PENALTY_MULTIPLIER.get());

        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.COMBATROLL_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PARCOOL_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SHIELDEXP_BONUS_GAIN_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SHIELDEXP_PARRY_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.WALLJUMPTXF_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.ELENAIDODGE2_COST_MULTIPLIER.get());
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(StaminaCapability.class);
    }

    public void attachEntityCaps(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        if (event.getObject() instanceof net.minecraft.world.entity.player.Player) {
            if (!event.getObject().getCapability(StaminaCapability.INSTANCE).isPresent()) {
                event.addCapability(new ResourceLocation(MODID, "stamina"), new StaminaCapability.Provider());
            }
        }
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        StaminaCommand.register(event.getDispatcher());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ConfigReloadHandler {

        @SubscribeEvent
        public static void onConfigReload(net.minecraftforge.fml.event.config.ModConfigEvent.Reloading event) {

            if (event.getConfig().getType() == net.minecraftforge.fml.config.ModConfig.Type.COMMON) {
                com.peakstamina.handlers.core.ServerStaminaHandler.refreshAllCaches();

                if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
                    com.peakstamina.client.events.ClientStaminaEvents.invalidateCache();
                }
            }

            if (event.getConfig().getType() == net.minecraftforge.fml.config.ModConfig.Type.CLIENT) {
                com.peakstamina.client.events.ClientStaminaEvents.invalidateCache();
                com.peakstamina.client.gui.CustomIconRegistry.reload();
            }
        }
    }
}

