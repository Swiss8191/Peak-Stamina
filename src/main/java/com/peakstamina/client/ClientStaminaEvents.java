package com.peakstamina.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.peakstamina.peakStaminaMod;
import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import com.peakstamina.config.StaminaLists;
import com.peakstamina.registry.StaminaAttributes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = peakStaminaMod.MODID, value = Dist.CLIENT)
public class ClientStaminaEvents {

    private static final String ICON_FATIGUE = "⚡";
    private static final String ICON_HUNGER = "🍖";
    private static final String ICON_POISON = "☠";
    private static final String ICON_WEIGHT = "⚓";

    private static float displayedStamina = 100.0f;
    private static float displayedBonusStamina = 0.0f;
    private static float displayedPenalty = 0.0f;
    private static float displayedHunger = 0.0f;
    private static float displayedPoison = 0.0f;
    private static float displayedWeight = 0.0f;
    private static float currentFadeProgress = 0.0f;
    private static float currentSlideProgress = 0.0f;
    private static int visibleLingerTimer = 0;

    private static final Map<Integer, Float> smoothedPenalties = new HashMap<>();
    private static boolean isCacheValid = false;
    private static final List<MobEffect> cachedInfiniteEffects = new ArrayList<>();
    private static final List<Integer> cachedPenaltyColors = new ArrayList<>();
    private static final List<String> cachedPenaltyIcons = new ArrayList<>();

    @SubscribeEvent
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (event.getOverlay().id().equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            renderStaminaHUD(event.getGuiGraphics());
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        net.minecraft.world.entity.player.Player player = event.getEntity();
        if (player == null) {
            return;
        }

        if (!com.peakstamina.config.StaminaConfig.COMMON.enableStamina.get()) {
            return;
        }
        if (player.isCreative() && com.peakstamina.config.StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (player.isSpectator() && com.peakstamina.config.StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        com.peakstamina.network.StaminaNetwork.CHANNEL.sendToServer(new com.peakstamina.network.PacketMissedAttack());
    }

    private static void validateCache() {
        if (isCacheValid) {
            return;
        }

        cachedInfiniteEffects.clear();
        for (String id : StaminaLists.LISTS.infiniteStaminaEffects.get()) {
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null && ForgeRegistries.MOB_EFFECTS.containsKey(loc)) {
                cachedInfiniteEffects.add(ForgeRegistries.MOB_EFFECTS.getValue(loc));
            }
        }

        cachedPenaltyColors.clear();
        cachedPenaltyIcons.clear();
        for (String entry : StaminaLists.LISTS.universalPenalties.get()) {
            int color = 0xFFFFFF;
            String iconText = null;
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 7) {
                    color = Integer.parseInt(parts[6].trim());
                }
                if (parts.length >= 8) {
                    String text = parts[7].trim();
                    if (!text.isEmpty() && !text.equalsIgnoreCase("none") && !text.equalsIgnoreCase("null")) {
                        iconText = text;
                    }
                }
            } catch (Exception ignored) {
            }
            cachedPenaltyColors.add(color);
            cachedPenaltyIcons.add(iconText);
        }
        isCacheValid = true;
    }

    private static boolean hasInfiniteStamina(net.minecraft.world.entity.LivingEntity player) {
        validateCache();
        for (MobEffect effect : cachedInfiniteEffects) {
            if (player.hasEffect(effect)) {
                return true;
            }
        }
        return false;
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int) (((color >> 24) & 0xFF) * alpha);
        return (a << 24) | (color & 0x00FFFFFF);
    }

    private static int shadeColor(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (r << 16) | (g << 8) | b;
    }

    private static float applyEasing(float t, StaminaConfig.AutoHudEasing easing) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        switch (easing) {
            case SMOOTHSTEP:
                return t * t * (3.0f - 2.0f * t);
            case EASE_OUT_SINE:
                return (float) Math.sin((t * Math.PI) / 2.0);
            case EASE_OUT_EXPO:
                return t == 1.0f ? 1.0f : (float) (1.0 - Math.pow(2.0, -10.0 * t));
            case LINEAR:
            default:
                return t;
        }
    }

    private static void renderStaminaHUD(GuiGraphics gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) {
            return;
        }

        if (mc.player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) {
            return;
        }
        if (mc.player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) {
            return;
        }
        boolean showIcons = StaminaConfig.CLIENT.showIcons.get();
        validateCache();
        mc.player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();

            float hungerTarget = cap.currentHungerPenalty;
            float poisonTarget = cap.poisonPenalty;
            float weightTarget = cap.weightPenalty;

            displayedStamina += (cap.stamina - displayedStamina) * 0.2f;
            displayedBonusStamina += (cap.bonusStamina - displayedBonusStamina) * 0.2f;

            displayedPenalty += (cap.fatiguePenalty - displayedPenalty) * 0.1f;
            displayedHunger += (hungerTarget - displayedHunger) * 0.1f;
            displayedPoison += (poisonTarget - displayedPoison) * 0.1f;
            displayedWeight += (weightTarget - displayedWeight) * 0.1f;

            if (Math.abs(cap.stamina - displayedStamina) < 0.05f) {
                displayedStamina = cap.stamina;
            }
            if (Math.abs(cap.bonusStamina - displayedBonusStamina) < 0.05f) {
                displayedBonusStamina = cap.bonusStamina;
            }
            if (Math.abs(cap.fatiguePenalty - displayedPenalty) < 0.05f) {
                displayedPenalty = cap.fatiguePenalty;
            }
            if (Math.abs(hungerTarget - displayedHunger) < 0.05f) {
                displayedHunger = hungerTarget;
            }
            if (Math.abs(poisonTarget - displayedPoison) < 0.05f) {
                displayedPoison = poisonTarget;
            }
            if (Math.abs(weightTarget - displayedWeight) < 0.05f) {
                displayedWeight = weightTarget;
            }
            float totalPenaltySum = displayedPenalty + displayedHunger + displayedPoison + displayedWeight;
            if (cap.penaltyValues != null) {
                for (int i = 0; i < cap.penaltyValues.length; i++) {
                    if (i >= cachedPenaltyColors.size()) {
                        break;
                    }
                    float targetVal = cap.penaltyValues[i];
                    float currentVal = smoothedPenalties.getOrDefault(i, 0.0f);
                    currentVal += (targetVal - currentVal) * 0.1f;
                    if (Math.abs(targetVal - currentVal) < 0.05f) {
                        currentVal = targetVal;
                    }
                    smoothedPenalties.put(i, currentVal);
                    totalPenaltySum += currentVal;
                }
            }

            float baseMax = 100.0f;
            AttributeInstance maxAttr = mc.player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
            if (maxAttr != null) {
                baseMax = (float) maxAttr.getValue();
            }
            if (baseMax <= 0) {
                baseMax = 100.0f;
            }

            boolean isRecentlyUsed = cap.staminaRegenDelay > 0;
            boolean hasPenalties = totalPenaltySum > 0.5f && StaminaConfig.CLIENT.autoHudShowOnPenalties.get();
            boolean isBelowThreshold = displayedStamina <= (baseMax * StaminaConfig.CLIENT.autoHudThreshold.get());
            boolean hasBonus = displayedBonusStamina > 0.5f;

            boolean isActive = isRecentlyUsed || isBelowThreshold || hasPenalties || hasBonus;

            if (isActive) {
                visibleLingerTimer = StaminaConfig.CLIENT.autoHudLingerTime.get();
            } else if (visibleLingerTimer > 0) {
                visibleLingerTimer--;
                isActive = true;
            }

            boolean shouldShow = !StaminaConfig.CLIENT.autoHudEnable.get() || isActive;
            float targetAnim = shouldShow ? 1.0f : 0.0f;

            float fadeSpeed = shouldShow ? StaminaConfig.CLIENT.autoHudFadeInSpeed.get().floatValue() : StaminaConfig.CLIENT.autoHudFadeOutSpeed.get().floatValue();
            float slideSpeed = shouldShow ? StaminaConfig.CLIENT.autoHudSlideInSpeed.get().floatValue() : StaminaConfig.CLIENT.autoHudSlideOutSpeed.get().floatValue();

            currentFadeProgress += (targetAnim - currentFadeProgress) * fadeSpeed;
            currentSlideProgress += (targetAnim - currentSlideProgress) * slideSpeed;

            if (currentFadeProgress < 0.01f && currentSlideProgress < 0.01f && !shouldShow) {
                currentFadeProgress = 0.0f;
                currentSlideProgress = 0.0f;
                return;
            }

            StaminaConfig.AutoHudMode mode = StaminaConfig.CLIENT.autoHudMode.get();
            float renderAlpha = (mode == StaminaConfig.AutoHudMode.FADE || mode == StaminaConfig.AutoHudMode.BOTH) ? currentFadeProgress : 1.0f;

            int slideX = 0;
            int slideY = 0;
            if (mode == StaminaConfig.AutoHudMode.SLIDE || mode == StaminaConfig.AutoHudMode.BOTH) {
                float easedSlide = applyEasing(currentSlideProgress, StaminaConfig.CLIENT.autoHudEasing.get());
                int maxDist = StaminaConfig.CLIENT.autoHudSlideDistance.get();
                int offsetAmount = (int) ((1.0f - easedSlide) * maxDist);
                switch (StaminaConfig.CLIENT.autoHudSlideDir.get()) {
                    case DOWN:
                        slideY = offsetAmount;
                        break;
                    case UP:
                        slideY = -offsetAmount;
                        break;
                    case RIGHT:
                        slideX = offsetAmount;
                        break;
                    case LEFT:
                        slideX = -offsetAmount;
                        break;
                }
            }

            boolean isIconMode = false;
            try {
                isIconMode = StaminaConfig.CLIENT.hudStyle.get().name().equalsIgnoreCase("ICON");
            } catch (Exception e) {
                isIconMode = false;
            }

            int sBarW = isIconMode ? 81 : StaminaConfig.CLIENT.barWidth.get();
            int sBarH = isIconMode ? 9 : StaminaConfig.CLIENT.barHeight.get();

            int offsetX = isIconMode ? StaminaConfig.CLIENT.iconXOffset.get() : StaminaConfig.CLIENT.barXOffset.get();
            int offsetY = isIconMode ? StaminaConfig.CLIENT.iconYOffset.get() : StaminaConfig.CLIENT.barYOffset.get();

            int sBarX = (width / 2) - (sBarW / 2) + offsetX + slideX;
            int sBarY = height - 24 - offsetY + slideY;

            if (isIconMode) {
                sBarX = (width / 2) - 90 + offsetX + slideX;

                // Dynamically calculate the Y position based on health, absorption, and armor
                int healthRows = (int) Math.ceil((mc.player.getMaxHealth() + mc.player.getAbsorptionAmount()) / 20.0f);
                int armorRows = mc.player.getArmorValue() > 0 ? 1 : 0;

                // Base health row starts at 39. Each active row pushes the UI up by 10 pixels.
                int totalOffsetRows = healthRows + armorRows;
                int dynamicYOffset = 39 + (totalOffsetRows * 10);

                sBarY = height - dynamicYOffset - offsetY + slideY;
            }

            int iconY = isIconMode ? sBarY - 12 : sBarY;

            float compressionRatio = 1.0f;
            if (totalPenaltySum > baseMax && baseMax > 0) {
                compressionRatio = baseMax / totalPenaltySum;
            }

            int bgCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorBackground.get(), renderAlpha);
            int stripeCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorStripes.get(), renderAlpha);
            int energyCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyHunger.get(), renderAlpha);
            int poisonCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyPoison.get(), renderAlpha);
            int weightCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyWeight.get(), renderAlpha);
            int sepCol = applyAlpha(0xFF000000, renderAlpha);

            if (isIconMode) {
                for (int i = 0; i < 10; i++) {
                    drawLightningOutline(gfx, sBarX + i * 8, sBarY, applyAlpha(0xFF000000, renderAlpha));
                }
            } else {
                gfx.fill(sBarX - 1, sBarY - 1, sBarX + sBarW + 1, sBarY + sBarH + 1, sepCol);
                gfx.fill(sBarX, sBarY, sBarX + sBarW, sBarY + sBarH, bgCol);
            }

            float pxScale = sBarW / baseMax;
            float effectivePenaltyScale = pxScale * compressionRatio;
            int currentPenaltyRightEdge = sBarW;

            // Fatigue
            int fatiguePx = (int) (displayedPenalty * effectivePenaltyScale);
            if (fatiguePx > 0) {
                if (fatiguePx > currentPenaltyRightEdge) {
                    fatiguePx = currentPenaltyRightEdge;
                }
                int startX = currentPenaltyRightEdge - fatiguePx;

                if (isIconMode) {
                    gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawLightningStripes(gfx, sBarX + i * 8, sBarY, stripeCol);
                    }
                    gfx.disableScissor();
                } else {
                    drawStripesHUD(gfx, sBarX + startX, sBarY, fatiguePx, sBarH, stripeCol, renderAlpha);
                    gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                }
                if (showIcons) {
                    drawIcon(gfx, sBarX + startX, iconY, fatiguePx, sBarH, ICON_FATIGUE, stripeCol, renderAlpha);
                }
                currentPenaltyRightEdge -= fatiguePx;
            }

            // Hunger
            int hungerPx = (int) (displayedHunger * effectivePenaltyScale);
            if (hungerPx > 0 && currentPenaltyRightEdge > 0) {
                if (hungerPx > currentPenaltyRightEdge) {
                    hungerPx = currentPenaltyRightEdge;
                }
                int startX = currentPenaltyRightEdge - hungerPx;

                if (isIconMode) {
                    gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawLightningStripes(gfx, sBarX + i * 8, sBarY, energyCol);
                    }
                    gfx.disableScissor();
                } else {
                    drawStripesHUD(gfx, sBarX + startX, sBarY, hungerPx, sBarH, energyCol, renderAlpha);
                    gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                }
                if (showIcons) {
                    drawIcon(gfx, sBarX + startX, iconY, hungerPx, sBarH, ICON_HUNGER, energyCol, renderAlpha);
                }
                currentPenaltyRightEdge -= hungerPx;
            }

            // Poison
            int poisonPx = (int) (displayedPoison * effectivePenaltyScale);
            if (poisonPx > 0 && currentPenaltyRightEdge > 0) {
                if (poisonPx > currentPenaltyRightEdge) {
                    poisonPx = currentPenaltyRightEdge;
                }
                int startX = currentPenaltyRightEdge - poisonPx;

                if (isIconMode) {
                    gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawLightningStripes(gfx, sBarX + i * 8, sBarY, poisonCol);
                    }
                    gfx.disableScissor();
                } else {
                    drawStripesHUD(gfx, sBarX + startX, sBarY, poisonPx, sBarH, poisonCol, renderAlpha);
                    gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                }
                if (showIcons) {
                    drawIcon(gfx, sBarX + startX, iconY, poisonPx, sBarH, ICON_POISON, poisonCol, renderAlpha);
                }
                currentPenaltyRightEdge -= poisonPx;
            }

            // Weight
            int weightPx = (int) (displayedWeight * effectivePenaltyScale);
            if (weightPx > 0 && currentPenaltyRightEdge > 0) {
                if (weightPx > currentPenaltyRightEdge) {
                    weightPx = currentPenaltyRightEdge;
                }
                int startX = currentPenaltyRightEdge - weightPx;

                if (isIconMode) {
                    gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawLightningStripes(gfx, sBarX + i * 8, sBarY, weightCol);
                    }
                    gfx.disableScissor();
                } else {
                    drawStripesHUD(gfx, sBarX + startX, sBarY, weightPx, sBarH, weightCol, renderAlpha);
                    gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                }
                if (showIcons) {
                    drawIcon(gfx, sBarX + startX, iconY, weightPx, sBarH, ICON_WEIGHT, weightCol, renderAlpha);
                }
                currentPenaltyRightEdge -= weightPx;
            }

            // Universal Config Penalties
            if (cap.penaltyValues != null) {
                for (int i = 0; i < cap.penaltyValues.length; i++) {
                    if (i >= cachedPenaltyColors.size()) {
                        break;
                    }
                    int color = cachedPenaltyColors.get(i);
                    float currentVal = smoothedPenalties.getOrDefault(i, 0.0f);
                    int pPx = (int) (currentVal * effectivePenaltyScale);
                    if (pPx > 0 && currentPenaltyRightEdge > 0) {
                        if (pPx > currentPenaltyRightEdge) {
                            pPx = currentPenaltyRightEdge;
                        }
                        int startX = currentPenaltyRightEdge - pPx;
                        int customCol = applyAlpha(0xFF000000 | color, renderAlpha);
                        if (isIconMode) {
                            gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                            for (int idx = 0; idx < 10; idx++) {
                                drawLightningStripes(gfx, sBarX + idx * 8, sBarY, customCol);
                            }
                            gfx.disableScissor();
                        } else {
                            drawStripesHUD(gfx, sBarX + startX, sBarY, pPx, sBarH, customCol, renderAlpha);
                            gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                        }

                        if (showIcons && i < cachedPenaltyIcons.size()) {
                            String icon = cachedPenaltyIcons.get(i);
                            if (icon != null) {
                                drawIcon(gfx, sBarX + startX, iconY, pPx, sBarH, icon, customCol, renderAlpha);
                            }
                        }
                        currentPenaltyRightEdge -= pPx;
                    }
                }
            }

            int colorTop;
            int colorBottom;
            if (hasInfiniteStamina(mc.player)) {
                colorBottom = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorTireless.get(), renderAlpha);
                colorTop = applyAlpha((0xFF000000 | StaminaConfig.CLIENT.colorTireless.get()) + 0x002222, renderAlpha);
            } else if (displayedStamina <= (baseMax * StaminaConfig.COMMON.fatigueThreshold.get())) {
                int baseCrit = StaminaConfig.CLIENT.colorCritical.get();

                int r = (baseCrit >> 16) & 0xFF;
                int g = (baseCrit >> 8) & 0xFF;
                int b = baseCrit & 0xFF;

                if (isIconMode) {

                    int rTop = (int) (r * 0.24f);
                    int gTop = (int) (g * 0.24f);
                    int bTop = (int) (b * 0.24f);

                    colorBottom = applyAlpha(0xFF000000 | baseCrit, renderAlpha);
                    colorTop = applyAlpha(0xFF000000 | (rTop << 16) | (gTop << 8) | bTop, renderAlpha);
                } else {
                    colorBottom = applyAlpha(0xFF000000 | baseCrit, renderAlpha);
                    colorTop = applyAlpha((0xFF000000 | baseCrit) + 0x222222, renderAlpha);
                }
            } else {
                colorBottom = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorSafe.get(), renderAlpha);
                colorTop = applyAlpha((0xFF000000 | StaminaConfig.CLIENT.colorSafe.get()) + 0x222222, renderAlpha);
            }

            int normalW = (int) (displayedStamina * pxScale);
            int renderNormalW = Math.min(normalW, currentPenaltyRightEdge);

            if (renderNormalW > 0) {
                if (isIconMode) {
                    gfx.enableScissor(sBarX, sBarY, sBarX + renderNormalW, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawLightningBaseGradient(gfx, sBarX + i * 8, sBarY, colorTop, colorBottom);
                    }
                    gfx.disableScissor();
                } else {
                    gfx.fillGradient(sBarX, sBarY, sBarX + renderNormalW, sBarY + sBarH, colorTop, colorBottom);
                    if (renderNormalW < currentPenaltyRightEdge) {
                        gfx.fill(sBarX + renderNormalW, sBarY, sBarX + renderNormalW + 1, sBarY + sBarH, sepCol);
                    }
                }
            }

            if (displayedBonusStamina > 0.1f && currentPenaltyRightEdge > 0) {
                float totalBonusPx = displayedBonusStamina * effectivePenaltyScale;
                int fullBars = (int) (totalBonusPx / currentPenaltyRightEdge);
                int remainderPx = (int) (totalBonusPx % currentPenaltyRightEdge);
                if (fullBars > 0 && remainderPx == 0) {
                    fullBars--;
                    remainderPx = currentPenaltyRightEdge;
                }

                int bTopRGB = StaminaConfig.CLIENT.colorBonusTop.get();
                int bBotRGB = StaminaConfig.CLIENT.colorBonusBottom.get();
                int hRGB = StaminaConfig.CLIENT.colorBonusHighlight.get();
                int hAlpha = (int) (StaminaConfig.CLIENT.bonusHighlightAlpha.get() * renderAlpha);
                int sheenCol = (hAlpha << 24) | hRGB;

                if (fullBars > 0) {
                    int underTier = fullBars - 1;
                    float uFactor = 1.0f - ((underTier % 3) * 0.35f);
                    int uTop = shadeColor(bTopRGB, uFactor);
                    int uBot = shadeColor(bBotRGB, uFactor);
                    int underTopCol = applyAlpha(0xFF000000 | uTop, renderAlpha);
                    int underBotCol = applyAlpha(0xFF000000 | uBot, renderAlpha);

                    if (isIconMode) {
                        gfx.enableScissor(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                        for (int i = 0; i < 10; i++) {
                            drawLightningBaseGradient(gfx, sBarX + i * 8, sBarY, underTopCol, underBotCol);
                        }
                        for (int i = 0; i < 10; i++) {
                            drawLightningSheen(gfx, sBarX + i * 8, sBarY, sheenCol);
                        }
                        gfx.disableScissor();
                    } else {
                        gfx.fillGradient(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH, underTopCol, underBotCol);
                        if (sBarH > 2) {
                            gfx.fill(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + 1, sheenCol); 
                        }else {
                            gfx.fill(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH, sheenCol);
                        }
                    }
                }

                if (remainderPx > 0) {
                    int overTier = fullBars;
                    float oFactor = 1.0f - ((overTier % 3) * 0.35f);
                    int oTop = shadeColor(bTopRGB, oFactor);
                    int oBot = shadeColor(bBotRGB, oFactor);
                    int overTopCol = applyAlpha(0xFF000000 | oTop, renderAlpha);
                    int overBotCol = applyAlpha(0xFF000000 | oBot, renderAlpha);

                    if (isIconMode) {
                        gfx.enableScissor(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH);
                        for (int i = 0; i < 10; i++) {
                            drawLightningBaseGradient(gfx, sBarX + i * 8, sBarY, overTopCol, overBotCol);
                        }
                        for (int i = 0; i < 10; i++) {
                            drawLightningSheen(gfx, sBarX + i * 8, sBarY, sheenCol);
                        }
                        gfx.disableScissor();

                        if (remainderPx < currentPenaltyRightEdge) {
                            gfx.enableScissor(sBarX + remainderPx, sBarY, sBarX + remainderPx + 1, sBarY + sBarH);
                            int sepColor = applyAlpha(0xCCFFFFFF, renderAlpha);
                            for (int i = 0; i < 10; i++) {
                                drawLightningBaseGradient(gfx, sBarX + i * 8, sBarY, sepColor, sepColor);
                            }
                            gfx.disableScissor();
                        }
                    } else {
                        gfx.fillGradient(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, overTopCol, overBotCol);
                        if (sBarH > 2) {
                            gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + 1, sheenCol); 
                        }else {
                            gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, sheenCol);
                        }

                        if (remainderPx < currentPenaltyRightEdge) {
                            gfx.fill(sBarX + remainderPx, sBarY, sBarX + remainderPx + 1, sBarY + sBarH, applyAlpha(0xFFFFFFFF, renderAlpha));
                        }
                    }
                }

                if (fullBars > 0) {
                    String multText = (fullBars + 1) + "x";
                    gfx.pose().pushPose();
                    float scale = 0.6f;
                    int textWidth = mc.font.width(multText);
                    int textX = sBarX - (int) (textWidth * scale) - 4;
                    int textY = sBarY + (sBarH / 2) - (int) (4 * scale);

                    gfx.pose().translate(textX, textY, 0);
                    gfx.pose().scale(scale, scale, 1.0f);
                    gfx.drawString(mc.font, multText, 0, 0, applyAlpha(0xFFFFD700, renderAlpha), true);
                    gfx.pose().popPose();
                }
            }

            double regenVal = 1.0;
            AttributeInstance regenAttr = mc.player.getAttribute(StaminaAttributes.STAMINA_REGEN.get());
            if (regenAttr != null) {
                regenVal = regenAttr.getValue();
            }

            net.minecraft.network.chat.MutableComponent regenComp = null;
            int regenColor = 0xFFFFFFFF;
            if (regenVal >= 1.5) {
                regenComp = net.minecraft.network.chat.Component.literal(">>>");
                regenColor = 0xFF00FF00;
            } else if (regenVal >= 1.25) {
                regenComp = net.minecraft.network.chat.Component.literal(">>");
                regenColor = 0xFF55FF55;
            } else if (regenVal > 1.01) {
                regenComp = net.minecraft.network.chat.Component.literal(">");
                regenColor = 0xFFAAFF55;
            } else if (regenVal <= 0.5) {
                regenComp = net.minecraft.network.chat.Component.literal("<<<");
                regenColor = 0xFFFF0000;
            } else if (regenVal <= 0.75) {
                regenComp = net.minecraft.network.chat.Component.literal("<<");
                regenColor = 0xFFFF5555;
            } else if (regenVal < 0.99) {
                regenComp = net.minecraft.network.chat.Component.literal("<");
                regenColor = 0xFFFF9955;
            }

            if (regenComp != null && renderNormalW > 2) {
                regenComp.withStyle(net.minecraft.ChatFormatting.BOLD);
                float scale = 0.5f;
                int textWidth = mc.font.width(regenComp);

                int targetX;
                int targetY;

                if (isIconMode) {
                    targetX = sBarX + renderNormalW - (int) (textWidth * scale) - 2;
                    targetY = iconY + 5;
                } else {
                    targetX = sBarX + renderNormalW - (int) (textWidth * scale) - 2;
                    targetY = sBarY + (sBarH / 2) - (int) (4 * scale);
                }

                if (targetX >= sBarX) {
                    gfx.pose().pushPose();
                    gfx.pose().translate(targetX, targetY, 0);
                    gfx.pose().scale(scale, scale, 1.0f);
                    gfx.drawString(mc.font, regenComp, 0, 0, applyAlpha(regenColor, renderAlpha), true);
                    gfx.pose().popPose();
                }
            }
        });
    }

    private static final int[][] LIGHTNING_SHAPE = {
        {3, 5}, // row 0
        {2, 4},
        {1, 3},
        {0, 5},
        {3, 5},
        {2, 4},
        {1, 3},
        {0, 2}, // row 7
    };

    private static void drawLightningBaseGradient(GuiGraphics gfx, int x, int y, int colorTop, int colorBottom) {
        for (int r = 0; r < LIGHTNING_SHAPE.length; r++) {
            int startX = LIGHTNING_SHAPE[r][0];
            int endX = LIGHTNING_SHAPE[r][1];

            float ratioStart = (float) r / (LIGHTNING_SHAPE.length);
            float ratioEnd = (float) (r + 1) / (LIGHTNING_SHAPE.length);

            int rowColorTop = interpolateColor(colorTop, colorBottom, ratioStart);
            int rowColorBottom = interpolateColor(colorTop, colorBottom, ratioEnd);

            gfx.fillGradient(x + startX, y + r, x + endX + 1, y + r + 1, rowColorTop, rowColorBottom);
        }
    }

    private static void drawLightningStripes(GuiGraphics gfx, int x, int y, int color) {
        for (int r = 0; r < LIGHTNING_SHAPE.length; r++) {
            int startX = LIGHTNING_SHAPE[r][0];
            int endX = LIGHTNING_SHAPE[r][1];
            for (int c = startX; c <= endX; c++) {
                if ((r + c) % 4 < 2) {
                    gfx.fill(x + c, y + r, x + c + 1, y + r + 1, color);
                }
            }
        }
    }

    private static void drawLightningOutline(GuiGraphics gfx, int x, int y, int color) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                for (int r = 0; r < LIGHTNING_SHAPE.length; r++) {
                    int startX = LIGHTNING_SHAPE[r][0];
                    int endX = LIGHTNING_SHAPE[r][1];
                    gfx.fill(x + startX + dx, y + r + dy, x + endX + 1 + dx, y + r + dy + 1, color);
                }
            }
        }
    }

    private static void drawLightningSheen(GuiGraphics gfx, int x, int y, int color) {
        for (int r = 0; r < Math.min(2, LIGHTNING_SHAPE.length); r++) {
            int startX = LIGHTNING_SHAPE[r][0];
            int endX = LIGHTNING_SHAPE[r][1];
            gfx.fill(x + startX, y + r, x + endX + 1, y + r + 1, color);
        }
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * factor);
        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawIcon(GuiGraphics gfx, int x, int y, int w, int h, String text, int color, float alpha) {
        if (text == null || text.isEmpty()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        int textWidth = font.width(text);
        float fontHeight = 9.0f;
        float scale = 0.6f;

        if (h > fontHeight) {
            scale = ((h * 0.9f) / fontHeight) * 0.6f;
        }

        float maxScaleW = (w * 0.95f) / (float) textWidth;
        scale = Math.min(scale, maxScaleW);

        if (scale < 0.1f) {
            return;
        }

        float centerX = x + w / 2.0f;
        float centerY = y + h / 2.0f;

        gfx.pose().pushPose();
        gfx.pose().translate(centerX, centerY, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        float localBarHeight = h / scale;
        float localBarHalfH = localBarHeight / 2.0f;

        float drawX = -textWidth / 2.0f;
        float drawY = localBarHalfH - fontHeight + 1.0f;

        int shadowColor = applyAlpha(0x88000000, alpha);
        gfx.drawString(font, text, (int) drawX - 1, (int) drawY, shadowColor, false);
        gfx.drawString(font, text, (int) drawX + 1, (int) drawY, shadowColor, false);
        gfx.drawString(font, text, (int) drawX, (int) drawY - 1, shadowColor, false);
        gfx.drawString(font, text, (int) drawX, (int) drawY + 1, shadowColor, false);
        gfx.drawString(font, text, (int) drawX - 1, (int) drawY - 1, shadowColor, false);
        gfx.drawString(font, text, (int) drawX + 1, (int) drawY - 1, shadowColor, false);
        gfx.drawString(font, text, (int) drawX - 1, (int) drawY + 1, shadowColor, false);
        gfx.drawString(font, text, (int) drawX + 1, (int) drawY + 1, shadowColor, false);

        gfx.drawString(font, text, (int) drawX, (int) drawY, color, false);
        gfx.pose().popPose();
    }

    private static void drawStripesHUD(GuiGraphics gfx, int x, int y, int w, int h, int colorRGB, float alpha) {
        if (w <= 0) {
            return;
        }
        gfx.enableScissor(x, y, x + w, y + h);
        RenderSystem.enableBlend();
        int bandWidth = 3;
        int gap = 3;
        int totalHeight = h + w + 20;
        int r = (colorRGB >> 16) & 0xFF;
        int g = (colorRGB >> 8) & 0xFF;
        int b = colorRGB & 0xFF;
        int a = (int) (200 * alpha);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f matrix = gfx.pose().last().pose();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = -20; i < totalHeight; i += (bandWidth + gap)) {
            float yStart = y + i;
            buffer.vertex(matrix, x, yStart, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x, yStart + bandWidth, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x + w * 2, yStart - w + bandWidth, 0).color(r, g, b, a).endVertex();
            buffer.vertex(matrix, x + w * 2, yStart - w, 0).color(r, g, b, a).endVertex();
        }
        tesselator.end();
        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private static int adjustBrightness(int color, float factor) {
        int r = Math.min(255, (int) (((color >> 16) & 0xFF) * factor));
        int g = Math.min(255, (int) (((color >> 8) & 0xFF) * factor));
        int b = Math.min(255, (int) ((color & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }
}
