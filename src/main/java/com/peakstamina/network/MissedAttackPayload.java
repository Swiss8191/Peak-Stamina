package com.peakstamina.network;

import com.peakstamina.PeakStaminaMod;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.data.StaminaData;
import com.peakstamina.handlers.ServerStaminaHandler;
import com.peakstamina.handlers.WeightHandler;
import com.peakstamina.registry.StaminaAttachments;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record MissedAttackPayload() implements CustomPacketPayload {

    public static final Type<MissedAttackPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PeakStaminaMod.MODID, "missed_attack"));
    public static final StreamCodec<FriendlyByteBuf, MissedAttackPayload> STREAM_CODEC = StreamCodec.unit(new MissedAttackPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.flow().isServerbound()) {
                ServerPlayer player = (ServerPlayer) ctx.player();
                if (player == null) return;
                
                if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
                if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;

                float cost = StaminaConfig.COMMON.depletionMissedAttack.get().floatValue();

                if (StaminaConfig.COMMON.missedAttackCostScalesWithWeight.get()) {
                    ItemStack weapon = player.getMainHandItem();
                    double wepWeight = WeightHandler.getItemWeight(weapon, StaminaConfig.COMMON.autoWeightBase.get()) / Math.max(1, weapon.getCount());
                    
                    double normalizer = StaminaConfig.COMMON.missedAttackWeightNormalizer.get();
                    double scaleFactor = StaminaConfig.COMMON.missedAttackWeightScaleFactor.get();
                    double minMult = StaminaConfig.COMMON.missedAttackWeightMinMultiplier.get();
                    double maxMult = StaminaConfig.COMMON.missedAttackWeightMaxMultiplier.get();

                    double weightMult = 1.0 + (((wepWeight - normalizer) / normalizer) * scaleFactor);
                    weightMult = Math.max(minMult, Math.min(maxMult, weightMult)); 
                    cost *= (float) weightMult;
                }

                if (cost > 0) {
                    StaminaData cap = player.getData(StaminaAttachments.STAMINA);
                    
                    var usageAttr = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.STAMINA_USAGE.get()));
                    double usageMult = usageAttr != null ? usageAttr.getValue() : 1.0;

                    var missedAttr = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.MISSED_ATTACK_COST_MULTIPLIER.get()));
                    double missedMult = missedAttr != null ? missedAttr.getValue() : 1.0;
            
                    double finalCalculatedCost = cost * usageMult * missedMult;

                    ServerStaminaHandler.consumeStamina(cap, (float) finalCalculatedCost);
                    
                    cap.stamina = Math.clamp(cap.stamina, 0, cap.maxStamina);

                    var delayAttr = player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.REGEN_DELAY_MULTIPLIER.get()));
                    double delayMult = delayAttr != null ? delayAttr.getValue() : 1.0;
                    cap.staminaRegenDelay = (int) (StaminaConfig.COMMON.recoveryDelay.get() * delayMult);
                     
                    ServerStaminaHandler.sync(player, cap);
                }
            }
        });
    }
}