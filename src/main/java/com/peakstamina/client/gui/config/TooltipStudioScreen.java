package com.peakstamina.client.gui.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.peakstamina.config.StaminaConfig;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;

public class TooltipStudioScreen extends Screen {
    private final Screen parent;
    private final List<TooltipData> tooltips = new ArrayList<>();
    private int selectedIdx = 0;
    
    private TooltipList listWidget;

    private static final String[] TYPES = {"WEIGHT", "ATTACK_COST", "MISSED_ATTACK_COST", "USE_COST", "TICK_COST", "BLOCK_COST", "INSTANT_STAMINA", "BONUS_STAMINA", "REGEN_MODIFIER", "CURES", "ATTRIBUTE"};
    private static final String[] PLACEMENTS = {"BOTTOM", "BELOW_NAME"};
    private static final String[] COLORS = {"WHITE", "GRAY", "DARK_GRAY", "BLACK", "RED", "DARK_RED", "GREEN", "DARK_GREEN", "AQUA", "DARK_AQUA", "BLUE", "DARK_BLUE", "YELLOW", "GOLD", "LIGHT_PURPLE", "DARK_PURPLE"};

    private Button typeBtn, placeBtn, labelColBtn, valColBtn, autoFillBtn;
    private EditBox prefixBox, formatBox;
    
    private int rightX, rightW, listW;

    public TooltipStudioScreen(Screen parent) {
        super(Component.literal("Tooltip Studio"));
        this.parent = parent;
        for (String s : StaminaConfig.CLIENT.customTooltips.get()) {
            tooltips.add(new TooltipData(s));
        }
    }

    @Override
    protected void init() {
        clearWidgets();
        
        listW = Math.max(120, this.width / 3 - 10);
        rightX = listW + 20;
        rightW = this.width - rightX - 10;

        listWidget = new TooltipList(this.minecraft, listW, this.height, 35, this.height - 40, 24);
        listWidget.setLeftPos(10);
        addWidget(listWidget);
        refreshList();

        addRenderableWidget(Button.builder(Component.literal("Add New"), b -> { 
            String avail = getFirstAvailableType();
            if (avail != null) { // Prevents adding if all labels are already used
                TooltipData newData = new TooltipData(avail + ";BOTTOM;DARK_GRAY;WHITE;New Label: ;%s");
                applyTypeDefaults(newData);
                tooltips.add(newData); 
                selectedIdx = tooltips.size() - 1; 
                refreshList(); 
                updatePanel(); 
            }
        }).bounds(10, this.height - 30, listW / 2 - 2, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Delete"), b -> { 
            if (selectedIdx >= 0 && selectedIdx < tooltips.size()) { 
                tooltips.remove(selectedIdx); 
                selectedIdx = Math.max(0, selectedIdx - 1); 
                refreshList(); 
                updatePanel(); 
            } 
        }).bounds(10 + listW / 2 + 2, this.height - 30, listW / 2 - 2, 20).build());

        int halfBtnW = rightW / 2 - 2;
        
        typeBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (selectedIdx >= 0) {
                List<String> availableTypes = new ArrayList<>();
                for (String t : TYPES) {
                    boolean used = false;
                    for (int i = 0; i < tooltips.size(); i++) {
                        // Exclude types used by OTHER tooltips, keeping current type visible
                        if (i != selectedIdx && tooltips.get(i).type.equals(t)) {
                            used = true;
                            break;
                        }
                    }
                    if (!used) availableTypes.add(t);
                }
                this.minecraft.setScreen(new OptionSelectionScreen(this, "Select Type", availableTypes, tooltips.get(selectedIdx).type, selected -> {
                    tooltips.get(selectedIdx).type = selected;
                    applyTypeDefaults(tooltips.get(selectedIdx));
                }));
            }
        }).bounds(rightX, 35, halfBtnW, 20).build());
        
        placeBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (selectedIdx >= 0) {
                this.minecraft.setScreen(new OptionSelectionScreen(this, "Select Placement", List.of(PLACEMENTS), tooltips.get(selectedIdx).place, selected -> {
                    tooltips.get(selectedIdx).place = selected;
                }));
            }
        }).bounds(rightX + halfBtnW + 4, 35, halfBtnW, 20).build());
        
        labelColBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (selectedIdx >= 0) {
                this.minecraft.setScreen(new OptionSelectionScreen(this, "Select Label Color", List.of(COLORS), tooltips.get(selectedIdx).lCol, selected -> {
                    tooltips.get(selectedIdx).lCol = selected;
                }));
            }
        }).bounds(rightX, 60, halfBtnW, 20).build());
        
        valColBtn = addRenderableWidget(Button.builder(Component.empty(), b -> {
            if (selectedIdx >= 0) {
                this.minecraft.setScreen(new OptionSelectionScreen(this, "Select Value Color", List.of(COLORS), tooltips.get(selectedIdx).vCol, selected -> {
                    tooltips.get(selectedIdx).vCol = selected;
                }));
            }
        }).bounds(rightX + halfBtnW + 4, 60, halfBtnW, 20).build());

        prefixBox = new EditBox(this.font, rightX, 95, rightW, 16, Component.empty());
        prefixBox.setMaxLength(64);
        prefixBox.setResponder(s -> { if (selectedIdx >= 0 && !s.equals(tooltips.get(selectedIdx).prefix)) tooltips.get(selectedIdx).prefix = s; });
        addWidget(prefixBox);
        
        formatBox = new EditBox(this.font, rightX, 130, rightW, 16, Component.empty());
        formatBox.setMaxLength(64);
        formatBox.setResponder(s -> { if (selectedIdx >= 0 && !s.equals(tooltips.get(selectedIdx).format)) tooltips.get(selectedIdx).format = s; });
        addWidget(formatBox);

        autoFillBtn = addRenderableWidget(Button.builder(Component.literal("Auto-Fill Defaults & Colors"), b -> autoFill(true))
            .bounds(rightX, 155, rightW, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
            .bounds(rightX, this.height - 30, rightW / 2 - 5, 20).build());
        
        addRenderableWidget(Button.builder(Component.literal("Save"), b -> saveAndClose())
            .bounds(rightX + rightW / 2 + 5, this.height - 30, rightW / 2 - 5, 20).build());

        updatePanel();
    }

    private String cycleArray(String[] array, String current) {
        int dir = Screen.hasShiftDown() ? -1 : 1;
        int idx = 0;
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(current)) {
                idx = i;
                break;
            }
        }
        idx = (idx + dir + array.length) % array.length;
        return array[idx];
    }

    private void refreshList() {
        listWidget.children().clear();
        for (int i = 0; i < tooltips.size(); i++) {
            int index = i;
            listWidget.addEntry(new TooltipRow(tooltips.get(i), () -> { selectedIdx = index; updatePanel(); }));
        }
    }

    private void applyTypeDefaults(TooltipData d) {
        switch (d.type) {
            case "WEIGHT": d.prefix = "Weight: "; d.format = "%s"; break;
            case "ATTACK_COST": d.prefix = "Attack Cost: "; d.format = "%s"; break;
            case "BLOCK_COST": d.prefix = "Block Cost: "; d.format = "%1$s + (Dmg * %2$s)"; break;
            case "USE_COST": d.prefix = "Use Cost: "; d.format = "%s"; break;
            case "TICK_COST": d.prefix = "Active Cost: "; d.format = "%s/t"; break;
            case "MISSED_ATTACK_COST": d.prefix = "Miss Cost: "; d.format = "%s"; break;
            case "INSTANT_STAMINA": d.prefix = "Restores: "; d.format = "%s Stamina"; break;
            case "BONUS_STAMINA": d.prefix = "Bonus: "; d.format = "+%s Stamina"; break;
            case "ATTRIBUTE": d.prefix = "Buff: "; d.format = " ▪ %1$s %2$s (%3$ds)"; break;
            case "REGEN_MODIFIER": d.prefix = "Regen: "; d.format = "%1$s%2$.0f%% (%3$ds)"; break;
            case "CURES": d.prefix = "Cures: "; d.format = "%s"; break;
        }
    }

    private void autoFill(boolean updateColors) {
        if (selectedIdx < 0) return;
        TooltipData d = tooltips.get(selectedIdx);
        applyTypeDefaults(d);
        
        if (updateColors) {
            d.lCol = "DARK_GRAY"; d.vCol = "WHITE";
            if (d.type.equals("INSTANT_STAMINA")) d.vCol = "GREEN";
            if (d.type.equals("BONUS_STAMINA")) d.vCol = "GOLD";
            if (d.type.equals("ATTRIBUTE")) d.vCol = "AQUA";
            if (d.type.equals("REGEN_MODIFIER")) d.vCol = "YELLOW";
        }
        updatePanel();
    }

    private void updatePanel() {
        boolean active = selectedIdx >= 0 && selectedIdx < tooltips.size();
        typeBtn.active = active; placeBtn.active = active; labelColBtn.active = active; valColBtn.active = active; 
        prefixBox.setEditable(active); formatBox.setEditable(active); autoFillBtn.active = active;
        
        if (active) {
            TooltipData d = tooltips.get(selectedIdx);
            typeBtn.setMessage(Component.literal("Type: " + d.type));
            placeBtn.setMessage(Component.literal("Pos: " + d.place));
            labelColBtn.setMessage(Component.literal("L.Color: " + d.lCol));
            valColBtn.setMessage(Component.literal("V.Color: " + d.vCol));
            
            if (!prefixBox.isFocused()) prefixBox.setValue(d.prefix);
            if (!formatBox.isFocused()) formatBox.setValue(d.format);
        } else {
            typeBtn.setMessage(Component.literal("Type")); placeBtn.setMessage(Component.literal("Pos")); 
            labelColBtn.setMessage(Component.literal("L.Color")); valColBtn.setMessage(Component.literal("V.Color")); 
            prefixBox.setValue(""); formatBox.setValue("");
        }
    }

    private ChatFormatting getFormatting(String name, ChatFormatting fallback) {
        try { return ChatFormatting.valueOf(name.toUpperCase()); } 
        catch (Exception e) { return fallback; }
    }

    private Component buildPreview(TooltipData d) {
        ChatFormatting lFmt = getFormatting(d.lCol, ChatFormatting.DARK_GRAY);
        ChatFormatting vFmt = getFormatting(d.vCol, ChatFormatting.WHITE);
        String pfx = d.prefix.replace("\\t", "    ").replace("\\n", "\n");
        String fmt = d.format.replace("\\t", "    ").replace("\\n", "\n");

        MutableComponent prefixComp = Component.literal(pfx).withStyle(lFmt);
        MutableComponent valueComp;

        try {
            switch(d.type) {
                case "BLOCK_COST": valueComp = Component.literal(String.format(fmt, "1.5", "0.8")); break;
                case "REGEN_MODIFIER": valueComp = Component.literal(String.format(fmt, "+", 25.0f, 15)); break;
                case "ATTRIBUTE": valueComp = Component.literal(String.format(fmt, "+20%", "Max Health", 30)); break;
                default: valueComp = Component.literal(String.format(fmt, "5.0")); break;
            }
        } catch (Exception e) {
            valueComp = Component.literal(" [Format Error]").withStyle(ChatFormatting.RED);
        }
        return prefixComp.append(valueComp.withStyle(vFmt));
    }

    @SuppressWarnings("unchecked")
    private void saveAndClose() {
        List<String> toSave = new ArrayList<>();
        for (TooltipData d : tooltips) toSave.add(d.toString());
        ((net.minecraftforge.common.ForgeConfigSpec.ConfigValue<List<String>>)(Object)StaminaConfig.CLIENT.customTooltips).set(toSave);
        onClose();
    }

    @Override 
    public void onClose() { 
        this.minecraft.setScreen(parent); 
    }

    @Override 
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx);
        listWidget.render(gfx, mouseX, mouseY, partialTick);
        
        gfx.drawCenteredString(this.font, "Tooltip Studio", this.width / 2, 10, 0xFFFFAA00);
        
        if (selectedIdx >= 0 && selectedIdx < tooltips.size()) {
            gfx.drawString(this.font, "Prefix", rightX + 2, 84, 0xAAAAAA);
            gfx.drawString(this.font, "Format String", rightX + 2, 119, 0xAAAAAA);

            prefixBox.render(gfx, mouseX, mouseY, partialTick);
            formatBox.render(gfx, mouseX, mouseY, partialTick);

            TooltipData d = tooltips.get(selectedIdx);
            String guideText = "Use %s for the value.";
            if (d.type.equals("BLOCK_COST")) guideText = "Vars: %1$s = Base Cost, %2$s = Multiplier";
            else if (d.type.equals("REGEN_MODIFIER")) guideText = "Vars: %1$s = +/- Sign, %2$.0f = Percent, %3$d = Secs";
            else if (d.type.equals("ATTRIBUTE")) guideText = "Vars: %1$s = Value, %2$s = Stat Name, %3$d = Secs";
            
            String formatGuide = "Format: \\n creates a new line, \\t adds a tab indent";
            
            gfx.pose().pushPose();
            gfx.pose().scale(0.8f, 0.8f, 1.0f);
            gfx.drawString(this.font, guideText, (int)((rightX + 2) / 0.8f), (int)(177 / 0.8f), 0x55FF55);
            gfx.drawString(this.font, formatGuide, (int)((rightX + 2) / 0.8f), (int)(187 / 0.8f), 0x55FF55);
            gfx.pose().popPose();

            int previewY = 197;
            int previewH = this.height - 35 - previewY;

            if (previewH >= 25) {
                gfx.fill(rightX, previewY, rightX + rightW, previewY + previewH, 0xEE111111);
                gfx.renderOutline(rightX, previewY, rightW, previewH, 0xFF444444);
                gfx.drawString(this.font, "Preview:", rightX + 5, previewY + 5, 0x888888);

                Component previewComp = buildPreview(d);
                List<FormattedCharSequence> lines = this.font.split(previewComp, rightW - 10);
                int ly = previewY + 16;
                for (FormattedCharSequence line : lines) {
                    if (ly > previewY + previewH - this.font.lineHeight) break; 
                    gfx.drawString(this.font, line, rightX + 5, ly, 0xFFFFFF, true);
                    ly += this.font.lineHeight;
                }
            }
        }
        
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    private static class TooltipData {
        String type="WEIGHT", place="BOTTOM", lCol="DARK_GRAY", vCol="WHITE", prefix="Weight: ", format="%s";
        TooltipData(String config) {
            String[] p = config.split(";", -1);
            if (p.length > 0 && !p[0].isEmpty()) type = p[0].trim();
            if (p.length > 1 && !p[1].isEmpty()) place = p[1].trim();
            if (p.length > 2 && !p[2].isEmpty()) lCol = p[2].trim();
            if (p.length > 3 && !p[3].isEmpty()) vCol = p[3].trim();
            if (p.length > 4 && !p[4].isEmpty()) prefix = p[4].trim();
            if (p.length > 5 && !p[5].isEmpty()) format = p[5].trim();
        }
        @Override public String toString() { return type+";"+place+";"+lCol+";"+vCol+";"+prefix+";"+format; }
    }

    private class TooltipList extends ContainerObjectSelectionList<TooltipRow> {
        TooltipList(Minecraft mc, int w, int h, int t, int b, int ih) { super(mc, w, h, t, b, ih); }
        
        @Override 
        public int addEntry(TooltipRow e) { return super.addEntry(e); } 
        
        @Override public int getRowWidth() { return this.width - 20; }
        @Override protected int getScrollbarPosition() { return this.getLeft() + this.width - 10; }
    }

    private class TooltipRow extends ContainerObjectSelectionList.Entry<TooltipRow> {
        private final TooltipData data;
        private final Runnable onClick;
        
        TooltipRow(TooltipData data, Runnable onClick) { this.data = data; this.onClick = onClick; }
        
        @Override 
        public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {
            int bgColor = 0;
            if (tooltips.indexOf(data) == selectedIdx) {
                bgColor = 0x5500FF00;
            } else if (hover) {
                bgColor = 0x33FFFFFF;
            }
            
            if (bgColor != 0) {
                gfx.fill(left - 2, top - 2, left + w + 2, top + h - 2, bgColor);
            }
            
            gfx.drawString(Minecraft.getInstance().font, data.type, left + 4, top + 2, 0xFFFFFF);
            
            gfx.pose().pushPose();
            gfx.pose().scale(0.8f, 0.8f, 1.0f);
            gfx.drawString(Minecraft.getInstance().font, data.place, (int)((left + 4) / 0.8f), (int)((top + 12) / 0.8f), 0xAAAAAA);
            gfx.pose().popPose();
        }
        
        @Override public boolean mouseClicked(double mx, double my, int btn) { onClick.run(); return true; }
        @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
    }

    private String getFirstAvailableType() {
        for (String t : TYPES) {
            boolean used = false;
            for (TooltipData d : tooltips) {
                if (d.type.equals(t)) {
                    used = true;
                    break;
                }
            }
            if (!used) return t;
        }
        return null; // Returns null if all types are already configured
    }

    private class OptionSelectionScreen extends Screen {
        private final Screen parent;
        private final String titleText;
        private final List<String> options;
        private final String currentSelection;
        private final java.util.function.Consumer<String> onSelect;
        private SelectionList listWidget;

        public OptionSelectionScreen(Screen parent, String titleText, List<String> options, String currentSelection, java.util.function.Consumer<String> onSelect) {
            super(Component.literal(titleText));
            this.parent = parent;
            this.titleText = titleText;
            this.options = options;
            this.currentSelection = currentSelection;
            this.onSelect = onSelect;
        }

        @Override
        protected void init() {
            listWidget = new SelectionList(this.minecraft, this.width, this.height, 40, this.height - 40, 24);
            for (String opt : options) {
                listWidget.addEntry(new SelectionRow(opt, opt.equals(currentSelection), () -> {
                    onSelect.accept(opt);
                    this.minecraft.setScreen(parent); // Triggers parent's init() and updatePanel() upon return
                }));
            }
            addWidget(listWidget);
            addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> this.minecraft.setScreen(parent))
                .bounds(this.width / 2 - 75, this.height - 30, 150, 20).build());
        }

        @Override
        public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
            renderBackground(gfx);
            listWidget.render(gfx, mouseX, mouseY, partialTick);
            gfx.drawCenteredString(this.font, this.titleText, this.width / 2, 15, 0xFFFFAA00);
            super.render(gfx, mouseX, mouseY, partialTick);
        }
        
        private class SelectionList extends ContainerObjectSelectionList<SelectionRow> {
            SelectionList(Minecraft mc, int w, int h, int t, int b, int ih) { super(mc, w, h, t, b, ih); }
            
            @Override 
            public int addEntry(SelectionRow e) { return super.addEntry(e); } 
            
            @Override public int getRowWidth() { return 200; }
            @Override protected int getScrollbarPosition() { return this.width / 2 + 110; }
        }

        private class SelectionRow extends ContainerObjectSelectionList.Entry<SelectionRow> {
            private final String text;
            private final boolean isSelected;
            private final Runnable onClick;
            
            SelectionRow(String text, boolean isSelected, Runnable onClick) { 
                this.text = text; 
                this.isSelected = isSelected; 
                this.onClick = onClick; 
            }
            
            @Override 
            public void render(GuiGraphics gfx, int idx, int top, int left, int w, int h, int mX, int mY, boolean hover, float pt) {
                int bgColor = isSelected ? 0x5500FF00 : (hover ? 0x33FFFFFF : 0);
                if (bgColor != 0) {
                    gfx.fill(left - 2, top - 2, left + w + 2, top + h - 2, bgColor);
                }
                gfx.drawString(Minecraft.getInstance().font, text, left + 4, top + 2, 0xFFFFFF);
            }
            
            @Override public boolean mouseClicked(double mx, double my, int btn) { onClick.run(); return true; }
            @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
            @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
        }
    }
}