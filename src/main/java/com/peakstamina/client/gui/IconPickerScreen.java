package com.peakstamina.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class IconPickerScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> onPick;
    private IconGridList listWidget;
    private EditBox searchBox;

    private int panelW, panelX;

    private static final List<IconItem> DEFAULT_ICONS = List.of(
        new IconItem("none", "∅", "Disable Icon", null),
        new IconItem("💧", "💧", "Water", null),
        new IconItem("🔥", "🔥", "Fire", null),
        new IconItem("❄", "❄", "Cold", null),
        new IconItem("💀", "💀", "Death", null),
        new IconItem("☣", "☣", "Poison", null),
        new IconItem("🛡", "🛡", "Defense", null),
        new IconItem("⚡", "⚡", "Energy", null),
        new IconItem("❤", "❤", "Health", null),
        new IconItem("🍗", "🍗", "Food", null),
        new IconItem("🏹", "🏹", "Ranged", null),
        new IconItem("🏃", "🏃", "Sprint", null),
        new IconItem("🏊", "🏊", "Swim", null),
        new IconItem("💤", "💤", "Fatigue", null)
    );

    public IconPickerScreen(Screen parent, Consumer<String> onPick) {
        super(Component.literal("Select an Icon"));
        this.parent = parent;
        this.onPick = onPick;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        
        panelW = Math.min(420, this.width - 40);
        panelX = this.width / 2 - panelW / 2;

        searchBox = new EditBox(this.font, panelX + 10, 38, panelW - 20, 16, Component.literal("Search..."));
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.literal("Search icons..."));
        searchBox.setResponder(this::rebuildList);
        this.addRenderableWidget(searchBox);

        listWidget = new IconGridList(this.minecraft, panelW, this.height, 60, this.height - 40, 30);
        listWidget.setLeftPos(panelX);
        this.addWidget(listWidget);

        rebuildList("");

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(this.width / 2 - 155, this.height - 30, 150, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.literal("✏ Draw New Icon"), b -> {
            Minecraft.getInstance().setScreen(new CustomIconStudioScreen(this, null, savedKey -> {
                onPick.accept(savedKey);
                Minecraft.getInstance().setScreen(parent); 
            }));
        }).bounds(this.width / 2 + 5, this.height - 30, 150, 20).build());
    }

    private void rebuildList(String query) {
        listWidget.children().clear();
        String q = query.toLowerCase(java.util.Locale.ROOT);

        // Filter Defaults
        List<IconItem> filteredDefaults = new ArrayList<>();
        for (IconItem item : DEFAULT_ICONS) {
            if (item.tooltip.toLowerCase().contains(q) || item.label.toLowerCase().contains(q)) {
                filteredDefaults.add(item);
            }
        }

        if (!filteredDefaults.isEmpty()) {
            listWidget.addEntry(new HeaderRow("Default & OS Emojis"));
            addGridRows(filteredDefaults);
        }

        // Filter Custom 
        List<IconItem> filteredCustoms = new ArrayList<>();
        for (CustomIconRegistry.CustomIcon ci : CustomIconRegistry.getAllIcons()) {
            if (ci.name.toLowerCase().contains(q) || ci.desc.toLowerCase().contains(q)) {
                filteredCustoms.add(new IconItem("CUSTOM:" + ci.name, "", ci.desc, ci));
            }
        }

        if (!filteredCustoms.isEmpty() || query.isEmpty()) {
            if (!filteredDefaults.isEmpty()) listWidget.addEntry(new SpacerRow());
            listWidget.addEntry(new HeaderRow("Custom Drawn Icons"));
            
            if (filteredCustoms.isEmpty()) {
                listWidget.addEntry(new TextRow("No custom icons found."));
            } else {
                addGridRows(filteredCustoms);
            }
        }
        
        listWidget.setScrollAmount(0);
    }

    private void addGridRows(List<IconItem> items) {
        int buttonSize = 26;
        int gap = 4;
        int itemsPerRow = Math.max(1, (panelW - 20) / (buttonSize + gap));
        
        List<IconItem> currentRow = new ArrayList<>();
        for (IconItem item : items) {
            currentRow.add(item);
            if (currentRow.size() >= itemsPerRow) {
                listWidget.addEntry(new GridRow(new ArrayList<>(currentRow), this));
                currentRow.clear();
            }
        }
        if (!currentRow.isEmpty()) {
            listWidget.addEntry(new GridRow(new ArrayList<>(currentRow), this));
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);

        gfx.fill(panelX, 30, panelX + panelW, this.height - 35, 0xEE111111);
        gfx.renderOutline(panelX, 30, panelW, this.height - 65, 0xFF444444);
        
        listWidget.render(gfx, mouseX, mouseY, partialTick);
        gfx.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFAA00);
        
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onClose();
            return true;
        }
        if (searchBox.isFocused() && keyCode != GLFW.GLFW_KEY_ENTER) {
            return searchBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() { Minecraft.getInstance().setScreen(parent); }


    private static class IconItem {
        String value; String label; String tooltip; CustomIconRegistry.CustomIcon customIcon;
        IconItem(String v, String l, String t, CustomIconRegistry.CustomIcon c) { 
            value = v; label = l; tooltip = t; customIcon = c; 
        }
    }

    private class IconGridList extends ContainerObjectSelectionList<ListEntry> {
        public IconGridList(Minecraft mc, int w, int h, int t, int b, int ih) { super(mc, w, h, t, b, ih); }
        @Override public int addEntry(ListEntry e) { return super.addEntry(e); }
        @Override public int getRowWidth() { return this.width - 20; }
        @Override protected int getScrollbarPosition() { return this.getLeft() + this.width - 10; }
    }

    private static abstract class ListEntry extends ContainerObjectSelectionList.Entry<ListEntry> {}

    private static class HeaderRow extends ListEntry {
        String text; HeaderRow(String t) { text = t; }
        @Override public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {
            gfx.drawString(Minecraft.getInstance().font, "§l" + text, left + 4, top + h / 2 - 4, 0xFFFFAA, false);
            gfx.fill(left, top + h - 2, left + w - 10, top + h - 1, 0x55FFFFFF); 
        }
        @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
    }
    
    private static class TextRow extends ListEntry {
        String text; TextRow(String t) { text = t; }
        @Override public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {
            gfx.drawString(Minecraft.getInstance().font, "§8" + text, left + 4, top + h / 2 - 4, 0xFFFFFF, false);
        }
        @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
    }

    private static class SpacerRow extends ListEntry {
        @Override public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {}
        @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
    }

    private class GridRow extends ListEntry {
        private final List<FlatIconButton> buttons = new ArrayList<>();
        GridRow(List<IconItem> items, IconPickerScreen screen) {
            for (IconItem item : items) {
                buttons.add(new FlatIconButton(0, 0, item, b -> {
                    screen.onPick.accept(item.value);
                    screen.onClose();
                }));
            }
        }
        @Override
        public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {
            int buttonSize = 26;
            int gap = 4;
            int startX = left + 4;
            for (int i = 0; i < buttons.size(); i++) {
                FlatIconButton btn = buttons.get(i);
                btn.setX(startX + i * (buttonSize + gap));
                btn.setY(top + 2);
                btn.render(gfx, mX, mY, pt);
            }
        }
        @Override public List<? extends GuiEventListener> children() { return buttons; }
        @Override public List<? extends NarratableEntry> narratables() { return buttons; }
    }

    private static class FlatIconButton extends Button {
        private final IconItem iconItem;
        
        FlatIconButton(int x, int y, IconItem item, OnPress press) {
            super(x, y, 26, 26, Component.literal(""), press, DEFAULT_NARRATION);
            this.iconItem = item;

            String tip = item.customIcon != null ? 
                         (item.customIcon.desc.isEmpty() ? "Custom Icon: " + item.customIcon.name : item.customIcon.desc) 
                         : item.tooltip;
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.literal(tip)));
        }
        
        @Override
        public void renderWidget(GuiGraphics gfx, int mX, int mY, float pt) {
            boolean hovered = isHovered();

            int bgColor = hovered ? 0x66FFFFFF : 0x33000000;
            int outlineColor = hovered ? 0xFFFFAA00 : 0xFF444444; 
            
            gfx.fill(getX(), getY(), getX() + width, getY() + height, bgColor);
            gfx.renderOutline(getX(), getY(), width, height, outlineColor);

            if (iconItem.customIcon != null) {
                int expectedSize = CustomIconRegistry.CANVAS_SIZE;
                int drawX = getX() + (width - expectedSize) / 2;
                int drawY = getY() + (height - expectedSize) / 2;
                CustomIconRegistry.drawPreview(gfx, iconItem.customIcon.pixels, drawX, drawY, 1, 0xFFFFFF);
            } else {
                gfx.drawCenteredString(Minecraft.getInstance().font, iconItem.label, getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFF);
            }
        }
    }
}