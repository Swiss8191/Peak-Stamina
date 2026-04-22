# Peak Stamina

Replaces vanilla sprinting mechanics with a resource management system based on fatigue and hunger. comes with compatibility for Tough as Nails, Cold Sweat, ParCool and, Combat roll. For additional custom penalties or integrations, refer to the Advanced Features section below.

For a more indepth and better formatted guide visit the [wiki!](https://github.com/Swiss8191/Peak-Stamina/wiki/Peak-Stamina-Home) Or you can scroll down and read the guide there.

----------

## Features

-   **Action Costs**: Stamina is drained by sprinting, jumping, attacking, mining, swimming, climbing, and using items.
-   **Fatigue**: Dropping below 25% stamina and staying there accumulates _Fatigue_, which temporarily reduces your maximum stamina cap.
-   **Food Poisoning**: Eating bad food applies a lingering _Poison_ penalty (purple bar) that blocks a portion of your max stamina until it decays.
-   **Weight System**: Carrying heavy items reduces your max stamina. The more you carry, the greater the penalty.
-   **Climbing**: Ladders consume stamina. Holding **Shift** enables a Slow Climb (40% speed) that costs no stamina and allows gradual regeneration.
-   **Hunger Penalty**: Low food levels reduce your maximum stamina cap.
-   **HUD**: A custom, low-profile stamina bar with visual indicators for fatigue, poison, weight, and active penalties.

![Peak Stamina HUD](https://media.forgecdn.net/attachments/description/1412640/description_71318891-81ac-4cf2-9c28-13cdc054a439.png)

----------

## Configuration Files

-   **`config/peak_stamina/common.toml`**: All core gameplay values (costs, recovery, penalties).
-   **`config/peak_stamina/lists.toml`**: Weight system, item costs, consumables, universal penalties/buffs, and exhaustion profiles.
-   **`config/peak_stamina/client.toml`**: HUD and visual settings.
  
-  **Note**: With the newest update (v1.8.15+) you do not need to restart your game or server to load in new values inside the config (with the exception of a few configs that need you to leave and rejoin the server/world). The new value will be adjusted automatically in-game when you save the config file.
----------

## Base Settings

### General

-   `enableStamina` _(Default: `true`)_: Master switch for the mod.
-   `disableInCreative` _(Default: `true`)_: If true, Creative mode players are excluded from the stamina system entirely.
-   `disableInSpectator` _(Default: `true`)_: If true, Spectator mode players are excluded from the stamina system entirely.
-   `initialMaxStamina` _(Default: `100.0`)_: The starting maximum stamina for all players. All penalties and modifiers are calculated against this base value.
-   `slowClimbSpeed` _(Default: `0.4`)_: The movement speed multiplier when slow climbing (set to 1 to disable)


### Action Costs

> Set any value to `0` to disable it. Use a negative value to restore stamina during that action instead of draining it.

* `depletionSprint` _(Default: `0.15`)_: Drain per tick while running.
* `depletionJump` _(Default: `0.85`)_: Instant drain on jump.
* `depletionAttack` _(Default: `3.45`)_: Instant drain on weapon swing.
* `depletionMissedAttack` _(Default: `1.0`)_: Instant drain on a missed swing.
* `depletionBlockBreak` _(Default: `1.1`)_: Instant drain on block break.
* `depletionBlockPlace` _(Default: `1.1`)_: Instant drain on block place.
* `depletionClimb` _(Default: `0.7`)_: Drain per tick while on ladders.
* `depletionSwim` _(Default: `0.05`_): Drain per tick while in water.
* `itemInterruptionCooldown` _(Default: `120`)_: Ticks an item remains unusable after running out of stamina while using it.

### Recovery

* `recoveryPerTick` _(Default: `0.36`)_: Base regeneration speed.
* `recoveryRestMult` _(Default: `1.45`)_: Multiplier applied when standing still.
* `recoveryClimbMult` _(Default: `0.7`)_: Multiplier applied when resting on a ladder.
* `recoveryDelay` _(Default: `50`)_: Ticks before regeneration begins after performing an action.

### Penalties & Limits

-   `fatigueThreshold` _(Default: `0.25`)_: Stamina percentage at which fatigue begins accumulating.
-   `fatigueDurationToPenalty` _(Default: `180`)_: Ticks spent in the critical zone before the fatigue penalty activates.
-   `maxExertionPenalty` _(Default: `30.0`)_: Maximum stamina lost due to fatigue.
-   `maxHungerPenalty` _(Default: `30.0`)_: Maximum stamina lost due to starvation.
-   `hungerPenaltyThreshold` _(Default: `6`)_: Food level (3 shanks) at which hunger penalties begin.
-   `minMaxStamina` _(Default: `10.0`)_: Hard floor for maximum stamina: it can never be reduced below this value.
-   `penaltyRecoveryDelay` _(Default: `100`)_: Ticks before fatigue begins healing after conditions improve.
-   `penaltyBuildupRate` _(Default: `0.1`)_: Penalty accumulated per tick when conditions worsen.
-   `penaltyDecayRate` _(Default: `0.05`)_: Penalty recovered per tick when conditions improve.

----------
## Intermediate Features

### Sleep Mechanics

-   `sleepMode` _(Default: `DEFAULT`)_: Controls how fatigue decays:
    -   **`DEFAULT`**: Fatigue decays naturally over time. Sleeping and eating fatigue-curing food both reduce it.
    -   **`HARDCORE`**: Fatigue never decays on its own. You _must_ sleep or eat fatigue-curing food to remove it.
-   `sleepFatigueReduction` _(Default: `20.0`)_: Flat amount of fatigue penalty removed after a successful sleep. Set to a high number to fully clear it.

> **Note:** Only food tagged with `CURE;FATIGUE;value` will cure fatigue. Multiple CURE entries can be chained on a single item: for example, `CURE;FATIGUE;10;CURE;universalpenalty;10` removes 10 points of both. See the Consumables section for a full guide.

----------

### Combat & Dynamic Weapon Weight

**The Weight Scaling Formula:**

When weight scaling is enabled, the stamina drained by an attack is multiplied by a calculated weight factor based on the weapon you are holding.

`Multiplier = 1.0 + (((WeaponWeight - Normalizer) / Normalizer) * ScaleFactor)`

**Hit Attack Settings:**
-   `depletionAttack` _(Default: `3.45`)_: Base stamina drained per successful attack.
-   `attackCostScalesWithWeight` _(Default: `true`)_: Enables or disables the dynamic weight formula for successful hits.
-   `attackWeightNormalizer` _(Default: `3.0`)_: The baseline weapon weight that results in exactly a 1.0x stamina cost multiplier.
-   `attackWeightScaleFactor` _(Default: `1.0`)_: How intensely weight affects the attack cost.
-   `attackWeightMinMultiplier` _(Default: `0.4`)_: The absolute minimum stamina cost multiplier.
-   `attackWeightMaxMultiplier` _(Default: `5.0`)_: The absolute maximum stamina cost multiplier.
    

**Missed Attack Settings:**
-   `depletionMissedAttack` _(Default: `1.0`)_: Base stamina drained when swinging at the air.
-   `missedAttackCostScalesWithWeight` _(Default: `true`)_: Enables or disables the dynamic weight formula for missed attacks.
-   `missedAttackWeightNormalizer` _(Default: `3.0`)_: The baseline weapon weight for a 1.0x multiplier on missed attacks.
-   `missedAttackWeightScaleFactor` _(Default: `1.0`)_: How intensely weight affects the missed attack cost.
-   `missedAttackWeightMinMultiplier` _(Default: `0.4`)_: The minimum possible stamina cost multiplier for missed attacks.
-   `missedAttackWeightMaxMultiplier` _(Default: `5.0`)_: The maximum possible stamina cost multiplier for missed attacks.
    
----------

### Elytra Mechanics

Controls what happens when a player runs out of stamina while flying.

-   `depletionElytra` _(Default: `0.25`)_: Drain per tick while flying.
-   `disableElytraWhenExhausted` _(Default: `true`)_: If true, the Elytra will aerodynamically stall when stamina hits 0. If false, flight continues normally.
-   `exhaustedElytraDrag` _(Default: `0.86`)_: Horizontal speed retained per tick during a stall. Lower = faster deceleration. (`0.9` = 10% speed loss per tick.)
-   `exhaustedElytraGravity` _(Default: `-0.025`)_: Extra vertical gravity per tick during a stall. Negative values pull the player downward.
-   `exhaustedElytraMinSpeed` _(Default: `0.5`)_: Speed threshold below which the Elytra force-closes and drops the player.
-   `exhaustedElytraTickInterval` _(Default: `3`)_: How often in ticks drag physics are applied. `1` = every tick.

----------

### Bonus Stamina

Controls how players earn bonus stamina and how it decays over time.

When Bonus Stamina exceeds your maximum capacity, the HUD bar dynamically stacks on top of itself. Each overflow tier is shaded slightly darker, and a multiplier label (e.g., `2x`, `3x`) appears on the left side of the bar.

-   `enableExcessStaminaConversion` _(Default: `true`)_: If true, restoring stamina past the max via consumables converts the overflow into Bonus Stamina.
-   `excessConversionRate` _(Default: `0.5`)_: Percentage of excess converted. `0.5` = 50% (e.g., 10 overflow → 5 Bonus Stamina).
-   `bonusStaminaDecayDelay` _(Default: `20`)_: Ticks before Bonus Stamina begins decaying after being gained or used. (20 ticks = 1 second.)
-   `bonusDecayScalesWithAmount` _(Default: `true`)_: If true, decay is a percentage of current bonus stamina: it burns faster when you have more. If false, decay is a flat amount.
-   `bonusStaminaDecayRate` _(Default: `0.05`)_: Bonus Stamina lost per second. With scaling enabled, `0.05` = 5% per second.

----------

## Advanced Features

> Advanced configuration is located in `config/peak_stamina/lists.toml`.

----------

### Weight System

Tracks the total weight of items in your inventory and applies a stamina penalty when you carry too much. Penalty scales linearly between the threshold and the limit.

-   `enableWeightSystem` _(Default: `true`)_: Enable or disable weight calculations.
-   `weightPenaltyThreshold` _(Default: `125.0`)_: Total weight at which the penalty begins (0%).
-   `weightPenaltyLimit` _(Default: `400.0`)_: Total weight at which the penalty reaches maximum (100%).
-   `maxWeightPenaltyAmount` _(Default: `40.0`)_: Maximum stamina reduction at full weight.
-   `maxWeightRecursionDepth` _(Default: `3`)_: How many levels deep to scan nested containers.
-   `autoWeightBase` _(Default: `10.0`)_: Base weight for items not explicitly listed.
    

#### How Weights Are Calculated

Every item's weight is determined by one of four priority tiers (highest wins):

1.  **Explicit Item Weights**: defined in `customItemWeights` (Supports standard IDs and Virtual NBT IDs).
2.  **Tag Weights**: defined in `customTagWeights`.
3.  **Advanced NBT Extraction**: defined in `nbtWeightPaths` (Extracts hidden IDs to check against explicit weights).
4.  **Auto-Weigher**: heuristic fallback: `Weight = (autoWeightBase / MaxStackSize) × Count`.
    

With `autoWeightBase = 10.0`, a sword (stack 1), 16 Ender Pearls, and 64 dirt blocks all weigh 10.0 total: carrying more of a stackable item doesn't multiply weight indefinitely. Containers (shulker boxes, backpacks) include the weight of their contents. Shulker Boxes, Curios, and PackedUp are supported automatically.

#### Explicit Item Weights

**Format:** `"modid:item;weight"`: weight is per individual item, not per stack.
_Note: This list also accepts "Virtual IDs" extracted by the Advanced NBT system (e.g., `"tacz:9mm;0.05"`)._

```
customItemWeights = [
    "minecraft:netherite_chestplate;15.0",
    "minecraft:diamond_sword;4.0",
    "minecraft:obsidian;1.2",
    "tacz:9mm;0.05"
]
```
#### Tag Weights

**Format:** `"tag;weight"`: weight applies to each individual item within the tag.

```
customTagWeights = [
    "forge:ores;0.25",
    "forge:storage_blocks;0.15",
    "forge:ingots;0.2"
]
```

#### Custom Container Paths

For backpack mods that store items in custom NBT. Only needed if a mod isn't detected automatically.

**Format:** `"modid:item;path.to.list"`

```
customContainerPaths = [
    "somemod:satchel;Inventory",
    "anothermod:backpack;StorageTag.Items"
]
```

#### Advanced NBT Weight Extraction (For Complex Mods)

**WHEN TO USE THIS:**
Most items in Minecraft use a standard Item ID (e.g., `minecraft:apple`). For those, you do NOT need this list; just put them directly into the Explicit Item Weights list.

However, some complex mods (like TACZ) use a single base item (e.g., `tacz:ammo`) and store the actual specific item ID (like `tacz:9mm`) hidden inside NBT folders. This list tells the weight system exactly where to look inside the NBT data to find that true "Virtual ID".

**WHERE TO PUT THE IDS:**
Once you define a path here, the weight system will extract the hidden Virtual ID. You must then put that extracted ID into your `Explicit Item Weights` list to assign it a weight.

**Format:** `"base_item_id ; nbt.path.to.id ; fallbackWeight ; applyFallbackIfMissing"`

**Examples:**

```
nbtWeightPaths = [
    # Example 1: Ammo (Uses 'true' for the fallback flag)
    # Looks inside 'tacz:ammo' for 'AmmoId'. If it finds an ID like 'tacz:9mm', it checks Explicit Weights.
    # If 'tacz:9mm' isn't explicitly configured, it adds 0.05 weight. 
    # If the AmmoId tag is missing entirely, it still adds 0.05 because the flag is true.
    "tacz:ammo;AmmoId;0.05;true",

    # Example 2: Attachments (Uses 'false' for the fallback flag)
    # Digs through a gun's NBT folders (opens AttachmentSCOPE, then tag, to read AttachmentId) to check for a scope.
    # If a scope is found, it checks Explicit Weights, else adds 0.5.
    # If no scope is equipped (the folder is empty), it adds 0.0 weight because the flag is false.
    # (If it was true, it would add 0.5 phantom weight even if the gun had no scope. So false is better here)
    "tacz:modern_kinetic_gun;AttachmentSCOPE.tag.AttachmentId;0.5;false"
]
```
#### Weight Limit Attribute
Increase a player's carry capacity with `/stamina attr weight_limit <value>`. Each point adds `+0.5` to the threshold and `+1.0` to the limit.

----------

### Item Depletion Costs
Assign stamina costs to any item. Multiple cost types can be stacked on a single item (e.g., a shield that drains stamina both to hold and to block with).

> Use a negative cost value to restore stamina during the action instead of draining it.

- `itemInterruptionCooldown` _(Default: `80`)_: Cooldown in ticks if you run out of stamina while using an item.

**Format:** `"ItemId;TYPE;Cost;TYPE;Cost..."`
**Cost types:**
-   `TICK`: Drains stamina continuously while the item is in use. Cancels the action if stamina hits 0.
-   `BLOCK`: Drains stamina only when a Shield successfully blocks damage. Supports dynamic scaling: `BLOCK;BaseCost;DamageMultiplier`.
-   `USE`: Drains stamina instantly on right-click.
-   `USE_ON_BLOCK`: Drains stamina only when the tool successfully modifies a block state.

**Examples:**
```
# Shield: `0.2/tick` to hold; when hit, `2.0` flat + `80%` of incoming damage:
"minecraft:shield;TICK;0.2;BLOCK;2.0;0.8"

# Bow: `0.5/tick` to draw:
"minecraft:bow;TICK;0.5"

# Fishing Rod: `3.0` stamina to cast:
"minecraft:fishing_rod;USE;3.0"
```

> A tag based list is also available using the same format, with a tag ID instead of an item ID. Specific item entries always take priority over tag entries.

----------

### Consumables System

Chain multiple effects onto a single item to create complex food and consumable interactions.

-   `penaltyReliefDuration` _(Default: `25`)_: Seconds the _Resistance_ buff lasts after eating a relief item.
-   `maxPoisonPenalty` _(Default: `40.0`)_: Maximum stamina blocked by food poisoning.
-   `poisonDecayDelay` _(Default: `45`)_: Seconds after eating before the poison penalty begins to heal.
-   `poisonDecayRate` _(Default: `0.05`)_: How fast the poison bar drains after the delay expires.

**Format:** `"ItemId;TYPE;Args...;TYPE;Args..."`

**Effect types:**

-   `INSTANT;Amount`: Instantly restores `Amount` stamina.
-   `BONUS;Amount`: Grants `Amount` Bonus Stamina (gold bar). Consumed before normal stamina and decays over time.
-   `POISON;Amount`: Applies a food poisoning penalty (purple bar) of `Amount`. Decays after `poisonDecayDelay` seconds.
-   `REGEN;Amount;Seconds`: Modifies regen speed for `Seconds`. Positive = faster, negative = slower. (`0.2 = +20%`, `-0.3 = -30%.`)
-   `PENALTY;Amount`: Grants _Resistance_ for `penaltyReliefDuration` seconds. Formula: **30% base + (Amount / 100)**. Caps at 80%.
-   `CURE;Target;Amount`: Removes `Amount` from a specific penalty. Valid targets: `FATIGUE`, `HUNGER`, `POISON`, `WEIGHT`, `ALL`, or any custom key (e.g., `temperature` or `modid:effectid`).

**Examples:**

```
# Restores 10.0 stamina and grants 5.0 Bonus Stamina.
"minecraft:apple;INSTANT;10.0;BONUS;5.0"

# Grants ~50% Resistance to penalty gain for the configured relief duration.
"minecraft:golden_carrot;PENALTY;20.0"

# Removes 100.0 points of Food Poisoning.
"minecraft:milk_bucket;CURE;POISON;100.0"

# Removes 50.0 Poison and 20.0 Fatigue.
"modid:herbal_tea;CURE;POISON;50.0;CURE;FATIGUE;20.0"

# Removes 100.0 points of penalty caused by the Wither Effect.
"minecraft:honey_bottle;CURE;minecraft:wither;100.0"

# Removes 50.0 from all active penalties and grants ~55% Resistance.
"minecraft:golden_apple;CURE;ALL;50.0;PENALTY;25.0"
```
----------

### Exhaustion Penalties
Define what happens when a player's stamina hits 0.

-   `exhaustionCooldownDuration` _(Default: `60`)_: Ticks (3 seconds) the penalties persist after hitting 0.
-   `exhaustedSpeedPenalty` _(Default: `-0.65`)_: Sprint speed reduction when running at 0 stamina. Players will not regenerate stamina while doing this.
-   `customExhaustionPenalties`: A list of attribute modifiers applied at exhaustion.

**Format:** `"AttributeName;Amount;Operation"`

Operations: `0` = Add flat, `1` = Multiply base, `2` = Multiply total.
Example: slow the player by 15%:

```
"minecraft:generic.movement_speed;-0.15;2"
```

----------

### Universal Penalties (NBT & Status Effects)

Link any mod's data: thirst, temperature, radiation, magic: to the stamina system using comparators. Supports gradual buildup or instant application.

-   `universalPenalties`: The list of rules defining external penalties.
-   `showIcons` _(Default: `true`)_: Toggle penalty icons on the HUD. _(Located in `peak_stamina-client.toml`. Check your version's config comments for availability.)_

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
| <strong>IconText</strong> |Emoji or text to display on the bar. Write <code>none</code> to disable. 

> `EFFECT` checks the amplifier, not the displayed level. Amplifier `0` = Level I.

#### Comparator Modes

| Operator | Behavior |
| :--- | :--- |
| `>` | Linear penalty as value rises above Threshold, maxing at WorstValue. |
| `<` | Linear penalty as value falls below Threshold, maxing at WorstValue. |
| `*` | Each point above Threshold adds MaxPenalty, capped at WorstValue. Formula: `(Current - Threshold) * MaxPenalty` |
| `*<` | Each point below Threshold adds MaxPenalty, capped at WorstValue. Formula: `(Threshold - Current) * MaxPenalty` |

Prefix any operator with `!` (e.g., `!>`, `!*<`) to apply the penalty instantly rather than building up gradually.

The 8th argument (IconText) sets what appears on the bar. Type any character or emoji directly (e.g., `💧`, `🔥`), or use `none` to hide it.


#### Examples
```
# Linear penalty as thirst level falls below 6
"NBT;thirstLevel;<;6;0;20.0;38143;💧"
```

**Cold Sweat (Heat & Cold):**
```
# Linear penalty for extreme heat (> 2) or extreme cold (< 2)
"NBT;targetTemperatureLevel;>;2;4;20.0;16724016;🔥"
"NBT;targetTemperatureLevel;<;2;0;20.0;65535;❄"
```

**Multiplier (Thirst):**
```
# Each point below 6 adds 2.0 penalty, capped at 30
"NBT;thirstLevel;*<;6;30;2.0;38143;💧"
```

**Multiplier (Poison Effect):**
```
# Each amplifier level adds 5.0 penalty, capped at 50
"EFFECT;minecraft:poison;*;0;50;5.0;4488448;☣"
```

**Instant Application:**
```
# Instantly applies the penalty using the ! prefix
"EFFECT;hordes:infected;!*;0;80;10.0;8388736;🦠"
```

### Universal Buffs (NBT & Status Effects)

Works identically to Universal Penalties, but grants Bonus Stamina instead of penalizing the player.

* `universalBuffs`: The list of rules defining external buffs.
* `universalBuffRegenWhileActive` *(Default: `false`)*: If false, Bonus Stamina from PASSIVE/BOTH modes pauses regeneration while the player is actively draining stamina or on regen cooldown.

**Format:** `"Type;Key;ActionMode;Threshold;LimitOrCooldown;Amount;BurstAmount;ScalingFactor"`

> `BurstAmount` is exclusive to `BOTH` modes. `ScalingFactor` is exclusive to `MULTIPLIER` modes.

**Action Modes:**

| Action Mode | Behavior |
| :--- | :--- |
| `PASSIVE_OVER` / `PASSIVE_UNDER` | Regenerates Bonus Stamina per second while the stat is above/below Threshold. |
| `BURST_OVER` / `BURST_UNDER` | Instantly grants Bonus Stamina once when the stat crosses Threshold. |
| `BOTH_OVER` / `BOTH_UNDER` | Grants a one-time burst when crossing Threshold, then regenerates passively up to the hardcap. |

Append `_MULTIPLIER` to any mode (e.g., `PASSIVE_OVER_MULTIPLIER`) to scale the effect based on how far past the threshold the player is.

`LimitOrCooldown` is context dependent: for PASSIVE/BOTH modes it sets the Bonus Stamina hardcap; for BURST modes it sets the cooldown in ticks before the burst can re-trigger.

**Examples:**

**Passive (Regeneration Effect):**
```toml
# Generates 1.0/sec while active, cap 20.0
"EFFECT;minecraft:regeneration;PASSIVE_OVER;-1;20.0;1.0"
```

**Burst + Passive (NBT Mana Stat):**
```toml
# Burst of 30.0 when mana exceeds 50, then 2.0/sec passively up to cap 100.0
"NBT;player_mana;BOTH_OVER;50;100.0;2.0;30.0"
```

**Multiplier (Strength Effect):**
```toml
# Strength I (Amp 0): 1.25x -> 2.5/sec, cap 25.0
# Strength II (Amp 1): 1.50x -> 3.0/sec, cap 30.0
"EFFECT;minecraft:strength;PASSIVE_OVER_MULTIPLIER;-1;20.0;2.0;1.25"
```

#### Nested NBT Paths

Use `.` to navigate inside nested NBT tags, chaining as many levels as needed:

```
# ForgeCaps tag → legendarysurvivaloverhaul:thirst tag → hydrationLevel value 
"ForgeCaps.legendarysurvivaloverhaul:thirst.hydrationLevel" 

# ForgeCaps tag → legendarysurvivaloverhaul:temperature tag → temperature value 
"ForgeCaps.legendarysurvivaloverhaul:temperature.temperature" 

# ForgeCaps tag → somemod:player_stats tag → skills tag → mining_level value 
"ForgeCaps.somemod:player_stats.skills.mining_level"
```

> **Troubleshooting:** If the correct path still doesn't work, try prefixing with `ForgeCaps.` followed by the rest of the path. Example: `"NBT;ForgeCaps.legendarysurvivaloverhaul:temperature.temperature;*<;12;40;2.0;65535;❄"`
> 
----------

## ⚠️ Experimental Features

> These features are experimental and may impact performance or game balance. Use with caution.

### Entity Stamina

Brings exhaustion mechanics to non-player entities, allowing mobs to run out of stamina and suffer attribute debuffs during combat.

-   `enableMobStamina` _(Default: `false`)_: Master toggle for the mob stamina system.
-   `enableExhaustionParticles` _(Default: `true`)_: Toggles the visual "sweat" particle effect when a mob enters an exhausted state.

#### Exhaustion Profiles

Define reusable attribute debuff templates that can be assigned to any mob. Modded attributes are fully supported.

**Format:** `"ProfileName;AttributeID=Multiplier,AttributeID=Multiplier..."`

**Examples:**
```
"MeleeTired;minecraft:generic.movement_speed=-0.40,minecraft:generic.attack_damage=-0.4"
"HeavyMelee;minecraft:generic.movement_speed=-0.60,minecraft:generic.attack_damage=-0.50"
```

#### Custom Mob Stamina (Whitelist)

Defines exactly which mobs participate in the stamina system. Mobs not listed here are completely unaffected.
> **Note:** Boss entities are not guaranteed to function correctly with this system.

**Format:** `"EntityID;MaxAttacks;ExhaustionTicks;ProfileName"`

-   **EntityID**: The registry name of the mob (e.g., `minecraft:zombie`).
-   **MaxAttacks**: Number of melee hits or ranged shots before exhaustion triggers. For ranged mobs, counts shots fired regardless of whether they land.
-   **ExhaustionTicks**: Duration the mob stays exhausted. (20 ticks = 1 second.)
-   **ProfileName**: The Exhaustion Profile to apply, as defined above.

> Ranged mobs using bows or crossbows that have an attack damage reduction in their Exhaustion Profile will also suffer reduced arrow velocity while exhausted.

### Custom Stamina Actions

This system allows you to define specific triggers that consume or regenerate stamina. Each action follows a specific string format consisting of three main components.

**Format:** `"Event_Type;Argument;Cost"`

| Parameter | Description |
| :--- | :--- |
| **Event_Type** | The specific category of interaction or event that triggers the cost (e.g., `MOUNT`, `HURT`). |
| **Argument** | Filters the event. Use a specific ID to target one object, or `ANY` to trigger the cost for all events in that category. |
| **Cost**| The amount of stamina consumed. |

#### Available Event Types & Arguments
| Event Type | Argument | Description |
| :--- | :--- | :---|
| `MOUNT` | `entity_id` | Triggers when the player mounts an entity (e.g., `minecraft:horse`, `minecraft:boat`) |
| `INTERACT_ENTITY` | `entity_id` | Triggers when right-clicking an entity (e.g., `minecraft:villager`, `minecraft:pig`). |
| `HURT` | `damage_source` | Triggers when the player takes damage (e.g., `fall`, `onFire`, `mob`, `arrow`). |
| `FISH` | `damage_source` | Triggers when the player successfully catches something with a fishing rod. |

### Examples
-   `"HURT;fall;10.0"` Losing 10 stamina when taking fall damage.
-   `"MOUNT;minecraft:horse;25.0"` A one time cost of 25 stamina to mount a horse.
-   `"FISH;ANY;-5.0"` Regenerating 5 stamina every time you catch a fish.
-   `"INTERACT_ENTITY;ANY;2.0"` A small stamina drain for interacting with any mob.
    
> **Note:** Use `ANY` for the argument if you want the stamina cost to apply regardless of the specific entity or damage source type. Negative values regen stamina instead.

----------

### ParCool Compat

As long as any stamina remains (> 0), you can initiate a ParCool action: even if the cost exceeds your current amount. You will simply drop to 0. Fast run and fast swim costs are added on top of the base sprint and swim costs.

> Use a negative cost value to restore stamina during an action instead of draining it.

- `parCoolActionCosts`: The list defining stamina costs for specific parkour moves.

**Format:** `"ActionName;START;Cost;CONTINUE;Cost"`

-   `START`: One-time cost when the action begins.
-   `CONTINUE`: Cost per tick while the action is active.

**Examples:**
```
# Vault: 8.0 stamina instantly:
"Vault;START;8.0"

# Wall Run: 15.0 to start, then 0.5/tick:
"WallRun;START;15.0;CONTINUE;0.5"
```

----------

### Combat Roll Compat

You can only use a roll when you have sufficient stamina to initiate one. Otherwise you can't.

- `combatRollCost`: How much each roll costs.

----------

### Auto HUD System

> All visual and HUD settings are in `config/peak_stamina/client.toml`. Disabled by default.

-   `autoHudMode`: How the bar animates: `FADE`, `SLIDE`, or `BOTH`.
-   `autoHudSlideDir`: Direction the bar travels when sliding away: `UP`, `DOWN`, `LEFT`, `RIGHT`.
-   `autoHudEasing`: Animation curve: `LINEAR`, `SMOOTHSTEP`, `EASE_OUT_SINE`, or `EASE_OUT_EXPO`. Detailed descriptions are in the config file comments.
-   `autoHudFadeInSpeed` / `autoHudFadeOutSpeed`: Independent speed controls for transparency transitions.
-   `autoHudSlideInSpeed` / `autoHudSlideOutSpeed`: Independent speed controls for slide movement.
-   `autoHudSlideDistance`: Pixels the bar moves when sliding off-screen.
-   `autoHudLingerTime`: Ticks the bar stays visible after stamina stops changing.
-   `autoHudThreshold`: Automatically shows the bar if stamina drops below this percentage.
-   `autoHudShowOnPenalties`: Forces the bar to stay visible whenever any active penalty is present.

### Custom Tooltips 

> All visual and HUD settings are in `config/peak_stamina/client.toml`. 

This system allows you to define which stamina related statistics appear on item tooltips in game.
-   `enableTooltips` _(Default: `true`)_: Toggle to enable or disable stamina information on item tooltips.
-   `advancedTooltipsOnly` _(Default: `false`)_: If true, stamina tooltips will only appear when F3+H (Advanced Tooltips) is active in game.

**Format:** `"Content_Type;Placement;Label_Color;Value_Color"`

| Parameter | Description |
| :--- | :--- |
| **Content_Type** | The specific stamina stat or effect to display (e.g., `WEIGHT`, `INSTANT_STAMINA`). |
| **Placement** | Where the tooltip appears: `BOTTOM` (end of tooltip) or `BELOW_NAME` (immediately under the item name). |
| **Label_Color** | The color of the stat name (e.g., "Weight:"). See color list below. |
| **Value_Color** | The color of the numerical value or effect (e.g., "15.0"). See color list below. |

#### Available Content Types

-   `WEIGHT`: Displays the item's weight.
-   `ATTACK_COST`: Shows stamina drain per successful swing.
-   `MISSED_ATTACK_COST`: Shows stamina drain when swinging at air.
-   `USE_COST`: Shows instant cost on right-click.
-   `TICK_COST`: Shows continuous drain while holding/using.
-   `BLOCK_COST`: Shows stamina drain when blocking with a shield.
-   `INSTANT_STAMINA`: Displays how much stamina is restored upon consumption.
-   `BONUS_STAMINA`: Displays the amount of gold "Bonus Stamina" granted.
-   `REGEN_MODIFIER`: Shows the percentage boost or penalty to recovery speed.
-   `CURES`: Lists which penalties (Fatigue, Poison, etc.) the item removes.
> You can configure what will show in the tooltips for each of them as well.

#### Color Palette

You can use the following standard Minecraft color names for both labels and values:

`BLACK`, `DARK_BLUE`, `DARK_GREEN`, `DARK_AQUA`, `DARK_RED`, `DARK_PURPLE`, `GOLD`, `GRAY`, `DARK_GRAY`, `BLUE`, `GREEN`, `AQUA`, `RED`, `LIGHT_PURPLE`, `YELLOW`, `WHITE`.

#### Examples
The order in which you list these entries determines the order they appear in your tooltips. 

```
customTooltips = [
    "WEIGHT;BOTTOM;DARK_GRAY;WHITE",
    "ATTACK_COST;BOTTOM;DARK_GRAY;WHITE",
    "INSTANT_STAMINA;BOTTOM;DARK_GRAY;GREEN",
    "BONUS_STAMINA;BOTTOM;DARK_GRAY;GOLD",
    "CURES;BOTTOM;DARK_GRAY;WHITE"
]
```

> **Note:** Tooltips will only appear on items that actually possess the corresponding stat or effect. For example, a `WEIGHT` tooltip won't show on an item with 0 weight unless explicitly configured.

----------

## Attributes & Commands

### Attributes

All of the following can be modified per-player via `/stamina attr <attribute> <value>`:

```
peak_stamina:max_stamina
peak_stamina:stamina_regen
peak_stamina:stamina_action_recovery          # Multiplies stamina restored during actions
peak_stamina:regen_delay_multiplier
peak_stamina:exhaustion_duration_multiplier
peak_stamina:current_stamina
peak_stamina:penalty_gain_multiplier
peak_stamina:penalty_decay_multiplier
peak_stamina:penalty_amount_multiplier
peak_stamina:slow_climb_speed
peak_stamina:sprint_speed
peak_stamina:weight_limit
peak_stamina:current_weight
peak_stamina:weight_calculation_multiplier    # Multiplies the weight of all items
peak_stamina:stamina_usage
peak_stamina:jump_cost_multiplier
peak_stamina:sprint_cost_multiplier
peak_stamina:attack_cost_multiplier
peak_stamina:missed_attack_cost_multiplier
peak_stamina:shield_block_cost_multiplier
peak_stamina:item_cost_multiplier
peak_stamina:block_break_cost_multiplier
peak_stamina:block_place_cost_multiplier
peak_stamina:swim_cost_multiplier
peak_stamina:climb_cost_multiplier
peak_stamina:elytra_cost_multiplier
peak_stamina:bonus_stamina_capacity
peak_stamina:bonus_stamina_decay_rate
peak_stamina:bonus_stamina_decay_delay
peak_stamina:excess_conversion_multiplier

```

### Commands

-   `/stamina get`: View your current and maximum stamina.
-   `/stamina set <amount>`: Set your current stamina to a specific value.
-   `/stamina attr <attribute> <value>`: Modify a personal stamina attribute.
-   `/stamina debug`: Full debug readout of all stamina stats, penalties, and attribute multipliers.
-   `/stamina reload`: Reloads all caches and config values.

----------

## Debugging

### Finding the Correct NBT Path

Guess the path in your config, then launch the game and check `logs/latest.log`. If the path is wrong, the mod prints a WARNING listing the available keys at that level: use those to correct it.

> If a correct path still doesn't work, try prefixing with `ForgeCaps.` followed by the rest of the path. Example: `"NBT;ForgeCaps.legendarysurvivaloverhaul:temperature.temperature;*<;12;40;2.0;65535;❄"`

### Getting Decimal Colors

Minecraft configs use decimal color values. Take your hex color (e.g., `FF0000`), run it through any Hex to Decimal converter, and paste the result into your config (e.g., `16711680`).

----------

## Q&A

**Q: Can you add compatibility for X mod (thirst, temperature, etc.)?** You can do it yourself using the Universal Penalties & Buffs section above

**Q: How do I disable the Hunger penalty?** Set `maxHungerPenalty` to `0.0`. You can also set `maxExertionPenalty` to `0.0` to disable Fatigue.

**Q: How do I disable the Weight System?** Set `enableWeightSystem` to `false`.

**Q: Do I need this mod on the server?** Yes.

**Q: Does this work in multiplayer?** While not exhaustively tested across all versions, testing has not revealed any issues.

