package com.peakstamina.client.gui;

import com.peakstamina.capabilities.StaminaCapability;
import com.peakstamina.config.StaminaConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class ActiveBuffsScreen extends Screen {
    private final Screen parentInventory;
    private final int panelWidth = 176;
    private final int panelHeight = 166;
    private int panelX, panelY;
    private double scrollAmount = 0;
    private int maxScrollable = 0;

    public ActiveBuffsScreen(Screen parentInventory) {
        super(Component.literal("Active Buffs"));
        this.parentInventory = parentInventory;
    }

    @Override
    protected void init() {
        super.init();
        panelX = this.width / 2 - panelWidth / 2;
        panelY = this.height / 2 - panelHeight / 2;

        this.addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("Close"), b -> this.onClose())
                .bounds(panelX + panelWidth / 2 - 30, panelY + panelHeight - 22, 60, 16).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx);

        // Vanilla UI Container Background
        gfx.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xFFC6C6C6);
        gfx.fill(panelX, panelY, panelX + panelWidth, panelY + 2, 0xFFFFFFFF); // Top
        gfx.fill(panelX, panelY, panelX + 2, panelY + panelHeight, 0xFFFFFFFF); // Left
        gfx.fill(panelX + panelWidth - 2, panelY, panelX + panelWidth, panelY + panelHeight, 0xFF555555); // Right
        gfx.fill(panelX, panelY + panelHeight - 2, panelX + panelWidth, panelY + panelHeight, 0xFF555555); // Bottom

        // Title
        gfx.drawString(this.font, "Active Buffs", panelX + 8, panelY + 6, 0x404040, false);

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.player.getCapability(StaminaCapability.INSTANCE).ifPresent(cap -> {
            var buffs = cap.activeBuffs;
            if (buffs == null || buffs.isEmpty()) {
                gfx.drawCenteredString(this.font, "No active buffs.", panelX + panelWidth / 2, panelY + panelHeight / 2 - 10, 0xFF555555);
                maxScrollable = 0;
                return;
            }

            // Group Buffs by Attribute Name
            Map<String, List<StaminaCapability.BuffInstance>> groupedBuffs = new LinkedHashMap<>();
            for (var buff : buffs) {
                groupedBuffs.computeIfAbsent(buff.attributeName, k -> new ArrayList<>()).add(buff);
            }

            int listTop = panelY + 18;
            int listBottom = panelY + panelHeight - 26;
            
            int headerHeight = 16;
            int itemHeight = 10;
            float scale = 0.8f;
            boolean showIcons = StaminaConfig.CLIENT.activeBuffsShowIcons.get();

            gfx.enableScissor(panelX + 4, listTop, panelX + panelWidth - 4, listBottom);

            int yOffset = listTop + 2 - (int) scrollAmount;

            for (Map.Entry<String, List<StaminaCapability.BuffInstance>> entry : groupedBuffs.entrySet()) {
                String rawName = entry.getKey();
                List<StaminaCapability.BuffInstance> group = entry.getValue();

                // Check visibility for Header
                if (yOffset + headerHeight >= listTop && yOffset <= listBottom) {
                    // Find the most recent source item to use as the icon
                    String source = "";
                    for (int j = group.size() - 1; j >= 0; j--) {
                        if (group.get(j).sourceItem != null && !group.get(j).sourceItem.isEmpty()) {
                            source = group.get(j).sourceItem;
                            break;
                        }
                    }

                    Item sourceItem = Items.AIR;
                    if (!source.isEmpty()) {
                        ResourceLocation loc = ResourceLocation.tryParse(source);
                        if (loc != null && ForgeRegistries.ITEMS.containsKey(loc)) {
                            sourceItem = ForgeRegistries.ITEMS.getValue(loc);
                        }
                    }
                    ItemStack renderStack = new ItemStack(sourceItem);
                    boolean hasItem = !renderStack.isEmpty() && renderStack.getItem() != Items.AIR;

                    // Pull proper localization key
                    String cleanName = rawName;
                    ResourceLocation attrLoc = ResourceLocation.tryParse(rawName);
                    if (attrLoc != null && ForgeRegistries.ATTRIBUTES.containsKey(attrLoc)) {
                        Attribute attr = ForgeRegistries.ATTRIBUTES.getValue(attrLoc);
                        if (attr != null) {
                            cleanName = Component.translatable(attr.getDescriptionId()).getString();
                        }
                    } else {
                        cleanName = Component.translatable("attribute.name." + rawName.replace(":", ".")).getString();
                    }

                    int textXOffset = panelX + 8;
                    if (showIcons && hasItem) {
                        gfx.pose().pushPose();
                        gfx.pose().translate(panelX + 6, yOffset + 1, 0);
                        gfx.pose().scale(scale, scale, 1.0f);
                        gfx.renderItem(renderStack, 0, 0);
                        gfx.pose().popPose();

                        textXOffset = panelX + 22;
                    }

                    gfx.pose().pushPose();
                    gfx.pose().translate(textXOffset, yOffset + 4, 0);
                    gfx.pose().scale(scale, scale, 1.0f);
                    gfx.drawString(this.font, cleanName, 0, 0, 0xFF222222, false); 
                    gfx.pose().popPose();
                }

                yOffset += headerHeight;

                // Render each individual operation modifier under the header
                for (var buff : group) {
                    if (yOffset + itemHeight >= listTop && yOffset <= listBottom) {
                        boolean isDebuff = buff.amount < 0;
                        java.util.List<? extends String> customInverted = StaminaConfig.CLIENT.invertedTooltipAttributes.get();
                        if (customInverted != null) {
                            for (String custom : customInverted) {
                                if (buff.attributeName.equalsIgnoreCase(custom.trim())) {
                                    isDebuff = !isDebuff;
                                    break;
                                }
                            }
                        }

                        String sign = buff.amount > 0 ? "+" : "";
                        String opSuffix = "";
                        if (buff.operation == 1) opSuffix = " Base";
                        if (buff.operation == 2) opSuffix = " Total";

                        String formattedAmount = (buff.operation == 1 || buff.operation == 2) 
                                ? String.format("%s%.0f%%%s", sign, buff.amount * 100, opSuffix) 
                                : String.format("%s%.1f", sign, buff.amount);

                        int seconds = buff.durationTicks / 20;
                        String timeStr = String.format("(%d:%02d)", seconds / 60, seconds % 60);

                        int textColor = isDebuff ? 0xFFFF3333 : 0xFF228822;
                        int timerColor = 0xFF777777; 

                        gfx.pose().pushPose();
                        int xIndent = showIcons ? panelX + 22 : panelX + 12;
                        gfx.pose().translate(xIndent, yOffset + 1, 0);
                        gfx.pose().scale(scale, scale, 1.0f);
                        
                        String prefix = "▪ " + formattedAmount + " ";
                        gfx.drawString(this.font, prefix, 0, 0, textColor, false);
                        
                        int prefixWidth = this.font.width(prefix);
                        gfx.drawString(this.font, timeStr, prefixWidth, 0, timerColor, false);
                        
                        gfx.pose().popPose();
                    }
                    yOffset += itemHeight;
                }
            }

            gfx.disableScissor();

            // Scrollbar Logic
            int totalContentHeight = (groupedBuffs.size() * headerHeight) + (buffs.size() * itemHeight);
            int visibleHeight = listBottom - listTop;
            maxScrollable = Math.max(0, totalContentHeight - visibleHeight);
            
            if (maxScrollable > 0) {
                int scrollbarX = panelX + panelWidth - 7;
                int thumbHeight = Math.max(10, (int) ((visibleHeight / (float) totalContentHeight) * visibleHeight));
                int thumbY = listTop + (int) ((scrollAmount / maxScrollable) * (visibleHeight - thumbHeight));

                gfx.fill(scrollbarX, listTop, scrollbarX + 3, listBottom, 0xFF555555);
                gfx.fill(scrollbarX, thumbY, scrollbarX + 3, thumbY + thumbHeight, 0xFFFFFFFF);
            }
        });

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (maxScrollable > 0) {
            scrollAmount -= delta * 12; 
            scrollAmount = Math.max(0, Math.min(scrollAmount, maxScrollable));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_E) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentInventory);
    }
}