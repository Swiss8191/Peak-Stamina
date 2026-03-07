package com.peakstamina.handlers;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.config.ExperimentalConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID)
public class MobStaminaHandler {

    private static final UUID MOB_EXHAUSTION_UUID = UUID.nameUUIDFromBytes("peakstamina_exhausted_mob".getBytes());

    private static final Map<String, MobStaminaData> MOB_STAMINA_CACHE = new HashMap<>();
    private static List<? extends String> lastProfilesRef = null;
    private static List<? extends String> lastMobsRef = null;

    private static class MobStaminaData {

        int maxAttacks;
        int exhaustionTicks;
        Map<String, Double> attributeModifiers;

        public MobStaminaData(int maxAttacks, int exhaustionTicks, Map<String, Double> attributeModifiers) {
            this.maxAttacks = maxAttacks;
            this.exhaustionTicks = exhaustionTicks;
            this.attributeModifiers = attributeModifiers;
        }
    }

    private static void refreshCacheIfNeeded() {
        List<? extends String> currentProfiles = ExperimentalConfig.EXPERIMENTAL.exhaustionProfiles.get();
        List<? extends String> currentMobs = ExperimentalConfig.EXPERIMENTAL.customMobStamina.get();

        if (currentProfiles != lastProfilesRef || currentMobs != lastMobsRef) {
            MOB_STAMINA_CACHE.clear();
            lastProfilesRef = currentProfiles;
            lastMobsRef = currentMobs;

            Map<String, Map<String, Double>> profileMap = new HashMap<>();
            for (String entry : currentProfiles) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 2) {
                        String profileName = parts[0].trim();
                        Map<String, Double> attrs = new HashMap<>();

                        String[] attrPairs = parts[1].split(",");
                        for (String pair : attrPairs) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                attrs.put(kv[0].trim(), Double.parseDouble(kv[1].trim()));
                            }
                        }
                        profileMap.put(profileName, attrs);
                    }
                } catch (Exception ignored) {
                }
            }

            for (String entry : currentMobs) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 4) {
                        String id = parts[0].trim();
                        int attacks = Integer.parseInt(parts[1].trim());
                        int ticks = Integer.parseInt(parts[2].trim());
                        String profileName = parts[3].trim();

                        Map<String, Double> linkedAttrs = profileMap.getOrDefault(profileName, new HashMap<>());
                        MOB_STAMINA_CACHE.put(id, new MobStaminaData(attacks, ticks, linkedAttrs));
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @SubscribeEvent
    public static void onMobAttack(LivingAttackEvent event) {
        if (!ExperimentalConfig.EXPERIMENTAL.enableMobStamina.get()) {
            return;
        }

        if (event.getSource().getEntity() instanceof Mob mob && event.getSource().getDirectEntity() == mob) {
            if (!mob.level().isClientSide) {
                processMobAttack(mob);
            }
        }
    }

    @SubscribeEvent
    public static void onProjectileFired(EntityJoinLevelEvent event) {
        if (!ExperimentalConfig.EXPERIMENTAL.enableMobStamina.get()) {
            return;
        }

        if (event.getEntity() instanceof Projectile projectile && !event.getLevel().isClientSide) {
            if (projectile.getOwner() instanceof Mob mob) {

                processMobAttack(mob);

                if (mob.getPersistentData().getInt("peak_exhaustion_timer") > 0 && projectile instanceof AbstractArrow arrow) {
                    refreshCacheIfNeeded();
                    ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
                    if (typeId != null) {
                        MobStaminaData data = MOB_STAMINA_CACHE.get(typeId.toString());
                        if (data != null && data.attributeModifiers.containsKey("minecraft:generic.attack_damage")) {

                            double damagePenalty = data.attributeModifiers.get("minecraft:generic.attack_damage");
                            double multiplier = Math.max(0.1, 1.0 + damagePenalty);

                            arrow.setDeltaMovement(arrow.getDeltaMovement().scale(multiplier));
                            arrow.setBaseDamage(arrow.getBaseDamage() * multiplier);
                        }
                    }
                }
            }
        }
    }

    private static void processMobAttack(Mob mob) {
        refreshCacheIfNeeded();
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (typeId == null) {
            return;
        }

        String mobId = typeId.toString();
        if (!MOB_STAMINA_CACHE.containsKey(mobId)) {
            return;
        }

        MobStaminaData data = MOB_STAMINA_CACHE.get(mobId);

        if (mob.getPersistentData().getInt("peak_exhaustion_timer") > 0) {
            return;
        }

        int attacksMade = mob.getPersistentData().getInt("peak_attacks_made") + 1;

        if (attacksMade >= data.maxAttacks) {
            mob.getPersistentData().putInt("peak_exhaustion_timer", data.exhaustionTicks);
            mob.getPersistentData().putInt("peak_attacks_made", 0);
            applyExhaustionDebuffs(mob, data);
        } else {
            mob.getPersistentData().putInt("peak_attacks_made", attacksMade);
        }
    }

    @SubscribeEvent
    public static void onMobTick(LivingEvent.LivingTickEvent event) {
        if (!ExperimentalConfig.EXPERIMENTAL.enableMobStamina.get()) {
            return;
        }

        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide) {
            if (!mob.getPersistentData().contains("peak_exhaustion_timer")) {
                return;
            }

            int timer = mob.getPersistentData().getInt("peak_exhaustion_timer");
            if (timer > 0) {
                timer--;
                mob.getPersistentData().putInt("peak_exhaustion_timer", timer);

                if (timer % 15 == 0 && mob.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.SPLASH,
                            mob.getX() + (Math.random() - 0.5) * mob.getBbWidth(),
                            mob.getY() + mob.getBbHeight() + 0.2,
                            mob.getZ() + (Math.random() - 0.5) * mob.getBbWidth(),
                            2, 0.1, 0.1, 0.1, 0.0
                    );
                }

                if (timer <= 0) {
                    removeExhaustionDebuffs(mob);
                }
            }
        }
    }

    private static void applyExhaustionDebuffs(Mob mob, MobStaminaData data) {
        for (Map.Entry<String, Double> entry : data.attributeModifiers.entrySet()) {
            ResourceLocation attrId = ResourceLocation.tryParse(entry.getKey());
            if (attrId != null && ForgeRegistries.ATTRIBUTES.containsKey(attrId)) {
                Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attrId);
                if (attr != null) {
                    AttributeInstance inst = mob.getAttribute(attr);
                    if (inst != null && inst.getModifier(MOB_EXHAUSTION_UUID) == null) {
                        inst.addTransientModifier(new AttributeModifier(
                                MOB_EXHAUSTION_UUID,
                                "Peak Stamina Mob Exhaustion",
                                entry.getValue(),
                                AttributeModifier.Operation.MULTIPLY_TOTAL
                        ));
                    }
                }
            }
        }
    }

    private static void removeExhaustionDebuffs(Mob mob) {
        refreshCacheIfNeeded();
        ResourceLocation typeId = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (typeId != null) {
            MobStaminaData data = MOB_STAMINA_CACHE.get(typeId.toString());
            if (data != null) {
                for (String attrKey : data.attributeModifiers.keySet()) {
                    ResourceLocation attrId = ResourceLocation.tryParse(attrKey);
                    if (attrId != null && ForgeRegistries.ATTRIBUTES.containsKey(attrId)) {
                        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attrId);
                        if (attr != null) {
                            AttributeInstance inst = mob.getAttribute(attr);
                            if (inst != null) {
                                inst.removeModifier(MOB_EXHAUSTION_UUID);
                            }
                        }
                    }
                }
            }
        }
    }
}
