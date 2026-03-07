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

        if (mc.player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
        if (mc.player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;
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

            if (Math.abs(cap.stamina - displayedStamina) < 0.05f) displayedStamina = cap.stamina;
            if (Math.abs(cap.bonusStamina - displayedBonusStamina) < 0.05f) displayedBonusStamina = cap.bonusStamina;
            if (Math.abs(cap.fatiguePenalty - displayedPenalty) < 0.05f) displayedPenalty = cap.fatiguePenalty;
            if (Math.abs(hungerTarget - displayedHunger) < 0.05f) displayedHunger = hungerTarget;
            if (Math.abs(poisonTarget - displayedPoison) < 0.05f) displayedPoison = poisonTarget;
            if (Math.abs(weightTarget - displayedWeight) < 0.05f) displayedWeight = weightTarget;
            float totalPenaltySum = displayedPenalty + displayedHunger + displayedPoison + displayedWeight;
            if (cap.penaltyValues != null) {
                for (int i = 0; i < cap.penaltyValues.length; i++) {
                    if (i >= cachedPenaltyColors.size()) break;
                    float targetVal = cap.penaltyValues[i];
                    float currentVal = smoothedPenalties.getOrDefault(i, 0.0f);
                    currentVal += (targetVal - currentVal) * 0.1f;
                    if (Math.abs(targetVal - currentVal) < 0.05f) currentVal = targetVal;
                    smoothedPenalties.put(i, currentVal);
                    totalPenaltySum += currentVal;
                }
            }

            float baseMax = 100.0f;
            AttributeInstance maxAttr = mc.player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
            if (maxAttr != null) baseMax = (float) maxAttr.getValue();
            if (baseMax <= 0) baseMax = 100.0f;

            boolean isRecentlyUsed = cap.staminaRegenDelay > 0;
            boolean hasPenalties = totalPenaltySum > 0.5f && StaminaConfig.CLIENT.autoHudShowOnPenalties.get();
            boolean isBelowThreshold = displayedStamina <= (baseMax * StaminaConfig.CLIENT.autoHudThreshold.get());
            boolean hasBonus = displayedBonusStamina > 0.5f;

            boolean isActive = isRecentlyUsed || isBelowThreshold || hasPenalties || hasBonus;

            // Handle linger timer
            if (isActive) {
                visibleLingerTimer = StaminaConfig.CLIENT.autoHudLingerTime.get();
            } else if (visibleLingerTimer > 0) {
                visibleLingerTimer--;
                isActive = true; 
            }

            boolean shouldShow = !StaminaConfig.CLIENT.autoHudEnable.get() || isActive;
            float targetAnim = shouldShow ? 1.0f : 0.0f;
            
            // Interpolate Fade and Slide independently
            float fadeSpeed = shouldShow ? StaminaConfig.CLIENT.autoHudFadeInSpeed.get().floatValue() : StaminaConfig.CLIENT.autoHudFadeOutSpeed.get().floatValue();
            float slideSpeed = shouldShow ? StaminaConfig.CLIENT.autoHudSlideInSpeed.get().floatValue() : StaminaConfig.CLIENT.autoHudSlideOutSpeed.get().floatValue();

            currentFadeProgress += (targetAnim - currentFadeProgress) * fadeSpeed;
            currentSlideProgress += (targetAnim - currentSlideProgress) * slideSpeed;

            // Stop rendering completely if fully hidden
            if (currentFadeProgress < 0.01f && currentSlideProgress < 0.01f && !shouldShow) {
                currentFadeProgress = 0.0f;
                currentSlideProgress = 0.0f;
                return; 
            }

            // Apply Mode Math
            StaminaConfig.AutoHudMode mode = StaminaConfig.CLIENT.autoHudMode.get();
            float renderAlpha = (mode == StaminaConfig.AutoHudMode.FADE || mode == StaminaConfig.AutoHudMode.BOTH) ? currentFadeProgress : 1.0f;
            
            int slideX = 0;
            int slideY = 0;
            if (mode == StaminaConfig.AutoHudMode.SLIDE || mode == StaminaConfig.AutoHudMode.BOTH) {
                float easedSlide = applyEasing(currentSlideProgress, StaminaConfig.CLIENT.autoHudEasing.get());
                int maxDist = StaminaConfig.CLIENT.autoHudSlideDistance.get();
                int offsetAmount = (int)((1.0f - easedSlide) * maxDist);
                
                switch (StaminaConfig.CLIENT.autoHudSlideDir.get()) {
                    case DOWN: slideY = offsetAmount; break;
                    case UP: slideY = -offsetAmount; break;
                    case RIGHT: slideX = offsetAmount; break;
                    case LEFT: slideX = -offsetAmount; break;
                }
            }

            int sBarW = StaminaConfig.CLIENT.barWidth.get();
            int sBarH = StaminaConfig.CLIENT.barHeight.get();
            int offsetX = StaminaConfig.CLIENT.barXOffset.get();
            int sBarX = (width / 2) - (sBarW / 2) + offsetX + slideX;
            int offsetY = StaminaConfig.CLIENT.barYOffset.get();
            int sBarY = height - offsetY + slideY;

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

            gfx.fill(sBarX - 1, sBarY - 1, sBarX + sBarW + 1, sBarY + sBarH + 1, sepCol);
            gfx.fill(sBarX, sBarY, sBarX + sBarW, sBarY + sBarH, bgCol);

            float pxScale = sBarW / baseMax;
            float effectivePenaltyScale = pxScale * compressionRatio;
            int currentPenaltyRightEdge = sBarW;

            int fatiguePx = (int) (displayedPenalty * effectivePenaltyScale);
            if (fatiguePx > 0) {
                if (fatiguePx > currentPenaltyRightEdge) fatiguePx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - fatiguePx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, fatiguePx, sBarH, stripeCol, renderAlpha);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, fatiguePx, sBarH, ICON_FATIGUE, stripeCol, renderAlpha);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= fatiguePx;
            }

            int hungerPx = (int) (displayedHunger * effectivePenaltyScale);
            if (hungerPx > 0 && currentPenaltyRightEdge > 0) {
                if (hungerPx > currentPenaltyRightEdge) hungerPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - hungerPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, hungerPx, sBarH, energyCol, renderAlpha);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, hungerPx, sBarH, ICON_HUNGER, energyCol, renderAlpha);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= hungerPx;
            }

            int poisonPx = (int) (displayedPoison * effectivePenaltyScale);
            if (poisonPx > 0 && currentPenaltyRightEdge > 0) {
                if (poisonPx > currentPenaltyRightEdge) poisonPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - poisonPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, poisonPx, sBarH, poisonCol, renderAlpha);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, poisonPx, sBarH, ICON_POISON, poisonCol, renderAlpha);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= poisonPx;
            }
            
            int weightPx = (int) (displayedWeight * effectivePenaltyScale);
            if (weightPx > 0 && currentPenaltyRightEdge > 0) {
                if (weightPx > currentPenaltyRightEdge) weightPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - weightPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, weightPx, sBarH, weightCol, renderAlpha);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, weightPx, sBarH, ICON_WEIGHT, weightCol, renderAlpha);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= weightPx;
            }

            if (cap.penaltyValues != null) {
                for (int i = 0; i < cap.penaltyValues.length; i++) {
                    if (i >= cachedPenaltyColors.size()) break;
                    int color = cachedPenaltyColors.get(i);
                    float currentVal = smoothedPenalties.getOrDefault(i, 0.0f);
                    int pPx = (int) (currentVal * effectivePenaltyScale);
                    if (pPx > 0 && currentPenaltyRightEdge > 0) {
                        if (pPx > currentPenaltyRightEdge) pPx = currentPenaltyRightEdge;
                        int startX = currentPenaltyRightEdge - pPx;
                        int customCol = applyAlpha(0xFF000000 | color, renderAlpha);
                        drawStripesHUD(gfx, sBarX + startX, sBarY, pPx, sBarH, customCol, renderAlpha);
                        if (showIcons && i < cachedPenaltyIcons.size()) {
                            String icon = cachedPenaltyIcons.get(i);
                            if (icon != null) drawIcon(gfx, sBarX + startX, sBarY, pPx, sBarH, icon, customCol, renderAlpha);
                        }
                        gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                        currentPenaltyRightEdge -= pPx;
                    }
                }
            }
            
            int colorTop;
            int colorBottom;
            int safeCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorSafe.get(), renderAlpha);
            int critCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorCritical.get(), renderAlpha);
            int tirelessCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorTireless.get(), renderAlpha);
            if (hasInfiniteStamina(mc.player)) {
                colorBottom = tirelessCol;
                colorTop = applyAlpha((0xFF000000 | StaminaConfig.CLIENT.colorTireless.get()) + 0x002222, renderAlpha);
            } else if (displayedStamina <= (baseMax * StaminaConfig.COMMON.fatigueThreshold.get())) {
                colorBottom = critCol;
                colorTop = applyAlpha((0xFF000000 | StaminaConfig.CLIENT.colorCritical.get()) + 0x222222, renderAlpha);
            } else {
                colorBottom = safeCol;
                colorTop = applyAlpha((0xFF000000 | StaminaConfig.CLIENT.colorSafe.get()) + 0x222222, renderAlpha);
            }

            int normalW = (int) (displayedStamina * pxScale);
            int renderNormalW = Math.min(normalW, currentPenaltyRightEdge);

            if (renderNormalW > 0) {
                gfx.fillGradient(sBarX, sBarY, sBarX + renderNormalW, sBarY + sBarH, colorTop, colorBottom);
                if (renderNormalW < currentPenaltyRightEdge) {
                    gfx.fill(sBarX + renderNormalW, sBarY, sBarX + renderNormalW + 1, sBarY + sBarH, sepCol);
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
                
                if (fullBars > 0) {
                    int underTier = fullBars - 1;
                    float uFactor = 1.0f - ((underTier % 3) * 0.35f); 
                    int uTop = shadeColor(bTopRGB, uFactor);
                    int uBot = shadeColor(bBotRGB, uFactor);
                    int underTopCol = applyAlpha(0xFF000000 | uTop, renderAlpha);
                    int underBotCol = applyAlpha(0xFF000000 | uBot, renderAlpha);
                    gfx.fillGradient(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH, underTopCol, underBotCol);
                    
                    int sheenCol = (hAlpha << 24) | hRGB;
                    if (sBarH > 2) {
                        gfx.fill(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + 1, sheenCol);
                    } else {
                        gfx.fill(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH, sheenCol);
                    }
                }

                if (remainderPx > 0) {
                    int overTier = fullBars;
                    float oFactor = 1.0f - ((overTier % 3) * 0.35f);
                    int oTop = shadeColor(bTopRGB, oFactor);
                    int oBot = shadeColor(bBotRGB, oFactor);
                    int overTopCol = applyAlpha(0xFF000000 | oTop, renderAlpha);
                    int overBotCol = applyAlpha(0xFF000000 | oBot, renderAlpha);
                    gfx.fillGradient(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, overTopCol, overBotCol);
                    
                    int sheenCol = (hAlpha << 24) | hRGB;
                    if (sBarH > 2) {
                        gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + 1, sheenCol);
                    } else {
                        gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, sheenCol);
                    }

                    if (remainderPx < currentPenaltyRightEdge) {
                         gfx.fill(sBarX + remainderPx, sBarY, sBarX + remainderPx + 1, sBarY + sBarH, applyAlpha(0xFFFFFFFF, renderAlpha));
                    }
                }

                if (fullBars > 0) {
                    String multText = (fullBars + 1) + "x";
                    gfx.pose().pushPose();
                    float scale = 0.6f;
                    int textWidth = mc.font.width(multText);
                    int textX = sBarX - (int)(textWidth * scale) - 4;
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
                int targetX = sBarX + renderNormalW - (int) (textWidth * scale) - 2;
                if (targetX >= sBarX) {
                    int targetY = sBarY + (sBarH / 2) - (int) (4 * scale);
                    gfx.pose().pushPose();
                    gfx.pose().translate(targetX, targetY, 0);
                    gfx.pose().scale(scale, scale, 1.0f);
                    gfx.drawString(mc.font, regenComp, 0, 0, applyAlpha(regenColor, renderAlpha), true);
                    gfx.pose().popPose();
                }
            }
        });
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
        int bandWidth = 2;
        int gap = 2;
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
}