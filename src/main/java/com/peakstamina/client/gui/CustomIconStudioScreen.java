package com.peakstamina.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.peakstamina.client.gui.CustomIconRegistry.CustomIcon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.Stack;
import java.util.function.Consumer;

public class CustomIconStudioScreen extends Screen {

    private static final int CANVAS_SIZE = CustomIconRegistry.CANVAS_SIZE;
    private int CELL_PX = 14; 
    private int CANVAS_PX;

    private static final int BAR_W = 81;
    private static final int ZONE_W = 22;
    private static final int ICON_MODE_H = 10;

    private enum Tool { DRAW, ERASE, FILL }
    private Tool activeTool = Tool.DRAW;

    private final byte[] pixels = new byte[CANVAS_SIZE * CANVAS_SIZE];
    private final Stack<byte[]> undoStack = new Stack<>();
    private final Stack<byte[]> redoStack = new Stack<>();

    private int canvasX, canvasY;
    private int leftPanelX, rightPanelX;
    private int panelHeight;

    private EditBox nameBox;
    private EditBox descBox;
    private EditBox colorBox;
    private Button drawBtn, eraseBtn, fillBtn;

    private final String existingName;
    private final Consumer<String> onSave;
    private final Screen parent;

    private int previewColor = 16777215; 
    private boolean isUpdatingFromCode = false;
    private boolean isDrawingOnCanvas = false;

    private String validationError = "";

    private static final int[] PRESETS = {
        0xFFFFFF, 0xAAAAAA, 0x555555, 0x000000, 
        0xFFFF55, 0xFFAA00, 0xFF5555, 0xAA0000,
        0x55FF55, 0x00AA00, 0x55FFFF, 0x00AAAA, 
        0x5555FF, 0x0000AA, 0xFF55FF, 0xAA00AA
    };

    public CustomIconStudioScreen(Screen parent, CustomIcon existingIcon, Consumer<String> onSave) {
        super(Component.literal(existingIcon == null ? "Icon Studio \u2014 New Icon" : "Icon Studio \u2014 Edit: " + existingIcon.name));
        this.parent = parent;
        this.onSave = onSave;
        this.existingName = existingIcon != null ? existingIcon.name : null;
        if (existingIcon != null) {
            System.arraycopy(existingIcon.pixels, 0, pixels, 0, pixels.length);
        }
    }

    @Override
    protected void init() {
        this.clearWidgets();
        net.minecraft.client.gui.Font f = Minecraft.getInstance().font;

        CELL_PX = Math.min(14, (this.height - 40) / CANVAS_SIZE);
        if (CELL_PX < 6) CELL_PX = 6; 
        CANVAS_PX = CANVAS_SIZE * CELL_PX;

        canvasX = this.width / 2 - (CANVAS_PX / 2);
        canvasY = Math.max(15, this.height / 2 - (CANVAS_PX / 2));

        panelHeight = Math.max(CANVAS_PX, 215);
        int panelY = canvasY + (CANVAS_PX / 2) - (panelHeight / 2);

        leftPanelX = canvasX - 120;
        int leftW = 115;
        
        rightPanelX = canvasX + CANVAS_PX + 5;
        int rightW = 130;

        int cy = panelY;
        drawBtn = new IconButton(leftPanelX + 5, cy + 20, 50, 20, "Draw", "draw", b -> setTool(Tool.DRAW));
        fillBtn = new IconButton(leftPanelX + 60, cy + 20, 50, 20, "Fill", "fill", b -> setTool(Tool.FILL));
        eraseBtn = new IconButton(leftPanelX + 5, cy + 44, 50, 20, "Erase", "erase", b -> setTool(Tool.ERASE));
        addRenderableWidget(new IconButton(leftPanelX + 60, cy + 44, 50, 20, "Clear", "clear", b -> clearCanvas()));

        addRenderableWidget(drawBtn);
        addRenderableWidget(fillBtn);
        addRenderableWidget(eraseBtn);

        // History
        addRenderableWidget(new IconButton(leftPanelX + 5, cy + 68, 50, 20, "Undo", "undo", b -> undo()));
        addRenderableWidget(new IconButton(leftPanelX + 60, cy + 68, 50, 20, "Redo", "redo", b -> redo()));

        // Shift Arrows
        int shiftY = cy + 100;
        addRenderableWidget(Button.builder(Component.literal("↑"), b -> shift(0, -1)).bounds(leftPanelX + 47, shiftY, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("←"), b -> shift(-1, 0)).bounds(leftPanelX + 25, shiftY + 22, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("↓"), b -> shift(0, 1)).bounds(leftPanelX + 47, shiftY + 22, 20, 20).build());
        addRenderableWidget(Button.builder(Component.literal("→"), b -> shift(1, 0)).bounds(leftPanelX + 69, shiftY + 22, 20, 20).build());

        cy = panelY;
        nameBox = new EditBox(f, rightPanelX + 5, cy + 28, rightW - 10, 16, Component.empty());
        nameBox.setMaxLength(32);
        if (existingName != null) nameBox.setValue(existingName);
        nameBox.setResponder(s -> validationError = "");
        addRenderableWidget(nameBox);

        descBox = new EditBox(f, rightPanelX + 5, cy + 58, rightW - 10, 16, Component.empty());
        descBox.setMaxLength(32);
        if (existingName != null) {
            CustomIcon existing = CustomIconRegistry.getAllIcons().stream()
                    .filter(ic -> ic.name.equals(existingName)).findFirst().orElse(null);
            if (existing != null) descBox.setValue(existing.desc);
        }
        addRenderableWidget(descBox);

        colorBox = new EditBox(f, rightPanelX + 5, cy + 88, rightW - 10, 16, Component.empty());
        colorBox.setMaxLength(10);
        colorBox.setValue(String.valueOf(16777215));
        colorBox.setResponder(val -> {
            if (isUpdatingFromCode) return;
            try {
                previewColor = Integer.parseInt(val.trim()) & 0xFFFFFF; // Parse as decimal
            } catch (NumberFormatException ignored) {}
        });
        addRenderableWidget(colorBox);

        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(leftPanelX + 5, panelY + panelHeight - 25, leftW - 10, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Save Icon"), b -> trySave())
                .bounds(rightPanelX + 5, panelY + panelHeight - 25, rightW - 10, 20).build());

        setPreviewColor(previewColor);
    }

    private void setPreviewColor(int rgb) {
        this.previewColor = rgb;
        isUpdatingFromCode = true;
        if (colorBox != null) colorBox.setValue(String.valueOf(this.previewColor)); // Set as decimal
        isUpdatingFromCode = false;
    }

    private void setTool(Tool t) {
        activeTool = t;
    }

    private class IconButton extends Button {
        private final String actionName;
        private final net.minecraft.resources.ResourceLocation iconLocation;

        public IconButton(int x, int y, int w, int h, String actionName, String fileName, OnPress press) {
            super(x, y, w, h, Component.empty(), press, DEFAULT_NARRATION);
            this.actionName = actionName;
            this.iconLocation = new net.minecraft.resources.ResourceLocation("peakstamina", "textures/gui/studio/" + fileName + ".png");
        }

        @Override
        public void renderWidget(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(gfx, mouseX, mouseY, partialTick); 
            
            boolean isActive = false;
            if (activeTool == Tool.DRAW && actionName.equals("Draw")) isActive = true;
            if (activeTool == Tool.ERASE && actionName.equals("Erase")) isActive = true;
            if (activeTool == Tool.FILL && actionName.equals("Fill")) isActive = true;

            int color = isActive ? 0xFF55FF55 : 0xFFFFFFFF; 
            if (!this.active) color = 0xFFAAAAAA; 

            // Set up color tinting for the custom icon
            float a = ((color >> 24) & 0xFF) / 255f;
            if (a <= 0.01f) a = 1f;
            float rCol = ((color >> 16) & 0xFF) / 255f;
            float gCol = ((color >> 8) & 0xFF) / 255f;
            float bCol = (color & 0xFF) / 255f;

            com.mojang.blaze3d.systems.RenderSystem.enableBlend();
            com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(rCol, gCol, bCol, a);

            gfx.blit(iconLocation, this.getX() + 2, this.getY() + 2, 0, 0, 16, 16, 16, 16);
            
            com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();

        net.minecraft.client.gui.Font font = Minecraft.getInstance().font;
        int textW = font.width(actionName);
        int textX = this.getX() + 18 + ((this.width - 18) - textW) / 2;
        gfx.drawString(font, actionName, textX, this.getY() + (this.height - 8) / 2, color, true);
    }
    }

    private void pushUndo() {
        undoStack.push(Arrays.copyOf(pixels, pixels.length));
        redoStack.clear();
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(Arrays.copyOf(pixels, pixels.length));
            System.arraycopy(undoStack.pop(), 0, pixels, 0, pixels.length);
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(Arrays.copyOf(pixels, pixels.length));
            System.arraycopy(redoStack.pop(), 0, pixels, 0, pixels.length);
        }
    }

    private void clearCanvas() {
        pushUndo();
        Arrays.fill(pixels, (byte) 0);
    }

    private void shift(int dx, int dy) {
        pushUndo();
        byte[] newPx = new byte[pixels.length];
        for (int y = 0; y < CANVAS_SIZE; y++) {
            for (int x = 0; x < CANVAS_SIZE; x++) {
                int nx = x - dx;
                int ny = y - dy;
                if (nx >= 0 && nx < CANVAS_SIZE && ny >= 0 && ny < CANVAS_SIZE) {
                    newPx[y * CANVAS_SIZE + x] = pixels[ny * CANVAS_SIZE + nx];
                }
            }
        }
        System.arraycopy(newPx, 0, pixels, 0, pixels.length);
    }

    private void drawPanel(GuiGraphics gfx, int x, int y, int w, int h, String title) {
        gfx.fill(x, y, x + w, y + h, 0xEE1A1A1A);
        gfx.renderOutline(x, y, w, h, 0xFF444444);
        if (title != null) {
            gfx.fill(x, y, x + w, y + 15, 0xFF282828);
            gfx.renderOutline(x, y, w, 15, 0xFF444444);
            gfx.drawString(Minecraft.getInstance().font, title, x + 6, y + 4, 0xFFFFAA00, false);
        }
    }

    private void drawSpriteTinted(GuiGraphics gfx, int x, int y, int w, int h, int u, int v, int tw, int th, int color) {
        net.minecraft.resources.ResourceLocation textureSheet = new net.minecraft.resources.ResourceLocation("peakstamina", "textures/gui/peakstamina_texture_sheet.png");
        float a = ((color >> 24) & 0xFF) / 255f;
        if (a <= 0.01f) a = 1f;
        float rCol = ((color >> 16) & 0xFF) / 255f;
        float gCol = ((color >> 8) & 0xFF) / 255f;
        float bCol = (color & 0xFF) / 255f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(rCol, gCol, bCol, a);
        gfx.blit(textureSheet, x, y, w, h, u, v, tw, th, 512, 512);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    private void drawInGameAccuratePreview(GuiGraphics gfx, byte[] px, int barX, int barY, int barW, int barH, int color, boolean isIconMode) {
        int iconY = isIconMode ? barY - 12 : barY;
        int iconH = Math.max(10, (int)(barH * 1.25f));
        int drawX = barX + Math.max(0, (barW - iconH) / 2);
        int drawY = iconY + (barH - iconH) / 2 - (isIconMode ? 0 : 1);

        float scale = (float)iconH / (float)CANVAS_SIZE;

        byte[] outlinePx = new byte[CANVAS_SIZE * CANVAS_SIZE];
        for (int y = 0; y < CANVAS_SIZE; y++) {
            for (int x = 0; x < CANVAS_SIZE; x++) {
                if (px[y * CANVAS_SIZE + x] == 0) {
                    boolean hasN = false;
                    for (int oy = -1; oy <= 1; oy++) {
                        for (int ox = -1; ox <= 1; ox++) {
                            int nx = x + ox, ny = y + oy;
                            if (nx >= 0 && nx < CANVAS_SIZE && ny >= 0 && ny < CANVAS_SIZE && px[ny * CANVAS_SIZE + nx] != 0) {
                                hasN = true; break;
                            }
                        }
                        if (hasN) break;
                    }
                    if (hasN) outlinePx[y * CANVAS_SIZE + x] = 1;
                }
            }
        }

        gfx.pose().pushPose();
        gfx.pose().translate(drawX, drawY, 0);
        gfx.pose().scale(scale, scale, 1.0f);
        
        CustomIconRegistry.drawPreview(gfx, outlinePx, 0, 0, 1, 0x88000000);
        CustomIconRegistry.drawPreview(gfx, px, 0, 0, 1, color | 0xFF000000);
        
        gfx.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= canvasX && mouseX < canvasX + CANVAS_PX && mouseY >= canvasY && mouseY < canvasY + CANVAS_PX) {
            pushUndo();
            isDrawingOnCanvas = true;
            applyToolToCanvas((int)mouseX, (int)mouseY, button);
            return true;
        }

        // Color Palette Click
        int panelY = canvasY + (CANVAS_PX / 2) - (panelHeight / 2);
        int gridX = rightPanelX + 5;
        int gridY = panelY + 108;
        if (mouseX >= gridX && mouseX < gridX + 8 * 14 && mouseY >= gridY && mouseY < gridY + 2 * 14) {
            int col = (int)(mouseX - gridX) / 14;
            int row = (int)(mouseY - gridY) / 14;
            int idx = row * 8 + col;
            if (idx >= 0 && idx < PRESETS.length) {
                setPreviewColor(PRESETS[idx]);
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDrawingOnCanvas && mouseX >= canvasX && mouseX < canvasX + CANVAS_PX && mouseY >= canvasY && mouseY < canvasY + CANVAS_PX) {
            applyToolToCanvas((int)mouseX, (int)mouseY, button);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDrawingOnCanvas = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void applyToolToCanvas(int mx, int my, int button) {
        int col = (mx - canvasX) / CELL_PX;
        int row = (my - canvasY) / CELL_PX;
        if (col < 0 || col >= CANVAS_SIZE || row < 0 || row >= CANVAS_SIZE) return;

        byte targetState = (button == 1) ? (byte) 0 : (byte) 255;

        if (activeTool == Tool.FILL) {
            byte clickedState = pixels[row * CANVAS_SIZE + col];
            if (clickedState != targetState) floodFill(col, row, clickedState, targetState);
        } else if (activeTool == Tool.DRAW) {
            pixels[row * CANVAS_SIZE + col] = targetState;
        } else if (activeTool == Tool.ERASE) {
            pixels[row * CANVAS_SIZE + col] = 0;
        }
    }

    private void floodFill(int x, int y, byte targetColor, byte replacementColor) {
        if (x < 0 || x >= CANVAS_SIZE || y < 0 || y >= CANVAS_SIZE) return;
        if (pixels[y * CANVAS_SIZE + x] != targetColor) return;
        pixels[y * CANVAS_SIZE + x] = replacementColor;
        floodFill(x + 1, y, targetColor, replacementColor);
        floodFill(x - 1, y, targetColor, replacementColor);
        floodFill(x, y + 1, targetColor, replacementColor);
        floodFill(x, y - 1, targetColor, replacementColor);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { onClose(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void trySave() {
        String name = nameBox.getValue().trim();
        String desc = descBox.getValue().trim();
        if (name.isEmpty()) { validationError = "Name cannot be empty."; return; }
        if (name.contains(";") || name.contains(" ")) { validationError = "Name cannot contain spaces/semicolons."; return; }
        if (CustomIconRegistry.hasIcon(name) && !name.equals(existingName)) { validationError = "Icon '" + name + "' already exists."; return; }
        CustomIcon icon = new CustomIcon(name, desc, Arrays.copyOf(pixels, pixels.length));
        CustomIconRegistry.putIcon(icon);
        if (onSave != null) onSave.accept("CUSTOM:" + name);
        onClose();
    }

    @Override
    public void onClose() { Minecraft.getInstance().setScreen(parent); }

    // --- MAIN RENDERER ---

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);
        net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
        gfx.drawCenteredString(f, this.title, this.width / 2, Math.max(5, canvasY - 15), 0xFFFFAA00);

        int panelY = canvasY + (CANVAS_PX / 2) - (panelHeight / 2);

        // Paneling
        drawPanel(gfx, leftPanelX, panelY, 115, panelHeight, "Tools");
        drawPanel(gfx, rightPanelX, panelY, 130, panelHeight, "Properties");

        // Center Canvas
        gfx.fill(canvasX - 1, canvasY - 1, canvasX + CANVAS_PX + 1, canvasY + CANVAS_PX + 1, 0xFF444444);
        int dangerRows = 2;
        int safePx = CANVAS_PX - (dangerRows * CELL_PX);
        gfx.fill(canvasX, canvasY, canvasX + CANVAS_PX, canvasY + safePx, 0xFF111111);
        gfx.fill(canvasX, canvasY + safePx, canvasX + CANVAS_PX, canvasY + CANVAS_PX, 0xFF3A1111); // Red warning

        // Draw Pixels
        for (int row = 0; row < CANVAS_SIZE; row++) {
            for (int col = 0; col < CANVAS_SIZE; col++) {
                int cx = canvasX + col * CELL_PX;
                int cy = canvasY + row * CELL_PX;
                if (pixels[row * CANVAS_SIZE + col] != 0) {
                    gfx.fill(cx, cy, cx + CELL_PX, cy + CELL_PX, 0xFFFFFFFF);
                }
                gfx.fill(cx, cy, cx + CELL_PX, cy + 1, 0x1AFFFFFF);
                gfx.fill(cx, cy, cx + 1, cy + CELL_PX, 0x1AFFFFFF);
            }
        }

        // Mouse Highlight
        if (mouseX >= canvasX && mouseX < canvasX + CANVAS_PX && mouseY >= canvasY && mouseY < canvasY + CANVAS_PX) {
            int hc = (mouseX - canvasX) / CELL_PX;
            int hr = (mouseY - canvasY) / CELL_PX;
            gfx.fill(canvasX + hc * CELL_PX, canvasY + hr * CELL_PX, canvasX + hc * CELL_PX + CELL_PX, canvasY + hr * CELL_PX + CELL_PX, 0x44FFFFFF);
        }

        // Right Panel 
        int cy = panelY;
        gfx.drawString(f, "§7Name", rightPanelX + 5, cy + 18, 0xAAAAAA, false);
        gfx.drawString(f, "§7Desc", rightPanelX + 5, cy + 48, 0xAAAAAA, false);
        gfx.drawString(f, "§eColor (Decimal)", rightPanelX + 5, cy + 78, 0xFFFFFF, false);

        int gridX = rightPanelX + 5;
        int gridY = cy + 108;
        
        for (int i = 0; i < PRESETS.length; i++) {
            int col = i % 8;
            int row = i / 8;
            int px = gridX + col * 14;
            int py = gridY + row * 14;
            gfx.fill(px, py, px + 12, py + 12, 0xFF000000 | PRESETS[i]);
            gfx.renderOutline(px, py, 12, 12, (previewColor == PRESETS[i]) ? 0xFFFFFFFF : 0xFF555555);
        }

        int py = cy + 140;
        gfx.drawString(f, "§ePreview", rightPanelX + 5, py, 0xFFFFFF, false);

        int topCol = 0xFF3A8A3A;
        try { topCol = 0xFF000000 | com.peakstamina.config.StaminaConfig.CLIENT.colorSafe.get(); } catch (Exception ignored) {}
        int penCol = 0xFF000000 | previewColor;

        // HUD: Bar Mode
        py += 12;
        int barX = rightPanelX + 5;
        drawSpriteTinted(gfx, barX, py, 2, 4, 30, 33, 4, 8, 0xFFFFFFFF);
        drawSpriteTinted(gfx, barX+2, py, 77, 4, 39, 33, 1, 8, 0xFFFFFFFF);
        drawSpriteTinted(gfx, barX+79, py, 2, 4, 45, 33, 4, 8, 0xFFFFFFFF);
        gfx.fill(barX + 1, py + 1, barX + BAR_W - 1, py + 3, 0xFF222222);
        drawSpriteTinted(gfx, barX, py, 2, 4, 59, 33, 4, 8, topCol);
        drawSpriteTinted(gfx, barX+2, py, BAR_W - ZONE_W - 2, 4, 68, 33, 1, 8, topCol);

        int penStartX = barX + BAR_W - ZONE_W;
        gfx.enableScissor(penStartX, py + 1, barX + BAR_W - 1, py + 3);
        for (int i = 0; i < ZONE_W; i += 6) drawSpriteTinted(gfx, penStartX + i, py + 1, 5, 2, 30, 52, 10, 4, penCol);
        gfx.disableScissor();
        drawSpriteTinted(gfx, penStartX, py + 1, 1, 2, 51, 52, 2, 4, 0xFFFFFFFF);
        drawInGameAccuratePreview(gfx, pixels, penStartX, py, ZONE_W, 4, penCol, false);

        // HUD: Icon Mode
        py += 20;
        for (int i = 0; i < 7; i++) drawSpriteTinted(gfx, barX + i * 8, py, 8, 10, 27, 66, 16, 20, 0xFFFFFFFF);
        for (int i = 0; i < 5; i++) drawSpriteTinted(gfx, barX + i * 8, py, 8, 10, 53, 66, 16, 20, topCol);
        for (int i = 5; i < 7; i++) drawSpriteTinted(gfx, barX + i * 8 + 1, py + 1, 5, 8, 105, 68, 10, 16, penCol);
        drawInGameAccuratePreview(gfx, pixels, barX + 5 * 8, py, 16, ICON_MODE_H, penCol, true);

        // Error rendering
        if (!validationError.isEmpty()) {
            gfx.drawCenteredString(f, "§c" + validationError, this.width / 2, this.height - 12, 0xFF0000);
        }

        super.render(gfx, mouseX, mouseY, partialTick);
    }
}