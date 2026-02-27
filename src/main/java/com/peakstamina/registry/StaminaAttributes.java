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
            () -> new RangedAttribute("attribute.peakstamina.max_stamina", 100.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SLOW_CLIMB_SPEED = ATTRIBUTES.register("slow_climb_speed",
            () -> new RangedAttribute("attribute.peakstamina.slow_climb_speed", 0.4D, 0.0D, 1.0D).setSyncable(true));

    public static final RegistryObject<Attribute> STAMINA_REGEN = ATTRIBUTES.register("stamina_regen",
            () -> new RangedAttribute("attribute.peakstamina.stamina_regen", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CURRENT_STAMINA = ATTRIBUTES.register("current_stamina",
            () -> new RangedAttribute("attribute.peakstamina.current_stamina", 0.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXHAUSTION_DURATION_MULTIPLIER = ATTRIBUTES.register("exhaustion_duration_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.exhaustion_duration_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> REGEN_DELAY_MULTIPLIER = ATTRIBUTES.register("regen_delay_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.regen_delay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> STAMINA_ACTION_RECOVERY = ATTRIBUTES.register("stamina_action_recovery",
            () -> new RangedAttribute("attribute.peakstamina.stamina_action_recovery", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PENALTY_GAIN_MULTIPLIER = ATTRIBUTES.register("penalty_gain_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_gain_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PENALTY_DECAY_MULTIPLIER = ATTRIBUTES.register("penalty_decay_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_decay_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> PENALTY_AMOUNT_MULTIPLIER = ATTRIBUTES.register("penalty_amount_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.penalty_amount_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SPRINT_SPEED = ATTRIBUTES.register("sprint_speed",
            () -> new RangedAttribute("attribute.peakstamina.sprint_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CURRENT_WEIGHT = ATTRIBUTES.register("current_weight",
            () -> new RangedAttribute("attribute.peakstamina.current_weight", 0.0D, 0.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<Attribute> WEIGHT_LIMIT = ATTRIBUTES.register("weight_limit",
            () -> new RangedAttribute("attribute.peakstamina.weight_limit", 0.0D, -10000.0D, 10000.0D).setSyncable(true));

    public static final RegistryObject<Attribute> WEIGHT_CALC_MULTIPLIER = ATTRIBUTES.register("weight_calculation_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.weight_calculation_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> STAMINA_USAGE = ATTRIBUTES.register("stamina_usage",
            () -> new RangedAttribute("attribute.peakstamina.stamina_usage", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> JUMP_COST_MULTIPLIER = ATTRIBUTES.register("jump_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.jump_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SPRINT_COST_MULTIPLIER = ATTRIBUTES.register("sprint_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.sprint_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("attack_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> MISSED_ATTACK_COST_MULTIPLIER = ATTRIBUTES.register("missed_attack_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.missed_attack_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_BREAK_COST_MULTIPLIER = ATTRIBUTES.register("block_break_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.block_break_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> SWIM_COST_MULTIPLIER = ATTRIBUTES.register("swim_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.swim_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> CLIMB_COST_MULTIPLIER = ATTRIBUTES.register("climb_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.climb_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BLOCK_PLACE_COST_MULTIPLIER = ATTRIBUTES.register("block_place_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.block_place_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> ELYTRA_COST_MULTIPLIER = ATTRIBUTES.register("elytra_cost_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.elytra_cost_multiplier", 1.0D, 0.0D, 1024.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_CAPACITY = ATTRIBUTES.register("bonus_stamina_capacity",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_capacity", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_DECAY_RATE = ATTRIBUTES.register("bonus_stamina_decay_rate",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_decay_rate", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> BONUS_STAMINA_DECAY_DELAY = ATTRIBUTES.register("bonus_stamina_decay_delay",
            () -> new RangedAttribute("attribute.peakstamina.bonus_stamina_decay_delay", 1.0D, 0.0D, 100.0D).setSyncable(true));

    public static final RegistryObject<Attribute> EXCESS_CONVERSION_MULTIPLIER = ATTRIBUTES.register("excess_conversion_multiplier",
            () -> new RangedAttribute("attribute.peakstamina.excess_conversion_multiplier", 1.0D, 0.0D, 100.0D).setSyncable(true));

}
