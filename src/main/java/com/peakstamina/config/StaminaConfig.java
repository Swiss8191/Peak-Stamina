package com.peakstamina.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import java.util.List;

public class StaminaConfig {

    public static final Common COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    public enum SleepMode {
        DEFAULT,
        HARDCORE
    }

    public enum RegenIndicatorStyle {
        DEFAULT,
        CUSTOM,
        OFF
    }

    public enum AutoHudMode {
        FADE,
        SLIDE,
        BOTH
    }

    public enum AutoHudSlideDir {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public enum AutoHudEasing {
        LINEAR,
        SMOOTHSTEP,
        EASE_OUT_SINE,
        EASE_OUT_EXPO
    }

    static {
        final Pair<Common, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        final Pair<Client, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

    public static class Common {

        public ModConfigSpec.BooleanValue enableStamina;
        public ModConfigSpec.BooleanValue disableInCreative;
        public ModConfigSpec.BooleanValue disableInSpectator;
        public ModConfigSpec.DoubleValue initialMaxStamina;

        public ModConfigSpec.BooleanValue enableSlowClimb;
        public ModConfigSpec.DoubleValue slowClimbSpeed;

        public ModConfigSpec.DoubleValue depletionSprint;
        public ModConfigSpec.DoubleValue depletionJump;
        public ModConfigSpec.DoubleValue depletionAttack;
        public ModConfigSpec.DoubleValue depletionMissedAttack;
        public ModConfigSpec.DoubleValue depletionBlockBreak;
        public ModConfigSpec.DoubleValue depletionBlockPlace;
        public ModConfigSpec.DoubleValue depletionSwim;
        public ModConfigSpec.DoubleValue depletionClimb;
        public ModConfigSpec.IntValue itemInterruptionCooldown;

        public ModConfigSpec.BooleanValue attackCostScalesWithWeight;
        public ModConfigSpec.DoubleValue attackWeightNormalizer;
        public ModConfigSpec.DoubleValue attackWeightScaleFactor;
        public ModConfigSpec.DoubleValue attackWeightMinMultiplier;
        public ModConfigSpec.DoubleValue attackWeightMaxMultiplier;

        public ModConfigSpec.BooleanValue missedAttackCostScalesWithWeight;
        public ModConfigSpec.DoubleValue missedAttackWeightNormalizer;
        public ModConfigSpec.DoubleValue missedAttackWeightScaleFactor;
        public ModConfigSpec.DoubleValue missedAttackWeightMinMultiplier;
        public ModConfigSpec.DoubleValue missedAttackWeightMaxMultiplier;

        public ModConfigSpec.DoubleValue recoveryPerTick;
        public ModConfigSpec.DoubleValue recoveryRestMult;
        public ModConfigSpec.DoubleValue recoveryClimbMult;
        public ModConfigSpec.DoubleValue recoveryWaterMult;
        public ModConfigSpec.IntValue recoveryDelay;

        public ModConfigSpec.DoubleValue lightweightLvl1;
        public ModConfigSpec.DoubleValue lightweightLvl2;
        public ModConfigSpec.DoubleValue lightweightLvl3;

        public ModConfigSpec.DoubleValue tirelessLvl1;
        public ModConfigSpec.DoubleValue tirelessLvl2;
        public ModConfigSpec.DoubleValue tirelessLvl3;

        public ModConfigSpec.DoubleValue minMaxStamina;
        public ModConfigSpec.DoubleValue fatigueThreshold;
        public ModConfigSpec.IntValue fatigueDurationToPenalty;
        public ModConfigSpec.IntValue penaltyRecoveryDelay;
        public ModConfigSpec.DoubleValue penaltyBaseRate;
        public ModConfigSpec.DoubleValue penaltyCurveFactor;
        public ModConfigSpec.DoubleValue penaltyDecayRate;
        public ModConfigSpec.DoubleValue penaltyBuildupRate;
        public ModConfigSpec.DoubleValue maxExertionPenalty;
        public ModConfigSpec.DoubleValue maxHungerPenalty;
        public ModConfigSpec.IntValue hungerPenaltyThreshold;
        public ModConfigSpec.IntValue penaltyReliefDuration;

        public ModConfigSpec.EnumValue<SleepMode> sleepMode;
        public ModConfigSpec.DoubleValue sleepFatigueReduction;

        public ModConfigSpec.BooleanValue enableExcessStaminaConversion;
        public ModConfigSpec.DoubleValue excessConversionRate;
        public ModConfigSpec.IntValue bonusStaminaDecayDelay;
        public ModConfigSpec.BooleanValue bonusDecayScalesWithAmount;
        public ModConfigSpec.DoubleValue bonusStaminaDecayRate;
        public ModConfigSpec.BooleanValue universalBuffRegenWhileActive;

        public ModConfigSpec.DoubleValue depletionElytra;
        public ModConfigSpec.BooleanValue disableElytraWhenExhausted;
        public ModConfigSpec.DoubleValue exhaustedElytraDrag;
        public ModConfigSpec.IntValue exhaustedElytraTickInterval;
        public ModConfigSpec.DoubleValue exhaustedElytraGravity;
        public ModConfigSpec.DoubleValue exhaustedElytraMinSpeed;

        public ModConfigSpec.BooleanValue enableWeightSystem;
        public ModConfigSpec.DoubleValue maxWeightPenaltyAmount;
        public ModConfigSpec.DoubleValue weightPenaltyThreshold;
        public ModConfigSpec.DoubleValue weightPenaltyLimit;
        public ModConfigSpec.DoubleValue autoWeightBase;
        public ModConfigSpec.IntValue maxWeightRecursionDepth;

        public ModConfigSpec.IntValue exhaustionCooldownDuration;
        public ModConfigSpec.DoubleValue exhaustedSpeedPenalty;

        public ModConfigSpec.DoubleValue maxPoisonPenalty;
        public ModConfigSpec.IntValue poisonDecayDelay;
        public ModConfigSpec.DoubleValue poisonDecayRate;

        public Common(ModConfigSpec.Builder builder) {
            initGeneral(builder);
            initRecovery(builder);
            initDepletion(builder);
            initCombat(builder);
            initEnchants(builder);
            initFatigueAndLimits(builder);
            initBonusStamina(builder);
            initElytra(builder);
            initWeightSystem(builder);
            initExhaustion(builder);
            initConsumables(builder);
        }

        private void initGeneral(ModConfigSpec.Builder builder) {
            builder.push("General");
            enableStamina = builder.comment(" Set to false to completely disable the stamina system").define("enableStamina", true);
            disableInCreative = builder.comment(" If true, players in Creative mode will not consume or use the stamina system.").define("disableInCreative", true);
            disableInSpectator = builder.comment(" If true, players in Spectator mode will not consume or use the stamina system.").define("disableInSpectator", true);
            initialMaxStamina = builder.comment(" Initial Max Stamina value for players.").defineInRange("initialMaxStamina", 100.0, 1.0, 10000.0);
            enableSlowClimb = builder.comment(" Enable the slow climb mechanic.").define("enableSlowClimb", true);
            slowClimbSpeed = builder.comment(" Base movement speed multiplier when slow climbing (sneaking on ladders/vines).").defineInRange("slowClimbSpeed", 0.4, 0.0, 10.0);
            builder.pop();
        }

        private void initRecovery(ModConfigSpec.Builder builder) {
            builder.push("Recovery Settings");
            recoveryPerTick = builder.comment(" Stamina recovered per tick").defineInRange("recoveryPerTick", 0.45, 0.0, 100.0);
            recoveryRestMult = builder.comment(" Multiplier for recovery when standing completely still").defineInRange("recoveryRestMult", 1.45, 1.0, 10.0);
            recoveryClimbMult = builder.comment(" Multiplier for recovery when hanging on a ladder/vine (slow climbing or not moving)").defineInRange("recoveryClimbMult", 0.4, 0.0, 10.0);
            recoveryWaterMult = builder.comment(" Multiplier for recovery while inside water").defineInRange("recoveryWaterMult", 0.3, 0.0, 10.0);
            recoveryDelay = builder.comment(" Ticks before stamina starts regenerating after action (20 ticks = 1 sec)").defineInRange("recoveryDelay", 50, 0, 2000);
            builder.pop();
        }

        private void initDepletion(ModConfigSpec.Builder builder) {
            builder.push("Depletion Rates");
            depletionSprint = builder.comment(" Stamina drained per tick while sprinting. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionSprint", 0.15, -100.0, 100.0);
            depletionJump = builder.comment(" Stamina drained per jump. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionJump", 0.85, -100.0, 100.0);
            depletionAttack = builder.comment(" Base stamina drained per attack. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionAttack", 3.45, -100.0, 100.0);
            depletionMissedAttack = builder.comment(" Stamina drained when swinging at the air (missing an attack). Set to 0.0 to disable.").defineInRange("depletionMissedAttack", 1.0, 0.0, 100.0);
            depletionBlockBreak = builder.comment(" Stamina drained per block broken. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionBlockBreak", 1.1, -100.0, 100.0);
            depletionBlockPlace = builder.comment(" Stamina drained per block placed. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionBlockPlace", 0.7, -100.0, 100.0);
            depletionSwim = builder.comment(" Stamina drained per tick while swimming. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionSwim", 0.05, -100.0, 100.0);
            depletionClimb = builder.comment(" Stamina drained per tick while climbing. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionClimb", 0.7, -100.0, 100.0);
            itemInterruptionCooldown = builder.comment(" Ticks to disable an item if you run out of stamina while using it.").defineInRange("itemInterruptionCooldown", 120, 0, 1200);
            builder.pop();
        }

        private void initCombat(ModConfigSpec.Builder builder) {
            builder.push("Combat");
            attackCostScalesWithWeight = builder.comment(" If true, the stamina drained per attack is modified by the weapon's weight.").define("attackCostScalesWithWeight", true);
            attackWeightNormalizer = builder.comment(" The baseline weapon weight that results in exactly a 1.0x stamina cost multiplier. Default 3.0 (Stone Sword)").defineInRange("attackWeightNormalizer", 3.0, 0.1, 1000.0);
            attackWeightScaleFactor = builder.comment(" How intensely weight affects the attack cost. 1.0 = direct scaling, 0.5 = half impact, 2.0 = double impact.").defineInRange("attackWeightScaleFactor", 1.0, 0.0, 10.0);
            attackWeightMinMultiplier = builder.comment(" The minimum possible stamina cost multiplier from weight scaling. Prevents light weapons from becoming free.").defineInRange("attackWeightMinMultiplier", 0.4, 0.0, 1.0);
            attackWeightMaxMultiplier = builder.comment(" The maximum possible stamina cost multiplier from weight scaling. Prevents excessively heavy weapons from draining all stamina instantly.").defineInRange("attackWeightMaxMultiplier", 5.0, 1.0, 100.0);

            missedAttackCostScalesWithWeight = builder.comment("", " If true, the stamina drained per missed attack is modified by the weapon's weight.").define("missedAttackCostScalesWithWeight", true);
            missedAttackWeightNormalizer = builder.comment(" The baseline weapon weight that results in exactly a 1.0x stamina cost multiplier for missed attacks.").defineInRange("missedAttackWeightNormalizer", 3.0, 0.1, 1000.0);
            missedAttackWeightScaleFactor = builder.comment(" How intensely weight affects the missed attack cost.").defineInRange("missedAttackWeightScaleFactor", 1.0, 0.0, 10.0);
            missedAttackWeightMinMultiplier = builder.comment(" The minimum possible stamina cost multiplier for missed attacks.").defineInRange("missedAttackWeightMinMultiplier", 0.4, 0.0, 1.0);
            missedAttackWeightMaxMultiplier = builder.comment(" The maximum possible stamina cost multiplier for missed attacks.").defineInRange("missedAttackWeightMaxMultiplier", 5.0, 1.0, 100.0);
            builder.pop();
        }

        private void initEnchants(ModConfigSpec.Builder builder) {
            builder.push("Enchantments");

            lightweightLvl1 = builder.comment("Weight reduction multiplier for Lightweight I (e.g. 0.25 = 25% reduction)")
                    .defineInRange("lightweightLvl1", 0.25, 0.0, 1.0);
            lightweightLvl2 = builder.comment("Weight reduction multiplier for Lightweight II")
                    .defineInRange("lightweightLvl2", 0.50, 0.0, 1.0);
            lightweightLvl3 = builder.comment("Weight reduction multiplier for Lightweight III")
                    .defineInRange("lightweightLvl3", 0.75, 0.0, 1.0);

            tirelessLvl1 = builder.comment("Stamina cost reduction for Tireless I (e.g. 0.20 = 20% reduction)")
                    .defineInRange("tirelessLvl1", 0.20, 0.0, 1.0);
            tirelessLvl2 = builder.comment("Stamina cost reduction for Tireless II")
                    .defineInRange("tirelessLvl2", 0.40, 0.0, 1.0);
            tirelessLvl3 = builder.comment("Stamina cost reduction for Tireless III")
                    .defineInRange("tirelessLvl3", 0.60, 0.0, 1.0);

            builder.pop();
        }

        private void initFatigueAndLimits(ModConfigSpec.Builder builder) {
            builder.push("Fatigue & Limits");
            fatigueDurationToPenalty = builder.comment(" Ticks spent in critical stamina (red zone) before fatigue penalty begins (180 ticks = 9s)").defineInRange("fatigueDurationToPenalty", 180, 0, 10000);
            minMaxStamina = builder.comment(" The absolute floor for Max Stamina (Stamina bar cannot shrink smaller than this)").defineInRange("minMaxStamina", 10.0, 1.0, 100.0);
            fatigueThreshold = builder.comment(" Percentage of Max Stamina where fatigue penalty starts (0.25 = 25%)").defineInRange("fatigueThreshold", 0.25, 0.0, 1.0);
            penaltyRecoveryDelay = builder.comment(" Ticks to wait after leaving red zone before penalty recovers").defineInRange("penaltyRecoveryDelay", 100, 0, 2000);

            penaltyBaseRate = builder.comment("", "", " Base rate for exponential penalty increase").defineInRange("penaltyBaseRate", 0.02, 0.0, 10.0);
            penaltyCurveFactor = builder.comment(" Divisor for exponential curve (Lower = Steeper curve)").defineInRange("penaltyCurveFactor", 150.0, 1.0, 1000.0);
            penaltyDecayRate = builder.comment(" How much penalty recovers per tick when conditions improve (0.05 = 1.0 penalty per second )").defineInRange("penaltyDecayRate", 0.05, 0.0, 100.0);
            penaltyBuildupRate = builder.comment(" How much penalty accumulates per tick when conditions worsen (0.1 = 2.0 penalty per second)").defineInRange("penaltyBuildupRate", 0.1, 0.0, 100.0);

            maxExertionPenalty = builder.comment("", "", " Maximum reduction to Max Stamina caused by physical exhaustion").defineInRange("maxExertionPenalty", 30.0, 0.0, 100.0);
            maxHungerPenalty = builder.comment(" Maximum reduction to Max Stamina caused by starvation").defineInRange("maxHungerPenalty", 30.0, 0.0, 100.0);
            hungerPenaltyThreshold = builder.comment(" Food level at which stamina penalty begins (6 = 3 shanks)").defineInRange("hungerPenaltyThreshold", 6, 0, 20);

            builder.push("Sleep Mechanics");
            sleepMode = builder.comment(" DEFAULT: Fatigue decays over time, sleeping, and eating consumables reduces it.",
                    " HARDCORE: Fatigue doesn't decay naturally. You must sleep or eat food that reduces fatigue.")
                    .defineEnum("sleepMode", SleepMode.DEFAULT);
            sleepFatigueReduction = builder.comment(" Flat amount of fatigue penalty to remove after a successful sleep.",
                    " Set to a high number (e.g. 100.0) to fully clear it.")
                    .defineInRange("sleepFatigueReduction", 20.0, 0.0, 1000.0);
            builder.pop();
            builder.pop();
        }

        private void initElytra(ModConfigSpec.Builder builder) {
            builder.push("Elytra Mechanics");
            depletionElytra = builder.comment(" Stamina drained per tick while flying with Elytra.").defineInRange("depletionElytra", 0.25, -100.0, 100.0);
            disableElytraWhenExhausted = builder.comment(" If true, players will experience stalling (drag increases, lift decreases) when stamina hits 0.",
                    " If false, players can continue flying normally even with 0 stamina.")
                    .define("disableElytraWhenExhausted", true);
            exhaustedElytraDrag = builder.comment(" How much horizontal speed is retained every X ticks when stalling (0.9 = 10% speed loss per tick). Lower = faster stop. (note that if the speed retainment is too high, the player will never fall out of the sky)")
                    .defineInRange("exhaustedElytraDrag", 0.86, 0.0, 1.0);
            exhaustedElytraTickInterval = builder.comment(" How often (in ticks) the drag physics are applied (I suggest changing this over elytra speed retainment). Higher = less frequent slowdown and longer gliding. 1 = Every tick.")
                    .defineInRange("exhaustedElytraTickInterval", 3, 1, 100);
            exhaustedElytraGravity = builder.comment(" Extra vertical gravity applied per tick when stalling. (Negative value pulls down).")
                    .defineInRange("exhaustedElytraGravity", -0.025, -10.0, 0.0);
            exhaustedElytraMinSpeed = builder.comment(" Speed threshold below which the Elytra will force-close and drop the player.")
                    .defineInRange("exhaustedElytraMinSpeed", 0.5, 0.0, 5.0);
            builder.pop();
        }

        private void initBonusStamina(ModConfigSpec.Builder builder) {
            builder.push("Bonus Stamina System");
            enableExcessStaminaConversion = builder.comment(" If true, restoring stamina (INSTANT) past the Max Stamina limit will convert the excess into Bonus Stamina.").define("enableExcessStaminaConversion", true);
            excessConversionRate = builder.comment(" The percentage of excess normal stamina that becomes bonus stamina.",
                    " 1.0 = 100% conversion (10 excess -> 10 bonus).",
                    " 0.5 = 50% conversion (10 excess -> 5 bonus).")
                    .defineInRange("excessConversionRate", 0.5, 0.0, 100.0);
            bonusStaminaDecayDelay = builder.comment(" Ticks before Bonus Stamina starts decaying after being gained/used.",
                    " Set to 0 to make it decay immediately and always.")
                    .defineInRange("bonusStaminaDecayDelay", 20, 0, 72000);
            bonusDecayScalesWithAmount = builder.comment(" If true, the decay rate is treated as a percentage of current bonus stamina (Exponential decay).",
                    " This makes decay faster when you have lots of bonus, and slower when you have little.",
                    " If false, the decay rate is a flat amount subtracted per tick (Linear decay).")
                    .define("bonusDecayScalesWithAmount", true);
            bonusStaminaDecayRate = builder.comment(" Amount of Bonus Stamina decaying per second.",
                    " If Scaling is TRUE: 0.05 = 5% of current bonus lost per second.",
                    " If Scaling is FALSE: 1.0 = 1.0 flat stamina lost per second.")
                    .defineInRange("bonusStaminaDecayRate", 0.05, 0.0, 100.0);
            universalBuffRegenWhileActive = builder.comment(" If false, Bonus Stamina from universal buffs (PASSIVE/BOTH modes) will pause its regeneration while the player is actively using stamina or on regen cooldown.")
                    .define("universalBuffRegenWhileActive", false);
            builder.pop();
        }

        private void initWeightSystem(ModConfigSpec.Builder builder) {
            builder.push("Weight System");
            enableWeightSystem = builder.comment(" Enable the weight calculation system.").define("enableWeightSystem", true);
            weightPenaltyThreshold = builder.comment(" Weight at which the penalty begins to apply (0% penalty).").defineInRange("weightPenaltyThreshold", 125, 0.0, 10000.0);
            weightPenaltyLimit = builder.comment(" Weight at which the penalty reaches maximum (100% penalty).").defineInRange("weightPenaltyLimit", 400.0, 1.0, 10000.0);
            maxWeightPenaltyAmount = builder.comment(" Maximum amount of Max Stamina removed when at full weight limit.").defineInRange("maxWeightPenaltyAmount", 40.0, 0.0, 100.0);
            maxWeightRecursionDepth = builder.comment(" Maximum depth for recursive weight calculation (Backpacks inside backpacks). Higher values may cause lag.").defineInRange("maxWeightRecursionDepth", 3, 0, 10);
            autoWeightBase = builder.comment(" The 'Auto-Weigher' (Stack Size Heuristic).",
                    " Used for ANY item that is not explicitly listed in the lists config.",
                    " Formula: Weight = (Base / MaxStackSize) * Count")
                    .defineInRange("autoWeightBase", 10, 0.0, 1000.0);
            builder.pop();
        }

        private void initExhaustion(ModConfigSpec.Builder builder) {
            builder.push("Exhaustion Penalties");
            exhaustionCooldownDuration = builder.comment(" Ticks the penalties persist after stamina regenerates above 0 (0 to disable)").defineInRange("exhaustionCooldownDuration", 60, 0, 12000);
            exhaustedSpeedPenalty = builder.comment("", " Movement speed multiplier when exhausted sprinting (e.g. -0.5 is 50% slower, -0.9 is 90% slower). Set to 0.0 to disable sprinting speed penalty.")
                    .defineInRange("exhaustedSpeedPenalty", -0.65, -1.0, 0.0);
            builder.pop();
        }

        private void initConsumables(ModConfigSpec.Builder builder) {
            builder.push("Consumables");
            penaltyReliefDuration = builder.comment("", " Duration (in seconds) for the penalty resistance buff applied by penalty-reducing items.").defineInRange("penaltyReliefDuration", 25, 0, 600);
            builder.push("Food Poisoning");
            maxPoisonPenalty = builder.comment(" Maximum reduction to Max Stamina caused by food poisoning (Flat value, e.g. 40.0 = 40% of default bar)").defineInRange("maxPoisonPenalty", 40.0, 0.0, 100.0);
            poisonDecayDelay = builder.comment(" Seconds to wait after eating bad food before poison penalty starts decaying").defineInRange("poisonDecayDelay", 45, 0, 600);
            poisonDecayRate = builder.comment(" How much poison penalty recovers per tick after the delay (Flat value)").defineInRange("poisonDecayRate", 0.05, 0.0, 100.0);
            builder.pop();
            builder.pop();
        }
    }

    public static class Client {

        public enum HudStyle {
            BAR,
            ICON
        }

        public enum WeightUnit {
            lbs,
            kg,
            CUSTOM
        }

        public final ModConfigSpec.EnumValue<HudStyle> hudStyle;
        public final ModConfigSpec.IntValue barXOffset;
        public final ModConfigSpec.IntValue barYOffset;
        public final ModConfigSpec.IntValue iconXOffset;
        public final ModConfigSpec.IntValue iconYOffset;
        public final ModConfigSpec.IntValue barWidth;
        public final ModConfigSpec.IntValue barHeight;
        public final ModConfigSpec.IntValue colorBackground;
        public final ModConfigSpec.IntValue colorSafe;
        public final ModConfigSpec.IntValue colorCritical;
        public final ModConfigSpec.IntValue colorTireless;
        public final ModConfigSpec.IntValue colorStripes;
        public final ModConfigSpec.IntValue colorPenaltyHunger;
        public final ModConfigSpec.IntValue colorPenaltyPoison;
        public final ModConfigSpec.BooleanValue showIcons;
        public final ModConfigSpec.IntValue colorBonusTop;
        public final ModConfigSpec.IntValue colorBonusBottom;
        public final ModConfigSpec.IntValue colorBonusHighlight;
        public final ModConfigSpec.IntValue bonusHighlightAlpha;
        public final ModConfigSpec.IntValue colorPenaltyWeight;

        public final ModConfigSpec.BooleanValue autoHudEnable;
        public final ModConfigSpec.EnumValue<AutoHudMode> autoHudMode;
        public final ModConfigSpec.EnumValue<AutoHudSlideDir> autoHudSlideDir;
        public final ModConfigSpec.EnumValue<AutoHudEasing> autoHudEasing;
        public final ModConfigSpec.DoubleValue autoHudFadeInSpeed;
        public final ModConfigSpec.DoubleValue autoHudFadeOutSpeed;
        public final ModConfigSpec.DoubleValue autoHudSlideInSpeed;
        public final ModConfigSpec.DoubleValue autoHudSlideOutSpeed;
        public final ModConfigSpec.IntValue autoHudSlideDistance;
        public final ModConfigSpec.IntValue autoHudLingerTime;
        public final ModConfigSpec.DoubleValue autoHudThreshold;
        public final ModConfigSpec.BooleanValue autoHudShowOnPenalties;
        public final ModConfigSpec.BooleanValue autoHudShowOnBonus;

        public final ModConfigSpec.EnumValue<RegenIndicatorStyle> regenIndicatorStyle;
        public final ModConfigSpec.BooleanValue showRegenBorder;

        public final ModConfigSpec.BooleanValue enableWeightHUD;
        public final ModConfigSpec.IntValue weightXOffset;
        public final ModConfigSpec.IntValue weightYOffset;
        public final ModConfigSpec.EnumValue<WeightUnit> displayUnit;
        public final ModConfigSpec.ConfigValue<String> customUnitLabel;
        public final ModConfigSpec.DoubleValue customUnitMultiplier;

        // Tooltip Configs
        public final ModConfigSpec.BooleanValue enableTooltips;
        public final ModConfigSpec.BooleanValue advancedTooltipsOnly;
        public final ModConfigSpec.ConfigValue<List<? extends String>> customTooltips;

        // Tooltip Labels
        public final ModConfigSpec.ConfigValue<String> labelWeight;
        public final ModConfigSpec.ConfigValue<String> labelAttackCost;
        public final ModConfigSpec.ConfigValue<String> labelUseCost;
        public final ModConfigSpec.ConfigValue<String> labelTickCost;
        public final ModConfigSpec.ConfigValue<String> labelBlockCost;
        public final ModConfigSpec.ConfigValue<String> labelMissCost;
        public final ModConfigSpec.ConfigValue<String> labelInstant;
        public final ModConfigSpec.ConfigValue<String> labelBonus;
        public final ModConfigSpec.ConfigValue<String> labelRegen;
        public final ModConfigSpec.ConfigValue<String> labelCures;

        public Client(ModConfigSpec.Builder builder) {
            builder.push("HUD Layout");
            hudStyle = builder.comment(" The style of the HUD. BAR is horizontal, ICON is vertical.").defineEnum("hudStyle", HudStyle.BAR);
            barXOffset = builder.comment("X offset for the Stamina HUD in BAR mode.").defineInRange("barXOffset", 0, -5000, 5000);
            barYOffset = builder.comment("Y offset for the Stamina HUD in BAR mode.").defineInRange("barYOffset", 1, -5000, 5000);
            iconXOffset = builder.comment("X offset for the Stamina HUD in ICON mode.").defineInRange("iconXOffset", 0, -5000, 5000);
            iconYOffset = builder.comment("Y offset for the Stamina HUD in ICON mode.").defineInRange("iconYOffset", 1, -5000, 5000);
            barWidth = builder.comment(" Width of the bar in pixels (Used for BAR style)").defineInRange("barWidth", 182, 1, 1000);
            barHeight = builder.comment(" Height of the bar in pixels (Used for BAR style)").defineInRange("barHeight", 4, 1, 100);
            regenIndicatorStyle = builder.comment("Regen Arrow Style: DEFAULT (uses >>> ), CUSTOM (uses your custom texture made in the sprite sheet) or OFF.").defineEnum("regenIndicatorStyle", RegenIndicatorStyle.DEFAULT);
            showRegenBorder = builder.comment("If true, draws a slightly dark border around the regen indicators (>).").define("showRegenBorder", true);
            showIcons = builder.comment(" Whether to render text/emoji icons on the stamina bar penalty zones.").define("showIcons", true);
            builder.pop();

            builder.push("Colors");
            colorBackground = builder.defineInRange("colorBackground", 2236962, 0, 16777215);
            colorSafe = builder.defineInRange("colorSafe", 65280, 0, 16777215);
            colorCritical = builder.defineInRange("colorCritical", 16711680, 0, 16777215);
            colorTireless = builder.defineInRange("colorTireless", 65450, 0, 16777215);
            colorStripes = builder.defineInRange("colorStripes", 16711680, 0, 16777215);
            colorPenaltyHunger = builder.comment(" Color for Hunger penalty stripes").defineInRange("colorPenaltyHunger", 16763904, 0, 16777215);
            colorPenaltyPoison = builder.comment(" Color for Food Poisoning penalty stripes").defineInRange("colorPenaltyPoison", 11141375, 0, 16777215);
            colorPenaltyWeight = builder.comment(" Color for weight stripes").defineInRange("colorPenaltyWeight", 5592405, 0, 16777215);
            builder.pop();

            builder.push("Bonus Stamina Bar");
            colorBonusTop = builder.comment(" Top gradient color for Bonus Stamina (RGB). Default: Gold").defineInRange("colorBonusTop", 16777184, 0, 16777215);
            colorBonusBottom = builder.comment(" Bottom gradient color for Bonus Stamina (RGB). Default: Dark Orange").defineInRange("colorBonusBottom", 16449331, 0, 16777215);
            colorBonusHighlight = builder.comment(" Color of the highlight sheen (RGB). Default: White").defineInRange("colorBonusHighlight", 16777215, 0, 16777215);
            bonusHighlightAlpha = builder.comment(" Opacity of the highlight sheen (0-255). 0 = Invisible, 255 = Solid. Default: 128 (Semi-transparent)").defineInRange("bonusHighlightAlpha", 128, 0, 255);
            builder.pop();

            builder.push("Encumbrance UI");

            enableWeightHUD = builder.comment("Enable the dynamic Weight/Encumbrance text on the inventory screen.")
                    .define("enableWeightHUD", true);

            weightXOffset = builder.comment("X position offset for the Weight HUD (0 is the center of the screen)")
                    .defineInRange("weightXOffset", 0, -4000, 4000);

            weightYOffset = builder.comment("Y position offset for the Weight HUD (0 is the center of the screen)")
                    .defineInRange("weightYOffset", 1, -4000, 4000);

            displayUnit = builder.comment("The unit of measurement to display for weight. (Internal logic remains in lbs)")
                    .defineEnum("displayUnit", WeightUnit.kg);

            customUnitLabel = builder.comment("If displayUnit is CUSTOM, what label should be shown? (e.g., 'oz', 'stones')")
                    .define("customUnitLabel", "kg");

            customUnitMultiplier = builder.comment("If displayUnit is CUSTOM, what should the internal lbs value be multiplied by.",
                    "For example, 1 lbs = 16 oz, so the multiplier for 'oz' to be displayed 'accurately' would be 16.0.")
                    .defineInRange("customUnitMultiplier", 1.0, 0.0001, 10000.0);

            builder.pop();

            builder.push("Auto Hud");
            autoHudEnable = builder.comment(" Enable hiding the stamina bar when full/not in use.").define("autoHudEnable", false);
            autoHudMode = builder.comment(" Animation type. FADE, SLIDE, or BOTH.").defineEnum("autoHudMode", AutoHudMode.FADE);
            autoHudSlideDir = builder.comment(" Direction the bar slides away to. UP, DOWN, LEFT, RIGHT.").defineEnum("autoHudSlideDir", AutoHudSlideDir.DOWN);
            autoHudEasing = builder.comment(" The mathematical curve used for the slide animation.",
                    " LINEAR: Constant speed from start to finish.",
                    " SMOOTHSTEP: Starts slow, speeds up in the middle, then ends slow.",
                    " EASE_OUT_SINE: Starts fast and then slows down to a stop.",
                    " EASE_OUT_EXPO: Starts extremely fast and then quickly slows down to a stop.").defineEnum("autoHudEasing", AutoHudEasing.EASE_OUT_SINE);
            autoHudFadeInSpeed = builder.comment(" Speed the bar fades in.").defineInRange("autoHudFadeInSpeed", 0.08, 0.01, 1.0);
            autoHudFadeOutSpeed = builder.comment(" Speed the bar fades out.").defineInRange("autoHudFadeOutSpeed", 0.05, 0.01, 1.0);
            autoHudSlideInSpeed = builder.comment(" Speed the bar slides in.").defineInRange("autoHudSlideInSpeed", 0.08, 0.01, 1.0);
            autoHudSlideOutSpeed = builder.comment(" Speed the bar slides out.").defineInRange("autoHudSlideOutSpeed", 0.05, 0.01, 1.0);
            autoHudSlideDistance = builder.comment(" How many pixels the bar moves when sliding out.").defineInRange("autoHudSlideDistance", 45, 0, 1000);
            autoHudLingerTime = builder.comment(" How long (in ticks) the bar stays visible after you stop using stamina (20 ticks = 1s).").defineInRange("autoHudLingerTime", 60, 0, 1200);
            autoHudThreshold = builder.comment(" Show the bar if stamina drops below this percentage (0.35 = 35%)").defineInRange("autoHudThreshold", 0.35, 0.0, 1.0);
            autoHudShowOnPenalties = builder.comment(" Force the bar to stay visible if you have penalties (hunger, poison, fatigue, weight).").define("autoHudShowOnPenalties", true);
            autoHudShowOnBonus = builder.comment("If true, the stamina HUD will automatically show when you have Bonus Stamina.").define("autoHudShowOnBonus", true);
            builder.pop();

            builder.push("Tooltips");
            enableTooltips = builder.comment("Enable stamina information on item tooltips.").define("enableTooltips", true);
            advancedTooltipsOnly = builder.comment("Only show tooltips when advanced tooltips are enabled (F3+H).").define("advancedTooltipsOnly", false);

            builder.push("Labels");
            labelWeight = builder.comment("Text shown before the Weight value.").define("labelWeight", "Weight: ");
            labelAttackCost = builder.comment("Text shown before the Attack Cost value.").define("labelAttackCost", "Attack Cost: ");
            labelUseCost = builder.comment("Text shown before the Use Cost value.").define("labelUseCost", "Use Cost: ");
            labelTickCost = builder.comment("Text shown before the Tick/Active Cost value.").define("labelTickCost", "Active Cost: ");
            labelBlockCost = builder.comment("Text shown before the Block Cost value.").define("labelBlockCost", "Block Cost: ");
            labelMissCost = builder.comment("Text shown before the Missed Attack Cost value.").define("labelMissCost", "Miss Cost: ");
            labelInstant = builder.comment("Text shown before the Instant Stamina value.").define("labelInstant", "Restores: ");
            labelBonus = builder.comment("Text shown before the Bonus Stamina value.").define("labelBonus", "Bonus: ");
            labelRegen = builder.comment("Text shown before the Regen Modifier value.").define("labelRegen", "Regen: ");
            labelCures = builder.comment("Text shown before the Cures value.").define("labelCures", "Cures: ");
            builder.pop();

            customTooltips = builder.comment(
                    " Define multiple tooltips to display on items. (Order here dictates order shown in-game)",
                    " Format: 'CONTENT_TYPE;PLACEMENT;LABEL_COLOR;VALUE_COLOR'",
                    " ",
                    " Available Content: WEIGHT, ATTACK_COST, MISSED_ATTACK_COST, USE_COST, TICK_COST, BLOCK_COST,",
                    "                    INSTANT_STAMINA, BONUS_STAMINA, REGEN_MODIFIER, CURES",
                    " Available Placements: BOTTOM, BELOW_NAME",
                    " Colors: BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE",
                    " ",
                    " Note: You can press Enter to format this list vertically in this file!"
            ).defineList("customTooltips", java.util.Arrays.asList(
                    "WEIGHT;BOTTOM;DARK_GRAY;WHITE",
                    "ATTACK_COST;BOTTOM;DARK_GRAY;WHITE",
                    "BLOCK_COST;BOTTOM;DARK_GRAY;WHITE",
                    "USE_COST;BOTTOM;DARK_GRAY;WHITE",
                    "TICK_COST;BOTTOM;DARK_GRAY;WHITE",
                    "INSTANT_STAMINA;BOTTOM;DARK_GRAY;GREEN",
                    "BONUS_STAMINA;BOTTOM;DARK_GRAY;GOLD",
                    "REGEN_MODIFIER;BOTTOM;DARK_GRAY;YELLOW",
                    "CURES;BOTTOM;DARK_GRAY;WHITE"
            ), obj -> obj instanceof String);
            builder.pop();
        }
    }
}
