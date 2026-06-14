package com.peakstamina.client.gui;

import com.peakstamina.client.gui.config.UniversalConfigUI;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaConfig.Client.WeightUnit;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.config.ExperimentalConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PeakConfigMenu {

    public static Screen createScreen(Screen parent) {
        return new UniversalConfigUI.Builder(Component.literal("Peak Stamina Settings"), parent)
            .withSpec(StaminaConfig.COMMON_SPEC)
            .withSpec(StaminaConfig.CLIENT_SPEC)
            .withSpec(StaminaLists.LISTS_SPEC)
            .withSpec(ExperimentalConfig.EXPERIMENTAL_SPEC)

            // ==========================================
            //                 CORE TAB
            // ==========================================
            .beginSection("Core", "General", "Baseline toggles for the entire mod mechanics.")
            .addBoolean("Core", "Enable Stamina", "Set to false to completely disable the stamina system", 0, StaminaConfig.COMMON.enableStamina)
            .addBoolean("Core", "Disable in Creative", "If true, players in Creative mode will not consume or use the stamina system.", 0, StaminaConfig.COMMON.disableInCreative)
            .addBoolean("Core", "Disable in Spectator", "If true, players in Spectator mode will not consume or use the stamina system.", 0, StaminaConfig.COMMON.disableInSpectator)
            .addNumber("Core", "Initial Max Stamina", "Initial Max Stamina value for players.", 1, StaminaConfig.COMMON.initialMaxStamina)
            
            .addBoolean("Core", "Enable Slow Climb", "Enable the slow climb mechanic.", 0, StaminaConfig.COMMON.enableSlowClimb)
            .pushDependency(StaminaConfig.COMMON.enableSlowClimb, "Enable Slow Climb")
            .addNumber("Core", "Slow Climb Speed", "Base movement speed multiplier when slow climbing (sneaking on ladders/vines).", 0, StaminaConfig.COMMON.slowClimbSpeed)
            .popDependency()

            .beginSection("Core", "Recovery & Sleep", "How quickly stamina naturally regenerates and clears fatigue.")
            .addNumber("Core", "Recovery Per Tick", "Stamina recovered per tick", 0, StaminaConfig.COMMON.recoveryPerTick)
            .addNumber("Core", "Recovery Delay", "Ticks before stamina starts regenerating after action (20 ticks = 1 sec)", 0, StaminaConfig.COMMON.recoveryDelay)
            .addNumber("Core", "Rest Multiplier", "Multiplier for recovery when standing completely still", 0, StaminaConfig.COMMON.recoveryRestMult)
            .addNumber("Core", "Climb Multiplier", "Multiplier for recovery when hanging on a ladder/vine (slow climbing or not moving)", 0, StaminaConfig.COMMON.recoveryClimbMult)
            .addNumber("Core", "Water Multiplier", "Multiplier for recovery while inside water", 0, StaminaConfig.COMMON.recoveryWaterMult)
            .addEnum("Core", "Sleep Mode", "DEFAULT: Fatigue decays over time, sleeping, and eating consumables reduces it.\nHARDCORE: Fatigue doesn't decay naturally. You must sleep or eat food that reduces fatigue.", 0, StaminaConfig.COMMON.sleepMode, StaminaConfig.SleepMode.class)
            .addNumber("Core", "Sleep Fatigue Reduction", "Flat amount of fatigue penalty to remove after a successful sleep.", 0, StaminaConfig.COMMON.sleepFatigueReduction)

            .beginSection("Core", "Depletion Rates", "Stamina drain per action. Set to 0.0 to disable. Negative values RECOVER stamina.")
            .addNumber("Core", "Sprint Drain", "Stamina drained per tick while sprinting.", 0, StaminaConfig.COMMON.depletionSprint)
            .addNumber("Core", "Jump Drain", "Stamina drained per jump.", 0, StaminaConfig.COMMON.depletionJump)
            .addNumber("Core", "Swim Drain", "Stamina drained per tick while swimming.", 0, StaminaConfig.COMMON.depletionSwim)
            .addNumber("Core", "Climb Drain", "Stamina drained per tick while actively climbing up.", 0, StaminaConfig.COMMON.depletionClimb)
            .addNumber("Core", "Elytra Drain", "Stamina drained per tick while flying with Elytra.", 0, StaminaConfig.COMMON.depletionElytra)
            .addNumber("Core", "Block Break Drain", "Stamina drained per block broken.", 0, StaminaConfig.COMMON.depletionBlockBreak)
            .addNumber("Core", "Block Place Drain", "Stamina drained per block placed.", 0, StaminaConfig.COMMON.depletionBlockPlace)
            .addNumber("Core", "Attack Drain", "Base stamina drained per attack.", 0, StaminaConfig.COMMON.depletionAttack)
            .addNumber("Core", "Missed Attack Drain", "Stamina drained when swinging at the air (missing an attack).", 0, StaminaConfig.COMMON.depletionMissedAttack)

            .beginSection("Core", "Combat Scaling", "How weapon heaviness impacts stamina cost on successful hits.")
            .addBoolean("Core", "Cost Scales With Weight", "If true, stamina drained per attack is modified by the weapon's weight.", 0, StaminaConfig.COMMON.attackCostScalesWithWeight)
            .pushDependency(StaminaConfig.COMMON.attackCostScalesWithWeight, "Cost Scales With Weight")
            .addNumber("Core", "Weight Normalizer", "Baseline weapon weight for exactly 1.0x multiplier. Default 3.0 (Stone Sword)", 0, StaminaConfig.COMMON.attackWeightNormalizer)
            .addNumber("Core", "Scale Factor", "How intensely weight affects the attack cost. 1.0 = direct scaling, 2.0 = double impact.", 0, StaminaConfig.COMMON.attackWeightScaleFactor)
            .addNumber("Core", "Min Multiplier", "Minimum possible stamina cost multiplier from weight scaling.", 0, StaminaConfig.COMMON.attackWeightMinMultiplier)
            .addNumber("Core", "Max Multiplier", "Maximum possible stamina cost multiplier from weight scaling.", 0, StaminaConfig.COMMON.attackWeightMaxMultiplier)
            .popDependency()

            .beginSection("Core", "Missed Attack Scaling", "How weapon heaviness impacts stamina cost when hitting nothing.")
            .addBoolean("Core", "Miss Scales With Weight", "If true, missed attacks scale heavily with the weapon's weight.", 0, StaminaConfig.COMMON.missedAttackCostScalesWithWeight)
            .pushDependency(StaminaConfig.COMMON.missedAttackCostScalesWithWeight, "Miss Scales With Weight")
            .addNumber("Core", "Miss Normalizer", "Baseline weight for a 1.0x stamina cost multiplier for missed attacks.", 0, StaminaConfig.COMMON.missedAttackWeightNormalizer)
            .addNumber("Core", "Miss Scale Factor", "How intensely weight affects the missed attack cost.", 0, StaminaConfig.COMMON.missedAttackWeightScaleFactor)
            .addNumber("Core", "Miss Min Multiplier", "Minimum possible cost multiplier for missed attacks.", 0, StaminaConfig.COMMON.missedAttackWeightMinMultiplier)
            .addNumber("Core", "Miss Max Multiplier", "Maximum possible cost multiplier for missed attacks.", 0, StaminaConfig.COMMON.missedAttackWeightMaxMultiplier)
            .popDependency()

            .beginSection("Core", "Custom Action Costs", "Override stamina costs for specific items or tag groups.")
            .addNumber("Core", "Interruption Cooldown", "Ticks to disable an item if you run out of stamina while using it.", 0, StaminaConfig.COMMON.itemInterruptionCooldown)
            .addStringList("Core", "Custom Item Costs", 
                "Custom Stamina Costs for Items. Priority 1\n" +
                "Note: Arguments after the Base Cost are optional, but you can use them to chain multiple actions infinitely onto the same item!\n" +
                "Format: modid:item_name;action;cost;...\n" +
                "@SUGGEST[0]: Item ID | ITEM\n" +
                "@SUGGEST[1]: Action 1 | ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)\n" +
                "@SUGGEST[2]: Base Cost 1 | FLOAT\n" +
                "@SUGGEST[*]: DYNAMIC(-2:TICK=Action {C}#-2:USE=Action {C}#-2:USE_ON_BLOCK=Action {C}#-3:BLOCK=Action {C}#-1:TICK=Cost {C}#-1:USE=Cost {C}#-1:USE_ON_BLOCK=Cost {C}#-1:BLOCK=Base Cost {C}#-2:BLOCK=Dmg Mult {C}#ANY=[OPTIONAL]) // DYNAMIC(-2:TICK=Action#-2:USE=Action#-2:USE_ON_BLOCK=Action#-3:BLOCK=Action#-1:TICK=Cost#-1:USE=Cost#-1:USE_ON_BLOCK=Cost#-1:BLOCK=Base Cost#-2:BLOCK=Multiplier#ANY=Add Action) | DYNAMIC(-2:TICK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-2:USE=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-2:USE_ON_BLOCK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-3:BLOCK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#ANY=FLOAT)\n" +
                "| Type | Argument Count | Description |\n" +
                "|---|---|---|\n" +
                "| TICK | 1 Arg: Cost | Cost per tick while using |\n" +
                "| USE | 1 Arg: Cost | Cost on right-click |\n" +
                "| BLOCK | 2 Args: BaseCost; DmgMult | Cost when blocking damage |\n" +
                "| USE_ON_BLOCK | 1 Arg: Cost | Cost on right-click block |\n" +
                "\n" +
                "Examples:\n" +
                " minecraft:shield;TICK;0.2;BLOCK;2.0;0.8\n" +
                " minecraft:bow;TICK;1.0\n" +
                " minecraft:iron_axe;USE_ON_BLOCK;5.0\n" +
                "Note: Negative cost values will restore stamina instead of draining it.", 0, StaminaLists.LISTS.itemCosts)
            .addStringList("Core", "Item Tag Costs", 
                "Custom Stamina Costs for Item Tags (Categories). Priority 2\n" +
                "Works exactly like itemCosts but applies to any item with the specified Tag.\n" +
                "Priority: Specific items in 'itemCosts' will override these tag settings.\n" +
                "Note: Arguments after the Base Cost are optional, but you can chain multiple actions infinitely.\n" +
                "Format: tag_id;action;cost;...\n" +
                "@SUGGEST[0]: Tag ID | TAG\n" +
                "@SUGGEST[1]: Action 1 | ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)\n" +
                "@SUGGEST[2]: Base Cost 1 | FLOAT\n" +
                "@SUGGEST[*]: DYNAMIC(-2:TICK=Action {C}#-2:USE=Action {C}#-2:USE_ON_BLOCK=Action {C}#-3:BLOCK=Action {C}#-1:TICK=Cost {C}#-1:USE=Cost {C}#-1:USE_ON_BLOCK=Cost {C}#-1:BLOCK=Base Cost {C}#-2:BLOCK=Dmg Mult {C}#ANY=[OPTIONAL]) // DYNAMIC(-2:TICK=Action#-2:USE=Action#-2:USE_ON_BLOCK=Action#-3:BLOCK=Action#-1:TICK=Cost#-1:USE=Cost#-1:USE_ON_BLOCK=Cost#-1:BLOCK=Base Cost#-2:BLOCK=Multiplier#ANY=Add Action) | DYNAMIC(-2:TICK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-2:USE=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-2:USE_ON_BLOCK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#-3:BLOCK=ENUM(TICK : While using, USE : On right click, BLOCK : Blocking damage, USE_ON_BLOCK : Right click block)#ANY=FLOAT)\n" +
                "Example: forge:tools/bows;TICK;0.5", 0, StaminaLists.LISTS.itemCostTags)

            .beginSection("Core", "Elytra", "Elytra stall mechanics.")
            .addBoolean("Core", "Stall When Exhausted", "If true, players will stall (drag increases, lift decreases) when stamina hits 0.", 0, StaminaConfig.COMMON.disableElytraWhenExhausted)
            .pushDependency(StaminaConfig.COMMON.disableElytraWhenExhausted, "Stall When Exhausted")
            .addNumber("Core", "Exhausted Drag", "How much horizontal speed is retained every X ticks when stalling (0.9 = 10% speed loss per tick).", 0, StaminaConfig.COMMON.exhaustedElytraDrag)
            .addNumber("Core", "Exhausted Gravity", "Extra vertical gravity applied per tick when stalling.", 0, StaminaConfig.COMMON.exhaustedElytraGravity)
            .addNumber("Core", "Exhausted Tick Interval", "How often (in ticks) the drag physics are applied. 1 = Every tick.", 0, StaminaConfig.COMMON.exhaustedElytraTickInterval)
            .addNumber("Core", "Exhausted Min Speed", "Speed threshold below which the Elytra will force-close and drop the player.", 0, StaminaConfig.COMMON.exhaustedElytraMinSpeed)
            .popDependency()

            .beginSection("Core", "Enchantments", "Configure the effectiveness of stamina enchantments.")
            .addNumber("Core", "Lightweight I", "Weight reduction multiplier for Lightweight I (e.g. 0.25 = 25% reduction)", 0, StaminaConfig.COMMON.lightweightLvl1)
            .addNumber("Core", "Lightweight II", "Weight reduction multiplier for Lightweight II", 0, StaminaConfig.COMMON.lightweightLvl2)
            .addNumber("Core", "Lightweight III", "Weight reduction multiplier for Lightweight III", 0, StaminaConfig.COMMON.lightweightLvl3)
            .addNumber("Core", "Tireless I", "Stamina cost reduction for Tireless I (e.g. 0.20 = 20% reduction)", 0, StaminaConfig.COMMON.tirelessLvl1)
            .addNumber("Core", "Tireless II", "Stamina cost reduction for Tireless II", 0, StaminaConfig.COMMON.tirelessLvl2)
            .addNumber("Core", "Tireless III", "Stamina cost reduction for Tireless III", 0, StaminaConfig.COMMON.tirelessLvl3)


            // ==========================================
            //                 SURVIVAL TAB
            // ==========================================
            .beginSection("Survival", "Fatigue Limits", "How the maximum stamina capacity behaves.")
            .addNumber("Survival", "Minimum Max Stamina", "The absolute floor for Max Stamina (Stamina bar cannot shrink smaller than this)", 0, StaminaConfig.COMMON.minMaxStamina)
            .addNumber("Survival", "Fatigue Threshold", "Percentage of Max Stamina where fatigue penalty starts (0.25 = 25%)", 0, StaminaConfig.COMMON.fatigueThreshold)
            .addNumber("Survival", "Duration To Penalty", "Ticks spent in critical stamina (red zone) before fatigue penalty begins.", 0, StaminaConfig.COMMON.fatigueDurationToPenalty)
            .addNumber("Survival", "Max Exertion Penalty", "Maximum reduction to Max Stamina caused by physical exhaustion", 0, StaminaConfig.COMMON.maxExertionPenalty)
            .addNumber("Survival", "Max Hunger Penalty", "Maximum reduction to Max Stamina caused by starvation", 0, StaminaConfig.COMMON.maxHungerPenalty)
            .addNumber("Survival", "Hunger Penalty Threshold", "Food level at which stamina penalty begins (6 = 3 shanks)", 0, StaminaConfig.COMMON.hungerPenaltyThreshold)

            .beginSection("Survival", "Exhaustion", "Define how exhaustion penalty behaves (Applies when low stamina)).")
            .addNumber("Survival", "Exhaustion Penalty Recovery Delay", "Ticks to wait after leaving red zone before exhaustion penalty recovers", 0, StaminaConfig.COMMON.penaltyRecoveryDelay)
            .addNumber("Survival", "Exhaustion Penalty Base Rate", "Base rate for exponential exhaustion penalty increase", 0, StaminaConfig.COMMON.penaltyBaseRate)
            .addNumber("Survival", "Exhaustion Penalty Curve Factor", "Divisor for exponential curve (Lower = Steeper curve)", 0, StaminaConfig.COMMON.penaltyCurveFactor)
            
            .beginSection("Survival", "Penalties", "Math defining how quickly max stamina drops or recovers from all penalties.")
            .addNumber("Survival", "Penalty Buildup Rate", "How much penalty accumulates per tick when conditions worsen.", 0, StaminaConfig.COMMON.penaltyBuildupRate)
            .addNumber("Survival", "Penalty Decay Rate", "How much penalty recovers per tick when conditions improve.", 0, StaminaConfig.COMMON.penaltyDecayRate)
            .addStringList("Survival", "Universal Penalties", 
                "Universal Compatibility with Scaling Penalties. This links external mod data to stamina.\n" +
                "Format: type;identifier;comparator;threshold;worst_value;max_penalty;color_int;icon_text\n" +
                "@SUGGEST[0]: Type | ENUM(NBT : Read Player Data, EFFECT : Read Status Effect)\n" +
                "@SUGGEST[1]: Key // DYNAMIC(0:EFFECT=Effect ID#0:NBT=NBT Path#0:ANY=Identifier) | DYNAMIC(0:EFFECT=EFFECT#0:NBT=ANY)\n" +
                "@SUGGEST[2]: Comparator // DYNAMIC(2:>=Scale Up#2:<=Scale Down#2:*=Multiply Up#2:*<=Multiply Down#2:!>=Instant Up#2:!<=Instant Down#2:!*=Instant Mult Up#2:!*<=Instant Mult Down#0:ANY=Logic) | ENUM(> : Scale Up, < : Scale Down, * : Multiply Up, *< : Multiply Down, !> : Instant Scale Up, !< : Instant Scale Down, !* : Instant Mult Up, !*< : Instant Mult Down)\n" +
                "@SUGGEST[3]: Threshold // DYNAMIC(0:EFFECT=Amplifier#0:NBT=Safe Value) | DYNAMIC(0:EFFECT=INT#ANY=FLOAT)\n" +
                "@SUGGEST[4]: WorstValue // DYNAMIC(2:*=Hard Cap#2:*<=Hard Cap#2:!*=Hard Cap#2:!*<=Hard Cap#0:ANY=Cap Value) | FLOAT\n" +
                "@SUGGEST[5]: MaxPenalty // DYNAMIC(2:*=Penalty per Unit#2:*<=Penalty per Unit#2:!*=Penalty per Unit#2:!*<=Penalty per Unit#0:ANY=Total Penalty) | FLOAT\n" +
                "@SUGGEST[6]: ColorInt | COLOR\n" +
                "@SUGGEST[7]: IconText | ICON\n" +
                "| Arg | Parameter | Description |\n" +
                "|---|---|---|\n" +
                "| 1 | Type | 'NBT' or 'EFFECT' |\n" +
                "| 2 | Key | The NBT path or Effect ID |\n" +
                "| 3 | Comparator | Logic for calculating penalty. See below. |\n" +
                "| 4 | Threshold | The safe value where penalty starts (0%) |\n" +
                "| 5 | WorstValue | SCALE mode: Value where penalty reaches 100%. MULTIPLIER: Hard Cap |\n" +
                "| 6 | MaxPenalty | SCALE mode: Total penalty. MULTIPLIER: Penalty per unit. |\n" +
                "| 7 | ColorInt | Decimal color code for overlay (e.g. 16711680) |\n" +
                "| 8 | IconText | Emoji to display on bar. Use 'none' to disable |\n" +
                "\n" +
                "| Mode | Symbols | Rule |\n" +
                "|---|---|---|\n" +
                "| SCALE UP | > | Penalty builds as value rises above Threshold. Cap at WorstValue. |\n" +
                "| SCALE DOWN | < | Penalty builds as value falls below Threshold. Cap at WorstValue. |\n" +
                "| MULTIPLIER UP | * | Each point ABOVE Threshold adds MaxPenalty to the total. |\n" +
                "| MULTIPLIER DOWN | *< | Each point BELOW Threshold adds MaxPenalty to the total. |\n" +
                "| INSTANT FLAG | ! | Add ! before ANY symbol to apply penalty instantly instead of gradually. |\n" +
                "\n" +
                "Examples:\n" +
                " NBT;thirstLevel;<;6;0;20.0;38143;💧\n" +
                " EFFECT;minecraft:wither;>;-1;3;40.0;3355443;💀\n" +
                "\n" +
                "--- ADVANCED NBT PATHS (Nested Tags & ForgeCaps) ---\n" +
                "Sometimes even with the correct path it may not work. In this scenario try adding 'ForgeCaps.' to the start. Also use '.' to look inside tags. For assistance refer to the wiki\n" +
                "Examples of using '.' to look inside tags:\n" +
                " ForgeCaps.legendarysurvivaloverhaul:thirst.hydrationLevel\n" +
                "   -> Open ForgeCaps ➡ open legendarysurvivaloverhaul:thirst ➡ grab hydrationLevel.", 0, StaminaLists.LISTS.universalPenalties)

            .beginSection("Survival", "Exhaustion Debuffs", "Debuffs for completely running out of stamina.")
            .addNumber("Survival", "Cooldown Duration", "Ticks the penalties persist after stamina regenerates above 0 (0 to disable)", 0, StaminaConfig.COMMON.exhaustionCooldownDuration)
            .addNumber("Survival", "Sprinting Speed Penalty", "Movement speed multiplier when exhausted sprinting (e.g. -0.5 is 50% slower).", 0, StaminaConfig.COMMON.exhaustedSpeedPenalty)
            .addStringList("Survival", "Custom Exhaustion Penalties", 
                "List of Attribute or Effect Penalties to apply when Stamina hits 0.\n" +
                "Format: type;value;operation_or_time\n" +
                "@SUGGEST[0]: Type // DYNAMIC(0:ATTRIBUTE=Attribute#0:EFFECT=Effect) | DYNAMIC(-1:ANY=ATTRIBUTE) \n" +
                "@SUGGEST[1]: Value/Amp // DYNAMIC(0:ATTRIBUTE=Amount#0:EFFECT=Amplifier#0:ANY=Value) | DYNAMIC(0:EFFECT=INT#ANY=FLOAT)\n" +
                "@SUGGEST[2]: Operation/Time // DYNAMIC(0:ATTRIBUTE=0=Add 1=Base 2=Tot#0:EFFECT=Duration (Ticks)#0:ANY=Time) | DYNAMIC(0:ATTRIBUTE=INT#0:EFFECT=INT#ANY=FLOAT)\n" +
                "| Type | Value / Amp | Operation / Time |\n" +
                "|---|---|---|\n" +
                "| ATTRIBUTE | Amount | Operation (0=ADD, 1=MULT_BASE, 2=MULT_TOTAL) |\n" +
                "| EFFECT | Amplifier | Duration (Ticks) |\n" +
                "\n" +
                "Example: minecraft:slowness;0;30\n" +
                "Note: The mod automatically refreshes the potion effect every 10 ticks (0.5 secs) anything above that value will be a lingering penalty after they regain enough stamina.", 0, StaminaLists.LISTS.customExhaustionPenalties)

            .beginSection("Survival", "Bonus Stamina", "Mechanics for overcharging the stamina bar.")
            .addBoolean("Survival", "Enable Excess Conversion", "Restoring stamina past the Max Stamina limit converts the excess into Bonus Stamina.", 0, StaminaConfig.COMMON.enableExcessStaminaConversion)
            .pushDependency(StaminaConfig.COMMON.enableExcessStaminaConversion, "Enable Excess Conversion")
            .addNumber("Survival", "Excess Conversion Rate", "Percentage of excess normal stamina that becomes bonus stamina (0.5 = 50%).", 0, StaminaConfig.COMMON.excessConversionRate)
            .popDependency()
            .addNumber("Survival", "Bonus Decay Delay", "Ticks before Bonus Stamina starts decaying after being gained/used.", 0, StaminaConfig.COMMON.bonusStaminaDecayDelay)
            .addBoolean("Survival", "Decay Scales With Amount", "True = Exponential percent decay. False = Linear flat decay.", 0, StaminaConfig.COMMON.bonusDecayScalesWithAmount)
            .addNumber("Survival", "Bonus Decay Rate", "Amount of Bonus Stamina decaying per second.", 0, StaminaConfig.COMMON.bonusStaminaDecayRate)
            .addBoolean("Survival", "Universal Buff Regen", "If false, Bonus Stamina from universal buffs pauses regeneration while the player is actively using stamina.", 0, StaminaConfig.COMMON.universalBuffRegenWhileActive)
            .addStringList("Survival", "Universal Buffs", 
                "Universal Compatibility for granting Bonus Stamina.\n" +
                "Format: type;identifier;mode;threshold;limit_or_cd;base_amt;burst_amt;factor\n" +
                "@SUGGEST[0]: Source Type | ENUM(NBT : Read Player Data, EFFECT : Read Status Effect)\n" +
                "@SUGGEST[1]: Identifier // DYNAMIC(0:EFFECT=Effect ID#0:NBT=NBT Path#0:ANY=Identifier) | DYNAMIC(0:EFFECT=EFFECT#0:NBT=ANY)\n" +
                "@SUGGEST[2]: Mode // DYNAMIC(2:PASSIVE_OVER=Regen Above#2:PASSIVE_UNDER=Regen Below#2:BURST_OVER=Instant Over#2:BURST_UNDER=Instant Under#2:BOTH_OVER=Burst + Regen Over#2:BOTH_UNDER=Burst + Regen Under#2:PASSIVE_OVER_MULTIPLIER=Scaled Regen#0:ANY=Action) | ENUM(PASSIVE_OVER : Regen Above Thresh, PASSIVE_UNDER : Regen Below Thresh, BURST_OVER : Instant Once, BURST_UNDER : Instant Once, BOTH_OVER : Burst then Regen, BOTH_UNDER : Burst then Regen, PASSIVE_OVER_MULTIPLIER : Regen scaled by amount)\n" +
                "@SUGGEST[3]: Threshold // DYNAMIC(0:EFFECT=Amplifier#0:NBT=Activation Value) | DYNAMIC(0:EFFECT=INT#ANY=FLOAT)\n" +
                "@SUGGEST[4]: Limit / CD // DYNAMIC(2:PASSIVE_OVER=Max Bonus#2:PASSIVE_UNDER=Max Bonus#2:BURST_OVER=Cooldown Ticks#2:BURST_UNDER=Cooldown Ticks#2:BOTH_OVER=Cooldown Ticks#2:BOTH_UNDER=Cooldown Ticks#2:PASSIVE_OVER_MULTIPLIER=Max Bonus#0:ANY=Limit) | FLOAT\n" +
                "@SUGGEST[5]: Base Amount | FLOAT\n" +
                "@SUGGEST[6]: Burst Amount | FLOAT\n" +
                "@SUGGEST[7]: Factor Mult | FLOAT\n" +
                "| Action Mode | Function |\n" +
                "|---|---|\n" +
                "| PASSIVE | Generates stamina per second while above/below Threshold |\n" +
                "| BURST | Instantly grants bonus stamina once when crossed |\n" +
                "| BOTH | Grants instant burst, then passive regen |\n" +
                "| MULTIPLIER | Multiply the amount per point past the threshold |\n" +
                "\n" +
                "Examples:\n" +
                " EFFECT;minecraft:regeneration;PASSIVE_OVER;-1;20.0;1.0\n" +
                " NBT;player_mana;BOTH_OVER;50;100.0;2.0;30.0", 0, StaminaLists.LISTS.universalBuffs)

            .beginSection("Survival", "Weight System", "Manage how inventory weight affects the player.")
            .addBoolean("Survival", "Enable Weight System", "Enable the weight calculation system.", 0, StaminaConfig.COMMON.enableWeightSystem)
            .pushDependency(StaminaConfig.COMMON.enableWeightSystem, "Enable Weight System")
            .addNumber("Survival", "Weight Threshold", "Weight at which the penalty begins to apply (0% penalty).", 0, StaminaConfig.COMMON.weightPenaltyThreshold)
            .addNumber("Survival", "Weight Max Limit", "Weight at which the penalty reaches maximum (100% penalty).", 0, StaminaConfig.COMMON.weightPenaltyLimit)
            .addNumber("Survival", "Max Weight Penalty", "Maximum amount of Max Stamina removed when at full weight limit.", 0, StaminaConfig.COMMON.maxWeightPenaltyAmount)
            .addStringList("Survival", "Custom Item Weights (Priority 1)", 
                "Priority 1: Explicit Item Weights.\n" +
                "These values override the Auto-Weigher. Use this for heavy equipment.\n" +
                "Format: modid:item_name;weight\n" +
                "@SUGGEST[0]: Item ID | ITEM\n" +
                "@SUGGEST[1]: Item Weight | FLOAT\n" +
                "Example: minecraft:netherite_chestplate;15.0\n" +
                "Note: This is the weight of every 1 item not stack of items.", 2, StaminaLists.LISTS.customItemWeights)
            .addStringList("Survival", "Custom Tag Weights (Priority 2)", 
                "Priority 2: Tag/Category Weights.\n" +
                "Used if the item is not in the explicit list above.\n" +
                "Format: tag_id;weight\n" +
                "@SUGGEST[0]: Forge Tag | ANY\n" +
                "@SUGGEST[1]: Tag Weight | FLOAT\n" +
                "Example: forge:obsidian;1.2", 2, StaminaLists.LISTS.customTagWeights)
            .addStringList("Survival", "NBT Weight Paths", 
                "Advanced NBT Weight Extraction for complex mod items (e.g. TACZ).\n" +
                "This list tells the weight system exactly where to look inside the NBT data to find that true ID.\n" +
                "Format: modid:item_name;nbt_path;fallback_weight;apply_fallback\n" +
                "@SUGGEST[0]: Base Item ID | ITEM\n" +
                "@SUGGEST[1]: NBT Path | ANY\n" +
                "@SUGGEST[2]: Fallback Wgt | FLOAT\n" +
                "@SUGGEST[3]: Apply Fallback | ENUM(true : Yes, false : No)\n" +
                "Examples:\n" +
                " tacz:ammo;AmmoId;0.05;true\n" +
                " tacz:modern_kinetic_gun;AttachmentSCOPE.tag.AttachmentId;0.5;false", 2, StaminaLists.LISTS.nbtWeightPaths)
            .addStringList("Survival", "Custom Container Paths", 
                "Custom Container NBT Paths (Backpacks, etc).\n" +
                "Use this for backpacks/containers that store items inside their NBT data.\n" +
                "Format: modid:item_name;items_path\n" +
                "@SUGGEST[0]: Container Item | ITEM\n" +
                "@SUGGEST[1]: Items Path | ANY\n" +
                "Examples:\n" +
                " minecraft:shulker_box;BlockEntityTag.Items\n" +
                " somemod:satchel;Inventory\n" +
                "Note: Vanilla Shulker Boxes and Bundles are already handled.", 2, StaminaLists.LISTS.customContainerPaths)
            .addNumber("Survival", "Auto-Weigher Base (Priority 3)", "The Auto Weigher (Stack Size Heuristic).\nUsed for any item that is not explicitly listed in the lists config.\nFormula: Weight = (Base / MaxStackSize) * Count", 0, StaminaConfig.COMMON.autoWeightBase)
            .addNumber("Survival", "Max Recursion Depth", "Maximum depth for recursive weight calculation (Backpacks inside backpacks). Higher values may cause lag.", 0, StaminaConfig.COMMON.maxWeightRecursionDepth)
            .popDependency()

            .beginSection("Survival", "Consumables", "Modifiers applied by eating or drinking food.")
            .addNumber("Survival", "Penalty Relief Duration", "Duration (in seconds) for the penalty resistance buff applied by penalty-reducing items.", 0, StaminaConfig.COMMON.penaltyReliefDuration)
            .addNumber("Survival", "Max Poison Penalty", "Maximum reduction to Max Stamina caused by food poisoning (Flat value, e.g. 40.0 = 40% of default bar)", 0, StaminaConfig.COMMON.maxPoisonPenalty)
            .addNumber("Survival", "Poison Decay Delay", "Seconds to wait after eating bad food before poison penalty starts decaying", 0, StaminaConfig.COMMON.poisonDecayDelay)
            .addNumber("Survival", "Poison Decay Rate", "How much poison penalty recovers per tick after the delay (Flat value)", 0, StaminaConfig.COMMON.poisonDecayRate)
            .addStringList("Survival", "Consumables Values", 
                "Attribute Modifiers applied when consuming an item.\n" +
                "Note: Arguments after Value 1 are optional. You can chain modifiers infinitely to make complex foods (e.g., golden apples cure poison AND grant bonus stamina AND give regen).\n" +
                "Format: modid:item_name;modifier_type;value;...\n" +
                "@SUGGEST[0]: Item ID | ITEM\n" +
                "@SUGGEST[1]: Mod Type 1 | ENUM(INSTANT : Restores Flat Stamina, BONUS : Grants Temp Max Stamina, REGEN : Modifies Regen Speed, POISON : Adds Food Poisoning, PENALTY : Resist Penalty Buildup, CURE : Removes Existing Penalties)\n" +
                "@SUGGEST[2]: DYNAMIC(1:INSTANT=Restore 1#1:BONUS=Bonus 1#1:REGEN=Speed 1#1:POISON=Poison 1#1:PENALTY=Resistance 1#1:CURE=Target 1#ANY=Value 1) // DYNAMIC(1:CURE=ENUM(FATIGUE : Fatigue, HUNGER : Hunger, POISON : Poison, WEIGHT : Weight, ALL : All Penalties)#ANY=Amount) | DYNAMIC(1:CURE=ENUM(FATIGUE : Fatigue, HUNGER : Hunger, POISON : Poison, WEIGHT : Weight, ALL : All Penalties)#ANY=FLOAT)\n" +
                "@SUGGEST[*]: DYNAMIC(-2:INSTANT=Mod {C}#-2:BONUS=Mod {C}#-2:POISON=Mod {C}#-2:PENALTY=Mod {C}#-3:REGEN=Mod {C}#-3:CURE=Mod {C}#-1:INSTANT=Restore {C}#-1:BONUS=Bonus {C}#-1:POISON=Poison {C}#-1:PENALTY=Resistance {C}#-1:REGEN=Speed {C}#-1:CURE=Target {C}#-2:REGEN=Duration {C}#-2:CURE=Amount {C}#ANY=[OPTIONAL]) // DYNAMIC(-2:INSTANT=Action#-2:BONUS=Action#-2:POISON=Action#-2:PENALTY=Action#-3:REGEN=Action#-3:CURE=Action#-1:INSTANT=Amount#-1:BONUS=Amount#-1:POISON=Amount#-1:PENALTY=Amount#-1:REGEN=Multiplier#-1:CURE=Penalty Target#-2:REGEN=Seconds#-2:CURE=Amount#ANY=Add Mod) | DYNAMIC(-2:INSTANT=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-2:BONUS=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-2:POISON=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-2:PENALTY=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-3:REGEN=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-3:CURE=ENUM(INSTANT : Restores Flat, BONUS : Temp Max, REGEN : Regen Speed, POISON : Food Poisoning, PENALTY : Resist Penalty, CURE : Cure Penalties)#-1:CURE=ENUM(FATIGUE : Fatigue, HUNGER : Hunger, POISON : Poison, WEIGHT : Weight, ALL : All Penalties)#ANY=FLOAT)\n" +
                "| Type | Scale | Arguments |\n" +
                "|---|---|---|\n" +
                "| INSTANT | FLAT | Amount to instantly restore |\n" +
                "| BONUS | FLAT | Temporary Bonus Stamina in a secondary bar |\n" +
                "| REGEN | PERCENT | Modifies regen speed (e.g. 0.2 = +20%); Seconds |\n" +
                "| POISON | FLAT | Adds X points of Food Poisoning penalty |\n" +
                "| PENALTY | PERCENT | Adds Resistance to penalty buildup |\n" +
                "| CURE | FLAT | Target (FATIGUE/HUNGER/POISON/WEIGHT/ALL); Amount |\n" +
                "\n" +
                "Examples:\n" +
                " minecraft:apple;INSTANT;10.0\n" +
                " minecraft:golden_apple;CURE;ALL;100.0;PENALTY;50.0;REGEN;0.25;60;BONUS;50", 0, StaminaLists.LISTS.consumableValues)
            .addStringList("Survival", "Infinite Stamina Effects", 
                "List of Status Effects that grant infinite stamina (No stamina depletion)\n" +
                "Format: modid:effect_name\n" +
                "@SUGGEST[0]: Status Effect | EFFECT\n" +
                "Example: minecraft:hero_of_the_village", 0, StaminaLists.LISTS.infiniteStaminaEffects)

            // ==========================================
            //                 COMPAT TAB
            // ==========================================
            .beginSection("Compat", "Parcool", "Costs and configs for Parcool.")
            .addStringList("Compat", "ParCool Action Costs", 
                "ParCool Action Stamina Costs\n" +
                "Note: You can chain the START cost and CONTINUE cost together.\n" +
                "Format: action_name;mode;cost;...\n" +
                "@SUGGEST[0]: Action Name | ANY\n" +
                "@SUGGEST[1]: Mode 1 | ENUM(START : One-time cost, CONTINUE : Cost per tick)\n" +
                "@SUGGEST[2]: Cost 1 | FLOAT\n" +
                "@SUGGEST[*]: DYNAMIC(-2:START=Mode {C}#-2:CONTINUE=Mode {C}#-1:START=Cost {C}#-1:CONTINUE=Cost {C}#ANY=[OPTIONAL]) // DYNAMIC(-2:START=Action#-2:CONTINUE=Action#-1:START=Cost#-1:CONTINUE=Cost#ANY=Add Mode) | DYNAMIC(-2:START=ENUM(START : One-time cost, CONTINUE : Cost per tick)#-2:CONTINUE=ENUM(START : One-time cost, CONTINUE : Cost per tick)#ANY=FLOAT)\n" +
                "| Type | Description |\n" +
                "|---|---|\n" +
                "| START | One-time cost when action begins |\n" +
                "| CONTINUE | Cost per tick while action is active |\n" +
                "\n" +
                "Examples:\n" +
                " WallRun;START;15.0;CONTINUE;0.5\n" +
                " Vault;START;8.0\n" +
                " Roll;START;5.0;CONTINUE;0.0\n" +
                "Note: Negative cost values will restore stamina instead of draining it.", 2, StaminaLists.LISTS.parCoolActionCosts)

            .beginSection("Compat", "Combat Roll", "Cost for Combat Roll.")
            .addNumber("Compat", "Combat Roll Cost", "How much stamina a Combat Roll costs.", 0, StaminaLists.LISTS.combatRollCost)
            
            .beginSection("Compat", "Shield Expansion", "Cost for parrying with Shield Expansion.")
            .addNumber("Compat", "Shield Expansion Parry Mult", "Multiplier for block cost when successfully parrying (0.0 = Free).", 0, StaminaLists.LISTS.shieldExpParryMult)
            .addNumber("Compat", "Shield Expansion Parry Bonus", "Amount of Bonus Stamina granted upon a successful parry.", 0, StaminaLists.LISTS.shieldExpParryBonus)

            .beginSection("Compat", "Wall-Jump TXF", "Cost for doing actions added from Wall-Jump TXF.")
            .addStringList("Compat", "Wall-Jump! TXF Action Costs", 
                "Wall-Jump TXF Action Stamina Costs\n" +
                "Note: You can chain the START cost and CONTINUE cost together.\n" +
                "Format: action_name;mode;cost;...\n" +
                "@SUGGEST[0]: Action Name | ENUM(WallJump : Kick off a wall, DoubleJump : Jump in mid-air, WallCling : Hold or slide down a wall, SpeedBoost : Speed boost, StepAssist : step up blocks)\n" +
                "@SUGGEST[1]: Mode 1 | ENUM(START : One-time cost, CONTINUE : Cost per tick)\n" +
                "@SUGGEST[2]: Cost 1 | FLOAT\n" +
                "@SUGGEST[*]: DYNAMIC(-2:START=Mode {C}#-2:CONTINUE=Mode {C}#-1:START=Cost {C}#-1:CONTINUE=Cost {C}#ANY=[OPTIONAL]) // DYNAMIC(-2:START=Action#-2:CONTINUE=Action#-1:START=Cost#-1:CONTINUE=Cost#ANY=Add Mode) | DYNAMIC(-2:START=ENUM(START : One-time cost, CONTINUE : Cost per tick)#-2:CONTINUE=ENUM(START : One-time cost, CONTINUE : Cost per tick)#ANY=FLOAT)\n" +
                "| Type | Description |\n" +
                "|---|---|\n" +
                "| START | One-time cost when action begins |\n" +
                "| CONTINUE | Cost per tick while action is active |\n" +
                "\n" +
                "Examples:\n" +
                " WallJump;START;10.0;CONTINUE;0.0\n" +
                " DoubleJump;START;15.0;CONTINUE;0.0\n" +
                " WallCling;START;0.0;CONTINUE;0.15\n" +
                "Note: Negative cost values will restore stamina instead of draining it.", 2, StaminaLists.LISTS.wallJumpActionCosts)
            .addNumber("Compat", "Speed Boost Lvl 1 Extra Drain", "Extra stamina drained per tick for Speed Boost Level 1.", 0, StaminaLists.LISTS.speedBoostExtraLvl1)
            .addNumber("Compat", "Speed Boost Lvl 2 Extra Drain", "Extra stamina drained per tick for Speed Boost Level 2.", 0, StaminaLists.LISTS.speedBoostExtraLvl2)
            .addNumber("Compat", "Speed Boost Lvl 3 Extra Drain", "Extra stamina drained per tick for Speed Boost Level 3.", 0, StaminaLists.LISTS.speedBoostExtraLvl3)
            .addBoolean("Compat", "Drop On Empty Cling", "If true, the player will be forced to let go of the wall if they run out of stamina.", 0, StaminaLists.LISTS.dropOnEmptyWallCling)

            // ==========================================
            //                 CLIENT TAB
            // ==========================================
            .beginSection("Client", "HUD Layout", "Configure dimensions, style, and positioning.")
            .addEnum("Client", "HUD Style", "The style of the HUD. BAR is horizontal, ICON is vertical.", 0, StaminaConfig.CLIENT.hudStyle, StaminaConfig.Client.HudStyle.class)
            .addBoolean("Client", "Show Icons", "Whether to render text/emoji icons on the stamina bar penalty zones.", 0, StaminaConfig.CLIENT.showIcons)
            .addNumber("Client", "Bar Width", "Width of the bar in pixels (Used for BAR style)", 0, StaminaConfig.CLIENT.barWidth)
            .addNumber("Client", "Bar Height", "Height of the bar in pixels (Used for BAR style)", 0, StaminaConfig.CLIENT.barHeight)
            .addNumber("Client", "Bar X Offset", "X offset for the Stamina HUD in BAR mode.", 0, StaminaConfig.CLIENT.barXOffset)
            .addNumber("Client", "Bar Y Offset", "Y offset for the Stamina HUD in BAR mode.", 0, StaminaConfig.CLIENT.barYOffset)
            .addNumber("Client", "Icon X Offset", "X offset for the Stamina HUD in ICON mode.", 0, StaminaConfig.CLIENT.iconXOffset)
            .addNumber("Client", "Icon Y Offset", "Y offset for the Stamina HUD in ICON mode.", 0, StaminaConfig.CLIENT.iconYOffset)
            .addEnum("Client", "Regen Indicator Style", "Whether to use the DEFAULT (>>>), CUSTOM (textures), or OFF.", 0, StaminaConfig.CLIENT.regenIndicatorStyle, StaminaConfig.RegenIndicatorStyle.class)

            .beginSection("Client", "Colors", "Decimal color codes for the bar elements (e.g. 16711680 is Red).")
            .addNumber("Client", "Color Background", "Color of the empty stamina track.", 0, StaminaConfig.CLIENT.colorBackground)
            .addNumber("Client", "Color Safe", "Color of stamina when high.", 0, StaminaConfig.CLIENT.colorSafe)
            .addNumber("Client", "Color Critical", "Color of stamina when in the warning zone.", 0, StaminaConfig.CLIENT.colorCritical)
            .addNumber("Client", "Color Tireless", "Color of stamina when infinite.", 0, StaminaConfig.CLIENT.colorTireless)
            .addNumber("Client", "Color Stripes", "Color for general exhaustion stripes.", 0, StaminaConfig.CLIENT.colorStripes)
            .addNumber("Client", "Color Penalty Hunger", "Color for Hunger penalty stripes", 0, StaminaConfig.CLIENT.colorPenaltyHunger)
            .addNumber("Client", "Color Penalty Poison", "Color for Food Poisoning penalty stripes", 0, StaminaConfig.CLIENT.colorPenaltyPoison)
            .addNumber("Client", "Color Penalty Weight", "Color for weight stripes", 0, StaminaConfig.CLIENT.colorPenaltyWeight)
            .addNumber("Client", "Color Bonus Top", "Top gradient color for Bonus Stamina (RGB). Default: Gold", 0, StaminaConfig.CLIENT.colorBonusTop)
            .addNumber("Client", "Color Bonus Bottom", "Bottom gradient color for Bonus Stamina (RGB). Default: Dark Orange", 0, StaminaConfig.CLIENT.colorBonusBottom)
            .addNumber("Client", "Color Bonus Highlight", "Color of the highlight sheen (RGB). Default: White", 0, StaminaConfig.CLIENT.colorBonusHighlight)
            .addNumber("Client", "Bonus Highlight Alpha", "Opacity of the highlight sheen (0-255). 0 = Invisible, 255 = Solid.", 0, StaminaConfig.CLIENT.bonusHighlightAlpha)

            .beginSection("Client", "Auto HUD Animations", "Dynamically hide the stamina overlay when you arent using it.")
            .addBoolean("Client", "Auto Hud Enable", "Enable hiding the stamina bar when full/not in use.", 0, StaminaConfig.CLIENT.autoHudEnable)
            .pushDependency(StaminaConfig.CLIENT.autoHudEnable, "Auto Hud Enable")
            .addEnum("Client", "Auto Hud Mode", "Animation type. FADE, SLIDE, or BOTH.", 0, StaminaConfig.CLIENT.autoHudMode, StaminaConfig.AutoHudMode.class)
            .addEnum("Client", "Auto Hud Slide Dir", "Direction the bar slides away to.", 0, StaminaConfig.CLIENT.autoHudSlideDir, StaminaConfig.AutoHudSlideDir.class)
            .addEnum("Client", "Auto Hud Easing", "The mathematical curve used for the slide animation.", 0, StaminaConfig.CLIENT.autoHudEasing, StaminaConfig.AutoHudEasing.class)
            .addNumber("Client", "Auto Hud Fade In Speed", "Speed the bar fades in.", 0, StaminaConfig.CLIENT.autoHudFadeInSpeed)
            .addNumber("Client", "Auto Hud Fade Out Speed", "Speed the bar fades out.", 0, StaminaConfig.CLIENT.autoHudFadeOutSpeed)
            .addNumber("Client", "Auto Hud Slide In Speed", "Speed the bar slides in.", 0, StaminaConfig.CLIENT.autoHudSlideInSpeed)
            .addNumber("Client", "Auto Hud Slide Out Speed", "Speed the bar slides out.", 0, StaminaConfig.CLIENT.autoHudSlideOutSpeed)
            .addNumber("Client", "Auto Hud Slide Distance", "How many pixels the bar moves when sliding out.", 0, StaminaConfig.CLIENT.autoHudSlideDistance)
            .addNumber("Client", "Auto Hud Linger Time", "How long (in ticks) the bar stays visible after you stop using stamina.", 0, StaminaConfig.CLIENT.autoHudLingerTime)
            .addNumber("Client", "Auto Hud Threshold", "Show the bar if stamina drops below this percentage (0.35 = 35%)", 0, StaminaConfig.CLIENT.autoHudThreshold)
            .addBoolean("Client", "Auto Hud Show On Penalties", "Force the bar to stay visible if you have penalties (hunger, poison, fatigue, weight).", 0, StaminaConfig.CLIENT.autoHudShowOnPenalties)
            .addBoolean("Client", "Auto Hud Show On Bonus Stamina", "If true, the stamina HUD will automatically show when you have Bonus Stamina.", 0, StaminaConfig.CLIENT.autoHudShowOnBonus)
            .popDependency()

            .beginSection("Client", "Weight UI", "Inventory and Hover details.")
            .addBoolean("Client", "Enable Weight HUD", "Enable the dynamic Weight/Encumbrance text on the inventory screen.", 0, StaminaConfig.CLIENT.enableWeightHUD)
            .pushDependency(StaminaConfig.CLIENT.enableWeightHUD, "Enable Weight HUD")
            .addNumber("Client", "Weight HUD X", "X position offset for the Weight HUD.", 0, StaminaConfig.CLIENT.weightXOffset)
            .addNumber("Client", "Weight HUD Y", "Y position offset for the Weight HUD.", 0, StaminaConfig.CLIENT.weightYOffset)
            .addEnum("Client", "Weight Unit", "Choose LBS, KG, or CUSTOM.", 0, StaminaConfig.CLIENT.displayUnit, StaminaConfig.Client.WeightUnit.class)
            .addString("Client", "Custom Unit Label", "The text label to show if CUSTOM is selected.", 0, StaminaConfig.CLIENT.customUnitLabel)
            .addNumber("Client", "Custom Unit Multiplier", "The math multiplier for custom units (Base is LBS).", 0, StaminaConfig.CLIENT.customUnitMultiplier)
            .popDependency()

            .beginSection("Client", "Tooltips & Labels", "Text to display next to values and the tooltip order.")
            .addBoolean("Client", "Enable Tooltips", "Enable stamina information on item tooltips.", 0, StaminaConfig.CLIENT.enableTooltips)
            .pushDependency(StaminaConfig.CLIENT.enableTooltips, "Enable Tooltips")
            .addBoolean("Client", "Advanced Tooltips Only", "Only show tooltips when advanced tooltips are enabled (F3+H).", 0, StaminaConfig.CLIENT.advancedTooltipsOnly)
            .addString("Client", "Label: Weight", "Text shown before the Weight value.", 0, StaminaConfig.CLIENT.labelWeight)
            .addString("Client", "Label: Attack", "Text shown before the Attack Cost value.", 0, StaminaConfig.CLIENT.labelAttackCost)
            .addString("Client", "Label: Missed Attack", "Text shown before the Missed Attack Cost value.", 0, StaminaConfig.CLIENT.labelMissCost)
            .addString("Client", "Label: Use", "Text shown before the Use Cost value.", 0, StaminaConfig.CLIENT.labelUseCost)
            .addString("Client", "Label: Tick", "Text shown before the Tick/Active Cost value.", 0, StaminaConfig.CLIENT.labelTickCost)
            .addString("Client", "Label: Block", "Text shown before the Block Cost value.", 0, StaminaConfig.CLIENT.labelBlockCost)
            .addString("Client", "Label: Instant Stamina", "Text shown before the Instant Stamina value.", 0, StaminaConfig.CLIENT.labelInstant)
            .addString("Client", "Label: Bonus Stamina", "Text shown before the Bonus Stamina value.", 0, StaminaConfig.CLIENT.labelBonus)
            .addString("Client", "Label: Regen Modifier", "Text shown before the Regen Modifier value.", 0, StaminaConfig.CLIENT.labelRegen)
            .addString("Client", "Label: Cures", "Text shown before the Cures value.", 0, StaminaConfig.CLIENT.labelCures)
            .addStringList("Client", "Custom Tooltips Formatting", 
                "Define multiple tooltips to display on items. (Order here dictates order shown in-game)\n" +
                "Format: content;placement;label_color;value_color\n" +
                "@SUGGEST[0]: Content | ENUM(WEIGHT : Total Weight, ATTACK_COST : Attack Cost, MISSED_ATTACK_COST : Miss Cost, USE_COST : Usage Cost, TICK_COST : Active Cost, BLOCK_COST : Blocking Cost, INSTANT_STAMINA : Stamina Recovery, BONUS_STAMINA : Bonus Max Stamina, REGEN_MODIFIER : Regen Speed Effect, CURES : Cures Penalties)\n" +
                "@SUGGEST[1]: Placement | ENUM(BOTTOM : Draw under item tooltips, BELOW_NAME : Draw directly under item name)\n" +
                "@SUGGEST[2]: Label Color | COLOR\n" +
                "@SUGGEST[3]: Value Color | COLOR\n" +
                "Example:\n WEIGHT;BOTTOM;8421504;16777215", 0, StaminaConfig.CLIENT.customTooltips)
            .popDependency()

            // ==========================================
            //                 EXPERIMENTAL TAB
            // ==========================================
            .beginSection("Experimental", "Entity Stamina", "Apply stamina logic to the world.")
            .addBoolean("Experimental", "Enable Mob Stamina", "Enable stamina system for Mobs.", 0, ExperimentalConfig.EXPERIMENTAL.enableMobStamina)
            .pushDependency(ExperimentalConfig.EXPERIMENTAL.enableMobStamina, "Enable Mob Stamina")
            .addBoolean("Experimental", "Exhaustion Particles", "Show sweat particles for exhausted mobs.", 0, ExperimentalConfig.EXPERIMENTAL.enableExhaustionParticles)
            .addStringList("Experimental", "Exhaustion Profiles", 
                "Define reusable attribute debuff templates here. Modded attributes are supported.\n" +
                "Format: profile_name;attribute_id=value,attribute_id=value\n" +
                "@SUGGEST[0]: Profile Name | ANY\n" +
                "@SUGGEST[1]: Attribute Target | ATTRIBUTE\n" +
                "Example:\n" +
                " MeleeTired; minecraft:generic.movement_speed=-0.40, minecraft:generic.attack_damage=-0.4", 2, ExperimentalConfig.EXPERIMENTAL.exhaustionProfiles)
            .addStringList("Experimental", "Custom Mob Stamina", 
                "Define which mobs use the stamina system and link them to a Profile defined above.\n" +
                "Format: entity_id;max_attacks;exhaustion_ticks;profile_name\n" +
                "@SUGGEST[0]: EntityID | ENTITY\n" +
                "@SUGGEST[1]: MaxAttacks | INT\n" +
                "@SUGGEST[2]: ExhaustionTicks | INT\n" +
                "@SUGGEST[3]: ProfileName | ANY\n" +
                "| Arg | Name | Description |\n" +
                "|---|---|---|\n" +
                "| 1 | EntityID | Registry name of the mob (e.g. minecraft:zombie) |\n" +
                "| 2 | MaxAttacks | Hits/shots before exhausting |\n" +
                "| 3 | ExhaustionTicks | How long they stay exhausted (20 = 1 sec) |\n" +
                "| 4 | ProfileName | Name of template from 'exhaustionProfiles' |", 2, ExperimentalConfig.EXPERIMENTAL.customMobStamina)
            .popDependency()

            .beginSection("Experimental", "Global Hooks", "Custom Event Actions.")
            .addStringList("Experimental", "Custom Action Hooks", 
                "Drain or restore stamina when specific Forge events occur.\n" +
                "Format: event_type;target;cost\n" +
                "@SUGGEST[0]: Event Type | ENUM(MOUNT : On mounting entity, INTERACT_ENTITY : On clicking entity, HURT : Upon taking damage, FISH : On using rod)\n" +
                "@SUGGEST[1]: Target // DYNAMIC(0:MOUNT=Entity ID#0:INTERACT_ENTITY=Entity ID#0:HURT=Damage Source#0:ANY=Target) | DYNAMIC(0:MOUNT=ENTITY#0:INTERACT_ENTITY=ENTITY#0:ANY=ANY)\n" +
                "@SUGGEST[2]: Cost | FLOAT\n" +
                "| Event Type | Target | Cost |\n" +
                "|---|---|---|\n" +
                "| MOUNT | Entity ID or ANY | FLOAT |\n" +
                "| INTERACT_ENTITY | Entity ID or ANY | FLOAT |\n" +
                "| HURT | Damage source or ANY | FLOAT |\n" +
                "| FISH | ANY | FLOAT |\n" +
                "\n" +
                "Note: Positive costs drain stamina, Negative costs restore stamina.", 2, ExperimentalConfig.EXPERIMENTAL.customActionHooks)

            .build();
    }
}