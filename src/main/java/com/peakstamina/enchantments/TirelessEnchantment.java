package com.peakstamina.enchantments;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import com.peakstamina.config.StaminaConfig;

public class TirelessEnchantment extends Enchantment {

    public TirelessEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
    }

    @Override
    public int getMinCost(int level) {
        return 10 + 20 * (level - 1);
    }

    @Override
    public int getMaxCost(int level) {
        return super.getMinCost(level) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public boolean isTradeable() {
        if (!StaminaConfig.COMMON.enableEnchants.get()) return false;
        return super.isTradeable();
    }

    @Override
    public boolean isDiscoverable() {
        if (!StaminaConfig.COMMON.enableEnchants.get()) return false;
        return super.isDiscoverable();
    }

    @Override
    public boolean isAllowedOnBooks() {
        if (!StaminaConfig.COMMON.enableEnchants.get()) return false;
        return super.isAllowedOnBooks();
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        if (!StaminaConfig.COMMON.enableEnchants.get()) return false;
        return super.canApplyAtEnchantingTable(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        if (!com.peakstamina.config.StaminaConfig.COMMON.enableEnchants.get()) return false;
        
        net.minecraft.world.item.Item item = stack.getItem();

        if (item instanceof net.minecraft.world.item.ArmorItem) {
            return false;
        }
        
        // Tireless can go on Tools (axes/picks/shovels/hoes), Swords, Bows/Crossbows, Shields, and Tridents
        return item instanceof net.minecraft.world.item.TieredItem || 
               item instanceof net.minecraft.world.item.SwordItem || 
               item instanceof net.minecraft.world.item.ProjectileWeaponItem ||
               item instanceof net.minecraft.world.item.ShieldItem ||
               item instanceof net.minecraft.world.item.TridentItem;
    }
}