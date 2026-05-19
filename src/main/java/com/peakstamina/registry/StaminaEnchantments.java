package com.peakstamina.registry;

import com.peakstamina.enchantments.LightweightEnchantment;
import com.peakstamina.enchantments.TirelessEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class StaminaEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, "peakstamina");

    public static final RegistryObject<Enchantment> LIGHTWEIGHT = ENCHANTMENTS.register("lightweight", 
            LightweightEnchantment::new);

    public static final RegistryObject<Enchantment> TIRELESS = ENCHANTMENTS.register("tireless", 
            TirelessEnchantment::new);
}