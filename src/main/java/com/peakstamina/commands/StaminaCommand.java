package com.peakstamina.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.network.StaminaNetwork;
import com.peakstamina.network.packets.PacketSyncStamina;
import com.peakstamina.registry.StaminaAttributes;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegistryObject;

public class StaminaCommand {

    private static final SuggestionProvider<CommandSourceStack> SUGGEST_ATTRIBUTES = (context, builder) -> 
        SharedSuggestionProvider.suggest(
            StaminaAttributes.ATTRIBUTES.getEntries().stream()
                .map(attr -> attr.getId().getPath())
                .sorted()
                .collect(Collectors.toList()), 
            builder
        );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stamina")
            .requires(source -> source.hasPermission(2))

            .then(Commands.literal("get")
                .executes(context -> getStamina(context.getSource(), Collections.singleton(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                    .executes(context -> getStamina(context.getSource(), EntityArgument.getPlayers(context, "targets")))
                )
            )

            .then(Commands.literal("set")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                        .executes(context -> setStamina(
                            context.getSource(), 
                            EntityArgument.getPlayers(context, "targets"), 
                            FloatArgumentType.getFloat(context, "amount")
                        ))
                    )
                )
            )

            .then(Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                        .executes(context -> addStamina(
                            context.getSource(), 
                            EntityArgument.getPlayers(context, "targets"), 
                            FloatArgumentType.getFloat(context, "amount")
                        ))
                    )
                )
            )

            .then(Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("amount", FloatArgumentType.floatArg(0))
                        .executes(context -> removeStamina(
                            context.getSource(), 
                            EntityArgument.getPlayers(context, "targets"), 
                            FloatArgumentType.getFloat(context, "amount")
                        ))
                    )
                )
            )

            .then(Commands.literal("attr")
                .then(Commands.argument("targets", EntityArgument.players())
                    .then(Commands.argument("attribute", StringArgumentType.word())
                        .suggests(SUGGEST_ATTRIBUTES)
                        .then(Commands.argument("value", FloatArgumentType.floatArg())
                            .executes(context -> setAttribute(
                                context.getSource(), 
                                EntityArgument.getPlayers(context, "targets"), 
                                StringArgumentType.getString(context, "attribute"), 
                                FloatArgumentType.getFloat(context, "value")
                            ))
                        )
                    )
                )
            )

            .then(Commands.literal("debug")
                .executes(context -> debugStamina(context.getSource(), context.getSource().getPlayerOrException()))
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> debugStamina(context.getSource(), EntityArgument.getPlayer(context, "target")))
                )
            )

            .then(Commands.literal("reload")
                .executes(context -> {
                    com.peakstamina.handlers.core.ServerStaminaHandler.refreshAllCaches();
                    com.peakstamina.handlers.mechanics.WeightHandler.validateCache();
                    com.peakstamina.handlers.experimental.MobStaminaHandler.refreshCache();
                    com.peakstamina.handlers.experimental.CustomActionHandler.refreshCache();

                    if (net.minecraftforge.fml.ModList.get().isLoaded("parcool")) {
                        com.peakstamina.compat.parcool.ParCoolCompat.refreshCache();
                    }

                    context.getSource().sendSuccess(() -> Component.literal("§a[PeakStamina] Configs and weight caches successfully reloaded!"), true);
                    return 1;
                })
            )
        );
    }

    private static int getStamina(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer player : targets) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                source.sendSuccess(() -> Component.literal(
                    "§a" + player.getDisplayName().getString() + "'s Stamina: §f" + 
                    String.format("%.2f", cap.stamina) + " / " + String.format("%.2f", cap.maxStamina)
                ), false);
            });
        }
        return targets.size();
    }

    private static int setStamina(CommandSourceStack source, Collection<ServerPlayer> targets, float amount) {
        for (ServerPlayer player : targets) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                cap.stamina = amount;
                if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;
                sync(player, cap);
            });
        }
        source.sendSuccess(() -> Component.literal("§aSet stamina to §f" + amount + " §afor " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int addStamina(CommandSourceStack source, Collection<ServerPlayer> targets, float amount) {
        for (ServerPlayer player : targets) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                cap.stamina += amount;
                if (cap.stamina > cap.maxStamina) cap.stamina = cap.maxStamina;
                sync(player, cap);
            });
        }
        source.sendSuccess(() -> Component.literal("§aAdded §f" + amount + " §astamina to " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int removeStamina(CommandSourceStack source, Collection<ServerPlayer> targets, float amount) {
        for (ServerPlayer player : targets) {
            player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
                cap.stamina -= amount;
                if (cap.stamina < 0) cap.stamina = 0;
                sync(player, cap);
            });
        }
        source.sendSuccess(() -> Component.literal("§cRemoved §f" + amount + " §cstamina from " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static RegistryObject<Attribute> getAttributeByName(String name) {
        return StaminaAttributes.ATTRIBUTES.getEntries().stream()
            .filter(attr -> attr.getId().getPath().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }

    private static int setAttribute(CommandSourceStack source, Collection<ServerPlayer> targets, String attrName, float value) {
        RegistryObject<Attribute> attrObj = getAttributeByName(attrName);

        if (attrObj == null) {
            source.sendFailure(Component.literal("§cUnknown attribute: " + attrName));
            return 0;
        }

        int successCount = 0;
        for (ServerPlayer player : targets) {
            AttributeInstance inst = player.getAttribute(attrObj.get());
            if (inst != null) {
                inst.setBaseValue(value);
                successCount++;
            }
        }
        
        if (successCount > 0) {
            final int finalSuccessCount = successCount;
            source.sendSuccess(() -> Component.literal("§aSet §e" + attrName + " §ato §f" + value + " §afor " + finalSuccessCount + " player(s)."), true);
        } else {
            source.sendFailure(Component.literal("§cAttribute instance not found on any targeted players."));
        }
        return successCount;
    }

    private static int debugStamina(CommandSourceStack source, ServerPlayer target) {
        target.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            StringBuilder msg = new StringBuilder("§e=== Peak Stamina Debug: " + target.getDisplayName().getString() + " ===\n");
            
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
            
            // Helper to get category based on naming convention
            java.util.function.Function<String, String> getCategory = (name) -> {
                if (name.contains("penalty")) return "Penalty";
                if (name.contains("cost")) return "Cost";
                if (name.contains("bonus")) return "Bonus";
                return "Core";
            };

            // Group by category, then sort alphabetically within groups
            var grouped = StaminaAttributes.ATTRIBUTES.getEntries().stream()
                    .collect(java.util.stream.Collectors.groupingBy(
                        attrObj -> getCategory.apply(attrObj.getId().getPath())
                    ));

            for (String category : new String[]{"Core", "Cost", "Penalty", "Bonus"}) {
                List<RegistryObject<Attribute>> entries = grouped.getOrDefault(category, List.of());
                if (entries.isEmpty()) continue;

                msg.append("\n§6§l--- ").append(category).append(" ---\n");
                entries.stream()
                    .sorted(Comparator.comparing(attrObj -> attrObj.getId().getPath()))
                    .forEach(attrObj -> {
                        msg.append("§7").append(attrObj.getId().getPath()).append(": §f")
                           .append(String.format("%.2f", getAttr(target, attrObj.get()))).append("\n");
                    });
            }

            source.sendSuccess(() -> Component.literal(msg.toString()), false);
        });
        return 1;
    }

    private static void sync(ServerPlayer player, StaminaCapability cap) {
        StaminaNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncStamina(
                cap.stamina, cap.maxStamina, cap.fatiguePenalty, cap.currentHungerPenalty, 
                cap.poisonPenalty, cap.weightPenalty, cap.exhaustionCooldown, 
                cap.bonusStamina, cap.penaltyValues
            )
        );
    }

    private static double getAttr(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attr) {
        AttributeInstance inst = player.getAttribute(attr);
        return inst != null ? inst.getValue() : -1.0;
    }
}