package com.peakstamina.compat;

import com.peakstamina.handlers.WeightHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

public class PackedUpCompat {

    private static Method getInventoryMethod;

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

                if (stack.hasTag() && stack.getTag().contains("packedup:invIndex")) {
                    int id = stack.getTag().getInt("packedup:invIndex");
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
                                return bagItemWeight + backpackWeight;
                            }
                        }
                    } catch (Exception e) {
                        return -1;
                    }
                }
            }
            return -1;
        });
    }
}
