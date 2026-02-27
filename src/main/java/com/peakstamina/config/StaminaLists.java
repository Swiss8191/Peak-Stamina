package com.peakstamina.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.Arrays;
import java.util.List;

public class StaminaLists {

    public static final Lists LISTS;
    public static final ForgeConfigSpec LISTS_SPEC;

    static {
        final Pair<Lists, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Lists::new);
        LISTS_SPEC = specPair.getRight();
        LISTS = specPair.getLeft();
    }

    public static class Lists {

        private static final List<String> DEFAULT_ITEM_COSTS = Arrays.asList(
                "minecraft:crossbow;TICK;0.5",
                "minecraft:trident;TICK;0.5",
                "minecraft:snowball;USE;1.0",
                "minecraft:egg;USE;1.0",
                "minecraft:ender_pearl;USE;4.0",
                "minecraft:splash_potion;USE;2.0",
                "minecraft:lingering_potion;USE;2.0",
                "minecraft:experience_bottle;USE;1.0",
                "minecraft:firework_rocket;USE;1.5",
                "minecraft:fishing_rod;USE;3.0",
                "minecraft:flint_and_steel;USE_ON_BLOCK;5.0",
                "minecraft:spyglass;TICK;0.1",
                "minecraft:goat_horn;TICK;0.6",
                "minecraft:wooden_axe;USE_ON_BLOCK;5",
                "minecraft:stone_axe;USE_ON_BLOCK;4.5",
                "minecraft:iron_axe;USE_ON_BLOCK;3",
                "minecraft:golden_axe;USE_ON_BLOCK;4",
                "minecraft:diamond_axe;USE_ON_BLOCK;2.5",
                "minecraft:netherite_axe;USE_ON_BLOCK;2.5",
                "minecraft:wooden_shovel;USE_ON_BLOCK;5",
                "minecraft:stone_shovel;USE_ON_BLOCK;4.5",
                "minecraft:iron_shovel;USE_ON_BLOCK;3",
                "minecraft:golden_shovel;USE_ON_BLOCK;4",
                "minecraft:diamond_shovel;USE_ON_BLOCK;2.5",
                "minecraft:netherite_shovel;USE_ON_BLOCK;2.5",
                "minecraft:wooden_hoe;USE_ON_BLOCK;5",
                "minecraft:stone_hoe;USE_ON_BLOCK;4.5",
                "minecraft:iron_hoe;USE_ON_BLOCK;3",
                "minecraft:golden_hoe;USE_ON_BLOCK;4",
                "minecraft:diamond_hoe;USE_ON_BLOCK;2.5",
                "minecraft:netherite_hoe;USE_ON_BLOCK;2.5"
        );

        private static final List<String> DEFAULT_ITEM_COST_TAGS = Arrays.asList(
                "forge:tools/bows;TICK;0.5",
                "forge:tools/shields;TICK;0.2;BLOCK;2.0;0.8"
        );

        private static final List<String> DEFAULT_CONSUMABLES = Arrays.asList(
                "minecraft:apple;INSTANT;10.0;BONUS;5.0",
                "minecraft:melon_slice;INSTANT;4.0;BONUS;2.0",
                "minecraft:sweet_berries;INSTANT;3.0;PENALTY;5.0;BONUS;1.5",
                "minecraft:glow_berries;INSTANT;3.0;PENALTY;5.0;BONUS;1.5",
                "minecraft:chorus_fruit;INSTANT;5.0;BONUS;2.5",
                "minecraft:carrot;INSTANT;6.0;BONUS;3.0",
                "minecraft:potato;INSTANT;5.0;BONUS;2.5",
                "minecraft:baked_potato;INSTANT;12.0;PENALTY;5.0;BONUS;6.0",
                "minecraft:beetroot;INSTANT;4.0;BONUS;2.0",
                "minecraft:dried_kelp;INSTANT;2.0;BONUS;1.0",
                "minecraft:bread;INSTANT;12.0;BONUS;6.0",
                "minecraft:cookie;INSTANT;4.0;PENALTY;2.0;BONUS;2.0",
                "minecraft:pumpkin_pie;INSTANT;16.0;BONUS;8.0",
                "minecraft:beef;INSTANT;6.0;BONUS;3.0",
                "minecraft:cooked_beef;INSTANT;18.0;PENALTY;5.0;BONUS;9.0",
                "minecraft:porkchop;INSTANT;6.0;PENALTY;5.0;BONUS;3.0",
                "minecraft:cooked_porkchop;INSTANT;18.0;PENALTY;5.0;BONUS;9.0",
                "minecraft:chicken;INSTANT;4.0;BONUS;2.0",
                "minecraft:cooked_chicken;INSTANT;14.0;PENALTY;5.0;BONUS;7.0",
                "minecraft:mutton;INSTANT;5.0;BONUS;2.5",
                "minecraft:cooked_mutton;INSTANT;16.0;PENALTY;5.0;BONUS;8.0",
                "minecraft:rabbit;INSTANT;5.0;BONUS;2.5",
                "minecraft:cooked_rabbit;INSTANT;12.0;PENALTY;5.0;BONUS;6.0",
                "minecraft:cod;INSTANT;5.0;BONUS;2.5",
                "minecraft:cooked_cod;INSTANT;12.0;PENALTY;5.0;BONUS;6.0",
                "minecraft:salmon;INSTANT;5.0;BONUS;2.5",
                "minecraft:cooked_salmon;INSTANT;14.0;PENALTY;5.0;BONUS;7.0",
                "minecraft:tropical_fish;INSTANT;4.0;BONUS;2.0",
                "minecraft:mushroom_stew;INSTANT;15.0;CURE;FATIGUE;10.0;BONUS;7.5",
                "minecraft:beetroot_soup;INSTANT;15.0;CURE;FATIGUE;10.0;BONUS;7.5",
                "minecraft:rabbit_stew;INSTANT;20.0;CURE;FATIGUE;25.0;BONUS;10.0",
                "minecraft:suspicious_stew;INSTANT;15.0;POISON;5.0;REGEN;-0.1;20;BONUS;7.5",
                "minecraft:milk_bucket;CURE;POISON;100.0;PENALTY;15.0",
                "minecraft:honey_bottle;INSTANT;10.0;REGEN;0.15;60;CURE;POISON;15.0;BONUS;5.0",
                "minecraft:golden_carrot;INSTANT;12.0;REGEN;0.2;60;PENALTY;20.0;BONUS;6.0",
                "minecraft:golden_apple;INSTANT;10.0;REGEN;0.25;60;CURE;ALL;20.0;PENALTY;10.0;BONUS;5.0",
                "minecraft:enchanted_golden_apple;INSTANT;20.0;REGEN;0.5;120;CURE;ALL;100.0;PENALTY;50.0;BONUS;10.0",
                "minecraft:rotten_flesh;INSTANT;8.0;REGEN;-0.3;30;POISON;15.0;BONUS;4.0",
                "minecraft:spider_eye;INSTANT;4.0;REGEN;-0.3;15;POISON;10.0;BONUS;2.0",
                "minecraft:pufferfish;INSTANT;4.0;REGEN;-0.5;15;POISON;20.0;BONUS;2.0",
                "minecraft:poisonous_potato;INSTANT;4.0;REGEN;-0.2;10;POISON;10.0;BONUS;2.0"
        );

        private static final List<String> DEFAULT_INFINITE_EFFECTS = Arrays.asList(
                "minecraft:speed",
                "minecraft:saturation",
                "parcool:inexhaustible"
        );

        private static final List<String> DEFAULT_EXHAUSTION_PENALTIES = Arrays.asList(
                "minecraft:generic.movement_speed;-0.10;2",
                "minecraft:generic.attack_speed;-0.15;2",
                "minecraft:generic.attack_damage;-0.15;2"
        );

        private static final List<String> DEFAULT_UNIVERSAL_PENALTIES = Arrays.asList(
                "NBT;thirstLevel;*<;6;0;20.0;38143;💧",
                "NBT;targetTemperatureLevel;>;2;4;15.0;16724016;🔥",
                "NBT;targetTemperatureLevel;<;2;0;15.0;65535;❄",
                "EFFECT;minecraft:poison;!*;0;50;15.0;4488448;☣"
        );

        private static final List<String> DEFAULT_PARCOOL_COSTS = Arrays.asList(
                "BreakfallReady;START;0.0",
                "CatLeap;START;2.0;CONTINUE;0.3",
                "ChargeJump;CONTINUE;0.2",
                "ClimbPoles;CONTINUE;0.3",
                "ClimbUp;START;2.0",
                "ClingToCliff;CONTINUE;0.2",
                "HideInBlock;START;1.0;CONTINUE;0.1",
                "Crawl;START;0.5;",
                "Dive;START;2.0",
                "Dodge;START;15.0",
                "FastRun;START;2.5;CONTINUE;0.1",
                "FastSwim;START;2.5;CONTINUE;0.06",
                "Flipping;START;2.0",
                "HangDown;CONTINUE;0.2",
                "HorizontalWallRun;CONTINUE;0.4",
                "JumpFromBar;START;5.0",
                "QuickTurn;START;2.0",
                "RideZipline;START;2.0;CONTINUE;0.2",
                "Roll;START;2.0",
                "SkyDive;START;0.0",
                "Slide;START;2.0;CONTINUE;0.2",
                "Tap;START;2.0",
                "Vault;START;3.0",
                "VerticalWallRun;CONTINUE;0.8",
                "WallJump;START;4.0",
                "WallSlide;CONTINUE;0.2"
        );

        private static final List<String> DEFAULT_CONTAINER_PATHS = Arrays.asList();

        private static final List<String> DEFAULT_ITEM_WEIGHTS = Arrays.asList(
                "minecraft:netherite_chestplate;15.0",
                "minecraft:netherite_leggings;12.0",
                "minecraft:netherite_boots;8.0",
                "minecraft:netherite_helmet;8.0",
                "minecraft:netherite_sword;5.0",
                "minecraft:netherite_pickaxe;6.0",
                "minecraft:netherite_axe;7.0",
                "minecraft:netherite_shovel;4.0",
                "minecraft:netherite_hoe;4.0",
                "minecraft:diamond_chestplate;12.0",
                "minecraft:diamond_leggings;10.0",
                "minecraft:diamond_boots;6.0",
                "minecraft:diamond_helmet;6.0",
                "minecraft:diamond_sword;4.0",
                "minecraft:diamond_pickaxe;5.0",
                "minecraft:diamond_axe;6.0",
                "minecraft:diamond_shovel;3.0",
                "minecraft:diamond_hoe;3.0",
                "minecraft:diamond_horse_armor;15.0",
                "minecraft:iron_chestplate;10.0",
                "minecraft:iron_leggings;8.0",
                "minecraft:iron_boots;5.0",
                "minecraft:iron_helmet;5.0",
                "minecraft:iron_sword;3.5",
                "minecraft:iron_pickaxe;4.5",
                "minecraft:iron_axe;5.5",
                "minecraft:iron_shovel;2.5",
                "minecraft:iron_hoe;2.5",
                "minecraft:iron_horse_armor;12.0",
                "minecraft:shield;4.0",
                "minecraft:golden_chestplate;12.0",
                "minecraft:golden_leggings;10.0",
                "minecraft:golden_boots;6.0",
                "minecraft:golden_helmet;6.0",
                "minecraft:golden_sword;4.0",
                "minecraft:golden_pickaxe;5.0",
                "minecraft:golden_axe;6.0",
                "minecraft:golden_shovel;3.0",
                "minecraft:golden_hoe;3.0",
                "minecraft:golden_horse_armor;15.0",
                "minecraft:chainmail_chestplate;8.0",
                "minecraft:chainmail_leggings;6.0",
                "minecraft:chainmail_boots;4.0",
                "minecraft:chainmail_helmet;4.0",
                "minecraft:leather_chestplate;3.0",
                "minecraft:leather_leggings;2.0",
                "minecraft:leather_boots;1.0",
                "minecraft:leather_helmet;1.0",
                "minecraft:leather_horse_armor;4.0",
                "minecraft:stone_sword;3.0",
                "minecraft:stone_pickaxe;4.0",
                "minecraft:stone_axe;5.0",
                "minecraft:stone_shovel;2.0",
                "minecraft:stone_hoe;2.0",
                "minecraft:wooden_sword;1.5",
                "minecraft:wooden_pickaxe;2.0",
                "minecraft:wooden_axe;2.5",
                "minecraft:wooden_shovel;1.0",
                "minecraft:wooden_hoe;1.0",
                "minecraft:bow;2.0",
                "minecraft:crossbow;3.0",
                "minecraft:trident;4.0"
        );

        private static final List<String> DEFAULT_TAG_WEIGHTS = Arrays.asList(
                "forge:ores;0.25",
                "forge:storage_blocks;0.15",
                "minecraft:logs;0.15",
                "minecraft:planks;0.1",
                "forge:stone;0.1",
                "forge:cobblestone;0.1",
                "forge:sand;0.1",
                "forge:gravel;0.1",
                "forge:ingots;0.2",
                "forge:gems;0.1",
                "forge:obsidian;1.2"
        );

        // --- Config Fields ---
        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemCosts;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> itemCostTags;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> customExhaustionPenalties;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> infiniteStaminaEffects;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> universalPenalties;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> consumableValues;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> customContainerPaths;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> customItemWeights;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> customTagWeights;
        public ForgeConfigSpec.ConfigValue<List<? extends String>> parCoolActionCosts;

        public Lists(ForgeConfigSpec.Builder builder) {
            builder.push("Lists");
            initItemCosts(builder);
            initWeightLists(builder);
            initCompatLists(builder);
            initConsumables(builder);
            initOtherLists(builder);
            builder.pop();
        }

        private void initConsumables(ForgeConfigSpec.Builder builder) {
            builder.push("Consumables");
            consumableValues = builder.comment(
                    "",
                    "Attribute Modifiers applied when consuming an item.",
                    "Format: \"ItemId;TYPE;Args...;TYPE;Args...\"",
                    "Keywords & Scales:",
                    "  INSTANT;Amount        (FLAT: Instantly restores X stamina points)",
                    "  BONUS;Amount          (FLAT: Grants X Temporary Bonus Stamina in a secondary bar)",
                    "  REGEN;Amount;Seconds (PERCENT: Modifies regen speed. 0.2 = +20%)",
                    "  POISON;Amount        (FLAT: Adds X points of Food Poisoning penalty)",
                    "  PENALTY;Amount        (PERCENT: Adds Resistance to penalty buildup)",
                    "     Formula: 30% Base Resistance + Amount. Caps at 80% total.",
                    "     Examples: 10.0 = 40% resist (0.3 + 0.1), 50.0 = 80% resist (0.3 + 0.5).",
                    "  CURE;Target;Amount    (FLAT: Removes X points of chosen penalties)",
                    "     Targets: FATIGUE, HUNGER, POISON, WEIGHT, ALL, or Custom NBT Keys (e.g. 'thirstLevel')",
                    "Examples:",
                    "  \"minecraft:apple;INSTANT;10.0\" (Restores 10 Stamina)",
                    "  \"minecraft:milk_bucket;CURE;POISON;100.0\" (Removes 100.0 Poison Penalty)",
                    "  \"modid:clean_water;CURE;thirstLevel;50.0\" (Removes 50.0 from custom 'thirstLevel' penalty)",
                    "  \"modid:herbal_tea;CURE;POISON;50.0;CURE;FATIGUE;20.0\" (Cures Poison AND Fatigue at the same time)",
                    "  \"modid:anti_toxin;CURE;minecraft:wither;25.0\" (Removes 25.0 penalty caused by the Wither effect)",
                    "  \"minecraft:golden_carrot;PENALTY;20.0;BONUS;20  \" (Grants 50% resistance to penalty buildup and gives 20 bonus stamina, cures nothing)",
                    "  \"minecraft:golden_apple;CURE;ALL;100.0;PENALTY;50.0;REGEN;0.25;60;BONUS;50\" (Cures ALL, Grants High Resist, +25% Regen, +50 bonus stamina)",
                    ""
            ).defineList("consumable_values", DEFAULT_CONSUMABLES, obj -> obj instanceof String);
            builder.pop();
        }

        private void initOtherLists(ForgeConfigSpec.Builder builder) {
            builder.push("Exhaustion Attribute Penalties");
            infiniteStaminaEffects = builder.comment("List of Status Effects that grant infinite stamina (No stamina depletion).")
                    .defineList("infiniteStaminaEffects", DEFAULT_INFINITE_EFFECTS, obj -> obj instanceof String);

            customExhaustionPenalties = builder.comment(
                    "List of Attribute Penalties to apply when Stamina hits 0.",
                    "Format: \"AttributeRegistryName;Amount;Operation\"",
                    "Operations: 0 = ADDITION (Flat number), 1 = MULTIPLY_BASE, 2 = MULTIPLY_TOTAL (Percentage)",
                    ""
            ).defineList("customExhaustionPenalties", DEFAULT_EXHAUSTION_PENALTIES, obj -> obj instanceof String);
            builder.pop();
        }

        private void initItemCosts(ForgeConfigSpec.Builder builder) {
            builder.push("Item Costs");
            itemCosts = builder.comment(
                    "Custom Stamina Costs for Items. Priority 1",
                    "Format: \"ItemId;TYPE;Cost;...\"",
                    "Types:",
                    "  TICK  = Cost per tick while using. (1 Arg: Cost)",
                    "  USE   = Cost on right-click. (1 Arg: Cost)",
                    "  BLOCK = Cost when blocking damage. (2 Args: BaseCost; DamageMultiplier)",
                    "  USE_ON_BLOCK = Cost on right-click block. (1 Arg: Cost)",
                    "",
                    "Examples:",
                    "  \"minecraft:shield;TICK;0.2;BLOCK;2.0;0.8\" (0.2/tick to hold, 2.0 + (Damage * 0.8) on block)",
                    "  \"minecraft:bow;TICK;1.0\"",
                    "  \"minecraft:iron_axe;USE_ON_BLOCK;5.0\" (Costs stamina to strip logs)",
                    "Note: Negative cost values will restore stamina instead of draining it.",
                    ""
            ).defineList("itemCosts", DEFAULT_ITEM_COSTS, obj -> obj instanceof String);

            itemCostTags = builder.comment(
                    "Custom Stamina Costs for Item Tags (Categories). Priority 2",
                    "Works exactly like itemCosts but applies to any item with the specified Tag.",
                    "Priority: Specific items in 'itemCosts' will override these tag settings.",
                    "Format: \"TagId;TYPE;Cost;...\"",
                    "Example: \"forge:tools/bows;TICK;0.5\""
            ).defineList("itemCostTags", DEFAULT_ITEM_COST_TAGS, obj -> obj instanceof String);
            builder.pop();
        }

        private void initWeightLists(ForgeConfigSpec.Builder builder) {
            builder.push("Weight Lists");
            customContainerPaths = builder.comment(
                    "Custom Container NBT Paths.",
                    "Use this for backpacks/containers that store items inside their NBT data.",
                    "Format: 'modid:item;path.to.list'",
                    "Use dots (.) for nested tags.",
                    "Example: 'minecraft:shulker_box;BlockEntityTag.Items'",
                    "Example: 'somemod:satchel;Inventory'",
                    "Note: Vanilla Shulker Boxes and Bundles are already handled.",
                    "Note: Compatible with Curios and Packedup by SuperMartijin642, should work with most backpack (or other containers) mods normally but if it doesnt work, add the path to this list"
            ).defineList("customContainerPaths", DEFAULT_CONTAINER_PATHS, obj -> obj instanceof String);

            customItemWeights = builder.comment(
                    "Priority 1: Explicit Item Weights.",
                    "These values override the Auto-Weigher. Use this for heavy equipment that doesn't stack.",
                    "(Works with stackable but ex. minecraft:obsidian;5 will mean EACH obisidan is 5 weight not 5/stacksize)",
                    "Format: 'modid:item;weight'",
                    "Example: 'minecraft:netherite_chestplate;15.0'"
            ).defineList("customItemWeights", DEFAULT_ITEM_WEIGHTS, obj -> obj instanceof String);

            customTagWeights = builder.comment(
                    "Priority 2: Tag/Category Weights.",
                    "Used if the item is not in the explicit list above.",
                    "Useful for making entire categories of items heavy.",
                    "(same as above, for every ONE item in the tag it adds X weight not X/stacksize)",
                    "Format: 'tag;weight'"
            ).defineList("customTagWeights", DEFAULT_TAG_WEIGHTS, obj -> obj instanceof String);
            builder.pop();
        }

        private void initCompatLists(ForgeConfigSpec.Builder builder) {
            builder.push("Mod Compat Lists");
            universalPenalties = builder.comment(
                    "",
                    "Universal Compatibility with Scaling Penalties. This section is advanced but very useful when learned, I've made a ton of examples for your assistance.",
                    "Allows you to link external mod data (NBT tags or Potion Effects) to the stamina system.",
                    "",
                    "Format: \"Type;Key;Comparator;Threshold;WorstValue;MaxPenalty;ColorInt;IconText\"",
                    "",
                    "Arguments Explained:",
                    "  1. Type:       'NBT' (checks player data) or 'EFFECT' (checks potion amplifier/level).",
                    "  2. Key:        The NBT path (e.g. 'thirstLevel') or Effect ID (e.g. 'minecraft:poison').",
                    "  3. Comparator: Logic for calculating the penalty.",
                    "       SCALE MODES (Linear penalty between two points):",
                    "         >  : Penalty increases as value rises above Threshold, maxing at WorstValue.",
                    "         <  : Penalty increases as value falls below Threshold, maxing at WorstValue.",
                    "       MULTIPLIER MODE (Penalty stacks per unit):",
                    "         * : (or *>) Each point ABOVE Threshold adds MaxPenalty to total.",
                    "             Formula: (CurrentValue - Threshold) * MaxPenalty",
                    "         *< : Each point BELOW Threshold adds MaxPenalty to total.",
                    "              Formula: (Threshold - CurrentValue) * MaxPenalty",
                    "       INSTANT FLAG:",
                    "         ! : Add ! before any comparator (!>, !<, !*, !*<) to apply penalty instantly.",
                    "              Without !, penalties build up gradually over time.",
                    "  4. Threshold:  The safe value where penalty starts (0%).",
                    "  5. WorstValue: In SCALE mode: The value where penalty reaches 100%.",
                    "                 In MULTIPLIER mode: The Maximum total penalty allowed (Hard Cap).",
                    "  6. MaxPenalty: In SCALE mode: Total penalty amount at WorstValue.",
                    "                 In MULTIPLIER mode: Penalty amount per 1 unit of difference.",
                    "  7. ColorInt:   Decimal color code for the HUD overlay (e.g. 16711680 is Red).",
                    "  8. IconText:   Emoji or text to display on the bar. Write 'none' to disable.",
                    "",
                    "Examples (SCALE MODE):",
                    "  \"NBT;thirstLevel;<;6;0;20.0;38143;💧\"",
                    "    -> Thirst dropping from 6 to 0 builds penalty from 0 to 20.",
                    "  \"NBT;temperature;>;37;42;25.0;16724016;🔥\"",
                    "    -> Temperature rising from 37 to 42 builds penalty from 0 to 25.",
                    "  \"EFFECT;minecraft:wither;>;-1;3;40.0;3355443;💀\"",
                    "    -> Wither I to IV builds penalty from 0 to 40.",
                    "",
                    "Examples (SCALE MODE):",
                    "  \"NBT;thirstLevel;!<;6;0;20.0;38143;💧\"",
                    "    -> Thirst dropping from 6 to 0 instantly applies penalty from 0 to 20.",
                    "",
                    "Examples (MULTIPLIER MODE):",
                    "  \"NBT;thirstLevel;*<;6;30;2.0;38143;💧\"",
                    "    -> Below 6 thirst, every point lost adds 2.0 penalty (builds over time). Caps at 30 total.",
                    "    -> Thirst 5 = 2 penalty, Thirst 4 = 4 penalty, Thirst 3 = 6 penalty, etc.",
                    "  \"EFFECT;minecraft:poison;*;0;50;5.0;4488448;☣\"",
                    "    -> Every poison level adds 5 penalty (builds over time). Caps at 50.",
                    "    -> Poison I = 5, Poison II = 10, Poison III = 15, etc.",
                    "  \"NBT;radiation;*;10;100;3.5;65280;☢\"",
                    "    -> Above 10 radiation, every point adds 3.5 penalty (builds over time). Caps at 100.",
                    "    -> Radiation 11 = 3.5, Radiation 12 = 7.0, Radiation 15 = 17.5, etc.",
                    "",
                    "Examples (MULTIPLIER MODE):",
                    "  \"NBT;thirstLevel;!*<;6;30;2.0;38143;💧\"",
                    "    -> Below 6 thirst, every point lost instantly adds 2.0 penalty. Caps at 30 total.",
                    "  \"EFFECT;minecraft:poison;!*;0;50;5.0;4488448;☣\"",
                    "    -> Every poison level instantly adds 5 penalty. Caps at 50.",
                    "    -> Poison I = 5, Poison II = 10, Poison III = 15, etc.",
                    "  \"EFFECT;hordes:infected;!*;0;80;10.0;8388736;🦠\"",
                    "    -> every level instantly adds 10 penalty. Caps at 80.",
                    "    -> Infection III = 10, Infection IV = 20, Infection V = 30, etc.",
                    ""
            ).defineList("universalPenalties", DEFAULT_UNIVERSAL_PENALTIES, obj -> obj instanceof String);

            parCoolActionCosts = builder.comment(
                    "ParCool Action Stamina Costs",
                    "Format: \"ActionName;START;Cost;CONTINUE;Cost\"",
                    "Types:",
                    "  START    = One-time cost when action begins",
                    "  CONTINUE = Cost per tick while action is active",
                    "",
                    "Examples:",
                    "  \"WallRun;START;15.0;CONTINUE;0.5\"",
                    "  \"Vault;START;8.0\" (no continue cost)",
                    "  \"Roll;START;5.0;CONTINUE;0.0\" (explicit zero)",
                    "Note: Negative cost values will restore stamina instead of draining it.",
                    "Note: Fast run and Fast swim costs are added onto the cost of regular sprinting and regular swimming. ",
                    ""
            ).defineList("parCoolActionCosts", DEFAULT_PARCOOL_COSTS, obj -> obj instanceof String);
            builder.pop();
        }
    }
}