package com.peakstamina.client;

import java.util.List;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.handlers.ServerStaminaHandler;
import com.peakstamina.handlers.WeightHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = PeakStaminaMod.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (!StaminaConfig.CLIENT.enableTooltips.get()) return;
        if (StaminaConfig.CLIENT.advancedTooltipsOnly.get() && !event.getFlags().isAdvanced()) return;

        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<? extends String> tooltipConfigs = StaminaConfig.CLIENT.customTooltips.get();
        if (tooltipConfigs == null || tooltipConfigs.isEmpty()) return;

        int belowNameIndex = 1;
        int bottomIndex = event.getToolTip().size();

        // Push our tooltips above the debug info (Namespaces, Durability, and 1.21 Data Components)
        if (event.getFlags().isAdvanced()) {
            for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
                String text = event.getToolTip().get(i).getString();
                if (text.startsWith("NBT: ") || text.startsWith("components: ") || text.endsWith(" component") || text.endsWith(" components") || (text.contains(":") && !text.contains(" ")) || text.startsWith("Durability: ")) {
                    bottomIndex = i;
                } else {
                    break;
                }
            }
        }

        // Fetch consumable data once for the item
        ServerStaminaHandler.ConsumableData consData = ServerStaminaHandler.getConsumableData(stack.getItem());
        
        for (String configLine : tooltipConfigs) {
            try {
                String[] parts = configLine.split(";");
                if (parts.length < 4) continue;

                String contentType = parts[0].trim().toUpperCase();
                String placement = parts[1].trim().toUpperCase();
                ChatFormatting labelColor = getFormatting(parts[2].trim(), ChatFormatting.DARK_GRAY);
                ChatFormatting valueColor = getFormatting(parts[3].trim(), ChatFormatting.WHITE);

                MutableComponent comp = null;

                switch (contentType) {
                    case "WEIGHT":
                        double stackWeight = WeightHandler.getRecursiveStackWeight(null, stack, StaminaConfig.COMMON.autoWeightBase.get(), 0);
                        if (stackWeight > 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelWeight.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f", stackWeight)).withStyle(valueColor));
                        }
                        break;
                    case "ATTACK_COST":
                        float attackCost = StaminaConfig.COMMON.depletionAttack.get().floatValue();
                        if (StaminaConfig.COMMON.attackCostScalesWithWeight.get()) {
                            double singleWeight = WeightHandler.getItemWeight(stack, StaminaConfig.COMMON.autoWeightBase.get()) / Math.max(1, stack.getCount());
                            double normalizer = StaminaConfig.COMMON.attackWeightNormalizer.get();
                            double scaleFactor = StaminaConfig.COMMON.attackWeightScaleFactor.get();
                            double minMult = StaminaConfig.COMMON.attackWeightMinMultiplier.get();
                            double maxMult = StaminaConfig.COMMON.attackWeightMaxMultiplier.get();
                            double weightMult = 1.0 + (((singleWeight - normalizer) / normalizer) * scaleFactor);
                            weightMult = Math.max(minMult, Math.min(maxMult, weightMult));
                            attackCost *= (float) weightMult;
                        }
                        
                        attackCost = ServerStaminaHandler.applyTirelessDiscount(stack, attackCost);

                        if (attackCost != 0 && isWeaponOrTool(stack)) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelAttackCost.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f", attackCost)).withStyle(valueColor));
                        }
                        break;
                    case "USE_COST":
                        float useCost = ServerStaminaHandler.getConfiguredItemCost(stack.getItem(), "USE");
                        useCost = ServerStaminaHandler.applyTirelessDiscount(stack, useCost);

                        if (useCost != 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelUseCost.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f", useCost)).withStyle(valueColor));
                        }
                        break;
                    case "TICK_COST":
                        float tickCost = ServerStaminaHandler.getConfiguredItemCost(stack.getItem(), "TICK");
                        tickCost = ServerStaminaHandler.applyTirelessDiscount(stack, tickCost);

                        if (tickCost != 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelTickCost.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f/t", tickCost)).withStyle(valueColor));
                        }
                        break;
                    case "BLOCK_COST":
                        float[] shieldVals = ServerStaminaHandler.getShieldValues(stack.getItem());

                        float baseBlockCost = ServerStaminaHandler.applyTirelessDiscount(stack, shieldVals[0]);
                        float multBlockCost = ServerStaminaHandler.applyTirelessDiscount(stack, shieldVals[1]);
                        
                        if (baseBlockCost != 0 || multBlockCost != 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelBlockCost.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f + (Dmg * %.1f)", baseBlockCost, multBlockCost)).withStyle(valueColor));
                        }
                        break;
                    case "MISSED_ATTACK_COST":
                        float missedCost = StaminaConfig.COMMON.depletionMissedAttack.get().floatValue();
                        if (StaminaConfig.COMMON.missedAttackCostScalesWithWeight.get()) {
                            double singleWeight = WeightHandler.getItemWeight(stack, StaminaConfig.COMMON.autoWeightBase.get()) / Math.max(1, stack.getCount());
                            double normalizer = StaminaConfig.COMMON.missedAttackWeightNormalizer.get();
                            double scaleFactor = StaminaConfig.COMMON.missedAttackWeightScaleFactor.get();
                            double minMult = StaminaConfig.COMMON.missedAttackWeightMinMultiplier.get();
                            double maxMult = StaminaConfig.COMMON.missedAttackWeightMaxMultiplier.get();
                            double weightMult = 1.0 + (((singleWeight - normalizer) / normalizer) * scaleFactor);
                            weightMult = Math.max(minMult, Math.min(maxMult, weightMult));
                            missedCost *= (float) weightMult;
                        }
                        missedCost = ServerStaminaHandler.applyTirelessDiscount(stack, missedCost);
                        
                        if (missedCost != 0 && isWeaponOrTool(stack)) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelMissCost.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f", missedCost)).withStyle(valueColor));
                        }
                        break;
                    case "INSTANT_STAMINA":
                        if (consData != null && consData.isInstant && consData.instantAmount > 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelInstant.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%.1f Stamina", consData.instantAmount)).withStyle(valueColor));
                        }
                        break;
                    case "BONUS_STAMINA":
                        if (consData != null && consData.isBonus && consData.bonusAmount > 0) {
                            comp = Component.literal(StaminaConfig.CLIENT.labelBonus.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("+%.1f Stamina", consData.bonusAmount)).withStyle(valueColor));
                        }
                        break;
                    case "REGEN_MODIFIER":
                        if (consData != null && consData.isRegen && consData.regenAmount != 0) {
                            String sign = consData.regenAmount > 0 ? "+" : "";
                            int seconds = consData.durationTicks / 20;
                            comp = Component.literal(StaminaConfig.CLIENT.labelRegen.get()).withStyle(labelColor)
                                    .append(Component.literal(String.format("%s%.0f%% (%ds)", sign, consData.regenAmount * 100, seconds)).withStyle(valueColor));
                        }
                        break;
                    case "CURES":
                        if (consData != null && !consData.specificCures.isEmpty()) {
                            StringBuilder curesStr = new StringBuilder();
                            for (int i = 0; i < consData.specificCures.size(); i++) {
                                var cure = consData.specificCures.get(i);
                                curesStr.append(cure.getKey()).append(" (").append(String.format("%.1f", cure.getValue())).append(")");
                                if (i < consData.specificCures.size() - 1) curesStr.append(", ");
                            }
                            comp = Component.literal(StaminaConfig.CLIENT.labelCures.get()).withStyle(labelColor)
                                    .append(Component.literal(curesStr.toString()).withStyle(valueColor));
                        }
                        break;
                }

                if (comp != null) {
                    if (placement.equals("BELOW_NAME")) {
                        int actualIndex = Math.min(belowNameIndex, event.getToolTip().size());
                        event.getToolTip().add(actualIndex, comp);
                        belowNameIndex++;
                        bottomIndex++; 
                    } else {
                        int actualIndex = Math.min(bottomIndex, event.getToolTip().size());
                        event.getToolTip().add(actualIndex, comp);
                        bottomIndex++; 
                    }
                }
            } catch (Exception ignored) { }
        }
    }

    private static ChatFormatting getFormatting(String name, ChatFormatting fallback) {
        try {
            return ChatFormatting.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }

    private static boolean isWeaponOrTool(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof net.minecraft.world.item.TieredItem ||
               item instanceof net.minecraft.world.item.SwordItem ||
               item instanceof net.minecraft.world.item.TridentItem ||
               item instanceof net.minecraft.world.item.ProjectileWeaponItem;
    }
}