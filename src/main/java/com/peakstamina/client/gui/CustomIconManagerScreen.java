package com.peakstamina.client.gui;

import java.util.List;

import com.peakstamina.client.gui.CustomIconRegistry.CustomIcon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CustomIconManagerScreen extends Screen {

    private final Screen parent;
    private IconList listWidget;

    public CustomIconManagerScreen(Screen parent) {
        super(Component.literal("✏ Custom Penalty Icons"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();

        listWidget = new IconList(this.minecraft, this.width, this.height - 90, 52, 34);
        rebuildList();
        addWidget(listWidget);

        addRenderableWidget(Button.builder(Component.literal("✏ Draw New Icon"),
                b -> Minecraft.getInstance().setScreen(
                        new CustomIconStudioScreen(this, null, null)
                )).bounds(this.width / 2 - 155, this.height - 28, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("Done"),
                b -> onClose()).bounds(this.width / 2 + 55, this.height - 28, 100, 20).build());
    }

    private void rebuildList() {
        listWidget.children().clear();
        for (CustomIcon icon : CustomIconRegistry.getAllIcons()) {
            listWidget.addEntry(new IconRow(icon, this));
        }
    }

    void refresh() {
        init(); 
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(gfx, mouseX, mouseY, partialTick);
        gfx.fill(0, 0, this.width, this.height, 0xFF181818); 
        gfx.fill(0, 52, this.width, this.height - 38, 0xFF0A0A0A);
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        super.render(gfx, mouseX, mouseY, partialTick);
        listWidget.render(gfx, mouseX, mouseY, partialTick);

        net.minecraft.client.gui.Font f = Minecraft.getInstance().font;
        gfx.drawCenteredString(f, this.title, this.width / 2, 12, 0xFFFFAA00);
        gfx.drawCenteredString(f,
                "§7Use §aCUSTOM:<name>§7 in any §eIconText§7 field to use an icon on the penalty bar.",
                this.width / 2, 28, 0xAAAAAA);

        if (CustomIconRegistry.getAllIcons().isEmpty()) {
            gfx.drawCenteredString(f,
                    "§8No custom icons yet. Click §7✏ Draw New Icon§8 to get started.",
                    this.width / 2, this.height / 2, 0x666666);
        }
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    static class IconList extends ContainerObjectSelectionList<IconRow> {
        IconList(Minecraft mc, int w, int h, int y, int ih) { super(mc, w, h, y, ih); }
        @Override public int addEntry(IconRow e) { return super.addEntry(e); }
        @Override public int getRowWidth() { return Math.min(400, this.width - 60); }
        @Override protected int getScrollbarPosition() { return this.getX() + this.width - 12; }
    }

    static class IconRow extends ContainerObjectSelectionList.Entry<IconRow> {

        private static final int PREVIEW_SCALE = 2;
        private static final int PREVIEW_SIZE  = CustomIconRegistry.CANVAS_SIZE * PREVIEW_SCALE;

        private final CustomIcon icon;
        private final CustomIconManagerScreen manager;
        private final Button editBtn;
        private final Button deleteBtn;

        IconRow(CustomIcon icon, CustomIconManagerScreen manager) {
            this.icon    = icon;
            this.manager = manager;

            editBtn = Button.builder(Component.literal("Edit"),
                    b -> Minecraft.getInstance().setScreen(
                            new CustomIconStudioScreen(manager, icon, savedKey -> manager.refresh())
                    )).bounds(0, 0, 50, 20).build();

            deleteBtn = Button.builder(Component.literal("§cDel"),
                    b -> {
                        CustomIconRegistry.removeIcon(icon.name);
                        manager.refresh();
                    }).bounds(0, 0, 36, 20).build();
        }

        @Override
        public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h,
                           int mX, int mY, boolean hover, float pt) {
            net.minecraft.client.gui.Font f = Minecraft.getInstance().font;

            if (hover) gfx.fill(left, top, left + w, top + h, 0x22FFFFFF);

            int previewPad = (h - PREVIEW_SIZE) / 2;
            CustomIconRegistry.drawPreview(gfx, icon.pixels,
                    left + 4, top + previewPad, PREVIEW_SCALE, 0xFFFFFF);

            int textX = left + 4 + PREVIEW_SIZE + 8;
            gfx.drawString(f, "§aCUSTOM:" + icon.name, textX, top + 4, 0xFFFFFF, false);
            String desc = icon.desc.isEmpty() ? "§8(no description)" : "§7" + icon.desc;
            gfx.drawString(f, desc, textX, top + 16, 0xAAAAAA, false);

            int btnY = top + (h - 20) / 2;
            editBtn.setX(left + w - 90);   editBtn.setY(btnY);
            deleteBtn.setX(left + w - 38); deleteBtn.setY(btnY);
            editBtn.render(gfx, mX, mY, pt);
            deleteBtn.render(gfx, mX, mY, pt);
        }

        @Override public List<? extends GuiEventListener>  children()   { return List.of(editBtn, deleteBtn); }
        @Override public List<? extends NarratableEntry>   narratables() { return List.of(editBtn, deleteBtn); }
    }
}