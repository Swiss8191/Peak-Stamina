package com.peakstamina.handlers;

import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists; 
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WeightHandler {

    private static final Map<Item, Double> itemWeightCache = new HashMap<>();
    private static final Map<TagKey<Item>, Double> tagWeightCache = new HashMap<>();
    private static final Map<Item, String> containerPathCache = new HashMap<>();
    private static List<? extends String> lastItemConfig = null;
    private static List<? extends String> lastTagConfig = null;
    private static List<? extends String> lastContainerConfig = null;

    public static final List<CustomWeightProvider> CUSTOM_PROVIDERS = new ArrayList<>();

    @FunctionalInterface
    public interface CustomWeightProvider {
        double getWeight(ItemStack stack, double baseHeuristic, int currentDepth);
    }

    public static double calculateTotalWeight(Player player) {
        validateCache();
        double totalWeight = 0.0;
        double baseHeuristic = StaminaConfig.COMMON.autoWeightBase.get();

        for (ItemStack stack : player.getInventory().items) {
            totalWeight += getRecursiveStackWeight(stack, baseHeuristic, 0);
        }
        for (ItemStack stack : player.getInventory().armor) {
            totalWeight += getRecursiveStackWeight(stack, baseHeuristic, 0);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            totalWeight += getRecursiveStackWeight(stack, baseHeuristic, 0);
        }

        if (ModList.get().isLoaded("curios")) {
            totalWeight += getCuriosWeight(player, baseHeuristic);
        }

        return totalWeight;
    }

    public static double getRecursiveStackWeight(ItemStack stack, double baseHeuristic, int depth) {
        if (stack.isEmpty()) return 0.0;

        for (CustomWeightProvider provider : CUSTOM_PROVIDERS) {
            double customW = provider.getWeight(stack, baseHeuristic, depth);
            if (customW >= 0) return customW;
        }

        double weight = getItemWeight(stack, baseHeuristic);
        if (depth >= StaminaConfig.COMMON.maxWeightRecursionDepth.get()) return weight;

        AtomicReference<Double> contentWeight = new AtomicReference<>(0.0);

        if (containerPathCache.containsKey(stack.getItem()) && stack.hasTag()) {
            String path = containerPathCache.get(stack.getItem());
            ListTag list = getListTagFromPath(stack.getTag(), path);
            
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    ItemStack subStack = ItemStack.of(itemTag);
                    if (!subStack.isEmpty()) {
                        contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(subStack, baseHeuristic, depth + 1));
                    }
                }
                return weight + contentWeight.get();
            }
        }

        stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack subStack = handler.getStackInSlot(i);
                if (!subStack.isEmpty()) {
                    contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(subStack, baseHeuristic, depth + 1));
                }
            }
        });

        if (contentWeight.get() > 0) {
            return weight + contentWeight.get();
        }

        if (stack.hasTag()) {
            CompoundTag tag = stack.getTag();
            if (tag.contains("BlockEntityTag", Tag.TAG_COMPOUND)) {
                CompoundTag bet = tag.getCompound("BlockEntityTag");
                if (bet.contains("Items", Tag.TAG_LIST)) {
                    ListTag list = bet.getList("Items", Tag.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag itemTag = list.getCompound(i);
                        ItemStack subStack = ItemStack.of(itemTag);
                        if (!subStack.isEmpty()) {
                            contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(subStack, baseHeuristic, depth + 1));
                        }
                    }
                    if (contentWeight.get() > 0) return weight + contentWeight.get();
                }
            }

            String[] keysToCheck = {"Items", "Inventory", "inventory", "ItemsList"};
            for (String key : keysToCheck) {
                if (tag.contains(key, Tag.TAG_LIST)) {
                    ListTag list = tag.getList(key, Tag.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag itemTag = list.getCompound(i);
                        ItemStack subStack = ItemStack.of(itemTag);
                        if (!subStack.isEmpty()) {
                            contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(subStack, baseHeuristic, depth + 1));
                        }
                    }
                }
                if (contentWeight.get() > 0) break;
            }
        }
        
        return weight + contentWeight.get();
    }

    private static ListTag getListTagFromPath(CompoundTag root, String path) {
        if (root == null) return null;
        String[] parts = path.split("\\.");
        CompoundTag current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            String key = parts[i];
            if (current.contains(key, Tag.TAG_COMPOUND)) {
                current = current.getCompound(key);
            } else {
                return null;
            }
        }
        String listKey = parts[parts.length - 1];
        if (current.contains(listKey, Tag.TAG_LIST)) {
            return current.getList(listKey, Tag.TAG_COMPOUND);
        }
        return null;
    }

    private static double getCuriosWeight(Player player, double baseHeuristic) {
        final double[] weightRef = {0.0};
        try {
            top.theillusivec4.curios.api.CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
                 handler.getCurios().forEach((id, stackHandler) -> {
                      net.minecraftforge.items.IItemHandlerModifiable itemHandler = stackHandler.getStacks();
                      for(int i = 0; i < itemHandler.getSlots(); i++) {
                          ItemStack stack = itemHandler.getStackInSlot(i);
                          if(!stack.isEmpty()) {
                               weightRef[0] += getRecursiveStackWeight(stack, baseHeuristic, 0);
                          }
                      }
                 });
            });
        } catch (Exception ignored) {}
        return weightRef[0];
    }

    public static double getItemWeight(ItemStack stack, double baseHeuristic) {
        if (stack.isEmpty()) return 0.0;
        Item item = stack.getItem();
        int count = stack.getCount();

        if (itemWeightCache.containsKey(item)) {
            return itemWeightCache.get(item) * count;
        }

        for (Map.Entry<TagKey<Item>, Double> entry : tagWeightCache.entrySet()) {
            if (stack.is(entry.getKey())) {
                return entry.getValue() * count;
            }
        }

        int maxStack = item.getMaxStackSize();
        if (maxStack <= 0) maxStack = 1;
        
        double singleWeight = baseHeuristic / (double) maxStack;
        return singleWeight * count;
    }

    private static void validateCache() {
        List<? extends String> itemConfig = StaminaLists.LISTS.customItemWeights.get();
        List<? extends String> tagConfig = StaminaLists.LISTS.customTagWeights.get();
        List<? extends String> containerConfig = StaminaLists.LISTS.customContainerPaths.get();

        boolean isItemDirty = itemConfig != lastItemConfig;
        boolean isTagDirty = tagConfig != lastTagConfig;
        boolean isContainerDirty = containerConfig != lastContainerConfig;

        if (!isItemDirty && !isTagDirty && !isContainerDirty) return;
        
        if (isItemDirty) {
            itemWeightCache.clear();
            lastItemConfig = itemConfig;
            for (String entry : itemConfig) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 2) {
                        ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                        if (loc != null && ForgeRegistries.ITEMS.containsKey(loc)) {
                            itemWeightCache.put(ForgeRegistries.ITEMS.getValue(loc), Double.parseDouble(parts[1].trim()));
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        if (isTagDirty) {
            tagWeightCache.clear();
            lastTagConfig = tagConfig;
            for (String entry : tagConfig) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 2) {
                        ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                        if (loc != null) {
                            TagKey<Item> tagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(), loc);
                            tagWeightCache.put(tagKey, Double.parseDouble(parts[1].trim()));
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        
        if (isContainerDirty) {
            containerPathCache.clear();
            lastContainerConfig = containerConfig;
            for (String entry : containerConfig) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 2) {
                        ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                        if (loc != null && ForgeRegistries.ITEMS.containsKey(loc)) {
                            containerPathCache.put(ForgeRegistries.ITEMS.getValue(loc), parts[1].trim());
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}