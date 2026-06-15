package com.peakstamina.compat.packedup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.peakstamina.handlers.mechanics.WeightHandler;

public class PackedUpCompat {

    private static Method getInventoryMethod;
    private static final Map<Integer, Double> WEIGHT_CACHE = new HashMap<>();
    private static final Map<Integer, Integer> NBT_HASH_CACHE = new HashMap<>();

    public static void init() {
        try {
            Class<?> managerClass = Class.forName("com.supermartijn642.packedup.storage.BackpackStorageManager");
            getInventoryMethod = managerClass.getMethod("getInventory", int.class);
        } catch (Exception e) {
            System.err.println("[PeakStamina] Could not reflect into PackedUp: " + e.getMessage());
        }

        WeightHandler.CUSTOM_PROVIDERS.add((stack, base, depth) -> {
            ResourceLocation regName = ForgeRegistries.ITEMS.getKey(stack.getItem());

            if (regName != null && "packedup".equals(regName.getNamespace())) {
                net.minecraft.nbt.CompoundTag tag = stack.getTag();
                
                if (tag != null && tag.contains("packedup:invIndex")) {
                    int id = tag.getInt("packedup:invIndex");
                    
                    // Use NBT hash to verify if the backpack contents changed
                    int currentHash = tag.hashCode();
                    if (WEIGHT_CACHE.containsKey(id) && NBT_HASH_CACHE.getOrDefault(id, 0) == currentHash) {
                        return WEIGHT_CACHE.get(id);
                    }
            
                    try {
                        if (getInventoryMethod != null) {
                            Object inventoryObj = getInventoryMethod.invoke(null, id);

                            if (inventoryObj instanceof IItemHandler) {
                                IItemHandler handler = (IItemHandler) inventoryObj;
                                double backpackWeight = 0.0;

                                for (int i = 0; i < handler.getSlots(); i++) {
                                    ItemStack subStack = handler.getStackInSlot(i);
                                    if (!subStack.isEmpty()) {
                                        backpackWeight += WeightHandler.getRecursiveStackWeight(subStack, base, depth + 1);
                                    }
                                }

                                double bagItemWeight = WeightHandler.getItemWeight(stack, base);
                                double totalWeight = bagItemWeight + backpackWeight;
                                
                                // Update Cache
                                WEIGHT_CACHE.put(id, totalWeight);
                                NBT_HASH_CACHE.put(id, currentHash);
                                
                                return totalWeight;
                            }
                        }
                    } catch (Exception e) {
                        return -1.0;
                    }
                }
            }
            return -1.0;
        });
    }
}
