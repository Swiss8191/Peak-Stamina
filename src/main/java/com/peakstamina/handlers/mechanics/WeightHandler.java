package com.peakstamina.handlers.mechanics;

import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.handlers.core.ServerStaminaHandler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class WeightHandler {

    public static boolean debugNbtPaths = false;
    private static final Map<Item, Double> itemWeightCache = new HashMap<>();
    private static final Map<TagKey<Item>, Double> tagWeightCache = new HashMap<>();
    private static final Map<Item, String> containerPathCache = new HashMap<>();
    private static final Map<Item, List<NbtWeightPath>> NBT_WEIGHT_PATHS_CACHE = new HashMap<>();
    public static final List<CustomWeightProvider> CUSTOM_PROVIDERS = new ArrayList<>();

    @FunctionalInterface
    public interface CustomWeightProvider {
        double getWeight(Player player, ItemStack stack, double baseHeuristic, int currentDepth);
    }

    private static class NbtWeightPath {
        String[] path;
        double fallbackWeight;
        boolean applyIfMissing;

        NbtWeightPath(String[] path, double fallbackWeight, boolean applyIfMissing) {
            this.path = path;
            this.fallbackWeight = fallbackWeight;
            this.applyIfMissing = applyIfMissing;
        }
    }

    public static double calculateTotalWeight(Player player) {
        double totalWeight = 0.0;
        double baseHeuristic = StaminaConfig.COMMON.autoWeightBase.get();

        for (ItemStack stack : player.getInventory().items) {
            totalWeight += getRecursiveStackWeight(player, stack, baseHeuristic, 0);
        }
        for (ItemStack stack : player.getInventory().armor) {
            totalWeight += getRecursiveStackWeight(player, stack, baseHeuristic, 0);
        }
        for (ItemStack stack : player.getInventory().offhand) {
            totalWeight += getRecursiveStackWeight(player, stack, baseHeuristic, 0);
        }

        if (ModList.get().isLoaded("curios")) {
            totalWeight += getCuriosWeight(player, baseHeuristic);
        }

        return totalWeight;
    }

    public static double getRecursiveStackWeight(Player player, ItemStack stack, double baseHeuristic, int depth) {
        if (stack.isEmpty()) return 0.0;

        for (CustomWeightProvider provider : CUSTOM_PROVIDERS) {
            double customW = provider.getWeight(player, stack, baseHeuristic, depth);
            if (customW >= 0) return customW;
        }

        double weight = getItemWeight(stack, baseHeuristic);
        if (depth >= StaminaConfig.COMMON.maxWeightRecursionDepth.get()) return weight;

        AtomicReference<Double> contentWeight = new AtomicReference<>(0.0);

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag stackTag = customData != null ? customData.copyTag() : null;

        if (stackTag != null && containerPathCache.containsKey(stack.getItem())) {
            String path = containerPathCache.get(stack.getItem());
            ListTag list = getListTagFromPath(stackTag, path);

            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    ItemStack subStack = ItemStack.parseOptional(player.registryAccess(), itemTag);
                    if (!subStack.isEmpty()) {
                        contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(player, subStack, baseHeuristic, depth + 1));
                    }
                }
                return weight + contentWeight.get();
            }
        }

        var handler = stack.getCapability(Capabilities.ItemHandler.ITEM);
        if (handler != null) {
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack subStack = handler.getStackInSlot(i);
                if (!subStack.isEmpty()) {
                    contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(player, subStack, baseHeuristic, depth + 1));
                }
            }
        }
        if (contentWeight.get() > 0) return weight + contentWeight.get();

        CustomData beData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (beData != null) {
            CompoundTag bet = beData.copyTag();
            if (bet.contains("Items", Tag.TAG_LIST)) {
                ListTag list = bet.getList("Items", Tag.TAG_COMPOUND);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag itemTag = list.getCompound(i);
                    ItemStack subStack = ItemStack.parseOptional(player.registryAccess(), itemTag);
                    if (!subStack.isEmpty()) {
                        contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(player, subStack, baseHeuristic, depth + 1));
                    }
                }
                if (contentWeight.get() > 0) return weight + contentWeight.get();
            }
        }

        if (stackTag != null) {
            String[] keysToCheck = {"Items", "Inventory", "inventory", "ItemsList"};
            for (String key : keysToCheck) {
                if (stackTag.contains(key, Tag.TAG_LIST)) {
                    ListTag list = stackTag.getList(key, Tag.TAG_COMPOUND);
                    for (int i = 0; i < list.size(); i++) {
                        CompoundTag itemTag = list.getCompound(i);
                        ItemStack subStack = ItemStack.parseOptional(player.registryAccess(), itemTag);
                        if (!subStack.isEmpty()) {
                            contentWeight.updateAndGet(v -> v + getRecursiveStackWeight(player, subStack, baseHeuristic, depth + 1));
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
            top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
                handler.getCurios().forEach((id, stackHandler) -> {
                    net.neoforged.neoforge.items.IItemHandlerModifiable itemHandler = stackHandler.getStacks();
                    for(int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if(!stack.isEmpty()) {
                            weightRef[0] += getRecursiveStackWeight(player, stack, baseHeuristic, 0);
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

        double totalWeight = 0.0;
        boolean overridden = false;
        
        if (itemWeightCache.containsKey(item)) {
            totalWeight += itemWeightCache.get(item);
            overridden = true;
        }

        if (!overridden) {
            for (Map.Entry<TagKey<Item>, Double> entry : tagWeightCache.entrySet()) {
                if (stack.is(entry.getKey())) {
                    totalWeight += entry.getValue();
                    overridden = true;
                    break;
                }
            }
        }

        if (NBT_WEIGHT_PATHS_CACHE.containsKey(item)) {
            overridden = true;
            net.minecraft.world.item.component.CustomData customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            net.minecraft.nbt.CompoundTag tag = customData != null ? customData.copyTag() : null;
            
            for (NbtWeightPath nbtPath : NBT_WEIGHT_PATHS_CACHE.get(item)) {
                String extractedId = tag != null ? getStringFromNbtPath(tag, nbtPath.path) : null;
                double addedWeight = 0.0;

                if (extractedId != null) {
                    net.minecraft.resources.ResourceLocation extractedLoc = net.minecraft.resources.ResourceLocation.tryParse(extractedId);
                    Item extractedItem = extractedLoc != null ? net.minecraft.core.registries.BuiltInRegistries.ITEM.get(extractedLoc) : null;
                    
                    if (extractedItem != null && itemWeightCache.containsKey(extractedItem)) {
                        addedWeight = itemWeightCache.get(extractedItem);
                    } else {
                        addedWeight = nbtPath.fallbackWeight;
                    }
                } else if (nbtPath.applyIfMissing) {
                    addedWeight = nbtPath.fallbackWeight;
                }

                totalWeight += addedWeight;
                if (debugNbtPaths) {
                    String regName = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
                    System.out.println("[PeakStamina NBT Debug] Base Item: " + regName +
                            " | Path: " + java.util.Arrays.toString(nbtPath.path) +
                            " | Found ID: " + extractedId +
                            " | Weight Added: " + addedWeight);
                }
            }
        }

        double lightweightDiscount = 0.0;
        int lightweightLevel = ServerStaminaHandler.getCustomEnchantLevel(stack, "peakstamina:lightweight");
        if (lightweightLevel > 0) {
            if (lightweightLevel == 1) lightweightDiscount = StaminaConfig.COMMON.lightweightLvl1.get();
            else if (lightweightLevel == 2) lightweightDiscount = StaminaConfig.COMMON.lightweightLvl2.get();
            else lightweightDiscount = StaminaConfig.COMMON.lightweightLvl3.get();
        }
        double weightMultiplier = Math.max(0.0, 1.0 - lightweightDiscount);

        if (overridden) {
            totalWeight *= weightMultiplier;
            return totalWeight * count;
        }

        int maxStack = item.getDefaultMaxStackSize();
        if (maxStack <= 0) maxStack = 1;
        double singleWeight = baseHeuristic / (double) maxStack;
        
        singleWeight *= weightMultiplier;

        return singleWeight * count;
    }

    private static String getStringFromNbtPath(CompoundTag root, String[] path) {
        if (root == null) return null;
        CompoundTag current = root;
        for (int i = 0; i < path.length - 1; i++) {
            if (current.contains(path[i], Tag.TAG_COMPOUND)) {
                current = current.getCompound(path[i]);
            } else {
                return null;
            }
        }
        Tag finalTag = current.get(path[path.length - 1]);
        return finalTag != null ? finalTag.getAsString() : null;
    }

    public static void validateCache() {
        List<? extends String> itemConfig = StaminaLists.LISTS.customItemWeights.get();
        List<? extends String> tagConfig = StaminaLists.LISTS.customTagWeights.get();
        List<? extends String> containerConfig = StaminaLists.LISTS.customContainerPaths.get();
        List<? extends String> nbtConfig = StaminaLists.LISTS.nbtWeightPaths.get();

        itemWeightCache.clear();
        for (String entry : itemConfig) {
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 2) {
                    ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                    if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                        itemWeightCache.put(BuiltInRegistries.ITEM.get(loc), Double.parseDouble(parts[1].trim()));
                    }
                }
            } catch (Exception ignored) {}
        }

        tagWeightCache.clear();
        for (String entry : tagConfig) {
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 2) {
                    ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                    if (loc != null) {
                        TagKey<Item> tagKey = TagKey.create(BuiltInRegistries.ITEM.key(), loc);
                        tagWeightCache.put(tagKey, Double.parseDouble(parts[1].trim()));
                    }
                }
            } catch (Exception ignored) {}
        }

        containerPathCache.clear();
        for (String entry : containerConfig) {
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 2) {
                    ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                    if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                        containerPathCache.put(BuiltInRegistries.ITEM.get(loc), parts[1].trim());
                    }
                }
            } catch (Exception ignored) {}
        }

        NBT_WEIGHT_PATHS_CACHE.clear();
        if (nbtConfig != null) {
            for (String entry : nbtConfig) {
                try {
                    String[] parts = entry.split(";");
                    if (parts.length >= 4) {
                        ResourceLocation loc = ResourceLocation.tryParse(parts[0].trim());
                        if (loc != null && BuiltInRegistries.ITEM.containsKey(loc)) {
                            Item item = BuiltInRegistries.ITEM.get(loc);
                            String[] path = parts[1].trim().split("\\.");
                            double fallback = Double.parseDouble(parts[2].trim());
                            boolean applyIfMissing = Boolean.parseBoolean(parts[3].trim());

                            NBT_WEIGHT_PATHS_CACHE.computeIfAbsent(item, k -> new ArrayList<>())
                                    .add(new NbtWeightPath(path, fallback, applyIfMissing));
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }
}