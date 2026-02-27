package com.peakstamina.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class StaminaConfig {

    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Client CLIENT;
    public static final ForgeConfigSpec CLIENT_SPEC;

    public enum SleepMode {
        DEFAULT,
        HARDCORE
    }

    static {
        final Pair<Common, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = commonSpecPair.getRight();
        COMMON = commonSpecPair.getLeft();

        final Pair<Client, ForgeConfigSpec> clientSpecPair = new ForgeConfigSpec.Builder().configure(Client::new);
        CLIENT_SPEC = clientSpecPair.getRight();
        CLIENT = clientSpecPair.getLeft();
    }

    public static class Common {

        public ForgeConfigSpec.BooleanValue enableStamina;
        public ForgeConfigSpec.BooleanValue disableInCreative;
        public ForgeConfigSpec.BooleanValue disableInSpectator;
        public ForgeConfigSpec.DoubleValue initialMaxStamina;

        public ForgeConfigSpec.DoubleValue depletionSprint;
        public ForgeConfigSpec.DoubleValue depletionJump;
        public ForgeConfigSpec.DoubleValue depletionAttack;
        public ForgeConfigSpec.DoubleValue depletionMissedAttack;
        public ForgeConfigSpec.DoubleValue depletionBlockBreak;
        public ForgeConfigSpec.DoubleValue depletionBlockPlace;
        public ForgeConfigSpec.DoubleValue depletionSwim;
        public ForgeConfigSpec.DoubleValue depletionClimb;
        public ForgeConfigSpec.IntValue itemInterruptionCooldown;

        public ForgeConfigSpec.DoubleValue recoveryPerTick;
        public ForgeConfigSpec.DoubleValue recoveryRestMult;
        public ForgeConfigSpec.DoubleValue recoveryClimbMult;
        public ForgeConfigSpec.DoubleValue recoveryWaterMult;
        public ForgeConfigSpec.IntValue recoveryDelay;

        public ForgeConfigSpec.DoubleValue minMaxStamina;
        public ForgeConfigSpec.DoubleValue fatigueThreshold;
        public ForgeConfigSpec.IntValue fatigueDurationToPenalty;
        public ForgeConfigSpec.IntValue penaltyRecoveryDelay;
        public ForgeConfigSpec.DoubleValue penaltyBaseRate;
        public ForgeConfigSpec.DoubleValue penaltyCurveFactor;
        public ForgeConfigSpec.DoubleValue penaltyDecayRate;
        public ForgeConfigSpec.DoubleValue penaltyBuildupRate;
        public ForgeConfigSpec.DoubleValue maxExertionPenalty;
        public ForgeConfigSpec.DoubleValue maxHungerPenalty;
        public ForgeConfigSpec.IntValue hungerPenaltyThreshold;
        public ForgeConfigSpec.IntValue penaltyReliefDuration;
        
        public ForgeConfigSpec.EnumValue<SleepMode> sleepMode;
        public ForgeConfigSpec.DoubleValue sleepFatigueReduction;

        public ForgeConfigSpec.BooleanValue enableExcessStaminaConversion;
        public ForgeConfigSpec.DoubleValue excessConversionRate;
        public ForgeConfigSpec.IntValue bonusStaminaDecayDelay;
        public ForgeConfigSpec.BooleanValue bonusDecayScalesWithAmount;
        public ForgeConfigSpec.DoubleValue bonusStaminaDecayRate;

        public ForgeConfigSpec.DoubleValue depletionElytra;
        public ForgeConfigSpec.BooleanValue disableElytraWhenExhausted;
        public ForgeConfigSpec.DoubleValue exhaustedElytraDrag;
        public ForgeConfigSpec.IntValue exhaustedElytraTickInterval;
        public ForgeConfigSpec.DoubleValue exhaustedElytraGravity;
        public ForgeConfigSpec.DoubleValue exhaustedElytraMinSpeed;

        public ForgeConfigSpec.BooleanValue enableWeightSystem;
        public ForgeConfigSpec.DoubleValue maxWeightPenaltyAmount;
        public ForgeConfigSpec.DoubleValue weightPenaltyThreshold;
        public ForgeConfigSpec.DoubleValue weightPenaltyLimit;
        public ForgeConfigSpec.DoubleValue autoWeightBase;
        public ForgeConfigSpec.IntValue maxWeightRecursionDepth;

        public ForgeConfigSpec.IntValue exhaustionCooldownDuration;
        public ForgeConfigSpec.DoubleValue exhaustedSpeedPenalty;

        public ForgeConfigSpec.DoubleValue maxPoisonPenalty;
        public ForgeConfigSpec.IntValue poisonDecayDelay;
        public ForgeConfigSpec.DoubleValue poisonDecayRate;


        public Common(ForgeConfigSpec.Builder builder) {
            initGeneral(builder);
            initRecovery(builder);
            initDepletion(builder);
            initFatigueAndLimits(builder);
            initBonusStamina(builder);
            initElytra(builder);
            initWeightSystem(builder);
            initExhaustion(builder);
            initConsumables(builder);
        }

        private void initGeneral(ForgeConfigSpec.Builder builder) {
            builder.push("General");
            enableStamina = builder.comment("Set to false to completely disable the stamina system").define("enableStamina", true);
            disableInCreative = builder.comment("If true, players in Creative mode will not consume or use the stamina system.").define("disableInCreative", true);
            disableInSpectator = builder.comment("If true, players in Spectator mode will not consume or use the stamina system.").define("disableInSpectator", true);
            initialMaxStamina = builder.comment("Initial Max Stamina value for players.").defineInRange("initialMaxStamina", 100.0, 1.0, 10000.0);
            builder.pop();
        }

        private void initRecovery(ForgeConfigSpec.Builder builder) {
            builder.push("Recovery Settings");
            recoveryPerTick = builder.comment("Stamina recovered per tick").defineInRange("recoveryPerTick", 0.45, 0.0, 100.0);
            recoveryRestMult = builder.comment("Multiplier for recovery when standing completely still").defineInRange("recoveryRestMult", 1.45, 1.0, 10.0);
            recoveryClimbMult = builder.comment("Multiplier for recovery when hanging on a ladder/vine (slow climbing or not moving)").defineInRange("recoveryClimbMult", 0.4, 0.0, 10.0);
            recoveryWaterMult = builder.comment("Multiplier for recovery while inside water").defineInRange("recoveryWaterMult", 0.3, 0.0, 10.0);
            recoveryDelay = builder.comment("Ticks before stamina starts regenerating after action (20 ticks = 1 sec)").defineInRange("recoveryDelay", 50, 0, 2000);
            builder.pop();
        }

        private void initDepletion(ForgeConfigSpec.Builder builder) {
            builder.push("Depletion Rates");
            depletionSprint = builder.comment("Stamina drained per tick while sprinting. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionSprint", 0.15, -100.0, 100.0);
            depletionJump = builder.comment("Stamina drained per jump. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionJump", 0.85, -100.0, 100.0);
            depletionAttack = builder.comment("Stamina drained per attack. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionAttack", 3.45, -100.0, 100.0);
            depletionMissedAttack = builder.comment("Stamina drained when swinging at the air (missing an attack). Set to 0.0 to disable.").defineInRange("depletionMissedAttack", 1.0, 0.0, 100.0); 
            depletionBlockBreak = builder.comment("Stamina drained per block broken. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionBlockBreak", 1.1, -100.0, 100.0);
            depletionBlockPlace = builder.comment("Stamina drained per block placed. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionBlockPlace", 0.7, -100.0, 100.0);
            depletionSwim = builder.comment("Stamina drained per tick while swimming. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionSwim", 0.05, -100.0, 100.0);
            depletionClimb = builder.comment("Stamina drained per tick while climbing. Set to 0.0 to disable. Set to negative to gain stamina").defineInRange("depletionClimb", 0.7, -100.0, 100.0);
            itemInterruptionCooldown = builder.comment("", "Ticks to disable an item if you run out of stamina while using it.").defineInRange("itemInterruptionCooldown", 120, 0, 1200);
            builder.pop();
        }

        private void initFatigueAndLimits(ForgeConfigSpec.Builder builder) {
            builder.push("Fatigue & Limits");
            fatigueDurationToPenalty = builder.comment("Ticks spent in critical stamina (red zone) before fatigue penalty begins (180 ticks = 9s)").defineInRange("fatigueDurationToPenalty", 180, 0, 10000);
            minMaxStamina = builder.comment("The absolute floor for Max Stamina (Stamina bar cannot shrink smaller than this)").defineInRange("minMaxStamina", 10.0, 1.0, 100.0);
            fatigueThreshold = builder.comment("Percentage of Max Stamina where fatigue penalty starts (0.25 = 25%)").defineInRange("fatigueThreshold", 0.25, 0.0, 1.0);
            penaltyRecoveryDelay = builder.comment("Ticks to wait after leaving red zone before penalty recovers").defineInRange("penaltyRecoveryDelay", 100, 0, 2000);

            penaltyBaseRate = builder.comment("", "", "Base rate for exponential penalty increase").defineInRange("penaltyBaseRate", 0.02, 0.0, 10.0);
            penaltyCurveFactor = builder.comment("Divisor for exponential curve (Lower = Steeper curve)").defineInRange("penaltyCurveFactor", 150.0, 1.0, 1000.0);
            penaltyDecayRate = builder.comment("How much penalty recovers per tick when conditions improve (0.05 = 1.0 penalty per second )").defineInRange("penaltyDecayRate", 0.05, 0.0, 100.0);
            penaltyBuildupRate = builder.comment("How much penalty accumulates per tick when conditions worsen (0.1 = 2.0 penalty per second)").defineInRange("penaltyBuildupRate", 0.1, 0.0, 100.0);

            maxExertionPenalty = builder.comment("", "", "Maximum reduction to Max Stamina caused by physical exhaustion").defineInRange("maxExertionPenalty", 30.0, 0.0, 100.0);
            maxHungerPenalty = builder.comment("Maximum reduction to Max Stamina caused by starvation").defineInRange("maxHungerPenalty", 30.0, 0.0, 100.0);
            hungerPenaltyThreshold = builder.comment("Food level at which stamina penalty begins (6 = 3 shanks)").defineInRange("hungerPenaltyThreshold", 6, 0, 20);

            builder.push("Sleep Mechanics");
            sleepMode = builder.comment("DEFAULT: Fatigue decays over time, sleeping, and eating consumables reduces it.",
                    "HARDCORE: Fatigue doesn't decay naturally. You must sleep or eat food that reduces fatigue.")
                    .defineEnum("sleepMode", SleepMode.DEFAULT);
            sleepFatigueReduction = builder.comment("Flat amount of fatigue penalty to remove after a successful sleep.",
                    "Set to a high number (e.g. 100.0) to fully clear it.")
                    .defineInRange("sleepFatigueReduction", 20.0, 0.0, 1000.0);
            builder.pop();
            builder.pop();
        }

        private void initBonusStamina(ForgeConfigSpec.Builder builder) {
            builder.push("Bonus Stamina System");
            enableExcessStaminaConversion = builder.comment("If true, restoring stamina (INSTANT) past the Max Stamina limit will convert the excess into Bonus Stamina.").define("enableExcessStaminaConversion", true);
            excessConversionRate = builder.comment("The percentage of excess normal stamina that becomes bonus stamina.", 
                    "1.0 = 100% conversion (10 excess -> 10 bonus).", 
                    "0.5 = 50% conversion (10 excess -> 5 bonus).")
                    .defineInRange("excessConversionRate", 0.5, 0.0, 100.0);
            bonusStaminaDecayDelay = builder.comment("Ticks before Bonus Stamina starts decaying after being gained/used.", 
                    "Set to 0 to make it decay immediately and always.")
                    .defineInRange("bonusStaminaDecayDelay", 20, 0, 72000);
            bonusDecayScalesWithAmount = builder.comment("If true, the decay rate is treated as a percentage of current bonus stamina (Exponential decay).",
                    "This makes decay faster when you have lots of bonus, and slower when you have little.",
                    "If false, the decay rate is a flat amount subtracted per tick (Linear decay).")
                    .define("bonusDecayScalesWithAmount", true);
            bonusStaminaDecayRate = builder.comment("Amount of Bonus Stamina decaying per second.", 
                    "If Scaling is TRUE: 0.05 = 5% of current bonus lost per second.", 
                    "If Scaling is FALSE: 1.0 = 1.0 flat stamina lost per second.")
                    .defineInRange("bonusStaminaDecayRate", 0.05, 0.0, 100.0);
            builder.pop();
        }

        private void initElytra(ForgeConfigSpec.Builder builder) {
            builder.push("Elytra Mechanics");
            depletionElytra = builder.comment("Stamina drained per tick while flying with Elytra.").defineInRange("depletionElytra", 0.25, -100.0, 100.0);
            disableElytraWhenExhausted = builder.comment("If true, players will experience stalling (drag increases, lift decreases) when stamina hits 0.",
                    "If false, players can continue flying normally even with 0 stamina.")
                    .define("disableElytraWhenExhausted", true);
            exhaustedElytraDrag = builder.comment("How much horizontal speed is retained every X ticks when stalling (0.9 = 10% speed loss per tick). Lower = faster stop. (note that if the speed retainment is too high, the player will never fall out of the sky)")
                    .defineInRange("exhaustedElytraDrag", 0.86, 0.0, 1.0);
            exhaustedElytraTickInterval = builder.comment("How often (in ticks) the drag physics are applied (I suggest changing this over elytra speed retainment). Higher = less frequent slowdown and longer gliding. 1 = Every tick.")
                    .defineInRange("exhaustedElytraTickInterval", 3, 1, 100);
            exhaustedElytraGravity = builder.comment("Extra vertical gravity applied per tick when stalling. (Negative value pulls down).")
                    .defineInRange("exhaustedElytraGravity", -0.025, -10.0, 0.0);
            exhaustedElytraMinSpeed = builder.comment("Speed threshold below which the Elytra will force-close and drop the player.")
                    .defineInRange("exhaustedElytraMinSpeed", 0.5, 0.0, 5.0);
            builder.pop();
        }

        private void initWeightSystem(ForgeConfigSpec.Builder builder) {
            builder.push("Weight System");
            enableWeightSystem = builder.comment("Enable the weight calculation system.").define("enableWeightSystem", true);
            weightPenaltyThreshold = builder.comment("Weight at which the penalty begins to apply (0% penalty).").defineInRange("weightPenaltyThreshold", 125, 0.0, 10000.0);
            weightPenaltyLimit = builder.comment("Weight at which the penalty reaches maximum (100% penalty).").defineInRange("weightPenaltyLimit", 400.0, 1.0, 10000.0);
            maxWeightPenaltyAmount = builder.comment("Maximum amount of Max Stamina removed when at full weight limit.").defineInRange("maxWeightPenaltyAmount", 40.0, 0.0, 100.0);
            maxWeightRecursionDepth = builder.comment("Maximum depth for recursive weight calculation (Backpacks inside backpacks). Higher values may cause lag.").defineInRange("maxWeightRecursionDepth", 3, 0, 10);
            autoWeightBase = builder.comment("The 'Auto-Weigher' (Stack Size Heuristic).",
                    "Used for ANY item that is not explicitly listed in the lists config.",
                    "Formula: Weight = (Base / MaxStackSize) * Count")
                    .defineInRange("autoWeightBase", 10, 0.0, 1000.0);
            builder.pop();
        }

        private void initExhaustion(ForgeConfigSpec.Builder builder) {
            builder.push("Exhaustion Penalties");
            exhaustionCooldownDuration = builder.comment("Ticks the penalties persist after stamina regenerates above 0 (0 to disable)").defineInRange("exhaustionCooldownDuration", 60, 0, 12000);
            exhaustedSpeedPenalty = builder.comment("", "Movement speed multiplier when exhausted sprinting (e.g. -0.5 is 50% slower, -0.9 is 90% slower). Set to 0.0 to disable sprinting speed penalty.")
                    .defineInRange("exhaustedSpeedPenalty", -0.65, -1.0, 0.0);
            builder.pop();
        }

        private void initConsumables(ForgeConfigSpec.Builder builder) {
            builder.push("Consumables");
            penaltyReliefDuration = builder.comment("", "Duration (in seconds) for the penalty resistance buff applied by penalty-reducing items.").defineInRange("penaltyReliefDuration", 25, 0, 600);
            builder.push("Food Poisoning");
            maxPoisonPenalty = builder.comment("Maximum reduction to Max Stamina caused by food poisoning (Flat value, e.g. 40.0 = 40% of default bar)").defineInRange("maxPoisonPenalty", 40.0, 0.0, 100.0);
            poisonDecayDelay = builder.comment("Seconds to wait after eating bad food before poison penalty starts decaying").defineInRange("poisonDecayDelay", 45, 0, 600);
            poisonDecayRate = builder.comment("How much poison penalty recovers per tick after the delay (Flat value)").defineInRange("poisonDecayRate", 0.05, 0.0, 100.0);
            builder.pop();
            builder.pop();
        }
    }

    public static class Client {
        public final ForgeConfigSpec.IntValue barXOffset;
        public final ForgeConfigSpec.IntValue barYOffset;
        public final ForgeConfigSpec.IntValue barWidth;
        public final ForgeConfigSpec.IntValue barHeight;
        public final ForgeConfigSpec.IntValue colorBackground;
        public final ForgeConfigSpec.IntValue colorSafe;
        public final ForgeConfigSpec.IntValue colorCritical;
        public final ForgeConfigSpec.IntValue colorTireless;
        public final ForgeConfigSpec.IntValue colorStripes;
        public final ForgeConfigSpec.IntValue colorPenaltyHunger;
        public final ForgeConfigSpec.IntValue colorPenaltyPoison;
        public final ForgeConfigSpec.BooleanValue showIcons;
        public final ForgeConfigSpec.IntValue colorBonusTop;
        public final ForgeConfigSpec.IntValue colorBonusBottom;
        public final ForgeConfigSpec.IntValue colorBonusHighlight;
        public final ForgeConfigSpec.IntValue bonusHighlightAlpha;
        public final ForgeConfigSpec.IntValue colorPenaltyWeight;

        public Client(ForgeConfigSpec.Builder builder) {
            builder.push("HUD Layout");
            barXOffset = builder.comment("Horizontal offset from center").defineInRange("barXOffset", 0, -1000, 1000);
            barYOffset = builder.comment("Vertical offset from bottom").defineInRange("barYOffset", 24, 0, 1000);
            barWidth = builder.comment("Width of the bar in pixels").defineInRange("barWidth", 180, 1, 1000);
            barHeight = builder.comment("Height of the bar in pixels").defineInRange("barHeight", 2, 1, 100);
            showIcons = builder.comment("Whether to render text/emoji icons on the stamina bar penalty zones.").define("showIcons", true);
            builder.pop();

            builder.push("Colors");
            colorBackground = builder.defineInRange("colorBackground", 2236962, 0, 16777215);
            colorSafe = builder.defineInRange("colorSafe", 65280, 0, 16777215);
            colorCritical = builder.defineInRange("colorCritical", 16711680, 0, 16777215);
            colorTireless = builder.defineInRange("colorTireless", 65450, 0, 16777215);
            colorStripes = builder.defineInRange("colorStripes", 16711680, 0, 16777215);
            colorPenaltyHunger = builder.comment("Color for Hunger penalty stripes").defineInRange("colorPenaltyHunger", 16763904, 0, 16777215);
            colorPenaltyPoison = builder.comment("Color for Food Poisoning penalty stripes").defineInRange("colorPenaltyPoison", 11141375, 0, 16777215);
            colorPenaltyWeight = builder.comment("color for weight stripes").defineInRange("colorPenaltyWeight", 5592405, 0, 16777215);
            builder.pop();

            builder.push("Bonus Stamina Bar");
            colorBonusTop = builder.comment("Top gradient color for Bonus Stamina (RGB). Default: Gold").defineInRange("colorBonusTop", 16766720, 0, 16777215); 
            colorBonusBottom = builder.comment("Bottom gradient color for Bonus Stamina (RGB). Default: Dark Orange").defineInRange("colorBonusBottom", 16747520, 0, 16777215);
            colorBonusHighlight = builder.comment("Color of the highlight sheen (RGB). Default: White").defineInRange("colorBonusHighlight", 16777215, 0, 16777215);
            bonusHighlightAlpha = builder.comment("Opacity of the highlight sheen (0-255). 0 = Invisible, 255 = Solid. Default: 128 (Semi-transparent)").defineInRange("bonusHighlightAlpha", 128, 0, 255);
            builder.pop();
        }
    }
}