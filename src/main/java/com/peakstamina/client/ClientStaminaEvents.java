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

    private static int shadeColor(int color, float factor) {
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (r << 16) | (g << 8) | b;
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

            int sBarW = StaminaConfig.CLIENT.barWidth.get();
            int sBarH = StaminaConfig.CLIENT.barHeight.get();
            int offsetX = StaminaConfig.CLIENT.barXOffset.get();
            int sBarX = (width / 2) - (sBarW / 2) + offsetX;
            int offsetY = StaminaConfig.CLIENT.barYOffset.get();
            int sBarY = height - offsetY;

            float baseMax = 100.0f;
            AttributeInstance maxAttr = mc.player.getAttribute(StaminaAttributes.MAX_STAMINA.get());
            if (maxAttr != null) baseMax = (float) maxAttr.getValue();
            if (baseMax <= 0) baseMax = 100.0f;

            float compressionRatio = 1.0f;
            if (totalPenaltySum > baseMax && baseMax > 0) {
                compressionRatio = baseMax / totalPenaltySum;
            }

            int bgCol = 0xFF000000 | StaminaConfig.CLIENT.colorBackground.get();
            int stripeCol = 0xFF000000 | StaminaConfig.CLIENT.colorStripes.get();
            int energyCol = 0xFF000000 | StaminaConfig.CLIENT.colorPenaltyHunger.get();
            int poisonCol = 0xFF000000 | StaminaConfig.CLIENT.colorPenaltyPoison.get();
            int weightCol = 0xFF000000 | StaminaConfig.CLIENT.colorPenaltyWeight.get();
            int sepCol = 0xFF000000;
            
            gfx.fill(sBarX - 1, sBarY - 1, sBarX + sBarW + 1, sBarY + sBarH + 1, 0xFF000000);
            gfx.fill(sBarX, sBarY, sBarX + sBarW, sBarY + sBarH, bgCol);

            float pxScale = sBarW / baseMax;
            float effectivePenaltyScale = pxScale * compressionRatio;
            int currentPenaltyRightEdge = sBarW;

            int fatiguePx = (int) (displayedPenalty * effectivePenaltyScale);
            if (fatiguePx > 0) {
                if (fatiguePx > currentPenaltyRightEdge) fatiguePx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - fatiguePx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, fatiguePx, sBarH, stripeCol);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, fatiguePx, sBarH, ICON_FATIGUE, stripeCol);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= fatiguePx;
            }

            int hungerPx = (int) (displayedHunger * effectivePenaltyScale);
            if (hungerPx > 0 && currentPenaltyRightEdge > 0) {
                if (hungerPx > currentPenaltyRightEdge) hungerPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - hungerPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, hungerPx, sBarH, energyCol);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, hungerPx, sBarH, ICON_HUNGER, energyCol);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= hungerPx;
            }

            int poisonPx = (int) (displayedPoison * effectivePenaltyScale);
            if (poisonPx > 0 && currentPenaltyRightEdge > 0) {
                if (poisonPx > currentPenaltyRightEdge) poisonPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - poisonPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, poisonPx, sBarH, poisonCol);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, poisonPx, sBarH, ICON_POISON, poisonCol);
                gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                currentPenaltyRightEdge -= poisonPx;
            }
            
            int weightPx = (int) (displayedWeight * effectivePenaltyScale);
            if (weightPx > 0 && currentPenaltyRightEdge > 0) {
                if (weightPx > currentPenaltyRightEdge) weightPx = currentPenaltyRightEdge;
                int startX = currentPenaltyRightEdge - weightPx;
                drawStripesHUD(gfx, sBarX + startX, sBarY, weightPx, sBarH, weightCol);
                if (showIcons) drawIcon(gfx, sBarX + startX, sBarY, weightPx, sBarH, ICON_WEIGHT, weightCol);
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
                        drawStripesHUD(gfx, sBarX + startX, sBarY, pPx, sBarH, 0xFF000000 | color);
                        if (showIcons && i < cachedPenaltyIcons.size()) {
                            String icon = cachedPenaltyIcons.get(i);
                            if (icon != null) drawIcon(gfx, sBarX + startX, sBarY, pPx, sBarH, icon, 0xFF000000 | color);
                        }
                        gfx.fill(sBarX + startX, sBarY, sBarX + startX + 1, sBarY + sBarH, sepCol);
                        currentPenaltyRightEdge -= pPx;
                    }
                }
            }
            
            int colorTop;
            int colorBottom;
            int safeCol = 0xFF000000 | StaminaConfig.CLIENT.colorSafe.get();
            int critCol = 0xFF000000 | StaminaConfig.CLIENT.colorCritical.get();
            int tirelessCol = 0xFF000000 | StaminaConfig.CLIENT.colorTireless.get();
            if (hasInfiniteStamina(mc.player)) {
                colorBottom = tirelessCol;
                colorTop = tirelessCol + 0x002222;
            } else if (displayedStamina <= (baseMax * StaminaConfig.COMMON.fatigueThreshold.get())) {
                colorBottom = critCol;
                colorTop = critCol + 0x222222;
            } else {
                colorBottom = safeCol;
                colorTop = safeCol + 0x222222;
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
                int hAlpha = StaminaConfig.CLIENT.bonusHighlightAlpha.get();

                if (fullBars > 0) {
                    int underTier = fullBars - 1;
                    float uFactor = 1.0f - ((underTier % 3) * 0.35f); 
                    int uTop = shadeColor(bTopRGB, uFactor);
                    int uBot = shadeColor(bBotRGB, uFactor);
                    int underTopCol = 0xFF000000 | uTop;
                    int underBotCol = 0xFF000000 | uBot;

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
                    int overTopCol = 0xFF000000 | oTop;
                    int overBotCol = 0xFF000000 | oBot;

                    gfx.fillGradient(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, overTopCol, overBotCol);
                    
                    int sheenCol = (hAlpha << 24) | hRGB;
                    if (sBarH > 2) {
                        gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + 1, sheenCol);
                    } else {
                        gfx.fill(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH, sheenCol);
                    }

                    if (remainderPx < currentPenaltyRightEdge) {
                         gfx.fill(sBarX + remainderPx, sBarY, sBarX + remainderPx + 1, sBarY + sBarH, 0xFFFFFFFF);
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
                    gfx.drawString(mc.font, multText, 0, 0, 0xFFFFD700, true);
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
                    gfx.drawString(mc.font, regenComp, 0, 0, regenColor, true);

                    gfx.pose().popPose();
                }
            }
        });
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (StaminaConfig.COMMON.depletionMissedAttack.get() > 0) {
             com.peakstamina.network.StaminaNetwork.CHANNEL.sendToServer(new com.peakstamina.network.PacketMissedAttack());
        }
    }

    private static void drawIcon(GuiGraphics gfx, int x, int y, int w, int h, String text, int color) {
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

        int shadowColor = 0x88000000;
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

    private static void drawStripesHUD(GuiGraphics gfx, int x, int y, int w, int h, int colorRGB) {
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
        int a = 200;
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