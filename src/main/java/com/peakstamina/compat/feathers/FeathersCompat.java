package com.peakstamina.compat.feathers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

public class FeathersCompat {

    private static boolean isFeathersLoaded = false;
    private static final UUID PEAK_FEATHER_REMOVAL_UUID = UUID.fromString("f00ba740-1111-2222-3333-ba5e57a314a1");

    public static void init() {
        if (ModList.get().isLoaded("feathers")) {
            isFeathersLoaded = true;
            MinecraftForge.EVENT_BUS.register(FeathersCompat.class);
        }
    }

    public static boolean isLoaded() {
        return isFeathersLoaded;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!isFeathersLoaded || event.phase != TickEvent.Phase.END || event.side.isClient()) return;

        Attribute maxFeathersAttr = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation("feathers", "max_feathers"));
        
        if (maxFeathersAttr != null) {
            AttributeInstance instance = event.player.getAttribute(maxFeathersAttr);
            if (instance != null && instance.getModifier(PEAK_FEATHER_REMOVAL_UUID) == null) {
                instance.addTransientModifier(new AttributeModifier(
                        PEAK_FEATHER_REMOVAL_UUID, 
                        "PeakStamina Feather Hider", 
                        -100.0, 
                        AttributeModifier.Operation.ADDITION
                ));
            }
        }
    }
}