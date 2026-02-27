package com.peakstamina;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.commands.StaminaCommand;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.resources.ResourceLocation;
import com.peakstamina.compat.ParCoolCompat;
import com.peakstamina.compat.PackedUpCompat;
import com.peakstamina.compat.ParCoolClientCompat;
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

        StaminaAttributes.ATTRIBUTES.register(modEventBus);
        modEventBus.addListener(this::attachAttributes);
        modEventBus.addListener(this::registerCaps);

        StaminaNetwork.register();

        if (FMLEnvironment.dist == Dist.CLIENT) {
            // MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge"); // Generally not needed in modern Forge unless specific mixin issues arise
        }

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addGenericListener(net.minecraft.world.entity.Entity.class, this::attachEntityCaps);

        if (net.minecraftforge.fml.ModList.get().isLoaded("parcool")) {
            ParCoolCompat.init();
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ParCoolClientCompat.init();
            }
        }

        if (net.minecraftforge.fml.ModList.get().isLoaded("packedup")) {
            PackedUpCompat.init();
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
        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_USAGE.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_USAGE.get());
        }
        if (!event.has(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY.get())) {
            event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.STAMINA_ACTION_RECOVERY.get());
        }

        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.CURRENT_STAMINA.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PENALTY_GAIN_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PENALTY_DECAY_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SPRINT_SPEED.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.WEIGHT_LIMIT.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.CURRENT_WEIGHT.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.JUMP_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.SPRINT_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.ATTACK_COST_MULTIPLIER.get());
        event.add(net.minecraft.world.entity.EntityType.PLAYER, StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get());
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
}