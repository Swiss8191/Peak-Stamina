package com.peakstamina.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists; 
import com.peakstamina.network.PacketSyncStamina;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

public class StaminaCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIBUTES = (context, builder) -> 
        SharedSuggestionProvider.suggest(new String[]{
            "regen", 
            "action_recovery",
            "regen_delay",
            "exhaustion_time",
            "penalty_gain", 
            "penalty_decay",
            "weight_limit",
            "weight_mult",
            "usage",
            "cost_jump", 
            "cost_sprint", 
            "cost_attack", 
            "cost_miss",
            "cost_break",
            "cost_place", 
            "cost_swim", 
            "cost_climb",
            "cost_elytra",
            "bonus_capacity",
            "bonus_decay_rate",
            "bonus_decay_delay",
            "excess_conversion"
        }, builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stamina")
            .requires(source -> source.hasPermission(2))
            
            .then(Commands.literal("get")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                        context.getSource().sendSuccess(() -> Component.literal(
                            "§aCurrent Stamina: §f" + String.format("%.2f", cap.stamina) + 
                            " / " + String.format("%.2f", cap.maxStamina)
                        ), false);
                    });
                    return 1;
                })
            )

            .then(Commands.literal("set")
                .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                    .executes(context -> {
                        float amount = FloatArgumentType.getFloat(context, "amount");
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        
                        player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                            cap.stamina = amount;
                            if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;
                            sync(player, cap);
                            context.getSource().sendSuccess(() -> Component.literal("§aStamina set to: §f" + amount), true);
                        });
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
                    player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
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
                        
                        msg.append("§e--- Attributes ---\n");
                        msg.append("§7Max (Base): §b").append(getAttr(player, StaminaAttributes.MAX_STAMINA.get())).append("\n");
                        msg.append("§7Regen Mult: §a").append(getAttr(player, StaminaAttributes.STAMINA_REGEN.get())).append("\n");
                        msg.append("§7Usage Mult: §c").append(getAttr(player, StaminaAttributes.STAMINA_USAGE.get())).append("\n");
                        msg.append("§7Action Recovery Mult: §e").append(getAttr(player, StaminaAttributes.STAMINA_ACTION_RECOVERY.get())).append("\n");
                        msg.append("§7Weight Limit Bonus: §f").append(getAttr(player, StaminaAttributes.WEIGHT_LIMIT.get())).append("\n");
                        
                        msg.append("§e--- Modifiers ---\n");
                        msg.append("§7Penalty Amount: §d").append(getAttr(player, StaminaAttributes.PENALTY_AMOUNT_MULTIPLIER.get())).append("\n");
                        msg.append("§7Penalty Gain: §c").append(getAttr(player, StaminaAttributes.PENALTY_GAIN_MULTIPLIER.get())).append("\n");
                        msg.append("§7Penalty Decay: §a").append(getAttr(player, StaminaAttributes.PENALTY_DECAY_MULTIPLIER.get())).append("\n");
                        
                        msg.append("§e--- RPG Modifiers ---\n");
                        msg.append("§7Regen Delay Mult: §a").append(getAttr(player, StaminaAttributes.REGEN_DELAY_MULTIPLIER.get())).append("\n");
                        msg.append("§7Exhaustion Time Mult: §c").append(getAttr(player, StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER.get())).append("\n");
                        msg.append("§7Weight Calc Mult: §e").append(getAttr(player, StaminaAttributes.WEIGHT_CALC_MULTIPLIER.get())).append("\n");

                        msg.append("§e--- Bonus Stamina Modifiers ---\n");
                        msg.append("§7Capacity Mult: §b").append(getAttr(player, StaminaAttributes.BONUS_STAMINA_CAPACITY.get())).append("\n");
                        msg.append("§7Decay Rate Mult: §c").append(getAttr(player, StaminaAttributes.BONUS_STAMINA_DECAY_RATE.get())).append("\n");
                        msg.append("§7Decay Delay Mult: §a").append(getAttr(player, StaminaAttributes.BONUS_STAMINA_DECAY_DELAY.get())).append("\n");
                        msg.append("§7Excess Conversion Mult: §e").append(getAttr(player, StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER.get())).append("\n");

                        context.getSource().sendSuccess(() -> Component.literal(msg.toString()), false);
                    });
                    return 1;
                })
            )
        );
    }

    private static void sync(ServerPlayer player, StaminaCapability cap) {
        StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncStamina(cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, cap.bonusStamina, cap.penaltyValues));
    }

    private static int setAttribute(CommandSourceStack source, String shortName, float value) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Attribute attr = null;

        switch (shortName.toLowerCase()) {
            case "regen": attr = StaminaAttributes.STAMINA_REGEN.get(); break;
            case "usage": attr = StaminaAttributes.STAMINA_USAGE.get(); break;
            case "action_recovery": attr = StaminaAttributes.STAMINA_ACTION_RECOVERY.get(); break;
            case "penalty_gain": attr = StaminaAttributes.PENALTY_GAIN_MULTIPLIER.get(); break;
            case "penalty_decay": attr = StaminaAttributes.PENALTY_DECAY_MULTIPLIER.get(); break;
            case "weight_limit": attr = StaminaAttributes.WEIGHT_LIMIT.get(); break;
            case "cost_jump": attr = StaminaAttributes.JUMP_COST_MULTIPLIER.get(); break;
            case "cost_sprint": attr = StaminaAttributes.SPRINT_COST_MULTIPLIER.get(); break;
            case "cost_attack": attr = StaminaAttributes.ATTACK_COST_MULTIPLIER.get(); break;
            case "cost_miss": attr = StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get(); break;
            case "cost_break": attr = StaminaAttributes.BLOCK_BREAK_COST_MULTIPLIER.get(); break;
            case "cost_place": attr = StaminaAttributes.BLOCK_PLACE_COST_MULTIPLIER.get(); break;
            case "cost_elytra": attr = StaminaAttributes.ELYTRA_COST_MULTIPLIER.get(); break;
            case "cost_swim": attr = StaminaAttributes.SWIM_COST_MULTIPLIER.get(); break;
            case "cost_climb": attr = StaminaAttributes.CLIMB_COST_MULTIPLIER.get(); break;
            case "regen_delay": attr = StaminaAttributes.REGEN_DELAY_MULTIPLIER.get(); break;
            case "exhaustion_time": attr = StaminaAttributes.EXHAUSTION_DURATION_MULTIPLIER.get(); break;
            case "weight_mult": attr = StaminaAttributes.WEIGHT_CALC_MULTIPLIER.get(); break;
            case "bonus_capacity": attr = StaminaAttributes.BONUS_STAMINA_CAPACITY.get(); break;
            case "bonus_decay_rate": attr = StaminaAttributes.BONUS_STAMINA_DECAY_RATE.get(); break;
            case "bonus_decay_delay": attr = StaminaAttributes.BONUS_STAMINA_DECAY_DELAY.get(); break;
            case "excess_conversion": attr = StaminaAttributes.EXCESS_CONVERSION_MULTIPLIER.get(); break;
        }

        if (attr == null) {
            source.sendFailure(Component.literal("§cUnknown attribute: " + shortName));
            return 0;
        }

        AttributeInstance inst = player.getAttribute(attr);
        if (inst != null) {
            inst.setBaseValue(value);
            source.sendSuccess(() -> Component.literal("§aSet §e" + shortName + "§a to §f" + value), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cAttribute instance not found on player."));
            return 0;
        }
    }

    private static double getAttr(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attr) {
        AttributeInstance inst = player.getAttribute(attr);
        return inst != null ? inst.getValue() : -1.0;
    }
}