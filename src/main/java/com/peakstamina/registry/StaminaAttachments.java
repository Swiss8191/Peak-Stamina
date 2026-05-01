package com.peakstamina.registry;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.data.StaminaData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class StaminaAttachments {
    
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = 
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, PeakStaminaMod.MODID);

    // This creates the attachment. By setting copyOnDeath, it automatically persists across respawns!
    public static final Supplier<AttachmentType<StaminaData>> STAMINA = ATTACHMENT_TYPES.register("stamina",
            () -> AttachmentType.serializable(() -> new StaminaData()).copyOnDeath().build());
}