package com.peakstamina.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;
import java.util.List;

public class ExperimentalConfig {

    public static final Experimental EXPERIMENTAL;
    public static final ForgeConfigSpec EXPERIMENTAL_SPEC;

    static {
        final Pair<Experimental, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Experimental::new);
        EXPERIMENTAL_SPEC = specPair.getRight();
        EXPERIMENTAL = specPair.getLeft();
    }

    public static class Experimental {

        private static final List<String> DEFAULT_PROFILES = Arrays.asList(
                "MeleeTired; minecraft:generic.movement_speed=-0.40, minecraft:generic.attack_damage=-0.4",
                "HeavyMelee; minecraft:generic.movement_speed=-0.60, minecraft:generic.attack_damage=-0.50",
                "RangedTired; minecraft:generic.movement_speed=-0.25, minecraft:generic.attack_damage=-0.40",
                "AgileTired; minecraft:generic.movement_speed=-0.40, minecraft:generic.attack_damage=-0.3"
        );

        private static final List<String> DEFAULT_MOB_STAMINA = Arrays.asList(
                "minecraft:zombie; 4; 120; MeleeTired",
                "minecraft:husk; 4; 120; MeleeTired",
                "minecraft:drowned; 5; 80; MeleeTired",
                "minecraft:piglin; 5; 120; MeleeTired",
                "minecraft:skeleton; 4; 80; RangedTired",
                "minecraft:stray; 5; 80; RangedTired",
                "minecraft:pillager; 5; 80; RangedTired",
                "minecraft:piglin; 4; 80; RangedTired",
                "minecraft:spider; 5; 60; AgileTired",
                "minecraft:cave_spider; 4; 40; AgileTired",
                "minecraft:enderman; 6; 100; AgileTired",
                "minecraft:vindicator; 4; 80; HeavyMelee",
                "minecraft:piglin_brute; 5; 100; HeavyMelee",
                "minecraft:ravager; 6; 120; HeavyMelee"
        );

        public final ForgeConfigSpec.ConfigValue<Boolean> enableMobStamina;
        public final ForgeConfigSpec.ConfigValue<Boolean> enableExhaustionParticles;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> exhaustionProfiles;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> customMobStamina;

        public Experimental(ForgeConfigSpec.Builder builder) {
            builder.comment("Experimental features! These may impact performance or balance, use with caution.").push("Entity Stamina");

            enableMobStamina = builder.comment("Enable stamina system for Mobs.")
                    .define("enableMobStamina", true);

            enableExhaustionParticles = builder.comment("Show sweat particles for exhausted mobs.")
                    .define("enableExhaustionParticles", true);

            exhaustionProfiles = builder.comment(
                    "",
                    "Exhaustion Profiles",
                    "Define reusable attribute debuff templates here. Modded attributes are supported.",
                    "Format: \"ProfileName; AttributeID=Multiplier, AttributeID=Multiplier...\"",
                    ""
            ).defineList("exhaustionProfiles", DEFAULT_PROFILES, obj -> obj instanceof String);

            customMobStamina = builder.comment(
                    "",
                    "Define which mobs use the stamina system and link them to a Profile defined above.",
                    "Note: This list acts as a whitelist, mobs not defined on it will not use the stamina system. This is also not guaranteed to work on bosses.",
                    "Format: \"EntityID; MaxAttacks; ExhaustionTicks; ProfileName\"",
                    "  - EntityID: The registry name of the mob (e.g. minecraft:zombie)",
                    "  - MaxAttacks: How many hits/shots they land before exhausting, for ranged mobs its how many shots they shoot (doesnt matter if they hit or miss)",
                    "  - ExhaustionTicks: How long they stay exhausted (20 = 1 sec)",
                    "  - ProfileName: The name of the template from 'exhaustionProfiles'",
                    "Note: Ranged mobs that use bows and crossbows (should work with modded?) with attack attribute reduction will also lower the velocity of the arrow.",
                    ""
            ).defineList("customMobStamina", DEFAULT_MOB_STAMINA, obj -> obj instanceof String);

            builder.pop();
        }
    }
}
