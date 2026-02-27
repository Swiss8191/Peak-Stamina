package com.peakstamina.handlers;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.network.PacketSyncStamina;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;
import com.peakstamina.compat.ParCoolCompat;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import com.peakstamina.handlers.WeightHandler;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID)
public class ServerStaminaHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final UUID EXHAUSTED_SPEED_UUID = UUID.fromString("73411111-2222-3333-4444-555555555555");
    private static List<UniversalPenaltyData> cachedUniversalPenalties = null;
    private static List<? extends String> lastUniversalConfigRef = null;

    private static List<ExhaustionPenaltyData> cachedExhaustionPenalties = null;
    private static List<? extends String> lastExhaustionConfigRef = null;

    private static class UniversalPenaltyData {
        String type;
        String key;
        String[] nbtPath;
        boolean isInstant;
        boolean isMultiplier;
        boolean isReverse;
        double threshold;
        double worstValue;
        float maxPenalty;
        MobEffect effect;
    }

    private static class ExhaustionPenaltyData {
        net.minecraft.world.entity.ai.attributes.Attribute attribute;
        String attrName;
        double amount;
        AttributeModifier.Operation operation;
        UUID uuid;
    }

    public static void consumeStamina(StaminaCapability cap, float amount) {
        if (amount <= 0) return;
        if (cap.bonusStamina > 0) {
            if (cap.bonusStamina >= amount) {
                cap.bonusStamina -= amount;
                return;
            } else {
                amount -= cap.bonusStamina;
                cap.bonusStamina = 0;
            }
        }
        cap.stamina -= amount;
    }

    private static void sync(net.minecraft.server.level.ServerPlayer player, StaminaCapability cap) {
        StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
    }

    private static boolean hasInfiniteStamina(LivingEntity entity) {
        List<? extends String> effects = StaminaLists.LISTS.infiniteStaminaEffects.get();
        for (String id : effects) {
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null && ForgeRegistries.MOB_EFFECTS.containsKey(loc)) {
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(loc);
                if (effect != null && entity.hasEffect(effect)) {
                    return true;
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updateMaxStaminaAttribute(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        updateMaxStaminaAttribute(event.getEntity());
    }

    private static void updateMaxStaminaAttribute(Player player) {
        if (player.level().isClientSide) {
            return;
        }
        AttributeInstance attr = player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
        if (attr != null) {
            attr.setBaseValue(StaminaConfig.COMMON.initialMaxStamina.get());
        }
    }

    private static int getRecoveryDelay(Player player) {
        int baseDelay = StaminaConfig.COMMON.recoveryDelay.get();
        double mult = getAttributeValue(player, StaminaAttributes.REGEN_DELAY_MULTIPLIER.get(), 1.0);
        return (int) (baseDelay * mult);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }

        Player player = event.player;
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }

        if (event.phase == TickEvent.Phase.END) {
            if (player.getPersistentData().contains("peak_stamina_restore_sprint")) {
                player.getPersistentData().remove("peak_stamina_restore_sprint");
                if (!player.isSprinting() && !player.isCrouching()) {
                    player.setSprinting(true);
                }
            }

            if (player.onClimbable() && player.isShiftKeyDown()) {
                double speedMult = 0.4;
                AttributeInstance climbAttr = player.getAttribute(StaminaAttributes.SLOW_CLIMB_SPEED.get());
                if (climbAttr != null) {
                    speedMult = climbAttr.getValue();
                }
                if (speedMult < 0.99) {
                    player.setDeltaMovement(player.getDeltaMovement().scale(speedMult));
                }
            }
            return;
        }

        if (event.phase != TickEvent.Phase.START || player.level().isClientSide) {
            return;
        }

        if (StaminaConfig.COMMON.enableWeightSystem.get() && player.tickCount % 20 == 0) {
            double rawWeight = WeightHandler.calculateTotalWeight(player);
            double weightMult = getAttributeValue(player, StaminaAttributes.WEIGHT_CALC_MULTIPLIER.get(), 1.0);
            double effectiveWeight = rawWeight * weightMult;

            AttributeInstance weightAttr = player.getAttribute(StaminaAttributes.CURRENT_WEIGHT.get());
            if (weightAttr != null) {
                weightAttr.setBaseValue(effectiveWeight);
            }

            double weightLimitAttr = getAttributeValue(player, StaminaAttributes.WEIGHT_LIMIT.get(), 0.0);
            double bonus = weightLimitAttr / 2.0;
            double threshold = StaminaConfig.COMMON.weightPenaltyThreshold.get() + bonus;
            double limit = StaminaConfig.COMMON.weightPenaltyLimit.get() + (bonus * 2);
            double maxPen = StaminaConfig.COMMON.maxWeightPenaltyAmount.get();

            float newWeightPenalty = 0.0f;
            if (effectiveWeight > threshold) {
                double range = limit - threshold;
                if (range <= 0) {
                    range = 1.0;
                }
                double excess = effectiveWeight - threshold;
                double ratio = Mth.clamp(excess / range, 0.0, 1.0);
                newWeightPenalty = (float) (maxPen * ratio);
            }

            final float fPen = newWeightPenalty;
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> cap.weightPenalty = fPen);
        }

        List<? extends String> currentUnivConfig = StaminaLists.LISTS.universalPenalties.get();
        if (cachedUniversalPenalties == null || currentUnivConfig != lastUniversalConfigRef) {
            refreshUniversalCache(currentUnivConfig);
        }

        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            ParCoolCompat.tick(player, cap);

            if (!cap.activeBuffs.isEmpty()) {
                Iterator<StaminaCapability.BuffInstance> it = cap.activeBuffs.iterator();
                while (it.hasNext()) {
                    StaminaCapability.BuffInstance buff = it.next();
                    applyBuffModifier(player, buff);
                    if (buff.durationTicks > 0) {
                        buff.durationTicks--;
                        if (buff.durationTicks == 0) {
                            removeBuffModifier(player, buff);
                            it.remove();
                        }
                    }
                }
            }

            if (cap.bonusStamina > 0) {
                if (cap.bonusStaminaDecayTimer > 0) {
                    cap.bonusStaminaDecayTimer--;
                } else if (player.tickCount % 20 == 0) { 
                    float decayAmount;
                    
                    // --- BONUS STAMINA ATTRIBUTE INTEGRATION ---
                    double bonusDecayRateMult = getAttributeValue(player, StaminaAttributes.BONUS_STAMINA_DECAY_RATE.get(), 1.0);
                    float rawRate = StaminaConfig.COMMON.bonusStaminaDecayRate.get().floatValue() * (float) bonusDecayRateMult;
                    
                    if (StaminaConfig.COMMON.bonusDecayScalesWithAmount.get()) {
                        decayAmount = cap.bonusStamina * rawRate;
                        if (decayAmount < 0.01f) decayAmount = 0.01f;
                    } else {
                        decayAmount = rawRate;
                    }
                    
                    if (decayAmount > 0) {
                        cap.bonusStamina -= decayAmount;
                        if (cap.bonusStamina < 0) cap.bonusStamina = 0;
                    }
                }
            }

            double baseAttr = 100.0;
            AttributeInstance attr = player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
            if (attr != null) {
                baseAttr = attr.getValue();
            }

            double regenMult = getAttributeValue(player, StaminaAttributes.STAMINA_REGEN.get(), 1.0);
            double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
            double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);
            double penaltyGainMult = getAttributeValue(player, StaminaAttributes.PENALTY_GAIN_MULTIPLIER.get(), 1.0);
            double penaltyDecayMult = getAttributeValue(player, StaminaAttributes.PENALTY_DECAY_MULTIPLIER.get(), 1.0);
            double penaltyAmountMult = getAttributeValue(player, StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER.get(), 1.0);
            float minMax = StaminaConfig.COMMON.minMaxStamina.get().floatValue();
            float maxAllowedPenalty = (float) baseAttr - minMax;
            if (maxAllowedPenalty < 0) {
                maxAllowedPenalty = 0;
            }
            float currentModPenaltySum = 0.0f;
            if (cap.penaltyValues != null) {
                for (float f : cap.penaltyValues) {
                    currentModPenaltySum += f;
                }
            }

            if (cap.poisonPenalty > 0) {
                if (cap.poisonTimer > 0) {
                    cap.poisonTimer--;
                } else {
                    float poisonDecay = StaminaConfig.COMMON.poisonDecayRate.get().floatValue() * (float) penaltyDecayMult;
                    cap.poisonPenalty = Math.max(0.0f, cap.poisonPenalty - poisonDecay);
                }
            }

            float targetHungerPenalty = 0.0f;
            float maxHungerPen = StaminaConfig.COMMON.maxHungerPenalty.get().floatValue();
            int foodLevel = player.getFoodData().getFoodLevel();
            int hungerThreshold = StaminaConfig.COMMON.hungerPenaltyThreshold.get();
            if (foodLevel <= hungerThreshold && hungerThreshold > 0) {
                targetHungerPenalty = ((float) (hungerThreshold - foodLevel) / (float) hungerThreshold) * maxHungerPen;
            }
            targetHungerPenalty *= (float) penaltyAmountMult;
            float decayRate = StaminaConfig.COMMON.penaltyDecayRate.get().floatValue() * (float) penaltyDecayMult;
            float buildupRate = StaminaConfig.COMMON.penaltyBuildupRate.get().floatValue() * (float) penaltyGainMult;
            if (cap.currentHungerPenalty < targetHungerPenalty) {
                float otherPenalties = cap.fatiguePenalty + cap.poisonPenalty + currentModPenaltySum + cap.weightPenalty;
                float room = Math.max(0, maxAllowedPenalty - otherPenalties);
                float effectiveTarget = Math.min(targetHungerPenalty, room);
                if (cap.currentHungerPenalty < effectiveTarget) {
                    cap.currentHungerPenalty += buildupRate;
                    if (cap.currentHungerPenalty > effectiveTarget) {
                        cap.currentHungerPenalty = effectiveTarget;
                    }
                }
            } else if (cap.currentHungerPenalty > targetHungerPenalty) {
                cap.currentHungerPenalty -= decayRate;
                if (cap.currentHungerPenalty < targetHungerPenalty) {
                    cap.currentHungerPenalty = targetHungerPenalty;
                }
            }

            if (cap.penaltyValues == null || cap.penaltyValues.length != cachedUniversalPenalties.size()) {
                float[] newArray = new float[cachedUniversalPenalties.size()];
                if (cap.penaltyValues != null) {
                    int copyLen = Math.min(cap.penaltyValues.length, cachedUniversalPenalties.size());
                    System.arraycopy(cap.penaltyValues, 0, newArray, 0, copyLen);
                }
                cap.penaltyValues = newArray;
                currentModPenaltySum = 0;
                for (float f : cap.penaltyValues) {
                    currentModPenaltySum += f;
                }
            }

            CompoundTag playerNBT = null;
            for (int i = 0; i < cachedUniversalPenalties.size(); i++) {
                UniversalPenaltyData data = cachedUniversalPenalties.get(i);
                float targetPenaltyVal = 0.0f;

                try {
                    double currentValue = Double.NaN;
                    if (data.type.equals("EFFECT")) {
                        if (data.effect != null && player.hasEffect(data.effect)) {
                            currentValue = player.getEffect(data.effect).getAmplifier();
                        } else {
                            currentValue = -1;
                        }
                    } else if (data.type.equals("NBT")) {
                        if (playerNBT == null) {
                            playerNBT = new CompoundTag();
                            player.saveWithoutId(playerNBT);
                        }
                        currentValue = getNbtValue(playerNBT, data.nbtPath);
                    }

                    if (!Double.isNaN(currentValue)) {
                        if (data.isMultiplier) {
                            double diff = 0.0;
                            if (data.isReverse) {
                                diff = data.threshold - currentValue;
                            } else {
                                diff = currentValue - data.threshold;
                            }

                            if (diff > 0) {
                                targetPenaltyVal = (float) (diff * data.maxPenalty);
                                if (data.worstValue > 0 && targetPenaltyVal > data.worstValue) {
                                    targetPenaltyVal = (float) data.worstValue;
                                }
                            }
                        } else {
                            float ratio = 0.0f;
                            if (data.isReverse) {
                                if (currentValue < data.threshold) {
                                    ratio = (float) ((data.threshold - currentValue) / (data.threshold - data.worstValue));
                                }
                            } else {
                                if (currentValue > data.threshold) {
                                    ratio = (float) ((currentValue - data.threshold) / (data.worstValue - data.threshold));
                                }
                            }
                            ratio = Mth.clamp(ratio, 0.0f, 1.0f);
                            targetPenaltyVal = data.maxPenalty * ratio;
                        }
                    }
                } catch (Exception ignored) {
                }

                targetPenaltyVal *= (float) penaltyAmountMult;
                if (data.isInstant) {
                    float sumOthers = currentModPenaltySum - cap.penaltyValues[i];
                    float totalNonMod = cap.fatiguePenalty + cap.currentHungerPenalty + cap.poisonPenalty + cap.weightPenalty;
                    float room = Math.max(0, maxAllowedPenalty - (totalNonMod + sumOthers));
                    float effectiveTarget = Math.min(targetPenaltyVal, room);
                    cap.penaltyValues[i] = effectiveTarget;
                } else {
                    float currentVal = cap.penaltyValues[i];
                    if (currentVal < targetPenaltyVal) {
                        float sumOthers = currentModPenaltySum - currentVal;
                        float totalNonMod = cap.fatiguePenalty + cap.currentHungerPenalty + cap.poisonPenalty + cap.weightPenalty;
                        float room = Math.max(0, maxAllowedPenalty - (totalNonMod + sumOthers));
                        float effectiveTarget = Math.min(targetPenaltyVal, room);

                        if (currentVal < effectiveTarget) {
                            currentVal += buildupRate;
                            if (currentVal > effectiveTarget) {
                                currentVal = effectiveTarget;
                            }
                        }
                    } else if (currentVal > targetPenaltyVal) {
                        currentVal -= decayRate;
                        if (currentVal > targetPenaltyVal) {
                            currentVal = targetPenaltyVal;
                        }
                    }
                    cap.penaltyValues[i] = currentVal;
                }
                currentModPenaltySum = currentModPenaltySum - cap.penaltyValues[i] + cap.penaltyValues[i];
            }

            float totalModPenalty = 0;
            if (cap.penaltyValues != null) {
                for (float f : cap.penaltyValues) {
                    totalModPenalty += f;
                }
            }

            float fatigueThresh = StaminaConfig.COMMON.fatigueThreshold.get().floatValue();
            if (cap.stamina <= (baseAttr * fatigueThresh)) {
                cap.fatigueTimer++;
                cap.penaltyRegenDelay = StaminaConfig.COMMON.penaltyRecoveryDelay.get();
            } else {
                cap.fatigueTimer = 0;
            }

            boolean isRecovering = cap.stamina > cap.lastTickStamina + 0.001f;
            int ticksBeforePenalty = StaminaConfig.COMMON.fatigueDurationToPenalty.get();

            if (cap.fatigueTimer > ticksBeforePenalty) {
                if (!isRecovering) {
                    float maxExertion = StaminaConfig.COMMON.maxExertionPenalty.get().floatValue();
                    maxExertion *= (float) penaltyAmountMult;
                    float otherPenalties = cap.currentHungerPenalty + cap.poisonPenalty + totalModPenalty + cap.weightPenalty;
                    float room = Math.max(0, maxAllowedPenalty - otherPenalties);
                    float effectiveMaxExertion = Math.min(maxExertion, room);
                    if (cap.fatiguePenalty < effectiveMaxExertion) {
                        float baseRate = StaminaConfig.COMMON.penaltyBaseRate.get().floatValue();
                        float curve = StaminaConfig.COMMON.penaltyCurveFactor.get().floatValue();
                        baseRate *= (float) penaltyGainMult;
                        float ticksExceeded = (float) (cap.fatigueTimer - ticksBeforePenalty);
                        float exponentialRate = baseRate * (float) Math.exp(ticksExceeded / curve);
                        cap.fatiguePenalty += exponentialRate;
                        if (cap.fatiguePenalty > effectiveMaxExertion) {
                            cap.fatiguePenalty = effectiveMaxExertion;
                        }
                    }
                }
            } else if (cap.fatigueTimer == 0 && cap.fatiguePenalty > 0) {
                if (StaminaConfig.COMMON.sleepMode.get() != StaminaConfig.SleepMode.HARDCORE) {
                    if (cap.penaltyRegenDelay > 0) {
                        cap.penaltyRegenDelay--;
                    } else {
                        cap.fatiguePenalty = Math.max(0.0f, cap.fatiguePenalty - decayRate);
                    }
                }
            }

            float effectiveMax = (float) baseAttr - cap.fatiguePenalty - cap.currentHungerPenalty - cap.poisonPenalty - totalModPenalty - cap.weightPenalty;
            if (effectiveMax < minMax) {
                effectiveMax = minMax;
            }
            cap.maxStamina = effectiveMax;

            double capacityMult = getAttributeValue(player, StaminaAttributes.BONUS_STAMINA_CAPACITY.get(), 1.0);
            float maxBonus = cap.maxStamina * (float) capacityMult;
            if (cap.bonusStamina > maxBonus) {
                cap.bonusStamina = maxBonus;
            }

            boolean isConsuming = false;
            boolean isInfinite = hasInfiniteStamina(player);
            if (!isInfinite) {
                float swimCost = StaminaConfig.COMMON.depletionSwim.get().floatValue();
                float climbCost = StaminaConfig.COMMON.depletionClimb.get().floatValue();
                float sprintCost = StaminaConfig.COMMON.depletionSprint.get().floatValue();
                float elytraCost = StaminaConfig.COMMON.depletionElytra.get().floatValue();

                double swimMult = getAttributeValue(player, StaminaAttributes.SWIM_COST_MULTIPLIER.get(), 1.0);
                double climbMult = getAttributeValue(player, StaminaAttributes.CLIMB_COST_MULTIPLIER.get(), 1.0);
                double sprintMult = getAttributeValue(player, StaminaAttributes.SPRINT_COST_MULTIPLIER.get(), 1.0);
                double elytraMult = getAttributeValue(player, StaminaAttributes.ELYTRA_COST_MULTIPLIER.get(), 1.0);
                if (player.isFallFlying() && elytraCost != 0) {
                    double finalCost;
                    if (elytraCost > 0) {
                        finalCost = elytraCost * usageMult * elytraMult;
                    } else {
                        finalCost = elytraCost * actionRecoveryMult;
                    }

                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                    isConsuming = elytraCost > 0;
                    if (cap.stamina <= 0 && StaminaConfig.COMMON.disableElytraWhenExhausted.get()) {
                        int interval = StaminaConfig.COMMON.exhaustedElytraTickInterval.get();
                        if (player.tickCount % interval == 0) {
                            Vec3 motion = player.getDeltaMovement();
                            double stallDrag = StaminaConfig.COMMON.exhaustedElytraDrag.get();
                            double stallGravity = StaminaConfig.COMMON.exhaustedElytraGravity.get();

                            player.setDeltaMovement(
                                    motion.x * stallDrag,
                                    Math.min(motion.y * stallDrag + stallGravity, motion.y),
                                    motion.z * stallDrag
                            );
                            player.hurtMarked = true;
                        }

                        double minSpeed = StaminaConfig.COMMON.exhaustedElytraMinSpeed.get();
                        double currentSpeed = player.getDeltaMovement().horizontalDistance();

                        if (currentSpeed < minSpeed) {
                            player.stopFallFlying();
                        }
                    }
                } else if (player.isSwimming() && swimCost != 0) {
                    double finalCost;
                    if (swimCost > 0) {
                        finalCost = swimCost * usageMult * swimMult;
                    } else {
                        finalCost = swimCost * actionRecoveryMult;
                    }
                    
                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                    isConsuming = swimCost > 0;
                } else if (player.onClimbable() && Math.abs(player.getDeltaMovement().y) > 0.1 && climbCost != 0) {
                    double finalCost;
                    if (climbCost > 0) {
                        finalCost = climbCost * usageMult * climbMult;
                    } else {
                        finalCost = climbCost * actionRecoveryMult;
                    }
                    
                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                    isConsuming = climbCost > 0;
                } else if (player.isSprinting() && sprintCost != 0) {
                    double finalCost;
                    if (sprintCost > 0) {
                        finalCost = sprintCost * usageMult * sprintMult;
                    } else {
                        finalCost = sprintCost * actionRecoveryMult;
                    }
                    
                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }
                    isConsuming = sprintCost > 0;
                }
            }

            if (cap.stamina < 0) {
                cap.stamina = 0;
            }
            if (cap.stamina > cap.maxStamina) {
                cap.stamina = cap.maxStamina;
            }

            if (isConsuming) {
                cap.staminaRegenDelay = getRecoveryDelay(player);
                if (cap.stamina <= 0) {
                    int baseExhaustion = StaminaConfig.COMMON.exhaustionCooldownDuration.get();
                    double exhMult = getAttributeValue(player, StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER.get(), 1.0);
                    cap.exhaustionCooldown = (int) (baseExhaustion * exhMult);
                }
            } else {
                if (cap.staminaRegenDelay > 0) {
                    cap.staminaRegenDelay--;
                } else if (cap.stamina < cap.maxStamina && cap.exhaustionCooldown <= 0) {
                    float recovery = StaminaConfig.COMMON.recoveryPerTick.get().floatValue();
                    if (player.onClimbable()) {
                        recovery *= StaminaConfig.COMMON.recoveryClimbMult.get().floatValue();
                    } else if (player.isInWater()) {
                        recovery *= StaminaConfig.COMMON.recoveryWaterMult.get().floatValue();
                    } else if (player.getDeltaMovement().lengthSqr() < 0.005) {
                        recovery *= StaminaConfig.COMMON.recoveryRestMult.get().floatValue();
                    }

                    recovery *= (float) regenMult;
                    cap.stamina += recovery;
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }
                }
            }

            if (cap.stamina < 0) {
                cap.stamina = 0;
            }
            if (cap.stamina > cap.maxStamina) {
                cap.stamina = cap.maxStamina;
            }

            if (cap.stamina <= 0) {
                if (player.isUsingItem()) {
                    float tickCost = getConfiguredItemCost(player.getUseItem().getItem(), "TICK");
                    if (tickCost > 0) {
                        player.stopUsingItem();
                    }
                }
            }

            if (cap.exhaustionCooldown > 0) {
                cap.exhaustionCooldown--;
            }

            boolean isExhausted = cap.stamina <= 0 || cap.exhaustionCooldown > 0;
            updateCustomPenalties(player, isExhausted);

            AttributeInstance currentStaminaAttr = player.getAttribute(StaminaAttributes.CURRENT_STAMINA.get());
            if (currentStaminaAttr != null) {
                if (Math.abs(currentStaminaAttr.getBaseValue() - cap.stamina) > 0.1) {
                    currentStaminaAttr.setBaseValue(cap.stamina);
                }
            }

            cap.lastTickStamina = cap.stamina;
            boolean penaltiesActive = cap.currentHungerPenalty > 0 || totalModPenalty > 0 || cap.fatiguePenalty > 0 || cap.poisonPenalty > 0 || cap.weightPenalty > 0 || cap.bonusStamina > 0;
            if (player.tickCount % 5 == 0 || isConsuming || penaltiesActive) {
                sync((net.minecraft.server.level.ServerPlayer) player, cap);
            }
        });
    }

    private static void refreshUniversalCache(List<? extends String> configList) {
        cachedUniversalPenalties = new ArrayList<>();
        lastUniversalConfigRef = configList;

        for (String entry : configList) {
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 7) {
                    UniversalPenaltyData data = new UniversalPenaltyData();
                    data.type = parts[0].trim().toUpperCase();
                    data.key = parts[1].trim();

                    String compRaw = parts[2].trim();
                    if (compRaw.startsWith("!")) {
                        data.isInstant = true;
                        compRaw = compRaw.substring(1);
                    }

                    data.isMultiplier = compRaw.contains("*");
                    data.isReverse = compRaw.contains("<");

                    data.threshold = Double.parseDouble(parts[3].trim());
                    data.worstValue = Double.parseDouble(parts[4].trim());
                    data.maxPenalty = Float.parseFloat(parts[5].trim());

                    if (data.type.equals("NBT")) {
                        data.nbtPath = data.key.split("\\.");
                    } else if (data.type.equals("EFFECT")) {
                        ResourceLocation loc = ResourceLocation.tryParse(data.key);
                        if (loc != null && ForgeRegistries.MOB_EFFECTS.containsKey(loc)) {
                            data.effect = ForgeRegistries.MOB_EFFECTS.getValue(loc);
                        }
                    }
                    cachedUniversalPenalties.add(data);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse universal penalty rule: " + entry);
            }
        }
    }

    private static void refreshExhaustionCache(List<? extends String> configList) {
        cachedExhaustionPenalties = new ArrayList<>();
        lastExhaustionConfigRef = configList;

        for (String entry : configList) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 3) {
                    continue;
                }

                ExhaustionPenaltyData data = new ExhaustionPenaltyData();
                data.attrName = parts[0].trim();
                data.amount = Double.parseDouble(parts[1].trim());
                int opId = Integer.parseInt(parts[2].trim());
                data.operation = AttributeModifier.Operation.fromValue(opId);
                if (data.operation == null) {
                    data.operation = AttributeModifier.Operation.MULTIPLY_TOTAL;
                }

                ResourceLocation loc = ResourceLocation.tryParse(data.attrName);
                if (loc != null) {
                    data.attribute = ForgeRegistries.ATTRIBUTES.getValue(loc);
                    if (data.attribute != null) {
                        data.uuid = UUID.nameUUIDFromBytes(("peak_stamina_exhaustion_" + data.attrName).getBytes());
                        cachedExhaustionPenalties.add(data);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    private static double getAttributeValue(Player player, net.minecraft.world.entity.ai.attributes.Attribute attr, double fallback) {
        AttributeInstance instance = player.getAttribute(attr);
        return (instance != null) ? instance.getValue() : fallback;
    }

    private static double getNbtValue(CompoundTag root, String[] path) {
        try {
            Tag currentTag = root;
            for (String part : path) {
                if (currentTag instanceof CompoundTag ct) {
                    if (!ct.contains(part)) {
                        return Double.NaN;
                    }
                    currentTag = ct.get(part);
                } else {
                    return Double.NaN;
                }
            }
            if (currentTag instanceof net.minecraft.nbt.NumericTag num) {
                return num.getAsDouble();
            }
            return Double.NaN;
        } catch (Exception e) {
            return Double.NaN;
        }
    }

    private static void updateCustomPenalties(Player player, boolean isExhausted) {
        AttributeInstance sprintAttr = player.getAttribute(StaminaAttributes.SPRINT_SPEED.get());
        if (sprintAttr != null) {
            AttributeModifier existing = sprintAttr.getModifier(EXHAUSTED_SPEED_UUID);
            if (isExhausted) {
                if (existing == null) {
                    double val = StaminaConfig.COMMON.exhaustedSpeedPenalty.get();
                    if (val != 0.0) {
                        sprintAttr.addTransientModifier(new AttributeModifier(
                                EXHAUSTED_SPEED_UUID,
                                "Peak Stamina Sprint Penalty",
                                val,
                                AttributeModifier.Operation.MULTIPLY_TOTAL
                        ));
                    }
                }
            } else {
                if (existing != null) {
                    sprintAttr.removeModifier(EXHAUSTED_SPEED_UUID);
                }
            }
        }

        List<? extends String> configList = StaminaLists.LISTS.customExhaustionPenalties.get(); 
        if (cachedExhaustionPenalties == null || configList != lastExhaustionConfigRef) {
            refreshExhaustionCache(configList);
        }

        for (ExhaustionPenaltyData data : cachedExhaustionPenalties) {
            try {
                AttributeInstance instance = player.getAttribute(data.attribute);
                if (instance == null) {
                    continue;
                }

                AttributeModifier existing = instance.getModifier(data.uuid);
                if (isExhausted) {
                    if (existing == null) {
                        instance.addTransientModifier(new AttributeModifier(data.uuid, "Stamina Exhaustion Penalty", data.amount, data.operation));
                    }
                } else {
                    if (existing != null) {
                        instance.removeModifier(data.uuid);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (event.getEntity() instanceof Player p && !p.level().isClientSide) {
            if (p.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
                return;
            }
            if (p.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
                return;
            }
            if (hasInfiniteStamina(event.getEntity())) {
                return;
            }

            float jumpCost = StaminaConfig.COMMON.depletionJump.get().floatValue();
            double jumpMult = getAttributeValue(p, StaminaAttributes.JUMP_COST_MULTIPLIER.get(), 1.0);
            double actionRecoveryMult = getAttributeValue(p, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

            if (jumpCost != 0) {
                p.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double finalCost;
                    if (jumpCost > 0) {
                        double usageMult = getAttributeValue(p, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                        finalCost = jumpCost * usageMult * jumpMult;
                    } else {
                        finalCost = jumpCost * actionRecoveryMult;
                    }

                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }

                    if (cap.stamina < 0) {
                        cap.stamina = 0;
                    }
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    if (jumpCost > 0) {
                        cap.staminaRegenDelay = getRecoveryDelay(p);
                    }

                    sync((net.minecraft.server.level.ServerPlayer) p, cap);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (!event.getEntity().level().isClientSide) {
            Player player = event.getEntity();
            if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
                return;
            }
            if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
                return;
            }
            if (hasInfiniteStamina(player)) {
                return;
            }

            float attackCost = StaminaConfig.COMMON.depletionAttack.get().floatValue();
            double attackMult = getAttributeValue(player, StaminaAttributes.ATTACK_COST_MULTIPLIER.get(), 1.0);
            double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

            if (attackCost != 0) {
                if (player.isSprinting()) {
                    player.getPersistentData().putBoolean("peak_stamina_restore_sprint", true);
                }

                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double finalCost;
                    if (attackCost > 0) {
                        double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                        finalCost = attackCost * usageMult * attackMult;
                    } else {
                        finalCost = attackCost * actionRecoveryMult;
                    }

                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }

                    if (cap.stamina < 0) {
                        cap.stamina = 0;
                    }
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    if (attackCost > 0) {
                        cap.staminaRegenDelay = getRecoveryDelay(player);
                    }

                    sync((net.minecraft.server.level.ServerPlayer) player, cap);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBroken(net.minecraftforge.event.level.BlockEvent.BreakEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (!event.getPlayer().level().isClientSide) {
            Player player = event.getPlayer();
            if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
                return;
            }
            if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
                return;
            }

            float breakCost = StaminaConfig.COMMON.depletionBlockBreak.get().floatValue();
            double breakMult = getAttributeValue(player, StaminaAttributes.BLOCK_BREAK_COST_MULTIPLIER.get(), 1.0);
            double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

            if (breakCost != 0) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    if (hasInfiniteStamina(player)) {
                        return;
                    }
                    double finalCost;
                    if (breakCost > 0) {
                        double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                        finalCost = breakCost * usageMult * breakMult;
                    } else {
                        finalCost = breakCost * actionRecoveryMult;
                    }

                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }

                    if (cap.stamina < 0) {
                        cap.stamina = 0;
                    }
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    if (breakCost > 0) {
                        cap.staminaRegenDelay = getRecoveryDelay(player);
                    }

                    sync((net.minecraft.server.level.ServerPlayer) player, cap);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }

        Player player = event.getEntity();
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        if (hasInfiniteStamina(player)) {
            return;
        }

        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        float cost = getConfiguredItemCost(stack.getItem(), "USE_ON_BLOCK");

        if (cost > 0) {

            boolean isValidAction = false;
            net.minecraft.world.level.block.state.BlockState state = event.getLevel().getBlockState(event.getPos());
            UseOnContext context = new UseOnContext(player, event.getHand(), event.getHitVec());

            net.minecraft.world.level.block.state.BlockState modifiedState = state.getToolModifiedState(context, ToolActions.AXE_STRIP, false);
            if (modifiedState == null) {
                modifiedState = state.getToolModifiedState(context, ToolActions.HOE_TILL, false);
            }
            if (modifiedState == null) {
                modifiedState = state.getToolModifiedState(context, ToolActions.SHOVEL_FLATTEN, false);
            }

            if (modifiedState != null) {
                isValidAction = true;
            }

            if (!isValidAction && (stack.is(net.minecraft.world.item.Items.FLINT_AND_STEEL) || stack.is(net.minecraft.world.item.Items.FIRE_CHARGE))) {
                net.minecraft.core.BlockPos placePos = event.getPos().relative(event.getHitVec().getDirection());
                if (BaseFireBlock.getState(event.getLevel(), placePos).canSurvive(event.getLevel(), placePos)) {
                    isValidAction = true;
                }
            }

            if (isValidAction) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                    double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

                    double finalCost;
                    if (cost > 0) {
                        finalCost = cost * usageMult;
                    } else {
                        finalCost = cost * actionRecoveryMult;
                    }

                    float totalAvailable = cap.stamina + cap.bonusStamina;

                    if (cost > 0 && totalAvailable < finalCost) {
                        event.setCanceled(true);
                        event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                    } else {
                        if (!player.level().isClientSide) {
                            if (finalCost > 0) {
                                consumeStamina(cap, (float) finalCost);
                            } else {
                                cap.stamina -= (float) finalCost;
                            }

                            if (cap.stamina > cap.maxStamina) {
                                cap.stamina = cap.maxStamina;
                            }
                            if (cap.stamina < 0) {
                                cap.stamina = 0;
                            }

                            if (cost > 0) {
                                cap.staminaRegenDelay = getRecoveryDelay(player);
                            }

                            sync((net.minecraft.server.level.ServerPlayer) player, cap);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlaced(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (event.getEntity() instanceof Player player && !player.level().isClientSide) {
            if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
                return;
            }
            if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
                return;
            }
            if (hasInfiniteStamina(player)) {
                return;
            }

            float placeCost = StaminaConfig.COMMON.depletionBlockPlace.get().floatValue();
            double placeMult = getAttributeValue(player, StaminaAttributes.BLOCK_PLACE_COST_MULTIPLIER.get(), 1.0);
            double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

            if (placeCost != 0) {
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double finalCost;
                    if (placeCost > 0) {
                        double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                        finalCost = placeCost * usageMult * placeMult;
                    } else {
                        finalCost = placeCost * actionRecoveryMult;
                    }

                    if (finalCost > 0) {
                        consumeStamina(cap, (float) finalCost);
                    } else {
                        cap.stamina -= (float) finalCost;
                    }

                    if (cap.stamina < 0) {
                        cap.stamina = 0;
                    }
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    if (placeCost > 0) {
                        cap.staminaRegenDelay = getRecoveryDelay(player);
                    }
                    sync((net.minecraft.server.level.ServerPlayer) player, cap);
                });
            }
        }
    }

    @SubscribeEvent
    public static void onItemUseTick(LivingEntityUseItemEvent.Tick event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        if (hasInfiniteStamina(player)) {
            return;
        }

        float cost = getConfiguredItemCost(event.getItem().getItem(), "TICK");
        if (cost != 0) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);
                double finalCost;

                if (cost > 0) {
                    double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                    finalCost = cost * usageMult;
                } else {
                    finalCost = cost * actionRecoveryMult;
                }

                if (finalCost > 0) {
                    consumeStamina(cap, (float) finalCost);
                } else {
                    cap.stamina -= (float) finalCost;
                }

                if (cap.stamina > cap.maxStamina) {
                    cap.stamina = cap.maxStamina;
                }
                if (cap.stamina < 0) {
                    cap.stamina = 0;
                }

                if (cost > 0) {
                    cap.staminaRegenDelay = getRecoveryDelay(player);
                }

                if (cap.stamina <= 0 && cost > 0) {
                    event.setCanceled(true);
                    player.stopUsingItem();
                    int cd = StaminaConfig.COMMON.itemInterruptionCooldown.get();
                    if (cd > 0) {
                        player.getCooldowns().addCooldown(event.getItem().getItem(), cd);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(ShieldBlockEvent event) {
        if (!StaminaConfig.COMMON.enableStamina.get() || event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        if (hasInfiniteStamina(player)) {
            return;
        }

        net.minecraft.world.item.Item usedItem = player.getUseItem().getItem();
        if (usedItem == net.minecraft.world.item.Items.AIR) {
            usedItem = player.getMainHandItem().is(net.minecraftforge.common.Tags.Items.TOOLS_SHIELDS)
                    ? player.getMainHandItem().getItem() : player.getOffhandItem().getItem();
        }

        final net.minecraft.world.item.Item itemForCooldown = usedItem;
        float[] values = getShieldValues(usedItem);
        float baseCost = values[0];
        float multiplier = values[1];

        float damage = event.getBlockedDamage();
        float cost = baseCost + (damage * multiplier);
        if (cost != 0) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);
                double finalCost;

                if (cost > 0) {
                    double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                    finalCost = cost * usageMult;
                } else {
                    finalCost = cost * actionRecoveryMult;
                }

                if (finalCost > 0) {
                    consumeStamina(cap, (float) finalCost);
                } else {
                    cap.stamina -= (float) finalCost;
                }

                if (cap.stamina > cap.maxStamina) {
                    cap.stamina = cap.maxStamina;
                }
                if (cap.stamina < 0) {
                    cap.stamina = 0;
                }

                if (cost > 0) {
                    cap.staminaRegenDelay = getRecoveryDelay(player);
                    if (cap.stamina <= 0) {
                        player.stopUsingItem();
                        int cd = StaminaConfig.COMMON.itemInterruptionCooldown.get();
                        if (cd > 0) {
                            player.getCooldowns().addCooldown(itemForCooldown, cd);
                        }
                    }
                }

                sync((net.minecraft.server.level.ServerPlayer) player, cap);
            });
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        Player player = event.getEntity();
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        if (hasInfiniteStamina(player)) {
            return;
        }
        net.minecraft.world.item.Item item = event.getItemStack().getItem();
        if (item == net.minecraft.world.item.Items.FIREWORK_ROCKET && !player.isFallFlying()) {
            return;
        }
        float useCost = getConfiguredItemCost(item, "USE");
        float tickCost = getConfiguredItemCost(item, "TICK");
        if (useCost > 0 || tickCost > 0) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                double usageMult = getAttributeValue(player, StaminaAttributes.STAMINA_USAGE.get(), 1.0);
                double actionRecoveryMult = getAttributeValue(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get(), 1.0);

                if (useCost != 0) {
                    double totalCost;
                    if (useCost > 0) {
                        totalCost = useCost * usageMult;
                    } else {
                        totalCost = useCost * actionRecoveryMult;
                    }

                    float totalAvailable = cap.stamina + cap.bonusStamina;

                    if (useCost > 0 && totalAvailable < totalCost) {
                        event.setCanceled(true);
                        event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                        return;
                    }
                    
                    if (totalCost > 0) {
                        consumeStamina(cap, (float) totalCost);
                    } else {
                        cap.stamina -= (float) totalCost;
                    }
                    
                    if (useCost > 0) {
                        cap.staminaRegenDelay = getRecoveryDelay(player);
                    }

                    if (!player.level().isClientSide) {
                        sync((net.minecraft.server.level.ServerPlayer) player, cap);
                    }
                }

                if (tickCost > 0) {
                    double totalTickCost = tickCost * usageMult;
                    float totalAvailable = cap.stamina + cap.bonusStamina;

                    if (totalAvailable < totalTickCost) {
                        event.setCanceled(true);
                        event.setCancellationResult(net.minecraft.world.InteractionResult.FAIL);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        if (!StaminaConfig.COMMON.enableStamina.get() || event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }

        ResourceLocation itemReg = ForgeRegistries.ITEMS.getKey(event.getItem().getItem());
        if (itemReg == null) {
            return;
        }
        String itemId = itemReg.toString();

        List<? extends String> modifiers = StaminaLists.LISTS.consumableValues.get();
        for (String entry : modifiers) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 2) {
                    continue;
                }

                String configId = parts[0].trim();
                if (!configId.equals(itemId)) {
                    continue;
                }

                double instantAmount = 0.0;
                double regenAmount = 0.0;
                double penaltyResistStrength = 0.0;
                double poisonAmount = 0.0;
                double bonusAmount = 0.0;
                int durationTicks = 0;
                boolean isInstantFound = false;
                boolean isRegenFound = false;
                boolean isPenaltyFound = false;
                boolean isPoisonFound = false;
                boolean isBonusFound = false;
                List<java.util.AbstractMap.SimpleEntry<String, Double>> specificCures = new ArrayList<>();

                for (int i = 1; i < parts.length; i++) {
                    String token = parts[i].trim().toUpperCase();
                    if (token.equals("INSTANT") && i + 1 < parts.length) {
                        instantAmount = Double.parseDouble(parts[++i].trim());
                        isInstantFound = true;
                    } else if (token.equals("BONUS") && i + 1 < parts.length) {
                        bonusAmount = Double.parseDouble(parts[++i].trim());
                        isBonusFound = true;
                    } else if (token.equals("REGEN") && i + 2 < parts.length) {
                        regenAmount = Double.parseDouble(parts[++i].trim());
                        int sec = Integer.parseInt(parts[++i].trim());
                        durationTicks = sec > 0 ? sec * 20 : -1;
                        isRegenFound = true;
                    } else if (token.equals("PENALTY") && i + 1 < parts.length) {
                        penaltyResistStrength = Double.parseDouble(parts[++i].trim());
                        isPenaltyFound = true;
                    } else if (token.equals("POISON") && i + 1 < parts.length) {
                        poisonAmount = Double.parseDouble(parts[++i].trim());
                        isPoisonFound = true;
                    } else if (token.equals("CURE") && i + 2 < parts.length) {
                        String target = parts[++i].trim();
                        double amount = Double.parseDouble(parts[++i].trim());
                        specificCures.add(new java.util.AbstractMap.SimpleEntry<>(target, amount));
                    }
                }

                final boolean isInstant = isInstantFound;
                final boolean isRegen = isRegenFound;
                final boolean isPenalty = isPenaltyFound;
                final boolean isPoison = isPoisonFound;
                final boolean isBonus = isBonusFound;
                final double finalInstant = instantAmount;
                final double finalRegen = regenAmount;
                final double finalPenaltyResist = penaltyResistStrength;
                final double finalPoison = poisonAmount;
                final double finalBonus = bonusAmount;
                final int finalDuration = durationTicks;
                player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                    double baseAttr = 100.0;
                    AttributeInstance attr = player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
                    if (attr != null) {
                        baseAttr = attr.getValue();
                    }

                    if (isPoison) {
                        double maxPoison = StaminaConfig.COMMON.maxPoisonPenalty.get();
                        double amount = finalPoison * getAttributeValue(player, StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER.get(), 1.0);

                        float minMax = StaminaConfig.COMMON.minMaxStamina.get().floatValue();
                        float maxAllowedPenalty = (float) baseAttr - minMax;

                        float currentModSum = 0;
                        if (cap.penaltyValues != null) {
                            for (float f : cap.penaltyValues) {
                                currentModSum += f;
                            }
                        }
                        float otherPenalties = cap.fatiguePenalty + cap.currentHungerPenalty + currentModSum;
                        float room = Math.max(0, maxAllowedPenalty - otherPenalties);

                        float potentialPoison = cap.poisonPenalty + (float) amount;
                        potentialPoison = Math.min(potentialPoison, (float) maxPoison);
                        potentialPoison = Math.min(potentialPoison, room);

                        cap.poisonPenalty = potentialPoison;
                        cap.poisonTimer = StaminaConfig.COMMON.poisonDecayDelay.get() * 20;
                    }

                    if (isPenalty) {
                        int finalPenDuration = StaminaConfig.COMMON.penaltyReliefDuration.get() * 20;
                        double addedReduction = Math.min(finalPenaltyResist / 100.0, 0.50);
                        double totalReduction = 0.30 + addedReduction;
                        double modifierValue = -totalReduction;
                        String attrName = StaminaAttributes.PENALTY_GAIN_MULTIPLIER.getId().toString();
                        int op = 1;

                        StaminaCapability.BuffInstance existingBuff = null;
                        for (StaminaCapability.BuffInstance b : cap.activeBuffs) {
                            if (b.attributeName.equals(attrName)) {
                                existingBuff = b;
                                break;
                            }
                        }

                        boolean shouldApply = false;
                        if (existingBuff == null) {
                            shouldApply = true;
                        } else {
                            if (modifierValue < existingBuff.amount) {
                                shouldApply = true;
                            }
                        }

                        if (shouldApply) {
                            if (existingBuff != null) {
                                cap.activeBuffs.remove(existingBuff);
                                removeBuffModifier(player, existingBuff);
                            }
                            StaminaCapability.BuffInstance newBuff = new StaminaCapability.BuffInstance(attrName, modifierValue, op, finalPenDuration);
                            cap.activeBuffs.add(newBuff);
                            applyBuffModifier(player, newBuff);
                        }
                    }

                    for (java.util.AbstractMap.SimpleEntry<String, Double> cure : specificCures) {
                        String target = cure.getKey();
                        float amount = cure.getValue().floatValue();

                        if (target.equalsIgnoreCase("ALL")) {
                            cap.fatiguePenalty = Math.max(0.0f, cap.fatiguePenalty - amount);
                            cap.currentHungerPenalty = Math.max(0.0f, cap.currentHungerPenalty - amount);
                            cap.poisonPenalty = Math.max(0.0f, cap.poisonPenalty - amount);
                            if (cap.penaltyValues != null) {
                                for (int i = 0; i < cap.penaltyValues.length; i++) {
                                    cap.penaltyValues[i] = Math.max(0.0f, cap.penaltyValues[i] - amount);
                                }
                            }
                        } else if (target.equalsIgnoreCase("FATIGUE")) {
                            cap.fatiguePenalty = Math.max(0.0f, cap.fatiguePenalty - amount);
                        } else if (target.equalsIgnoreCase("HUNGER")) {
                            cap.currentHungerPenalty = Math.max(0.0f, cap.currentHungerPenalty - amount);
                        } else if (target.equalsIgnoreCase("POISON")) {
                            cap.poisonPenalty = Math.max(0.0f, cap.poisonPenalty - amount);
                        } else {
                            if (cachedUniversalPenalties != null && cap.penaltyValues != null) {
                                for (int i = 0; i < cachedUniversalPenalties.size(); i++) {
                                     if (i >= cap.penaltyValues.length) {
                                        break;
                                    }
                                    UniversalPenaltyData data = cachedUniversalPenalties.get(i);
                                    if (data.key.equals(target) || data.key.endsWith(":" + target) || (target.contains(":") && data.key.equals(target))) {
                                        cap.penaltyValues[i] = Math.max(0.0f, cap.penaltyValues[i] - amount);
                                    }
                                }
                            }
                        }
                    }

                    float minMax = StaminaConfig.COMMON.minMaxStamina.get().floatValue();
                    float totalModPenalty = 0.0f;
                    if (cap.penaltyValues != null) {
                        for (float f : cap.penaltyValues) {
                            totalModPenalty += f;
                        }
                    }

                    float effectiveMax = (float) baseAttr - cap.fatiguePenalty - cap.currentHungerPenalty - cap.poisonPenalty - totalModPenalty - cap.weightPenalty;
                    if (effectiveMax < minMax) {
                        effectiveMax = minMax;
                    }
                    cap.maxStamina = effectiveMax;
                    if (cap.stamina > cap.maxStamina) {
                        cap.stamina = cap.maxStamina;
                    }

                    if (isInstant) {
                        float amountToAdd = (float) finalInstant;
                        float space = cap.maxStamina - cap.stamina;
                        
                        if (amountToAdd <= space) {
                            cap.stamina += amountToAdd;
                        } else {
                            cap.stamina = cap.maxStamina;
                            if (StaminaConfig.COMMON.enableExcessStaminaConversion.get()) {
                                float excess = amountToAdd - space;
     
                                double conversionMult = getAttributeValue(player, StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER.get(), 1.0);
                                float conversionRate = StaminaConfig.COMMON.excessConversionRate.get().floatValue() * (float) conversionMult;
                                
                                float bonusToAdd = excess * conversionRate;
                                if (bonusToAdd > 0) {
                                    cap.bonusStamina += bonusToAdd;
                                }
                            }
                        }
                        
                        if (finalInstant > 0) {
                            cap.staminaRegenDelay = 0;

                            double delayMult = getAttributeValue(player, StaminaAttributes.BONUS_STAMINA_DECAY_DELAY.get(), 1.0);
                            cap.bonusStaminaDecayTimer = (int) (StaminaConfig.COMMON.bonusStaminaDecayDelay.get() * delayMult);
                        }
                    }

                    if (isBonus) {
                        cap.bonusStamina += (float) finalBonus;

                        double delayMult = getAttributeValue(player, StaminaAttributes.BONUS_STAMINA_DECAY_DELAY.get(), 1.0);
                        cap.bonusStaminaDecayTimer = (int) (StaminaConfig.COMMON.bonusStaminaDecayDelay.get() * delayMult);
                    }

                    double capacityMult = getAttributeValue(player, StaminaAttributes.BONUS_STAMINA_CAPACITY.get(), 1.0);
                    float maxBonus = cap.maxStamina * (float) capacityMult;
                    if (cap.bonusStamina > maxBonus) {
                        cap.bonusStamina = maxBonus;
                    }

                    if (isRegen) {
                        String attrName = StaminaAttributes.STAMINA_REGEN.getId().toString();
                        int op = 1;

                        StaminaCapability.BuffInstance existingBuff = null;
                        for (StaminaCapability.BuffInstance b : cap.activeBuffs) {
                            if (b.attributeName.equals(attrName)) {
                                existingBuff = b;
                                break;
                            }
                        }

                        boolean shouldApply = false;
                        if (existingBuff == null) {
                            shouldApply = true;
                        } else {
                            if (finalRegen < 0) {
                                if (finalRegen < existingBuff.amount) {
                                    shouldApply = true;
                                }
                            } else {
                                if (existingBuff.amount >= 0 && finalRegen > existingBuff.amount) {
                                    shouldApply = true;
                                }
                            }
                        }

                        if (shouldApply) {
                            if (existingBuff != null) {
                                cap.activeBuffs.remove(existingBuff);
                                removeBuffModifier(player, existingBuff);
                            }
                            StaminaCapability.BuffInstance newBuff = new StaminaCapability.BuffInstance(attrName, finalRegen, op, finalDuration);
                            cap.activeBuffs.add(newBuff);
                            applyBuffModifier(player, newBuff);
                        }
                    }

                    sync((net.minecraft.server.level.ServerPlayer) player, cap);
                });

            } catch (Exception e) {
                LOGGER.error("Failed to parse consumable modifier: " + entry, e);
            }
        }
    }

    private static void applyBuffModifier(Player player, StaminaCapability.BuffInstance buff) {
        try {
            ResourceLocation loc = ResourceLocation.tryParse(buff.attributeName);
            if (loc == null) {
                return;
            }
            net.minecraft.world.entity.ai.attributes.Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(loc);
            if (attr == null) {
                return;
            }

            AttributeInstance inst = player.getAttribute(attr);
            if (inst == null) {
                return;
            }

            UUID id = UUID.nameUUIDFromBytes(("peak_stamina_buff_" + buff.attributeName).getBytes());
            if (inst.getModifier(id) == null) {
                AttributeModifier.Operation op = AttributeModifier.Operation.fromValue(buff.operation);
                inst.addTransientModifier(new AttributeModifier(id, "Peak Stamina Food Buff", buff.amount, op));
            }
        } catch (Exception ignored) {
        }
    }

    private static void removeBuffModifier(Player player, StaminaCapability.BuffInstance buff) {
        try {
            ResourceLocation loc = ResourceLocation.tryParse(buff.attributeName);
            if (loc == null) {
                return;
            }
            net.minecraft.world.entity.ai.attributes.Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(loc);
            if (attr == null) {
                return;
            }

            AttributeInstance inst = player.getAttribute(attr);
            if (inst == null) {
                return;
            }

            UUID id = UUID.nameUUIDFromBytes(("peak_stamina_buff_" + buff.attributeName).getBytes());
            inst.removeModifier(id);
        } catch (Exception ignored) {
        }
    }

    private static float getConfiguredItemCost(net.minecraft.world.item.Item item, String actionType) {
        ResourceLocation itemReg = ForgeRegistries.ITEMS.getKey(item);
        if (itemReg == null) {
            return 0.0f;
        }
        String itemId = itemReg.toString();

        List<? extends String> costs = StaminaLists.LISTS.itemCosts.get();
        for (String entry : costs) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 3) {
                    continue;
                }

                String configId = parts[0].trim();
                if (!configId.equals(itemId)) {
                    continue;
                }

                int i = 1;
                while (i < parts.length - 1) {
                    String configType = parts[i].trim().toUpperCase();
                    if (configType.equals("BLOCK")) {
                        if (configType.equals(actionType)) {
                            return Float.parseFloat(parts[i + 1].trim());
                        }
                        i += 3;
                    } else {
                        if (configType.equals(actionType)) {
                            return Float.parseFloat(parts[i + 1].trim());
                        }
                        i += 2;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        List<? extends String> tagCosts = StaminaLists.LISTS.itemCostTags.get();
        for (String entry : tagCosts) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 3) {
                    continue;
                }

                String configId = parts[0].trim();
                ResourceLocation tagLoc = ResourceLocation.tryParse(configId);
                if (tagLoc != null) {
                    net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tagKey = net.minecraft.tags.TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagLoc);
                    if (item.builtInRegistryHolder().is(tagKey)) {
                        int i = 1;
                        while (i < parts.length - 1) {
                            String configType = parts[i].trim().toUpperCase();
                            if (configType.equals("BLOCK")) {
                                if (configType.equals(actionType)) {
                                    return Float.parseFloat(parts[i + 1].trim());
                                }
                                i += 3;
                            } else {
                                if (configType.equals(actionType)) {
                                    return Float.parseFloat(parts[i + 1].trim());
                                }
                                i += 2;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return 0.0f;
    }

    private static float[] getShieldValues(net.minecraft.world.item.Item item) {
        ResourceLocation itemReg = ForgeRegistries.ITEMS.getKey(item);
        if (itemReg == null) {
            return new float[]{0f, 0f};
        }
        String itemId = itemReg.toString();

        List<? extends String> costs = StaminaLists.LISTS.itemCosts.get();
        for (String entry : costs) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 3) {
                    continue;
                }

                String configId = parts[0].trim();
                if (!configId.equals(itemId)) {
                    continue;
                }

                int i = 1;
                while (i < parts.length - 1) {
                    String configType = parts[i].trim().toUpperCase();
                    if (configType.equals("BLOCK")) {
                        float base = 0f;
                        float mult = 0f;
                        if (i + 1 < parts.length) {
                            base = Float.parseFloat(parts[i + 1].trim());
                        }
                        if (i + 2 < parts.length) {
                            mult = Float.parseFloat(parts[i + 2].trim());
                        }
                        return new float[]{base, mult};
                    } else {
                        i += 2;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        List<? extends String> tagCosts = StaminaLists.LISTS.itemCostTags.get();
        for (String entry : tagCosts) {
            try {
                String[] parts = entry.split(";");
                if (parts.length < 3) {
                    continue;
                }

                String configId = parts[0].trim();
                ResourceLocation tagLoc = ResourceLocation.tryParse(configId);
                if (tagLoc != null) {
                    net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tagKey = net.minecraft.tags.TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), tagLoc);
                    if (item.builtInRegistryHolder().is(tagKey)) {
                        int i = 1;
                        while (i < parts.length - 1) {
                            String configType = parts[i].trim().toUpperCase();
                            if (configType.equals("BLOCK")) {
                                float base = 0f;
                                float mult = 0f;
                                if (i + 1 < parts.length) {
                                    base = Float.parseFloat(parts[i + 1].trim());
                                }
                                if (i + 2 < parts.length) {
                                    mult = Float.parseFloat(parts[i + 2].trim());
                                }
                                return new float[]{base, mult};
                            } else {
                                i += 2;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return new float[]{0f, 0f};
    }
}