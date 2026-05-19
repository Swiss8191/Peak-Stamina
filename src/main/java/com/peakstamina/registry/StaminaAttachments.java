package com.peakstamina.registry;

import java.util.function.Supplier;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.data.StaminaData;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class StaminaAttachments {
    
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PeakStaminaMod.MODID);

    public static final Supplier<AttachmentType<StaminaData>> STAMINA = ATTACHMENT_TYPES.register("stamina",
            () -> AttachmentType.serializable(() -> new StaminaData()).copyOnDeath().build());
}