package com.peakstamina.client.events;

import com.peakstamina.peakStaminaMod;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.handlers.core.ServerStaminaHandler;
import com.peakstamina.handlers.mechanics.WeightHandler;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID, value = Dist.CLIENT)
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
        if (event.getFlags().isAdvanced()) {
            for (int i = event.getToolTip().size() - 1; i >= 0; i--) {
                String text = event.getToolTip().get(i).getString();
                if (text.startsWith("NBT: ") || (text.contains(":") && !text.contains(" ")) || text.startsWith("Durability: ")) {
                    bottomIndex = i;
                } else {
                    break;
                }
            }
        }

        ServerStaminaHandler.ConsumableData consData = ServerStaminaHandler.getConsumableData(stack);
        
        boolean isShiftDown = net.minecraft.client.gui.screens.Screen.hasShiftDown();
        boolean hasAddedShiftPrompt = false;

        for (String configLine : tooltipConfigs) {
            try {
                String[] parts = configLine.split(";");
                if (parts.length < 4) continue;

                String contentType = parts[0].trim().toUpperCase();
                String placement = parts[1].trim().toUpperCase();
                ChatFormatting labelColor = getFormatting(parts[2].trim(), ChatFormatting.DARK_GRAY);
                ChatFormatting valueColor = getFormatting(parts[3].trim(), ChatFormatting.WHITE);
                
                String prefix = parts.length > 4 ? parts[4].replace("\\t", "    ").replace("\\n", "\n") : "";
                String formatStr = parts.length > 5 ? parts[5].replace("\\t", "    ").replace("\\n", "\n") : "%s";

                java.util.List<MutableComponent> componentsToAdd = new java.util.ArrayList<>();
                Style lStyle = Style.EMPTY.applyFormat(labelColor);
                Style vStyle = Style.EMPTY.applyFormat(valueColor);

                switch (contentType) {
                    case "WEIGHT":
                        double stackWeight = WeightHandler.getRecursiveStackWeight(stack, StaminaConfig.COMMON.autoWeightBase.get(), 0);
                        if (stackWeight > 0) {
                            String val = safeFormat(formatStr, ClientStaminaEvents.getFormattedWeight(stackWeight));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
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
                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", attackCost));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "USE_COST":
                        float useCost = ServerStaminaHandler.getConfiguredItemCost(stack.getItem(), "USE");
                        useCost = ServerStaminaHandler.applyTirelessDiscount(stack, useCost);
                        
                        if (useCost != 0) {
                            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                            if (mc.player != null) {
                                double actionRecoveryMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER.get(), 1.0);
                                double useItemMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.ITEM_COST_MULTIPLIER.get(), 1.0);
                                double useActionMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.USE_COST_MULTIPLIER.get(), 1.0);
                                double usageMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.GLOBAL_STAMINA_USAGE.get(), 1.0);
                                
                                if (useCost > 0) {
                                    useCost = (float) (useCost * usageMult * useItemMult * useActionMult);
                                } else {
                                    useCost = (float) (useCost * actionRecoveryMult);
                                }
                            }

                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", useCost));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "TICK_COST":
                        float tickCost = ServerStaminaHandler.getConfiguredItemCost(stack.getItem(), "TICK");
                        tickCost = ServerStaminaHandler.applyTirelessDiscount(stack, tickCost);
                        
                        if (tickCost != 0) {
                            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
                            if (mc.player != null) {
                                double actionRecoveryMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.STAMINA_ACTION_RECOVERY_MULTIPLIER.get(), 1.0);
                                double useItemMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.ITEM_COST_MULTIPLIER.get(), 1.0);
                                double tickActionMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.TICK_COST_MULTIPLIER.get(), 1.0);
                                double usageMult = ServerStaminaHandler.getAttributeValue(mc.player, com.peakstamina.registry.StaminaAttributes.GLOBAL_STAMINA_USAGE.get(), 1.0);
                                
                                if (tickCost > 0) {
                                    tickCost = (float) (tickCost * usageMult * useItemMult * tickActionMult);
                                } else {
                                    tickCost = (float) (tickCost * actionRecoveryMult);
                                }
                            }

                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", tickCost));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "BLOCK_COST":
                        float[] shieldVals = ServerStaminaHandler.getShieldValues(stack.getItem());
                        float baseBlockCost = ServerStaminaHandler.applyTirelessDiscount(stack, shieldVals[0]);
                        float multBlockCost = ServerStaminaHandler.applyTirelessDiscount(stack, shieldVals[1]);
                        
                        if (baseBlockCost != 0 || multBlockCost != 0) {
                            String val = safeFormat(formatStr, 
                                String.format(java.util.Locale.US, "%.1f", baseBlockCost), 
                                String.format(java.util.Locale.US, "%.1f", multBlockCost));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
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
                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", missedCost));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "INSTANT_STAMINA":
                        if (consData != null && consData.isInstant && consData.instantAmount > 0) {
                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", consData.instantAmount));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "BONUS_STAMINA":
                        if (consData != null && consData.isBonus && consData.bonusAmount > 0) {
                            String val = safeFormat(formatStr, String.format(java.util.Locale.US, "%.1f", consData.bonusAmount));
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "REGEN_MODIFIER":
                        if (consData != null && consData.isRegen && consData.regenAmount != 0) {
                            if (!isShiftDown) {
                                if (!hasAddedShiftPrompt) {
                                    componentsToAdd.add(Component.literal("Hold ").withStyle(ChatFormatting.DARK_GRAY)
                                        .append(Component.literal("[SHIFT]").withStyle(ChatFormatting.YELLOW))
                                        .append(Component.literal(" for Buff Details").withStyle(ChatFormatting.DARK_GRAY)));
                                    hasAddedShiftPrompt = true;
                                }
                            } else {
                                String sign = consData.regenAmount > 0 ? "+" : "";
                                int seconds = consData.durationTicks / 20;
                                String val = safeFormat(formatStr, sign, (float)(consData.regenAmount * 100), seconds);
                                addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                            }
                        }
                        break;
                    case "CURES":
                        if (consData != null && !consData.specificCures.isEmpty()) {
                            StringBuilder curesStr = new StringBuilder();
                            for (int i = 0; i < consData.specificCures.size(); i++) {
                                var cure = consData.specificCures.get(i);
                                curesStr.append(cure.getKey()).append(" (").append(String.format(java.util.Locale.US, "%.1f", cure.getValue())).append(")");
                                if (i < consData.specificCures.size() - 1) curesStr.append(", ");
                            }
                            String val = safeFormat(formatStr, curesStr.toString());
                            addMultiLine(componentsToAdd, prefix, lStyle, val, vStyle);
                        }
                        break;
                    case "ATTRIBUTE":
                        if (consData != null && consData.attributeBuffs != null && !consData.attributeBuffs.isEmpty()) {
                            if (!isShiftDown) {
                                if (!hasAddedShiftPrompt) {
                                    componentsToAdd.add(Component.literal("Hold ").withStyle(ChatFormatting.DARK_GRAY)
                                        .append(Component.literal("[SHIFT]").withStyle(ChatFormatting.YELLOW))
                                        .append(Component.literal(" for Buff Details").withStyle(ChatFormatting.DARK_GRAY)));
                                    hasAddedShiftPrompt = true;
                                }
                            } else {
                                java.util.List<com.peakstamina.capabilities.StaminaCapability.BuffInstance> sortedBuffs = new java.util.ArrayList<>(consData.attributeBuffs);
                                sortedBuffs.sort((b1, b2) -> Double.compare(getEffectiveAmount(b2), getEffectiveAmount(b1)));
                                
                                boolean useColors = StaminaConfig.CLIENT.colorCodeBuffs.get();

                                for (var buff : sortedBuffs) {
                                    String opSymbol = buff.amount > 0 ? "+" : "";
                                    String opSuffix = "";
                                    if (buff.operation == 1) opSuffix = " Base";
                                    if (buff.operation == 2) opSuffix = " Total";

                                    String formattedAmount = (buff.operation == 1 || buff.operation == 2) 
                                            ? String.format(java.util.Locale.US, "%s%.0f%%%s", opSymbol, buff.amount * 100, opSuffix) 
                                            : String.format(java.util.Locale.US, "%s%.1f", opSymbol, buff.amount);
                                    
                                    MutableComponent attrName;
                                    net.minecraft.resources.ResourceLocation attrLoc = net.minecraft.resources.ResourceLocation.tryParse(buff.attributeName);
                                    if (attrLoc != null && net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.containsKey(attrLoc)) {
                                        net.minecraft.world.entity.ai.attributes.Attribute attr = net.minecraftforge.registries.ForgeRegistries.ATTRIBUTES.getValue(attrLoc);
                                        if (attr != null) {
                                            attrName = Component.translatable(attr.getDescriptionId());
                                        } else {
                                            attrName = Component.translatable("attribute.name." + buff.attributeName.replace(":", "."));
                                        }
                                    } else {
                                        attrName = Component.translatable("attribute.name." + buff.attributeName.replace(":", "."));
                                    }
                                    
                                    Style textStyle = vStyle;
                                    if (useColors) {
                                        textStyle = Style.EMPTY.withColor(getBuffColor(buff));
                                    }
                                    
                                    String val = safeFormat(formatStr, formattedAmount, attrName.getString(), buff.durationTicks / 20);
                                    addMultiLine(componentsToAdd, prefix, lStyle, val, textStyle);
                                }
                            }
                        }
                        break;
                }

                for (MutableComponent comp : componentsToAdd) {
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

    private static String safeFormat(String format, Object... args) {
        try {
            return String.format(java.util.Locale.US, format, args);
        } catch (Exception e) {
            return "[Format Error]";
        }
    }

    private static void addMultiLine(List<MutableComponent> list, String prefix, Style labelStyle, String formattedValue, Style valueStyle) {
        String fullText = prefix + formattedValue;
        String[] lines = fullText.split("\\n", -1); 
        
        int prefixLen = prefix.length();
        int currentPos = 0;
        
        for (String line : lines) {
            MutableComponent comp = Component.empty();
            
            int lineLen = line.length();
            int lineStart = currentPos;
            int lineEnd = currentPos + lineLen;
            
            if (lineEnd <= prefixLen) {
                comp.append(Component.literal(line).withStyle(labelStyle));
            } else if (lineStart >= prefixLen) {
                comp.append(Component.literal(line).withStyle(valueStyle));
            } else {
                String part1 = line.substring(0, prefixLen - lineStart);
                String part2 = line.substring(prefixLen - lineStart);
                comp.append(Component.literal(part1).withStyle(labelStyle));
                comp.append(Component.literal(part2).withStyle(valueStyle));
            }
            
            list.add(comp);
            currentPos += lineLen + 1; 
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

    private static boolean isInvertedAttribute(String attributeName) {
        String name = attributeName.toLowerCase(java.util.Locale.ROOT);
        
        java.util.List<? extends String> customInverted = StaminaConfig.CLIENT.invertedTooltipAttributes.get();
        if (customInverted != null) {
            for (String custom : customInverted) {
                if (name.equals(custom.trim().toLowerCase(java.util.Locale.ROOT))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static double getEffectiveAmount(com.peakstamina.capabilities.StaminaCapability.BuffInstance buff) {
        return isInvertedAttribute(buff.attributeName) ? -buff.amount : buff.amount;
    }

    private static int getBuffColor(com.peakstamina.capabilities.StaminaCapability.BuffInstance buff) {
        if (getEffectiveAmount(buff) > 0) {
            return 0x55FF55; 
        } else {
            return 0xFF5555; 
        }
    }
}