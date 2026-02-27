# Peak Stamina

Replaces vanilla sprinting mechanics with a resource management system based on fatigue and hunger. Comes with compat with tough as nails and cold sweat (parcool as well but in alpha). If that is not enough or you would like to define more penalties, refer to the guide below under advanced features.

## Features

*   **Action Costs:** Stamina is drained by sprinting, jumping, attacking, mining, swimming, climbing, and using items.
*   **Fatigue:** Dropping below 25% stamina and staying there for a configurable amount of time accumulates "Fatigue", which temporarily reduces your maximum stamina cap.
*   **Food Poisoning:** Eating bad food can apply a lingering "Poison" penalty (Purple bar) that blocks your max stamina until it decays.
*   **Weight System:** Carrying heavy items reduces your max stamina. The more you carry, the greater the penalty.
*   **Climbing:** Ladders consume stamina. Holding **Shift** allows for a "Slow Climb" (40% speed) that costs no energy and allows you to regen stamina at a slower rate.
*   **Hunger Penalty:** Low food levels reduce your maximum stamina cap.
*   **HUD:** A custom, low-profile stamina bar with visual indicators for fatigue, poison, weight, and penalties.

![image](https://media.forgecdn.net/attachments/description/1412640/description_71318891-81ac-4cf2-9c28-13cdc054a439.png)

***

## Base Configuration

All values are adjustable in `config/peak_stamina/common.toml`.

### General Settings

*   `enableStamina` (Default: `true`): Master switch for the mod.
*   `disableInCreative` (Default: `true`): If true, players in Creative mode will not consume or use the stamina system.
*   `disableInSpectator` (Default: `true`): If true, players in Spectator mode will not consume or use the stamina system.
*   `initialMaxStamina` (Default: `100.0`): The starting maximum stamina value for all players. This sets the base attribute value that penalties and modifiers are calculated against.

### Action Depletion (Cost)

0 to disable any of these, negative value to restore stamina during the actions.

*   `depletionSprint` (Default: `0.15`): Drain per tick while running.
*   `depletionJump` (Default: `0.85`): Instant drain on jump.
*   `depletionAttack` (Default: `3.45`): Instant drain on weapon swing.
*   `depletionMissedAttack` (Default: `1.0`): Instant drain on missed swing.
*   `depletionBlockBreak` (Default: `1.1`): Instant drain on block break.
*   `depletionBlockPlace` (Default: `1.1`): Instant drain on block place.
*   `depletionClimb` (Default: `0.7`): Drain per tick on ladders.
*   `depletionSwim` (Default: `0.05`): Drain per tick in water.
*   `itemInterruptionCooldown` (Default: `120`): Ticks an item is unusable when out of stamina while using it.

### Recovery

*   `recoveryPerTick` (Default: `0.36`): Base regeneration speed.
*   `recoveryRestMult` (Default: `1.45`): Multiplier when standing still.
*   `recoveryClimbMult` (Default: `0.7`): Multiplier when resting on a ladder.
*   `recoveryDelay` (Default: `50`): Ticks before regen starts after an action.

### Penalties & Limits

*   `fatigueThreshold` (Default: `0.25`): Stamina % where fatigue begins accumulating.
*   `fatigueDurationToPenalty` (Default: `180`): Ticks spent in critical stamina before fatigue penalty begins.
*   `maxExertionPenalty` (Default: `30.0`): Max stamina lost due to fatigue.
*   `maxHungerPenalty` (Default: `30.0`): Max stamina lost due to starvation.
*   `hungerPenaltyThreshold` (Default: `6`): Food level (3 shanks) where penalties begin.
*   `minMaxStamina` (Default: `10.0`): Hard floor for max stamina.
*   `penaltyRecoveryDelay` (Default: `100`): Ticks before fatigue heals after resting.
*   `penaltyBuildupRate` (Default: `0.1`): How much penalty accumulates per tick when conditions worsen.
*   `penaltyDecayRate` (Default: `0.05`): How much penalty recovers per tick when conditions improve.

#### Sleep Mechanics

*   `sleepMode` (Default: `DEFAULT`):
    *   **DEFAULT:** Fatigue decays naturally over time and sleeping as well as eating fatigue curing food reduces it.
    *   **HARDCORE:** Fatigue never decays naturally. You must sleep or eat fatigue curing food to reduce it.
*   `sleepFatigueReduction` (Default: `20.0`): Flat amount of fatigue penalty to remove after a successful sleep (set it to a high number to full clear).

Note: Only food that has the CURE;FATIGUE;value cures fatigue (keep in mind that you can chain multiple CURE in a row like CURE;FATIGUE;10;CURE;universalpenalty;10 and it will cure 10 of both fatigue and your custom universal penalty (enter in the name of it) Refer to advanced features of consumables/universal compat section for a guide.

***

## Intermediate Features

### Elytra Mechanics

Control what happens when players run out of stamina while flying with an Elytra.

**Related Configs:**

*   `depletionElytra` (Default: 0.25): Drain per tick while flying.
*   `disableElytraWhenExhausted` (Default: `true`): If true, players will experience an aerodynamic stall when stamina hits 0 during flight. If false, players can continue flying normally at 0 stamina.
*   `exhaustedElytraDrag` (Default: `0.86`): How much horizontal speed is retained per tick when stalling. 0.9 = 10% speed loss per tick. Lower values = faster deceleration.
*   `exhaustedElytraGravity` (Default: `-0.025`): Extra vertical gravity applied per tick when stalling. Negative values pull the player down.
*   `exhaustedElytraMinSpeed` (Default: `0.5`): Speed threshold below which the Elytra will force-close and drop the player.
*   `exhaustedElytraTickInterval` (Default: `3`): How often (in ticks) the drag physics are applied. 1 = Every tick. 3 = Every 3 ticks.

***

### Bonus Stamina Mechanics

Control how a player can obtain bonus stamina as well as how fast it decays.

**HUD Stacking:** The Bonus Stamina bar dynamically stacks on top of itself when it exceeds your maximum stamina capacity (by default your max is just the current max stamina of your standard bar but the attribute can increase or decrease this). Each tier automatically shades your configured bonus colors slightly darker so overlapping layers are clearly visible, and a dynamic multiplier (e.g., "2x", "3x") renders on the left side of the bar to show how many full bars you've built up.

**Related Configs:**

*   `enableExcessStaminaConversion` (Default: `true`): If true, restoring stamina past the Max Stamina limit via consumables will convert the excess amount into Bonus Stamina.
*   `excessConversionRate` (Default: `0.5`): The percentage of excess normal stamina that becomes bonus stamina. 0.5 = 50% conversion (e.g., 10 points of overflow becomes 5 points of Bonus Stamina).
*   `bonusStaminaDecayDelay` (Default: `20`): The delay in ticks (20 ticks = 1 second) before Bonus Stamina starts decaying after being gained or used. Default is 1 second.
*   `bonusDecayScalesWithAmount` (Default: `true`): If true, the decay rate is treated as a percentage of your _current_ bonus stamina (burns faster when you have more). If false, it decays by a flat amount.
*   `bonusStaminaDecayRate` (Default: `0.05`): The amount of Bonus Stamina lost per second. If scaling is enabled, `0.05` represents a 5% loss per second. If scaling is disabled, it represents a flat amount lost per second.

***

## Advanced Features

### (You can access these in the folder and file `config/peak_stamina/lists.toml`)

***

### Weight System

The Weight System tracks the total weight of items in your inventory and applies stamina penalties when you carry too much.

**Related Configs:**

*   `enableWeightSystem` (Default: `true`): Enable/disable weight calculations.
*   `weightPenaltyThreshold` (Default: `125.0`): Weight where penalty starts (0%).
*   `weightPenaltyLimit` (Default: `400.0`): Weight where penalty reaches maximum (100%).
*   `maxWeightPenaltyAmount` (Default: `40.0`): Max stamina reduction at full weight.
*   `maxWeightRecursionDepth` (Default: `3`): How deep to scan nested containers.
*   `autoWeightBase` (Default: `10.0`): Base weight for unlisted items.

**How It Works:**

*   Every item has a weight calculated from three priority tiers:
    1.  **Explicit Item Weights** (`customItemWeights`)
    2.  **Tag Weights** (`customTagWeights`)
    3.  **Auto-Weigher** (Heuristic: `autoWeightBase / MaxStackSize × Count`)
*   Containers (shulkers, backpacks) include the weight of their contents
*   Weight penalty scales linearly from threshold to limit

**Auto-Weigher Formula:**

```
Weight = (autoWeightBase / MaxStackSize) × Count
```

With `autoWeightBase = 10.0`:

*   1 Sword (stack 1) = 10.0 weight
*   16 Ender Pearls (stack 16) = 10.0 weight (0.625 each)
*   64 Dirt (stack 64) = 10.0 weight (0.156 each)

**Priority 1: Explicit Item Weights**

Format: `"modid:item;weight"`

Each individual item has this weight (not per stack).

```
customItemWeights = [
    "minecraft:netherite_chestplate;15.0",
    "minecraft:diamond_sword;4.0",
    "minecraft:obsidian;1.2"
]
```

**Priority 2: Tag Weights**

Format: `"tag;weight"`

Each individual item in the tag has this weight.

```
customTagWeights = [
    "forge:ores;0.25",
    "forge:storage_blocks;0.15",
    "forge:ingots;0.2"
]
```

**Custom Container Paths**

For backpack mods that store items in NBT.

Format: `"modid:item;path.to.list"`

Built-in support: Shulker Boxes, Curios, PackedUp (most backpack mods should work by default but if some dont, add it to this list)

```
customContainerPaths = [
    "somemod:satchel;Inventory",
    "anothermod:backpack;StorageTag.Items"
]
```

**Weight Limit Attribute**

Increase weight capacity using `/stamina attr weight_limit <value>`

*   Adds 0.5 weight capacity per point to threshold and double that to limit
*   Example: +20 weight\_limit = +10 threshold, +20 limit

***

### Item Depletion Costs

You can assign specific stamina costs to any item in the game. You can now stack multiple cost types on a single item (e.g., a shield that costs stamina to hold AND to block).

negative cost value to restore stamina during the action.

**Related Configs:**

*   `itemCosts`: The list of item rules.
*   `itemInterruptionCooldown` (Default: `80`): If you run out of stamina while using an item, a cooldown is applied to prevent spamming/glitching.

**Format:** `"ItemId;TYPE;Cost;TYPE;Cost..."`

**Types:**

*   **TICK:** Drains stamina _continuously_ while the item is being used (e.g., drawing a bow, holding up a shield). If you run out, the action is cancelled.
*   **BLOCK:** Drains stamina only when a Shield successfully blocks damage. **Now supports dynamic scaling.** (2 Arguments: Base Cost; Damage Multiplier)
*   **USE:** Drains stamina _instantly_ upon right-clicking (e.g., throwing a Snowball).
*   **USE\_ON\_BLOCK:** Drains stamina only when the tool successfully modifies a block state.

**Examples:**

Shield: Costs 0.2/tick to hold up. When hit, drains 2.0 flat stamina PLUS 80% of the incoming damage amount.

```
"minecraft:shield;TICK;0.2;BLOCK;2.0;0.8"
```

Bow: Costs 0.5/tick to draw and hold.

```
"minecraft:bow;TICK;0.5"
```

Fishing Rod: Costs 3.0 stamina to cast.

```
"minecraft:fishing_rod;USE;3.0"
```

There is also a tags section introduced in the newest version, it just takes the tagid instead of the itemid. The items in the specific items list will take priority in the case that an item is in both lists.

***

### Consumables System

You can chain multiple effects onto a single item to create more complex interactions.

**Related Configs:**

*   `consumable_values`: The list of attribute modifiers applied when eating.
*   `penaltyReliefDuration` (Default: `25`): Seconds the "Resistance" buff lasts after eating a relief item.
*   `maxPoisonPenalty` (Default: `40.0`): Max stamina blocked by food poisoning.
*   `poisonDecayDelay` (Default: `45`): Seconds to wait after eating before poison starts to heal.
*   `poisonDecayRate` (Default: `0.05`): How fast the poison bar drains after the delay.

**Format:** `"ItemId;TYPE;Args...;TYPE;Args..."`

**Types:**

*   **INSTANT;Amount**
    *   Instantly restores Amount stamina.
*   **BONUS;Amount**
    *   Grants Bonus Stamina (Yellow/Gold Bar) of Amount. This is a temporary buff that is consumed before normal stamina and decays over time.
*   **POISON;Amount**
    *   Applies a "Food Poisoning" penalty (Purple Bar) of Amount. This blocks Max Stamina and decays over time and after `poisonDecayDelay` seconds.
*   **REGEN;Amount;Seconds**
    *   Modifies your stamina regeneration speed for Seconds.
    *   **Positive (0.2):** Increases speed (+20%).
    *   **Negative (-0.3):** Decreases speed (-30%).
*   **PENALTY;Amount** (Resistance)
    *   **Grants Resistance** for `penaltyReliefDuration` seconds.
    *   Formula: 30% Base Resistance + (Amount / 100). Caps at 80%. This reduces the amount of penalty you gain.
*   **CURE;Target;Amount** (Removal)
    *   Removes Amount from a SPECIFIC penalty target.
    *   **Targets:** `FATIGUE`, `HUNGER`, `POISON`, `WEIGHT`, `ALL`, or any Custom Key (e.g., `temperature` or `modid:effectid`).

**Examples:**

`"minecraft:apple;INSTANT;10.0;BONUS;5.0"` Restores 10.0 Stamina and grants 5.0 Bonus Stamina.

`"minecraft:golden_carrot;PENALTY;20.0"` Grants ~50% Resistance to penalty gain for the configured relief duration.

`"minecraft:milk_bucket;CURE;POISON;100.0"` Removes 100.0 points of Food Poisoning penalty.

`"modid:herbal_tea;CURE;POISON;50.0;CURE;FATIGUE;20.0"` Removes 50.0 Poison penalty and 20.0 Fatigue penalty.

`"minecraft:honey_bottle;CURE;minecraft:wither;100.0"` Removes 100.0 points of penalty caused by the Wither Effect.

`"minecraft:golden_apple;CURE;ALL;50.0;PENALTY;25.0"` Removes 50.0 from all active penalties and grants ~55% Resistance.

### Exhaustion Penalties

Define what happens when a player hits 0 Stamina.

**Related Configs:**

*   `exhaustionCooldownDuration` (Default: `60`): How many ticks (3s) the penalties persist after hitting 0.
*   `customExhaustionPenalties`: A list of Attribute Modifiers to apply.
*   `exhaustedSpeedPenalty` (Default: `-0.65`): Controls the sprint speed reduction when sprinting while exhausted (0 stamina), players will not regen stamina when doing this.

**Format:** `"AttributeName;Amount;Operation"` (Operations: 0 = Add, 1 = Multiply Base, 2 = Multiply Total)

**Example:**

Slow down player by 15%

```
"minecraft:generic.movement_speed;-0.15;2"
```

***

### Universal Compatibility (NBT & Status Effects)

Link ANY mod's data (Thirst, Temperature, Radiation, Magic) to the stamina system with **advanced comparators** for instant application and multiplier modes.

**Related Configs:**

*   `universalPenalties`: A list of rules defining external penalties.
*   `showIcons`: True or False to turn on or off the icons (located in `peak_stamina-client.toml`). \[Only for certain versions (The config comments will tell you)\]

**Format:** `"Type;Key;Comparator;Threshold;WorstValue;MaxPenalty;ColorInt;IconText"`

| Parameter  |Description                                                                                                           |
| ---------- |--------------------------------------------------------------------------------------------------------------------- |
| <strong>Type</strong> |<code>NBT</code> (checks player data) or <code>EFFECT</code> (checks potion amplifier/level).                         |
| <strong>Key</strong> |The NBT path (e.g., <code>thirstLevel</code>) or Effect ID (e.g., <code>minecraft:poison</code>).                     |
| <strong>Comparator</strong> |Logic for calculating the penalty. See comparator modes below.                                                        |
| <strong>Threshold</strong> |The safe value where penalty starts (0%).                                                                             |
| <strong>WorstValue</strong> |In SCALE mode: The value where penalty reaches 100%.<br>In MULTIPLIER mode: The Maximum total penalty allowed (Hard Cap). |
| <strong>MaxPenalty</strong> |In SCALE mode: Total penalty amount at WorstValue.<br>In MULTIPLIER mode: Penalty amount per 1 unit of difference.    |
| <strong>ColorInt</strong> |Decimal color code for the HUD overlay (e.g., <code>16711680</code> is Red).                                          |
| <strong>IconText</strong> |Emoji or text to display on the bar. Write <code>none</code> to disable.                                              |

*   `EFFECT`: Checks the Amplifier level NOT THE EFFECT LEVEL.

#### Comparator Modes

**SCALE MODES** (Linear penalty between two points):

*   **`>`** : Penalty increases as value rises above Threshold, maxing at WorstValue.
*   **`<`** : Penalty increases as value falls below Threshold, maxing at WorstValue.

**MULTIPLIER MODE** (Penalty stacks per unit):

*   **`*`** (or `*>`): Each point ABOVE Threshold adds MaxPenalty to total.
    *   Formula: `(CurrentValue - Threshold) × MaxPenalty`
*   **`*<`** : Each point BELOW Threshold adds MaxPenalty to total.
    *   Formula: `(Threshold - CurrentValue) × MaxPenalty`

**INSTANT FLAG**:

*   **`!`** : Add `!` before any comparator (`!>`, `!<`, `!*`, `!*<`) to apply penalty instantly.
    *   Without `!`, penalties build up gradually over time.

| Operator |Mode             |Behavior                                                 |
| -------- |---------------- |-------------------------------------------------------- |
| <code>></code> |Scale            |Linear penalty from Threshold to WorstValue (increasing) |
| <code><</code> |Scale            |Linear penalty from Threshold to WorstValue (decreasing) |
| <code>*</code> |Multiply         |Penalty per point above Threshold (capped at WorstValue) |
| <code>*<</code> |Multiply         |Penalty per point below Threshold (capped at WorstValue) |
| <code>!></code> |Instant Scale    |Instant version of <code>></code>                     |
| <code>!<</code> |Instant Scale    |Instant version of <code><</code>                     |
| <code>!*</code> |Instant Multiply |Instant version of <code>*</code>                        |
| <code>!*<</code> |Instant Multiply |Instant version of <code>*<</code>                    |

**About Icons:** The 8th argument `Icon` allows you to customize what appears on the bar.

*   **Text/Emoji:** Directly type a character (e.g., `!`, `💧`, `🔥`).
*   **Disable:** Type `none` to hide the icon.

**Scale Mode Examples:**

**Tough As Nails (Thirst)**

```
"NBT;thirstLevel;<;6;0;20.0;38143;💧"
```

**Cold Sweat (Body Temperature)**

```
"NBT;targetTemperatureLevel;>;2;4;20.0;16724016;🔥"
"NBT;targetTemperatureLevel;<;2;0;20.0;65535;❄"
```

**Instant Application:**

```
"NBT;thirstLevel;!<;6;0;20.0;38143;💧"
```

**Multiplier Mode Examples:**

```
# Below 6 thirst: each point lost adds 2.0 penalty (builds over time)
# Thirst 5 = 2.0, Thirst 4 = 4.0, Thirst 2 = 8.0 (capped at 30)
"NBT;thirstLevel;*<;6;30;2.0;38143;💧"

# Each poison level adds 5.0 penalty (builds over time)
# Poison I = 5.0, Poison II = 10.0, Poison III = 15.0 (capped at 50)
"EFFECT;minecraft:poison;*;0;50;5.0;4488448;☣"
```

**Instant Application:**

```
# Each poison level instantly adds 5.0 penalty
"EFFECT;minecraft:poison;!*;0;50;5.0;4488448;☣"

# Infection mod: each level instantly adds 10.0 penalty
"EFFECT;hordes:infected;!*;0;80;10.0;8388736;🦠"
```

**Note:** sometimes even with the correct path it may not work, in this scenario try adding NBT;ForgeCaps.(rest of the path)

**Example:** `"NBT;ForgeCaps.legendarysurvivaloverhaul:temperature.temperature;*<;12;40;2.0;65535;❄"`

Also notice how its `ForgeCaps.legendarysurvivaloverhaul:temperature.temperature;` and not just `ForgeCaps.legendarysurvivaloverhaul:temperature` the `.` is used to say that INSIDE the legendarysurvivaloverhaul:temperature NBT tag, look for the temperature NBT tag. (Yes there can be NBT tags inside NBT tags)

**Examples of using `.` to look inside tags:**
```
"ForgeCaps.legendarysurvivaloverhaul:thirst.hydrationLevel"
# Open the ForgeCaps tag ➡ open the legendarysurvivaloverhaul:thirst tag inside it ➡ grab the exact hydrationLevel number.

"ForgeCaps.legendarysurvivaloverhaul:temperature.temperature"
# Open the ForgeCaps tag ➡ open the legendarysurvivaloverhaul:temperature tag ➡ grab the specific temperature value inside it.

"ForgeCaps.somemod:player_stats.skills.mining_level"
# Open the ForgeCaps tag ➡ open the somemod:player_stats tag ➡ open the skills tag inside that ➡ grab the mining_level number.
```
and this can be done multiple times like `somemod:player_stats.skills.mining_skills.mining_level.mining_speed`

***

### ParCool Compat

As long as you have any stamina remaining (> 0), you can initiate a ParCool action, even if the cost is higher than what you have left (you will simply drop to 0). Keep in mind that the fast run and fast swim costs are added onto the regular sprinting and swimming costs.

negative cost value to restore stamina during the action.

**Related Configs:**

*   `parCoolActionCosts`: The list defining stamina usage for specific parkour moves.

**Format:** `"ActionName;START;Cost;CONTINUE;Cost"`

**Types:**

*   **START:** One-time cost when the action begins (e.g., Vault, Jump).
*   **CONTINUE:** Cost per tick while the action is active (e.g., Wall Running, Sliding).

**Examples:**

**Vault**: Costs 8.0 stamina instantly.

`"Vault;START;8.0"`

**Wall Run**: Costs 15.0 to start, then 0.5 per tick to maintain.

`"WallRun;START;15.0;CONTINUE;0.5"`

***

## Attributes & Commands

**Attributes:** \[Latest Version, older versions have less\]

*   `peak_stamina:max_stamina`
*   `peak_stamina:stamina_regen`
*   `peak_stamina:stamina_action_recovery` (multiplies stamina restoration during action)
*   `peak_stamina:regen_delay_multiplier`
*   `peak_stamina:exhaustion_duration_multiplier`
*   `peak_stamina:current_stamina`
*   `peak_stamina:penalty_gain_multiplier`
*   `peak_stamina:penalty_decay_multiplier`
*   `peak_stamina:penalty_amount_multiplier`
*   `peak_stamina:slow_climb_speed`
*   `peak_stamina:sprint_speed`
*   `peak_stamina:weight_limit`
*   `peak_stamina:current_weight`
*   `peak_stamina:weight_calculation_multiplier` (multiplies the weight of items basically)
*   `peak_stamina:stamina_usage`
*   `peak_stamina:jump_cost_multiplier`
*   `peak_stamina:sprint_cost_multiplier`
*   `peak_stamina:attack_cost_multiplier`
*   `peak_stamina:missed_attack_cost_multiplier`
*   `peak_stamina:block_break_cost_multiplier`
*   `peak_stamina:block_place_cost_multiplier`
*   `peak_stamina:swim_cost_multiplier`
*   `peak_stamina:climb_cost_multiplier`
*   `peak_stamina:elytra_cost_multiplier`
*   `peak_stamina:bonus_stamina_capacity`
*   `peak_stamina:bonus_stamina_decay_rate`
*   `peak_stamina:bonus_stamina_decay_delay`
*   `peak_stamina:excess_conversion_multiplier`

**Commands:**

*   `/stamina get`: View your current stamina and max stamina.
*   `/stamina set <amount>`: Set your current stamina to a specific value.
*   `/stamina attr <Attributes Above> <value>`: Modify your personal stamina attributes.
*   `/stamina debug`: View a detailed debug readout of all your stamina stats, penalties, and attribute multipliers.

***

## Debugging Guide

**How to find the correct NBT Path**

First, guess the path in your config. Then launch the game and check `logs/latest.log`. If the path is wrong, the mod will print a **WARNING** showing available keys. Use those keys to fix your config.

**Note:** sometimes even with the correct path it may not work, in this scenario try adding NBT;ForgeCaps.(rest of the path)

**example:** `"NBT;ForgeCaps.legendarysurvivaloverhaul:temperature.temperature;*<;12;40;2.0;65535;❄"`


**How to get Colors**

Minecraft configs use Decimal colors. Go to a Hex to Decimal Converter, enter your Hex color (e.g., `FF0000` for Red), and copy the result (e.g., `16711680`).

***

## Q&A

**Q: Can you make this mod work with X mod (hydration, temperature)?**

**A:** You can do it yourself now! Using the Universal Compatibility config section described above.

**Q: How do I disable just the Hunger penalty?**

**A:** Go to the config and set `maxHungerPenalty` to `0.0`. You can do the same for `maxExertionPenalty` (Fatigue).

**Q: How do I disable the Weight System?**

**A:** Set `enableWeightSystem` to `false` in the config.

**Q: Do I need this mod on the server?**

**A:** Yes.

**Q: Does this mod work well for multiplayer on servers?**

**A:** While I haven't tested all versions, the versions I have tested on a server with a friend did not suffer from any issues.
