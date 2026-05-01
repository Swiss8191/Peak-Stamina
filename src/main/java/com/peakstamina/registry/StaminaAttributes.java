package com.peakstamina.registry;

import com.peakstamina.PeakStaminaMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.Supplier;

public class StaminaAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = 
            DeferredRegister.create(Registries.ATTRIBUTE, PeakStaminaMod.MODID);

    public static final DeferredHolder<Attribute, Attribute> MAX_STAMINA = ATTRIBUTES.register("max_stamina",
            () -> new RangedAttribute("attribute.peakstamina.max_stamina", 100.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SLOW_CLIMB_SPEED = ATTRIBUTES.register("slow_climb_speed",
            () -> new RangedAttribute("attribute.peakstamina.slow_climb_speed", 1.0D, 0.0D, 1.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STAMINA_REGEN = ATTRIBUTES.register("stamina_regen",
            () -> new RangedAttribute("attribute.peakstamina.stamina_regen", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> CURRENT_STAMINA = ATTRIBUTES.register("current_stamina",
            () -> new RangedAttribute("attribute.peakstamina.current_stamina", 0.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> EXHAUSTION_DURATION_MULTIPLIER = ATTRIBUTES.register("exhaustion_duration_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.exhaustion_duration_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> REGEN_DELAY_MULTIPLIER = ATTRIBUTES.register("regen_delay_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.regen_delay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STAMINA_ACTION_RECOVERY = ATTRIBUTES.register("stamina_action_recovery",
            () -> new RangedAttribute("attribute.peakstamina.stamina_action_recovery", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> PENALTY_GAIN_MULTIPLIER = ATTRIBUTES.register("penalty_gain_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_gain_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> PENALTY_DECAY_MULTIPLIER = ATTRIBUTES.register("penalty_decay_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_decay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> PENALTY_AMOUNT_MULTIPLIER = ATTRIBUTES.register("penalty_amount_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_amount_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SPRINT_SPEED = ATTRIBUTES.register("sprint_speed",
            () -> new RangedAttribute("attribute.peakstamina.sprint_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> CURRENT_WEIGHT = ATTRIBUTES.register("current_weight",
            () -> new RangedAttribute("attribute.peakstamina.current_weight", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> WEIGHT_LIMIT = ATTRIBUTES.register("weight_limit",
            () -> new RangedAttribute("attribute.peakstamina.weight_limit", 0.0D, -10000.0D, 10000.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> WEIGHT_CALC_MULTIPLIER = ATTRIBUTES.register("weight_calculation_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.weight_calculation_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STAMINA_USAGE = ATTRIBUTES.register("stamina_usage",
            () -> new RangedAttribute("attribute.peakstamina.stamina_usage", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> JUMP_COST_MULTIPLIER = ATTRIBUTES.register("jump_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.jump_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SPRINT_COST_MULTIPLIER = ATTRIBUTES.register("sprint_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.sprint_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("attack_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MISSED_ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("missed_attack_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.missed_attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHIELD_BLOCK_COST_MULTIPLIER = ATTRIBUTES.register("block_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.shield_block_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ITEM_COST_MULTIPLIER = ATTRIBUTES.register("use_item_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.item_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BLOCK_BREAK_COST_MULTIPLIER = ATTRIBUTES.register("block_break_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.block_break_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SWIM_COST_MULTIPLIER = ATTRIBUTES.register("swim_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.swim_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> CLIMB_COST_MULTIPLIER = ATTRIBUTES.register("climb_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.climb_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BLOCK_PLACE_COST_MULTIPLIER = ATTRIBUTES.register("block_place_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.block_place_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ELYTRA_COST_MULTIPLIER = ATTRIBUTES.register("elytra_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.elytra_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BONUS_STAMINA_CAPACITY = ATTRIBUTES.register("bonus_stamina_capacity",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_capacity", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BONUS_STAMINA_DECAY_RATE = ATTRIBUTES.register("bonus_stamina_decay_rate",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_decay_rate", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> BONUS_STAMINA_DECAY_DELAY = ATTRIBUTES.register("bonus_stamina_decay_delay",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_decay_delay", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> EXCESS_CONVERSION_MULTIPLIER = ATTRIBUTES.register("excess_conversion_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.excess_conversion_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));
}