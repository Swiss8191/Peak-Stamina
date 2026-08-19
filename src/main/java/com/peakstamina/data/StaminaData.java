package com.peakstamina.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class StaminaData implements INBTSerializable<CompoundTag> {
    public transient Map<String, Double> cachedNbtValues = new HashMap<>();
    public transient float lastSyncedStamina = -1.0f;

    public float stamina = 100.0f;
    public float maxStamina = 100.0f;
    public int staminaRegenDelay = 0; 
    public int fatigueTimer = 0;
    public float fatiguePenalty = 0.0f;
    public int penaltyRegenDelay = 0; 
    public float lastTickStamina = 100.0f;

    public int exhaustionCooldown = 0;
    public float currentHungerPenalty = 0.0f;
    public float[] penaltyValues = new float[0];
    public int[] buffCooldowns = new int[0]; 
    
    public float poisonPenalty = 0.0f;
    public int poisonTimer = 0; 

    public float weightPenalty = 0.0f;
    public float bonusStamina = 0.0f;
    public int bonusStaminaDecayTimer = 0;
    public int waterExhaustionTimer = 0;

    public String currentParCoolAction = null;
    public List<BuffInstance> activeBuffs = new ArrayList<>();

    public void copyFrom(StaminaData other) {
        this.stamina = other.stamina;
        this.maxStamina = other.maxStamina;
        this.staminaRegenDelay = other.staminaRegenDelay;
        this.fatigueTimer = other.fatigueTimer;
        this.fatiguePenalty = other.fatiguePenalty;
        this.penaltyRegenDelay = other.penaltyRegenDelay;
        this.lastTickStamina = other.lastTickStamina;
        this.exhaustionCooldown = other.exhaustionCooldown;
        this.currentHungerPenalty = other.currentHungerPenalty;
        this.penaltyValues = other.penaltyValues;
        this.buffCooldowns = other.buffCooldowns;
        this.poisonPenalty = other.poisonPenalty;
        this.poisonTimer = other.poisonTimer;
        this.waterExhaustionTimer = other.waterExhaustionTimer;
        this.weightPenalty = other.weightPenalty;
        this.bonusStamina = other.bonusStamina;
        this.bonusStaminaDecayTimer = other.bonusStaminaDecayTimer;
        
        this.activeBuffs.clear();
        this.activeBuffs.addAll(other.activeBuffs);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("Stamina", stamina);
        tag.putFloat("MaxStamina", maxStamina);
        tag.putInt("RegenDelay", staminaRegenDelay);
        tag.putInt("FatigueTimer", fatigueTimer);
        tag.putFloat("FatiguePenalty", fatiguePenalty);
        tag.putInt("PenaltyDelay", penaltyRegenDelay);
        tag.putInt("ExhaustionCooldown", exhaustionCooldown);
        tag.putFloat("HungerPenalty", currentHungerPenalty);
        tag.putFloat("PoisonPenalty", poisonPenalty);
        tag.putInt("PoisonTimer", poisonTimer);
        tag.putInt("WaterExhaustion", waterExhaustionTimer);
        tag.putFloat("WeightPenalty", weightPenalty);
        tag.putFloat("BonusStamina", bonusStamina);
        tag.putInt("BonusDecayTimer", bonusStaminaDecayTimer);

        tag.putInt("PenaltyCount", penaltyValues.length);
        for(int i=0; i<penaltyValues.length; i++) {
            tag.putFloat("PVal"+i, penaltyValues[i]);
        }
        
        tag.putIntArray("BuffCooldowns", buffCooldowns);
        
        ListTag buffsTag = new ListTag();
        for (BuffInstance buff : activeBuffs) {
            buffsTag.add(buff.save());
        }
        tag.put("ActiveBuffs", buffsTag);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        stamina = nbt.getFloat("Stamina");
        maxStamina = nbt.getFloat("MaxStamina");
        staminaRegenDelay = nbt.getInt("RegenDelay");
        fatigueTimer = nbt.getInt("FatigueTimer");
        fatiguePenalty = nbt.getFloat("FatiguePenalty");
        penaltyRegenDelay = nbt.getInt("PenaltyDelay");
        exhaustionCooldown = nbt.getInt("ExhaustionCooldown");
        currentHungerPenalty = nbt.getFloat("HungerPenalty");
        poisonPenalty = nbt.getFloat("PoisonPenalty");
        poisonTimer = nbt.getInt("PoisonTimer");
        waterExhaustionTimer = nbt.getInt("WaterExhaustion");
        weightPenalty = nbt.getFloat("WeightPenalty");
        bonusStamina = nbt.getFloat("BonusStamina");
        bonusStaminaDecayTimer = nbt.getInt("BonusDecayTimer");
        
        int count = nbt.getInt("PenaltyCount");
        penaltyValues = new float[count];
        for(int i=0; i<count; i++) {
            penaltyValues[i] = nbt.getFloat("PVal"+i);
        }
        
        if (nbt.contains("BuffCooldowns")) {
            buffCooldowns = nbt.getIntArray("BuffCooldowns");
        }
        
        activeBuffs.clear();
        if (nbt.contains("ActiveBuffs")) {
            ListTag buffsTag = nbt.getList("ActiveBuffs", Tag.TAG_COMPOUND);
            for (Tag t : buffsTag) {
                if (t instanceof CompoundTag ct) {
                    activeBuffs.add(BuffInstance.load(ct));
                }
            }
        }
    }

    public static class BuffInstance {
        public String attributeName;
        public double amount;
        public int operation;
        public int durationTicks;
        public String sourceItemId; // registry name of the item/potion that applied this buff, or null

        public BuffInstance(String attr, double amt, int op, int ticks) {
            this(attr, amt, op, ticks, null);
        }

        public BuffInstance(String attr, double amt, int op, int ticks, String sourceItemId) {
            this.attributeName = attr;
            this.amount = amt;
            this.operation = op;
            this.durationTicks = ticks;
            this.sourceItemId = sourceItemId;
        }
        
        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("Attr", attributeName);
            tag.putDouble("Amnt", amount);
            tag.putInt("Op", operation);
            tag.putInt("Dur", durationTicks);
            if (sourceItemId != null) {
                tag.putString("SrcItem", sourceItemId);
            }
            return tag;
        }
        
        public static BuffInstance load(CompoundTag tag) {
            return new BuffInstance(
                tag.getString("Attr"),
                tag.getDouble("Amnt"),
                tag.getInt("Op"),
                tag.getInt("Dur"),
                tag.contains("SrcItem") ? tag.getString("SrcItem") : null
            );
        }
    }
}