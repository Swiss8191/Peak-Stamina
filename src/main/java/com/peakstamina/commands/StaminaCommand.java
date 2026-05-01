package com.peakstamina.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.data.StaminaData;
import com.peakstamina.handlers.ServerStaminaHandler;
import com.peakstamina.registry.StaminaAttachments;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.neoforge.registries.DeferredHolder;

public class StaminaCommand {

    private static final Map<String, DeferredHolder<Attribute, Attribute>> ATTRIBUTE_MAP = new HashMap<>();

    static {
        ATTRIBUTE_MAP.put("regen", StaminaAttributes.STAMINA_REGEN);
        ATTRIBUTE_MAP.put("usage", StaminaAttributes.STAMINA_USAGE);
        ATTRIBUTE_MAP.put("action_recovery", StaminaAttributes.STAMINA_ACTION_RECOVERY);
        ATTRIBUTE_MAP.put("regen_delay", StaminaAttributes.REGEN_DELAY_MULTIPLIER);
        ATTRIBUTE_MAP.put("exhaustion_time", StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER);
        ATTRIBUTE_MAP.put("penalty_gain", StaminaAttributes.PENALTY_GAIN_MULTIPLIER);
        ATTRIBUTE_MAP.put("penalty_decay", StaminaAttributes.PENALTY_DECAY_MULTIPLIER);
        ATTRIBUTE_MAP.put("penalty_amount", StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER);
        ATTRIBUTE_MAP.put("weight_limit", StaminaAttributes.WEIGHT_LIMIT);
        ATTRIBUTE_MAP.put("weight_mult", StaminaAttributes.WEIGHT_CALC_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_jump", StaminaAttributes.JUMP_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_sprint", StaminaAttributes.SPRINT_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_attack", StaminaAttributes.ATTACK_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_miss", StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_break", StaminaAttributes.BLOCK_BREAK_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_place", StaminaAttributes.BLOCK_PLACE_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_swim", StaminaAttributes.SWIM_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_climb", StaminaAttributes.CLIMB_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_elytra", StaminaAttributes.ELYTRA_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_block", StaminaAttributes.SHIELD_BLOCK_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("cost_item_use", StaminaAttributes.ITEM_COST_MULTIPLIER);
        ATTRIBUTE_MAP.put("bonus_capacity", StaminaAttributes.BONUS_STAMINA_CAPACITY);
        ATTRIBUTE_MAP.put("bonus_decay_rate", StaminaAttributes.BONUS_STAMINA_DECAY_RATE);
        ATTRIBUTE_MAP.put("bonus_decay_delay", StaminaAttributes.BONUS_STAMINA_DECAY_DELAY);
        ATTRIBUTE_MAP.put("excess_conversion", StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER);
    }

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIBUTES = (context, builder) -> 
        SharedSuggestionProvider.suggest(ATTRIBUTE_MAP.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stamina")
            .requires(source -> source.hasPermission(2))
            
            .then(Commands.literal("get")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    
                    // FIX: Retrieve data natively
                    StaminaData cap = player.getData(StaminaAttachments.STAMINA);
                    
                    context.getSource().sendSuccess(() -> Component.literal(
                        "§aCurrent Stamina: §f" + String.format("%.2f", cap.stamina) + 
                        " / " + String.format("%.2f", cap.maxStamina)
                    ), false);
                    return 1;
                })
            )

            .then(Commands.literal("set")
                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                    .executes(context -> {
                        float amount = FloatArgumentType.getFloat(context, "amount");
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        
                        StaminaData cap = player.getData(StaminaAttachments.STAMINA);
                        cap.stamina = amount;
                        if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;
                        ServerStaminaHandler.sync(player, cap);
                        
                        context.getSource().sendSuccess(() -> Component.literal("§aStamina set to: §f" + amount), true);
                        return 1;
                    })
                )
            )

            .then(Commands.literal("attr")
                .then(Commands.argument("attribute", StringArgumentType.word())
                    .suggests(SUGGEST_ATTRIBUTES)
                    .then(Commands.argument("value", FloatArgumentType.floatArg())
                        .executes(context -> {
                            String attrName = StringArgumentType.getString(context, "attribute");
                            float value = FloatArgumentType.getFloat(context, "value");
                            
                            return setAttribute(context.getSource(), attrName, value);
                        })
                    )
                )
            )

            .then(Commands.literal("debug")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    StaminaData cap = player.getData(StaminaAttachments.STAMINA);
                    
                    StringBuilder msg = new StringBuilder("§e=== Peak Stamina Debug ===\n");
                    
                    msg.append("§7Stamina: §f").append(String.format("%.2f", cap.stamina)).append(" / ").append(String.format("%.2f", cap.maxStamina)).append("\n");
                    msg.append("§7Bonus Stamina: §e").append(String.format("%.2f", cap.bonusStamina)).append("\n");
                    msg.append("§7Fatigue Penalty: §c").append(String.format("%.2f", cap.fatiguePenalty)).append("\n");
                    msg.append("§7Hunger Penalty: §6").append(String.format("%.2f", cap.currentHungerPenalty)).append("\n");
                    msg.append("§7Poison Penalty: §5").append(String.format("%.2f", cap.poisonPenalty)).append("\n");
                    msg.append("§7Weight Penalty: §8").append(String.format("%.2f", cap.weightPenalty)).append("\n");
                    
                    if (cap.penaltyValues != null && cap.penaltyValues.length > 0) {
                        msg.append("§e--- Custom Penalties ---\n");
                        List<? extends String> configList = StaminaLists.LISTS.universalPenalties.get();
                        
                        for (int i = 0; i < cap.penaltyValues.length; i++) {
                            float val = cap.penaltyValues[i];
                            if (val > 0.01f) {
                                String name = "Penalty #" + i;
                                if (i < configList.size()) {
                                    try {
                                       String[] parts = configList.get(i).split(";");
                                       if (parts.length > 1) name = parts[1];
                                    } catch (Exception ignored) {}
                                }
                                 msg.append("§7").append(name).append(": §4").append(String.format("%.2f", val)).append("\n");
                            }
                        }
                    }
                    
                    msg.append("§e--- Core Stats ---\n");
                    msg.append("§7Max (Base): §b").append(getAttr(player, StaminaAttributes.MAX_STAMINA)).append("\n");

                    msg.append("§e--- Configured Attributes ---\n");
                    ATTRIBUTE_MAP.entrySet().stream()
                        .sorted(Map.Entry.comparingByKey())
                        .forEach(entry -> {
                            msg.append("§7").append(entry.getKey()).append(": §f")
                               .append(String.format("%.2f", getAttr(player, entry.getValue()))).append("\n");
                        });
                        
                    context.getSource().sendSuccess(() -> Component.literal(msg.toString()), false);
                    return 1;
                })
            )

            .then(Commands.literal("reload")
                .executes(context -> {
                    com.peakstamina.handlers.ServerStaminaHandler.refreshAllCaches();
                    com.peakstamina.handlers.WeightHandler.validateCache();
                    com.peakstamina.handlers.MobStaminaHandler.refreshCache();
                    
                    com.peakstamina.handlers.CustomActionHandler.refreshCache();

                    if (net.neoforged.fml.ModList.get().isLoaded("parcool")) {
                        com.peakstamina.compat.ParCoolCompat.refreshCache();
                    }

                    context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§a[PeakStamina] Configs and weight caches successfully reloaded!"), true);
                    return 1;
                })
            )
        );
    }

    private static int setAttribute(CommandSourceStack source, String shortName, float value) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Holder<Attribute> attrObj = ATTRIBUTE_MAP.get(shortName.toLowerCase());

        if (attrObj == null) {
            source.sendFailure(Component.literal("§cUnknown attribute: " + shortName));
            return 0;
        }

        AttributeInstance inst = player.getAttribute(attrObj);
        if (inst != null) {
            inst.setBaseValue(value);
            source.sendSuccess(() -> Component.literal("§aSet §e" + shortName + "§a to §f" + value), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cAttribute instance not found on player. Did you register it properly?"));
            return 0;
        }
    }

    private static double getAttr(ServerPlayer player, Holder<Attribute> attr) {
        AttributeInstance inst = player.getAttribute(attr);
        return inst != null ? inst.getValue() : -1.0;
    }
}