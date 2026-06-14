package com.peakstamina.compat.packedup;

import java.lang.reflect.Method;
import java.util.List;

import com.peakstamina.handlers.mechanics.WeightHandler;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class PackedUpCompat {

    private static Method getInventoryMethod;
    private static Method getStacksMethod;
    private static DataComponentType<Integer> inventoryIdComponent;
    private static final java.util.Map<Integer, Double> WEIGHT_CACHE = new java.util.HashMap<>();
    private static final java.util.Map<Integer, Integer> COMPONENT_HASH_CACHE = new java.util.HashMap<>();

    @SuppressWarnings("unchecked")
    public static void init() {
        try {
            Class<?> managerClass = Class.forName("com.supermartijn642.packedup.storage.BackpackStorageManager");
            getInventoryMethod = managerClass.getMethod("getInventory", int.class);

            Class<?> inventoryClass = Class.forName("com.supermartijn642.packedup.storage.BackpackInventory");
            getStacksMethod = inventoryClass.getMethod("getStacks");

            Class<?> backpackItemClass = Class.forName("com.supermartijn642.packedup.BackpackItem");
            java.lang.reflect.Field invIdField = backpackItemClass.getField("INVENTORY_ID");
            
            Object fieldVal = invIdField.get(null);
            
            // Handle cases where the modder wrapped it in a Supplier/DeferredHolder
            if (fieldVal instanceof java.util.function.Supplier supplier) {
                inventoryIdComponent = (net.minecraft.core.component.DataComponentType<Integer>) supplier.get();
            } else {
                inventoryIdComponent = (net.minecraft.core.component.DataComponentType<Integer>) fieldVal;
            }

        } catch (Exception e) {
            System.err.println("[PeakStamina] Could not reflect into PackedUp: " + e.getMessage());
        }

        WeightHandler.CUSTOM_PROVIDERS.add((player, stack, base, depth) -> {
            ResourceLocation regName = BuiltInRegistries.ITEM.getKey(stack.getItem());

            if (regName != null && "packedup".equals(regName.getNamespace())) {
                
                // Check if the bag has the Data Component ID
                if (inventoryIdComponent != null && stack.has(inventoryIdComponent)) {
                    Integer id = stack.get(inventoryIdComponent);
                    
                    if (id != null) {
                        int currentHash = stack.getComponents().hashCode();
                        if (WEIGHT_CACHE.containsKey(id) && COMPONENT_HASH_CACHE.getOrDefault(id, 0) == currentHash) {
                            return WEIGHT_CACHE.get(id);
                        }

                        try {
                            if (getInventoryMethod != null && getStacksMethod != null) {
                                Object inventoryObj = getInventoryMethod.invoke(null, id);
                                
                                if (inventoryObj != null) {
                                    // Extract the List<ItemStack> directly from the inventory
                                    List<ItemStack> backpackStacks = (List<ItemStack>) getStacksMethod.invoke(inventoryObj);
                                    
                                    double backpackWeight = 0.0;
                                    
                                    // Loop through the raw list and calculate recursive weight
                                    for (ItemStack subStack : backpackStacks) {
                                        if (subStack != null && !subStack.isEmpty()) {
                                            backpackWeight += WeightHandler.getRecursiveStackWeight(player, subStack, base, depth + 1);
                                        }
                                    }

                                    // Add the weight of the bag item itself
                                    double bagItemWeight = WeightHandler.getItemWeight(stack, base);
                                    double totalWeight = bagItemWeight + backpackWeight;
                                    
                                    WEIGHT_CACHE.put(id, totalWeight);
                                    COMPONENT_HASH_CACHE.put(id, currentHash);
                                    
                                    return totalWeight;
                                }
                            }
                        } catch (Exception e) {
                            return -1.0;
                        }
                    }
                }
            }
            return -1.0; // Return -1.0 so WeightHandler falls back to normal weight calculation if reflection fails
        });
    }
}