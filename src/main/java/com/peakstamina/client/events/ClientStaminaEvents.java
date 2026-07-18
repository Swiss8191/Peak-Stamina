package com.peakstamina.client.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
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
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

// Fixed the Bus warning by removing the deprecated enum!
@EventBusSubscriber(modid = "peakstamina", value = Dist.CLIENT)
public class ClientStaminaEvents {

    private static final String ICON_FATIGUE = "CUSTOM:Fatigue";
    private static final String ICON_HUNGER = "CUSTOM:Hunger";
    private static final String ICON_POISON = "CUSTOM:Poison";
    private static final String ICON_WEIGHT = "CUSTOM:Weight";

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
    private static final List<net.minecraft.core.Holder<MobEffect>> cachedInfiniteEffects = new ArrayList<>();
    private static final List<Integer> cachedPenaltyColors = new ArrayList<>();
    private static final List<String> cachedPenaltyIcons = new ArrayList<>();

    private static final ResourceLocation TEXTURE_SHEET = ResourceLocation.fromNamespaceAndPath("peakstamina", "textures/gui/peakstamina_texture_sheet.png");
    private static final ResourceLocation LAYOUT_JSON = ResourceLocation.fromNamespaceAndPath("peakstamina", "gui_layout.json");
    private static final Map<String, int[]> textureLayout = new HashMap<>();
    private static boolean layoutLoaded = false;

    private static void loadDefaultLayout() {
        textureLayout.put("empty_left", new int[]{30, 33, 4, 8});
        textureLayout.put("empty_mid", new int[]{39, 33, 1, 8});
        textureLayout.put("empty_right", new int[]{45, 33, 4, 8});
        textureLayout.put("fill_left", new int[]{59, 33, 4, 8});
        textureLayout.put("fill_mid", new int[]{68, 33, 1, 8});
        textureLayout.put("fill_right", new int[]{74, 33, 4, 8});
        textureLayout.put("bonus_fill_left", new int[]{88, 33, 4, 8});
        textureLayout.put("bonus_fill_mid", new int[]{97, 33, 1, 8});
        textureLayout.put("bonus_fill_right", new int[]{103, 33, 4, 8});
        textureLayout.put("single_stripe", new int[]{30, 52, 10, 4});
        textureLayout.put("penalty_sep", new int[]{51, 52, 2, 4});
        textureLayout.put("bonus_sep", new int[]{63, 52, 2, 4});
        textureLayout.put("icon_empty", new int[]{27, 66, 16, 20});
        textureLayout.put("icon_full", new int[]{53, 66, 16, 20});
        textureLayout.put("icon_bonus_full", new int[]{79, 66, 16, 20});
        textureLayout.put("icon_penalty", new int[]{105, 68, 10, 16});
        textureLayout.put("icon_penalty_sep", new int[]{130, 68, 2, 16});
        textureLayout.put("icon_bonus_sep", new int[]{142, 68, 2, 16});
        textureLayout.put("regen_pos_1", new int[]{26, 96, 6, 8});
        textureLayout.put("regen_pos_2", new int[]{26, 106, 12, 8});
        textureLayout.put("regen_pos_3", new int[]{26, 116, 18, 8});
        textureLayout.put("regen_neg_1", new int[]{26, 126, 6, 8});
        textureLayout.put("regen_neg_2", new int[]{26, 136, 12, 8});
        textureLayout.put("regen_neg_3", new int[]{26, 146, 18, 8});
    }

    private static void ensureLayoutLoaded() {
        if (layoutLoaded) return;
        loadDefaultLayout();
        try {
            var resource = Minecraft.getInstance().getResourceManager().getResource(LAYOUT_JSON);
            if (resource.isPresent()) {
                try (java.io.Reader reader = new java.io.InputStreamReader(resource.get().open())) {
                    com.google.gson.JsonObject json = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                    for (Map.Entry<String, com.google.gson.JsonElement> entry : json.entrySet()) {
                        com.google.gson.JsonObject obj = entry.getValue().getAsJsonObject();
                        int u = obj.has("u") ? obj.get("u").getAsInt() : 0;
                        int v = obj.has("v") ? obj.get("v").getAsInt() : 0;
                        int w = obj.has("w") ? obj.get("w").getAsInt() : 0;
                        int h = obj.has("h") ? obj.get("h").getAsInt() : 0;
                        textureLayout.put(entry.getKey(), new int[]{u, v, w, h});
                    }
                }
            }
        } catch (Exception ignored) {}
        layoutLoaded = true;
    }

    // UPDATED FOR NEOFORGE 1.21.1 TESSELATOR SYNTAX
    private static void drawTexturedQuadGradient(GuiGraphics gfx, int x, int y, int w, int h, int u, int v, int tw, int th, int texW, int texH, int colorTop, int colorBottom) {
        if (w <= 0 || h <= 0) return;
        RenderSystem.setShaderTexture(0, TEXTURE_SHEET);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float minU = (float) u / texW;
        float maxU = (float) (u + tw) / texW;
        float minV = (float) v / texH;
        float maxV = (float) (v + th) / texH;

        int tA = (colorTop >> 24) & 0xFF;
        int tR = (colorTop >> 16) & 0xFF;
        int tG = (colorTop >> 8) & 0xFF;
        int tB = colorTop & 0xFF;

        int bA = (colorBottom >> 24) & 0xFF;
        int bR = (colorBottom >> 16) & 0xFF;
        int bG = (colorBottom >> 8) & 0xFF;
        int bB = colorBottom & 0xFF;

        Matrix4f matrix = gfx.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buffer.addVertex(matrix, x, y, 0).setColor(tR, tG, tB, tA).setUv(minU, minV);
        buffer.addVertex(matrix, x, y + h, 0).setColor(bR, bG, bB, bA).setUv(minU, maxV);
        buffer.addVertex(matrix, x + w, y + h, 0).setColor(bR, bG, bB, bA).setUv(maxU, maxV);
        buffer.addVertex(matrix, x + w, y, 0).setColor(tR, tG, tB, tA).setUv(maxU, minV);

        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    private static void drawTexturedQuad(GuiGraphics gfx, int x, int y, int w, int h, int u, int v, int tw, int th, int texW, int texH, int color) {
        drawTexturedQuadGradient(gfx, x, y, w, h, u, v, tw, th, texW, texH, color, color);
    }

    private static void drawDynamicFill(GuiGraphics gfx, int x, int y, int fillWidth, int maxFillWidth, int renderHeight, String prefix, int colorTop, int colorBottom) {
        if (fillWidth <= 0) return;
        ensureLayoutLoaded();
        int[] leftUV = textureLayout.get(prefix + "_left");
        int[] midUV = textureLayout.get(prefix + "_mid");
        int[] rightUV = textureLayout.get(prefix + "_right");
        if (leftUV == null || midUV == null || rightUV == null) return;

        int lw = leftUV[2] / 2;
        int rw = rightUV[2] / 2;
        int texH = leftUV[3]; 
        
        int renderLw = Math.min(lw, fillWidth);
        drawTexturedQuadGradient(gfx, x, y, renderLw, renderHeight, leftUV[0], leftUV[1], renderLw * 2, texH, 512, 512, colorTop, colorBottom);

        if (fillWidth > lw) {
            if (fillWidth >= maxFillWidth) {
                int rightX = x + fillWidth - rw;
                drawTexturedQuadGradient(gfx, rightX, y, rw, renderHeight, rightUV[0], rightUV[1], rightUV[2], texH, 512, 512, colorTop, colorBottom);
                int midWidth = fillWidth - lw - rw;
                if (midWidth > 0) {
                    drawTexturedQuadGradient(gfx, x + lw, y, midWidth, renderHeight, midUV[0], midUV[1], midUV[2], texH, 512, 512, colorTop, colorBottom);
                }
            } else {
                int midWidth = fillWidth - lw;
                drawTexturedQuadGradient(gfx, x + lw, y, midWidth, renderHeight, midUV[0], midUV[1], midUV[2], texH, 512, 512, colorTop, colorBottom);
            }
        }
    }

    private static void drawSprite(GuiGraphics gfx, int x, int y, String spriteKey, int color) {
        ensureLayoutLoaded();
        int[] uv = textureLayout.get(spriteKey);
        if (uv != null) {
            int screenW = Math.max(1, uv[2] / 2);
            int screenH = Math.max(1, uv[3] / 2);
            drawTexturedQuad(gfx, x, y, screenW, screenH, uv[0], uv[1], uv[2], uv[3], 512, 512, color);
        }
    }

    private static void drawSpriteStretched(GuiGraphics gfx, int x, int y, int renderW, int renderH, String spriteKey, int color) {
        ensureLayoutLoaded();
        int[] uv = textureLayout.get(spriteKey);
        if (uv != null) {
            drawTexturedQuad(gfx, x, y, renderW, renderH, uv[0], uv[1], uv[2], uv[3], 512, 512, color);
        }
    }

    private static void drawSpriteGradient(GuiGraphics gfx, int x, int y, String spriteKey, int colorTop, int colorBottom) {
        ensureLayoutLoaded();
        int[] uv = textureLayout.get(spriteKey);
        if (uv != null) {
            int screenW = Math.max(1, uv[2] / 2);
            int screenH = Math.max(1, uv[3] / 2);
            drawTexturedQuadGradient(gfx, x, y, screenW, screenH, uv[0], uv[1], uv[2], uv[3], 512, 512, colorTop, colorBottom);
        }
    }

    private static void drawRepeatingStripe(GuiGraphics gfx, int x, int y, int renderW, String stripeKey, int color) {
        if (renderW <= 0) return;
        ensureLayoutLoaded();
        int[] uv = textureLayout.get(stripeKey);
        if (uv == null) return;
        
        int screenW = Math.max(1, uv[2] / 2);
        int screenH = Math.max(1, uv[3] / 2);
        gfx.enableScissor(x, y, x + renderW, y + screenH);
        for (int i = 0; i < renderW; i += screenW + 1) { 
            drawTexturedQuad(gfx, x + i, y, screenW, screenH, uv[0], uv[1], uv[2], uv[3], 512, 512, color);
        }
        gfx.disableScissor();
    }

    public static void invalidateCache() {
        isCacheValid = false;
    }

    private static void validateCache() {
        if (isCacheValid) return;
        cachedInfiniteEffects.clear();
        for (String id : StaminaLists.LISTS.infiniteStaminaEffects.get()) {
            ResourceLocation loc = ResourceLocation.tryParse(id);
            if (loc != null) {
                net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(loc).ifPresent(cachedInfiniteEffects::add);
            }
        }
        cachedPenaltyColors.clear();
        cachedPenaltyIcons.clear();
        for (String entry : StaminaLists.LISTS.universalPenalties.get()) {
            int color = 0xFFFFFF;
            String iconText = null;
            try {
                String[] parts = entry.split(";");
                if (parts.length >= 7) color = Integer.parseInt(parts[6].trim());
                if (parts.length >= 8) {
                    String text = parts[7].trim();
                    if (!text.isEmpty() && !text.equalsIgnoreCase("none") && !text.equalsIgnoreCase("null")) {
                        iconText = text;
                    }
                }
            } catch (Exception ignored) {}
            cachedPenaltyColors.add(color);
            cachedPenaltyIcons.add(iconText);
        }
        isCacheValid = true;
    }

    private static boolean hasInfiniteStamina(Player player) {
        validateCache();
        for (var effect : cachedInfiniteEffects) {
            if (player.hasEffect(effect)) return true;
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
            case SMOOTHSTEP: return t * t * (3.0f - 2.0f * t);
            case EASE_OUT_SINE: return (float) Math.sin((t * Math.PI) / 2.0);
            case EASE_OUT_EXPO: return t == 1.0f ? 1.0f : (float) (1.0 - Math.pow(2.0, -10.0 * t));
            case LINEAR:
            default: return t;
        }
    }

    // NeoForge 1.21.1 HUD Hook
    @SubscribeEvent
    public static void onRenderGuiLayerPost(net.neoforged.neoforge.client.event.RenderGuiLayerEvent.Post event) {
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (event.getName().getPath().equals("experience_bar")) {
            renderStaminaHUD(event.getGuiGraphics());
        }
    }

    private static void renderStaminaHUD(GuiGraphics gfx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || !StaminaConfig.COMMON.enableStamina.get()) return;
        if (mc.player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
        if (mc.player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;
        
        boolean showIcons = StaminaConfig.CLIENT.showIcons.get();
        validateCache();
        ensureLayoutLoaded();

        var cap = mc.player.getData(com.peakstamina.capabilities.StaminaCapability.STAMINA);
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
        AttributeInstance maxAttr = mc.player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.MAX_STAMINA.get()));
        if (maxAttr != null) baseMax = (float) maxAttr.getValue();
        if (baseMax <= 0) baseMax = 100.0f;

        boolean isRecentlyUsed = cap.staminaRegenDelay > 0;
        boolean hasPenalties = totalPenaltySum > 0.5f && StaminaConfig.CLIENT.autoHudShowOnPenalties.get();
        boolean isBelowThreshold = displayedStamina <= (baseMax * StaminaConfig.CLIENT.autoHudThreshold.get());
        boolean hasBonus = displayedBonusStamina > 0.5f && StaminaConfig.CLIENT.autoHudShowOnBonus.get();

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
            int offsetAmount = (int)((1.0f - easedSlide) * maxDist);
            switch (StaminaConfig.CLIENT.autoHudSlideDir.get()) {
                case DOWN -> slideY = offsetAmount;
                case UP -> slideY = -offsetAmount;
                case RIGHT -> slideX = offsetAmount;
                case LEFT -> slideX = -offsetAmount;
            }
        }

        boolean isIconMode = false;
        try {
            isIconMode = StaminaConfig.CLIENT.hudStyle.get().name().equalsIgnoreCase("ICON");
        } catch (Exception e) {
            isIconMode = false;
        }

        int sBarW = isIconMode ? 81 : StaminaConfig.CLIENT.barWidth.get();
        int sBarH = isIconMode ? 10 : StaminaConfig.CLIENT.barHeight.get();
        int offsetX = isIconMode ? StaminaConfig.CLIENT.iconXOffset.get() : StaminaConfig.CLIENT.barXOffset.get();
        int offsetY = isIconMode ? StaminaConfig.CLIENT.iconYOffset.get() : StaminaConfig.CLIENT.barYOffset.get();
        
        int sBarX = (width / 2) - (sBarW / 2) + offsetX + slideX;
        int sBarY = height - 24 - offsetY + slideY;
        
        if (isIconMode) {
            sBarX = (width / 2) - 90 + offsetX + slideX;
            int healthRows = (int) Math.ceil((mc.player.getMaxHealth() + mc.player.getAbsorptionAmount()) / 20.0f);
            int armorRows = mc.player.getArmorValue() > 0 ? 1 : 0;
            int totalOffsetRows = healthRows + armorRows;
            int dynamicYOffset = 39 + (totalOffsetRows * 10);
            sBarY = height - dynamicYOffset - offsetY + slideY;
        }

        int iconY = isIconMode ? sBarY - 12 : sBarY;

        float compressionRatio = 1.0f;
        if (totalPenaltySum > baseMax && baseMax > 0) {
            compressionRatio = baseMax / totalPenaltySum;
        }

        int stripeCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorStripes.get(), renderAlpha);
        int energyCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyHunger.get(), renderAlpha);
        int poisonCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyPoison.get(), renderAlpha);
        int weightCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorPenaltyWeight.get(), renderAlpha);
        int sepCol = applyAlpha(0xFFFFFFFF, renderAlpha);

        // 1. Draw Background
        if (isIconMode) {
            for (int i = 0; i < 10; i++) {
                drawSprite(gfx, sBarX + i * 8, sBarY, "icon_empty", applyAlpha(0xFFFFFFFF, renderAlpha));
            }
        } else {
            drawDynamicFill(gfx, sBarX, sBarY, sBarW, sBarW, sBarH, "empty", applyAlpha(0xFFFFFFFF, renderAlpha), applyAlpha(0xFFFFFFFF, renderAlpha));
            int innerBgCol = applyAlpha(0xFF000000 | StaminaConfig.CLIENT.colorBackground.get(), renderAlpha);
            gfx.fill(sBarX + 1, sBarY + 1, sBarX + sBarW - 1, sBarY + sBarH - 1, innerBgCol);
        }

        float pxScale = sBarW / baseMax;
        float effectivePenaltyScale = pxScale * compressionRatio;
        
        int rightCapW = isIconMode ? 1 : (textureLayout.containsKey("empty_right") ? textureLayout.get("empty_right")[2] / 2 : 2);
        int currentPenaltyRightEdge = sBarW - rightCapW;

        int barInnerY = sBarY + 1;
        int sepW = textureLayout.containsKey("penalty_sep") ? textureLayout.get("penalty_sep")[2] / 2 : 1;
        int sepH = textureLayout.containsKey("penalty_sep") ? textureLayout.get("penalty_sep")[3] / 2 : 2;

        // Fatigue
        int fatiguePx = (int) (displayedPenalty * effectivePenaltyScale);
        if (fatiguePx > 0) {
            if (fatiguePx > currentPenaltyRightEdge) fatiguePx = currentPenaltyRightEdge;
            int startX = currentPenaltyRightEdge - fatiguePx;
            
            if (isIconMode) {
                gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                for (int i = 0; i < 10; i++) {
                    drawSprite(gfx, sBarX + i * 8 + 1, sBarY + 1, "icon_penalty", stripeCol);
                }
                gfx.disableScissor();
            } else {
                drawRepeatingStripe(gfx, sBarX + startX, barInnerY, fatiguePx, "single_stripe", stripeCol);
                drawSpriteStretched(gfx, sBarX + startX, barInnerY, sepW, sepH, "penalty_sep", sepCol);
            }
            if (showIcons) drawIcon(gfx, sBarX + startX, iconY, fatiguePx, sBarH, ICON_FATIGUE, stripeCol, renderAlpha);
            currentPenaltyRightEdge -= fatiguePx;
        }

        // Hunger
        int hungerPx = (int) (displayedHunger * effectivePenaltyScale);
        if (hungerPx > 0 && currentPenaltyRightEdge > 0) {
            if (hungerPx > currentPenaltyRightEdge) hungerPx = currentPenaltyRightEdge;
            int startX = currentPenaltyRightEdge - hungerPx;
            
            if (isIconMode) {
                gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                for (int i = 0; i < 10; i++) {
                    drawSprite(gfx, sBarX + i * 8 + 1, sBarY + 1, "icon_penalty", energyCol);
                }
                gfx.disableScissor();
            } else {
                drawRepeatingStripe(gfx, sBarX + startX, barInnerY, hungerPx, "single_stripe", energyCol);
                drawSpriteStretched(gfx, sBarX + startX, barInnerY, sepW, sepH, "penalty_sep", sepCol);
            }
            if (showIcons) drawIcon(gfx, sBarX + startX, iconY, hungerPx, sBarH, ICON_HUNGER, energyCol, renderAlpha);
            currentPenaltyRightEdge -= hungerPx;
        }

        // Poison
        int poisonPx = (int) (displayedPoison * effectivePenaltyScale);
        if (poisonPx > 0 && currentPenaltyRightEdge > 0) {
            if (poisonPx > currentPenaltyRightEdge) poisonPx = currentPenaltyRightEdge;
            int startX = currentPenaltyRightEdge - poisonPx;
            
            if (isIconMode) {
                gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                for (int i = 0; i < 10; i++) {
                    drawSprite(gfx, sBarX + i * 8 + 1, sBarY + 1, "icon_penalty", poisonCol);
                }
                gfx.disableScissor();
            } else {
                drawRepeatingStripe(gfx, sBarX + startX, barInnerY, poisonPx, "single_stripe", poisonCol);
                drawSpriteStretched(gfx, sBarX + startX, barInnerY, sepW, sepH, "penalty_sep", sepCol);
            }
            if (showIcons) drawIcon(gfx, sBarX + startX, iconY, poisonPx, sBarH, ICON_POISON, poisonCol, renderAlpha);
            currentPenaltyRightEdge -= poisonPx;
        }

        // Weight
        int weightPx = (int) (displayedWeight * effectivePenaltyScale);
        if (weightPx > 0 && currentPenaltyRightEdge > 0) {
            if (weightPx > currentPenaltyRightEdge) weightPx = currentPenaltyRightEdge;
            int startX = currentPenaltyRightEdge - weightPx;
            
            if (isIconMode) {
                gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                for (int i = 0; i < 10; i++) {
                    drawSprite(gfx, sBarX + i * 8 + 1, sBarY + 1, "icon_penalty", weightCol);
                }
                gfx.disableScissor();
            } else {
                drawRepeatingStripe(gfx, sBarX + startX, barInnerY, weightPx, "single_stripe", weightCol);
                drawSpriteStretched(gfx, sBarX + startX, barInnerY, sepW, sepH, "penalty_sep", sepCol);
            }
            if (showIcons) drawIcon(gfx, sBarX + startX, iconY, weightPx, sBarH, ICON_WEIGHT, weightCol, renderAlpha);
            currentPenaltyRightEdge -= weightPx;
        }

        // Custom Config Penalties
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
                    
                    if (isIconMode) {
                        gfx.enableScissor(sBarX + startX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                        for (int idx = 0; idx < 10; idx++) {
                            drawSprite(gfx, sBarX + idx * 8 + 1, sBarY + 1, "icon_penalty", customCol);
                        }
                        gfx.disableScissor();
                    } else {
                        drawRepeatingStripe(gfx, sBarX + startX, barInnerY, pPx, "single_stripe", customCol);
                        drawSpriteStretched(gfx, sBarX + startX, barInnerY, sepW, sepH, "penalty_sep", sepCol);
                    }
                    if (showIcons && i < cachedPenaltyIcons.size()) {
                        String icon = cachedPenaltyIcons.get(i);
                        if (icon != null) drawIcon(gfx, sBarX + startX, iconY, pPx, sBarH, icon, customCol, renderAlpha);
                    }
                    currentPenaltyRightEdge -= pPx;
                }
            }
        }

        // Core Fill Colors
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

        // Draw Core Fill
        if (renderNormalW > 0) {
            if (isIconMode) {
                gfx.enableScissor(sBarX, sBarY, sBarX + renderNormalW, sBarY + sBarH);
                for (int i = 0; i < 10; i++) {
                    drawSpriteGradient(gfx, sBarX + i * 8, sBarY, "icon_full", colorTop, colorBottom);
                }
                gfx.disableScissor();
            } else {
                drawDynamicFill(gfx, sBarX, sBarY, renderNormalW, sBarW, sBarH, "fill", colorTop, colorBottom);
                
                if (renderNormalW < currentPenaltyRightEdge) {
                    drawSpriteStretched(gfx, sBarX + renderNormalW, barInnerY, sepW, sepH, "penalty_sep", sepCol);
                }
            }
        }

        // Draw Bonus
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
                float uFactor = 1.0f - ((underTier % 3) * 0.20f); 
                int underTopCol = applyAlpha(0xFF000000 | shadeColor(bTopRGB, uFactor), renderAlpha);
                int underBotCol = applyAlpha(0xFF000000 | shadeColor(bBotRGB, uFactor), renderAlpha);

                if (isIconMode) {
                    gfx.enableScissor(sBarX, sBarY, sBarX + currentPenaltyRightEdge, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawSpriteGradient(gfx, sBarX + i * 8, sBarY, "icon_bonus_full", underTopCol, underBotCol);
                        drawSprite(gfx, sBarX + i * 8, sBarY, "icon_bonus_full", sheenCol);
                    }
                    gfx.disableScissor();
                } else {
                    drawDynamicFill(gfx, sBarX, sBarY, currentPenaltyRightEdge, sBarW, sBarH, "bonus_fill", underTopCol, underBotCol);
                    if (sBarH > 2) gfx.fill(sBarX + 1, sBarY + 1, sBarX + currentPenaltyRightEdge, sBarY + 2, sheenCol);
                }
            }

            if (remainderPx > 0) {
                int overTier = fullBars;
                float oFactor = 1.0f - ((overTier % 3) * 0.20f);
                int overTopCol = applyAlpha(0xFF000000 | shadeColor(bTopRGB, oFactor), renderAlpha);
                int overBotCol = applyAlpha(0xFF000000 | shadeColor(bBotRGB, oFactor), renderAlpha);

                if (isIconMode) {
                    gfx.enableScissor(sBarX, sBarY, sBarX + remainderPx, sBarY + sBarH);
                    for (int i = 0; i < 10; i++) {
                        drawSpriteGradient(gfx, sBarX + i * 8, sBarY, "icon_bonus_full", overTopCol, overBotCol);
                        drawSprite(gfx, sBarX + i * 8, sBarY, "icon_bonus_full", sheenCol);
                    }
                    gfx.disableScissor();

                    if (remainderPx < currentPenaltyRightEdge) {
                        gfx.enableScissor(sBarX + remainderPx - 1, sBarY, sBarX + remainderPx, sBarY + sBarH);
                        for (int i = 0; i < 10; i++) {
                            drawSprite(gfx, sBarX + i * 8, sBarY, "icon_bonus_full", sepCol);
                        }
                        gfx.disableScissor();
                    }
                } else {
                    drawDynamicFill(gfx, sBarX, sBarY, remainderPx, sBarW, sBarH, "bonus_fill", overTopCol, overBotCol);
                    if (sBarH > 2) gfx.fill(sBarX + 1, sBarY + 1, sBarX + remainderPx, sBarY + 2, sheenCol);
                    
                    if (remainderPx < currentPenaltyRightEdge) {
                        int bonusSepW = textureLayout.containsKey("bonus_sep") ? textureLayout.get("bonus_sep")[2] / 2 : 1;
                        int bonusSepH = textureLayout.containsKey("bonus_sep") ? textureLayout.get("bonus_sep")[3] / 2 : 2;
                        drawSpriteStretched(gfx, sBarX + remainderPx, barInnerY, bonusSepW, bonusSepH, "bonus_sep", sepCol);
                    }
                }
            }

            if (fullBars > 0) {
                String multText = (fullBars + 1) + "x";
                gfx.pose().pushPose();
                float scale = 0.6f;
                int textX = sBarX - (int) (mc.font.width(multText) * scale) - 4;
                int textY = sBarY + (sBarH / 2) - (int) (4 * scale);
                gfx.pose().translate(textX, textY, 0);
                gfx.pose().scale(scale, scale, 1.0f);
                gfx.drawString(mc.font, multText, 0, 0, applyAlpha(0xFFFFD700, renderAlpha), true);
                gfx.pose().popPose();
            }
        }

        // Regen indicators (3-Way Toggle)
        double regenVal = 1.0;
        AttributeInstance regenAttr = mc.player.getAttribute(net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.wrapAsHolder(StaminaAttributes.STAMINA_REGEN.get()));
        if (regenAttr != null) regenVal = regenAttr.getValue();

        String regenSprite = null;
        String regenText = "";
        int regenColor = 0xFFFFFFFF;

        if (regenVal >= 1.5) { regenSprite = "regen_pos_3"; regenText = ">>>"; regenColor = 0xFF00FF00; }
        else if (regenVal >= 1.25) { regenSprite = "regen_pos_2"; regenText = ">>"; regenColor = 0xFF55FF55; }
        else if (regenVal > 1.01) { regenSprite = "regen_pos_1"; regenText = ">"; regenColor = 0xFFAAFF55; }
        else if (regenVal <= 0.5) { regenSprite = "regen_neg_3"; regenText = "<<<"; regenColor = 0xFFFF0000; }
        else if (regenVal <= 0.75) { regenSprite = "regen_neg_2"; regenText = "<<"; regenColor = 0xFFFF5555; }
        else if (regenVal < 0.99) { regenSprite = "regen_neg_1"; regenText = "<"; regenColor = 0xFFFF9955; }

        if (regenSprite != null && renderNormalW > 2) {
            StaminaConfig.RegenIndicatorStyle style = StaminaConfig.RegenIndicatorStyle.CUSTOM;
            try {
                style = StaminaConfig.CLIENT.regenIndicatorStyle.get();
            } catch (Exception ignored) {}

            if (style == StaminaConfig.RegenIndicatorStyle.CUSTOM) {
                int rTexW = textureLayout.containsKey(regenSprite) ? textureLayout.get(regenSprite)[2] : 10;
                int rTexH = textureLayout.containsKey(regenSprite) ? textureLayout.get(regenSprite)[3] : 8;
                
                int rW = Math.max(1, rTexW / 2);
                int rH = Math.max(1, rTexH / 2);
                
                int targetX = sBarX + renderNormalW - rW - 2;
                int targetY = isIconMode ? iconY + 5 : sBarY + (sBarH / 2) - (rH / 2);

                if (targetX >= sBarX) {
                    drawSpriteStretched(gfx, targetX, targetY, rW, rH, regenSprite, applyAlpha(regenColor, renderAlpha));
                }
            } else if (style == StaminaConfig.RegenIndicatorStyle.DEFAULT) {
                net.minecraft.network.chat.MutableComponent regenComp = net.minecraft.network.chat.Component.literal(regenText);
                regenComp.withStyle(net.minecraft.ChatFormatting.BOLD);
                float scale = 0.5f;
                
                int textWidth = (int) (mc.font.width(regenComp) * scale);
                int textHeight = (int) (9 * scale);
                
                int targetX = sBarX + renderNormalW - textWidth - 2;
                int targetY = isIconMode ? iconY + 5 : sBarY + (sBarH / 2) - (textHeight / 2);

                if (targetX >= sBarX) {
                    gfx.pose().pushPose();
                    gfx.pose().translate(targetX, targetY, 0);
                    gfx.pose().scale(scale, scale, 1.0f);
                    gfx.drawString(mc.font, regenComp, 0, 0, applyAlpha(regenColor, renderAlpha), false);
                    gfx.pose().popPose();
                }
            }
        }
    }

    private static void drawIcon(GuiGraphics gfx, int x, int y, int w, int h, String text, int color, float alpha) {
        if (text == null || text.isEmpty()) return;
 
        if (text.startsWith("CUSTOM:")) {
            String iconName = text.substring(7);
            
            float idealSize = Math.max(10.0f, h * 1.25f);
            float maxAllowed = w * 0.90f; 
            float sizeF = Math.min(idealSize, maxAllowed);
            
            if (sizeF < 2.0f) return; 
            
            boolean isIconMode = false;
            try {
                isIconMode = StaminaConfig.CLIENT.hudStyle.get().name().equalsIgnoreCase("ICON");
            } catch (Exception e) {}

            float scale = sizeF / com.peakstamina.client.gui.CustomIconRegistry.CANVAS_SIZE;
            float drawX = x + Math.max(0, (w - sizeF) / 2.0f);
            float drawY = y + (h - sizeF) / 2.0f - (isIconMode ? 0.0f : 1.0f); 
            
            gfx.pose().pushPose();
            gfx.pose().translate(drawX, drawY, 0);
            gfx.pose().scale(scale, scale, 1.0f);
            com.peakstamina.client.gui.CustomIconRegistry.drawIcon(gfx, 0, 0, com.peakstamina.client.gui.CustomIconRegistry.CANVAS_SIZE, com.peakstamina.client.gui.CustomIconRegistry.CANVAS_SIZE, iconName, color, alpha);
            gfx.pose().popPose();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int textWidth = font.width(text);
        float fontHeight = 9.0f;
        float scale = 0.8f; 
        if (h > fontHeight) scale = ((h * 0.95f) / fontHeight) * 0.8f;
        float maxScaleW = (w * 0.95f) / (float) textWidth;
        scale = Math.min(scale, maxScaleW);
        if (scale < 0.1f) return;

        float centerX = x + w / 2.0f;
        float centerY = y + h / 2.0f;
        gfx.pose().pushPose();
        gfx.pose().translate(centerX, centerY, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        float localBarHalfH = (h / scale) / 2.0f;
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

    public static String getFormattedWeight(double weight) {
        com.peakstamina.config.StaminaConfig.Client.WeightUnit unit = com.peakstamina.config.StaminaConfig.CLIENT.displayUnit.get();

        double multiplier = 0.453592; 
        String label = "kg";
        
        if (unit == com.peakstamina.config.StaminaConfig.Client.WeightUnit.lbs) {
            multiplier = 1.0; 
            label = "lbs";
        } else if (unit == com.peakstamina.config.StaminaConfig.Client.WeightUnit.CUSTOM) {
            multiplier = com.peakstamina.config.StaminaConfig.CLIENT.customUnitMultiplier.get();
            label = com.peakstamina.config.StaminaConfig.CLIENT.customUnitLabel.get();
        }
        
        return String.format(java.util.Locale.US, "%.1f %s", weight * multiplier, label);
    }

    public static String getFormattedWeightDual(double current, double max) {
        com.peakstamina.config.StaminaConfig.Client.WeightUnit unit = com.peakstamina.config.StaminaConfig.CLIENT.displayUnit.get();

        double multiplier = 0.453592;
        String label = "kg"; 
        
        if (unit == com.peakstamina.config.StaminaConfig.Client.WeightUnit.lbs) {
            multiplier = 1.0; 
            label = "lbs";
        } else if (unit == com.peakstamina.config.StaminaConfig.Client.WeightUnit.CUSTOM) {
            multiplier = com.peakstamina.config.StaminaConfig.CLIENT.customUnitMultiplier.get();
            label = com.peakstamina.config.StaminaConfig.CLIENT.customUnitLabel.get();
        }
        
        return String.format(java.util.Locale.US, "%.1f / %.1f %s", current * multiplier, max * multiplier, label);
    }

    @SubscribeEvent
    public static void onScreenRender(net.neoforged.neoforge.client.event.ScreenEvent.Render.Post event) {
        if (!StaminaConfig.CLIENT.enableWeightHUD.get()) return;
        if (event.getScreen() instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen invScreen) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {

                var cap = mc.player.getData(com.peakstamina.capabilities.StaminaCapability.STAMINA); 
                
                if (cap != null) {
                    double rawWeight = com.peakstamina.handlers.mechanics.WeightHandler.calculateTotalWeight(mc.player);
                    double weightMult = 1.0;

                    AttributeInstance wMultAttr = mc.player.getAttribute(StaminaAttributes.WEIGHT_CALC_MULTIPLIER);
         
                    if (wMultAttr != null) weightMult = wMultAttr.getValue();
                    double effectiveWeight = rawWeight * weightMult;

                    double threshold = StaminaConfig.COMMON.weightPenaltyThreshold.get();
                    double limit = StaminaConfig.COMMON.weightPenaltyLimit.get();

                    AttributeInstance limitAttr = mc.player.getAttribute(StaminaAttributes.WEIGHT_LIMIT);
                    if (limitAttr != null) {
                        double bonus = limitAttr.getValue() / 2.0;
                        threshold += bonus;
                        limit += (bonus * 2);
                    }

                    String text = getFormattedWeightDual(effectiveWeight, limit);
                    int color = effectiveWeight >= limit ? 0xFF5555 : (effectiveWeight >= threshold ? 0xFFAA00 : 0xFFFFFF);

                    int x = invScreen.getGuiLeft() + 50 + StaminaConfig.CLIENT.weightXOffset.get();
                    int y = invScreen.getGuiTop() + 8 + StaminaConfig.CLIENT.weightYOffset.get();

                    float baseScale = 0.6f;
                    int textWidth = mc.font.width(text);
                    float maxWidth = 46.0f; 

                    float finalScale = baseScale;
                    float yOffset = 0.0f;
                    
                    if (textWidth * baseScale > maxWidth) {
                        finalScale = maxWidth / (float) textWidth;
                        float originalHeight = mc.font.lineHeight * baseScale;
                        float newHeight = mc.font.lineHeight * finalScale;
                        yOffset = (originalHeight - newHeight) / 2.0f;
                    }

                    event.getGuiGraphics().pose().pushPose();
                    event.getGuiGraphics().pose().translate(x, y + yOffset, 0); 
                    event.getGuiGraphics().pose().scale(finalScale, finalScale, 1.0f);
                    event.getGuiGraphics().drawString(mc.font, text, -textWidth / 2, 0, color, true);
                    event.getGuiGraphics().pose().popPose();
                } 
            }
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        if (player == null) return;
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
        if (player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;
        
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(new com.peakstamina.network.packets.PacketMissedAttack());
    }

    @SubscribeEvent
    public static void onClickInput(net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        if (!StaminaConfig.COMMON.enableStamina.get()) return;
        if (mc.player.isCreative() && StaminaConfig.COMMON.disableInCreative.get()) return;
        if (mc.player.isSpectator() && StaminaConfig.COMMON.disableInSpectator.get()) return;

        if (event.isAttack()) {
            net.minecraft.world.item.Item weaponItem = mc.player.getMainHandItem().getItem();
            
            // Cancel the input entirely if the weapon is on cooldown
            if (mc.player.getCooldowns().isOnCooldown(weaponItem)) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }
}