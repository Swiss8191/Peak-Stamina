package com.peakstamina.handlers;

import java.util.HashMap;
import java.util.Map;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.config.ExperimentalConfig;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = PeakStaminaMod.MODID)
public class MobStaminaHandler {

    private static final ResourceLocation MOB_EXHAUSTION_ID = ResourceLocation.fromNamespaceAndPath(PeakStaminaMod.MODID, "mob_exhaustion");

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

    private static final Map<String, MobStaminaData> MOB_STAMINA_CACHE = new HashMap<>();

    public static void refreshCache() {
        MOB_STAMINA_CACHE.clear();
        Map<String, Map<String, Double>> profileMap = new HashMap<>();

        for (String entry : ExperimentalConfig.EXPERIMENTAL.exhaustionProfiles.get()) {
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
            } catch (Exception ignored) {}
        }

        for (String entry : ExperimentalConfig.EXPERIMENTAL.customMobStamina.get()) {
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
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public static void onMobAttack(LivingIncomingDamageEvent event) {
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
                    ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
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
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (typeId == null) {
            return;
        }
        String mobId = typeId.toString();
        if (!MOB_STAMINA_CACHE.containsKey(mobId)) {
            return;
        }

        MobStaminaData data = MOB_STAMINA_CACHE.get(mobId);
        int currentAttacks = mob.getPersistentData().getInt("peak_attack_count");
        int exhaustionTimer = mob.getPersistentData().getInt("peak_exhaustion_timer");

        if (exhaustionTimer <= 0) {
            currentAttacks++;
            if (currentAttacks >= data.maxAttacks) {
                mob.getPersistentData().putInt("peak_exhaustion_timer", data.exhaustionTicks);
                mob.getPersistentData().putInt("peak_attack_count", 0);
                applyExhaustionDebuffs(mob, data);
            } else {
                mob.getPersistentData().putInt("peak_attack_count", currentAttacks);
            }
        }
    }

    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Pre event) {
        if (!ExperimentalConfig.EXPERIMENTAL.enableMobStamina.get()) return;

        if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide) {
            int exhaustionTimer = mob.getPersistentData().getInt("peak_exhaustion_timer");
            if (exhaustionTimer > 0) {
                exhaustionTimer--;
                mob.getPersistentData().putInt("peak_exhaustion_timer", exhaustionTimer);

                if (exhaustionTimer <= 0) {
                    removeExhaustionDebuffs(mob);
                } else if (ExperimentalConfig.EXPERIMENTAL.enableExhaustionParticles.get() && mob.tickCount % 5 == 0) {
                    ((net.minecraft.server.level.ServerLevel) mob.level()).sendParticles(
                            net.minecraft.core.particles.ParticleTypes.SPLASH,
                            mob.getX(), mob.getY() + mob.getBbHeight(), mob.getZ(),
                            1, 0.2, 0.2, 0.2, 0.0
                    );
                }
            }
        }
    }

    private static void applyExhaustionDebuffs(Mob mob, MobStaminaData data) {
        for (Map.Entry<String, Double> entry : data.attributeModifiers.entrySet()) {
            ResourceLocation attrId = ResourceLocation.tryParse(entry.getKey());
            if (attrId != null && BuiltInRegistries.ATTRIBUTE.containsKey(attrId)) {
                var attr = BuiltInRegistries.ATTRIBUTE.getHolder(attrId).orElse(null);
                if (attr != null) {
                    AttributeInstance inst = mob.getAttribute(attr);
                    if (inst != null && inst.getModifier(MOB_EXHAUSTION_ID) == null) {
                        inst.addTransientModifier(new AttributeModifier(
                                MOB_EXHAUSTION_ID,
                                entry.getValue(),
                                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                        ));
                    }
                }
            }
        }
    }

    private static void removeExhaustionDebuffs(Mob mob) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        if (typeId != null) {
            MobStaminaData data = MOB_STAMINA_CACHE.get(typeId.toString());
            if (data != null) {
                for (String attrKey : data.attributeModifiers.keySet()) {
                    ResourceLocation attrId = ResourceLocation.tryParse(attrKey);
                    if (attrId != null && BuiltInRegistries.ATTRIBUTE.containsKey(attrId)) {
                        var attr = BuiltInRegistries.ATTRIBUTE.getHolder(attrId).orElse(null);
                        if (attr != null) {
                            AttributeInstance inst = mob.getAttribute(attr);
                            if (inst != null) {
                                inst.removeModifier(MOB_EXHAUSTION_ID);
                            }
                        }
                    }
                }
            }
        }
    }
}