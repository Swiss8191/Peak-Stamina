package com.peakstamina.registry;

import com.peakstamina.peakStaminaMod;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StaminaAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES
            = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, peakStaminaMod.MODID);

    public static final RegistryObject<Attribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina",
            () -> new RangedAttribute("attribute.name.peakstamina.max_stamina", 100.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SLOW_CLIMB_SPEED = ATTRIBUTES.register("slow_climb_speed",
            () -> new RangedAttribute("attribute.name.peakstamina.slow_climb_speed", 1.0D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> STAMINA_REGEN = ATTRIBUTES.register("stamina_regen",
            () -> new RangedAttribute("attribute.name.peakstamina.stamina_regen", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CURRENT_STAMINA = ATTRIBUTES.register("current_stamina",
            () -> new RangedAttribute("attribute.name.peakstamina.current_stamina", 0.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXHAUSTION_DURATION_MULTIPLIER = ATTRIBUTES.register("exhaustion_duration_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.exhaustion_duration_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> REGEN_DELAY_MULTIPLIER = ATTRIBUTES.register("regen_delay_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.regen_delay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> STAMINA_ACTION_RECOVERY_MULTIPLIER = ATTRIBUTES.register("stamina_action_recovery_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.stamina_action_recovery_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PENALTY_GAIN_MULTIPLIER = ATTRIBUTES.register("penalty_gain_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.penalty_gain_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PENALTY_DECAY_MULTIPLIER = ATTRIBUTES.register("penalty_decay_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.penalty_decay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_GLOBAL_PENALTY_MULTIPLIER = ATTRIBUTES.register("max_global_penalty_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.max_global_penalty_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_POISON_PENALTY_MULTIPLIER = ATTRIBUTES.register("max_poison_penalty_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.max_poison_penalty_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_HUNGER_PENALTY_MULTIPLIER = ATTRIBUTES.register("max_hunger_penalty_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.max_hunger_penalty_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_FATIGUE_PENALTY_MULTIPLIER = ATTRIBUTES.register("max_fatigue_penalty_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.max_fatigue_penalty_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MAX_WEIGHT_PENALTY_MULTIPLIER = ATTRIBUTES.register("max_weight_penalty_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.max_weight_penalty_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXHAUSTED_SPRINT_SPEED = ATTRIBUTES.register("exhausted_sprint_speed",
            () -> new RangedAttribute("attribute.name.peakstamina.exhausted_sprint_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CURRENT_WEIGHT = ATTRIBUTES.register("current_weight",
            () -> new RangedAttribute("attribute.name.peakstamina.current_weight", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<Attribute> WEIGHT_LIMIT = ATTRIBUTES.register("weight_limit",
            () -> new RangedAttribute("attribute.name.peakstamina.weight_limit", 0.0D, -10000.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<Attribute> WEIGHT_CALC_MULTIPLIER = ATTRIBUTES.register("weight_calculation_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.weight_calculation_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> GLOBAL_STAMINA_USAGE = ATTRIBUTES.register("global_stamina_usage",
            () -> new RangedAttribute("attribute.name.peakstamina.global_stamina_usage", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> JUMP_COST_MULTIPLIER = ATTRIBUTES.register("jump_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.jump_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SPRINT_COST_MULTIPLIER = ATTRIBUTES.register("sprint_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.sprint_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("attack_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MISSED_ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("missed_attack_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.missed_attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SHIELD_BLOCK_COST_MULTIPLIER = ATTRIBUTES.register("shield_block_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.shield_block_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ITEM_COST_MULTIPLIER = ATTRIBUTES.register("use_item_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.item_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> USE_COST_MULTIPLIER = ATTRIBUTES.register("use_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.use_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> TICK_COST_MULTIPLIER = ATTRIBUTES.register("tick_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.tick_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_BREAK_COST_MULTIPLIER = ATTRIBUTES.register("block_break_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.block_break_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SWIM_COST_MULTIPLIER = ATTRIBUTES.register("swim_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.swim_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CLIMB_COST_MULTIPLIER = ATTRIBUTES.register("climb_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.climb_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_PLACE_COST_MULTIPLIER = ATTRIBUTES.register("block_place_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.block_place_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ELYTRA_COST_MULTIPLIER = ATTRIBUTES.register("elytra_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.elytra_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_CAPACITY = ATTRIBUTES.register("bonus_stamina_capacity",
            () -> new RangedAttribute("attribute.name.peakstamina.bonus_stamina_capacity", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_DECAY_RATE = ATTRIBUTES.register("bonus_stamina_decay_rate",
            () -> new RangedAttribute("attribute.name.peakstamina.bonus_stamina_decay_rate", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_DECAY_DELAY = ATTRIBUTES.register("bonus_stamina_decay_delay",
            () -> new RangedAttribute("attribute.name.peakstamina.bonus_stamina_decay_delay", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXCESS_CONVERSION_MULTIPLIER = ATTRIBUTES.register("excess_conversion_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.excess_conversion_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> COMBATROLL_COST_MULTIPLIER = ATTRIBUTES.register("combatroll_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.combatroll_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ELENAIDODGE2_COST_MULTIPLIER = ATTRIBUTES.register("elenaidodge2_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.elenaidodge2_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PARCOOL_COST_MULTIPLIER = ATTRIBUTES.register("parcool_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.parcool_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SHIELDEXP_BONUS_GAIN_MULTIPLIER = ATTRIBUTES.register("shieldexp_bonus_gain_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.shieldexp_bonus_gain_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SHIELDEXP_PARRY_COST_MULTIPLIER = ATTRIBUTES.register("shieldexp_parry_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.shieldexp_parry_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> WALLJUMPTXF_COST_MULTIPLIER = ATTRIBUTES.register("walljumptxf_cost_multiplier",
            () -> new RangedAttribute("attribute.name.peakstamina.walljumptxf_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

}
