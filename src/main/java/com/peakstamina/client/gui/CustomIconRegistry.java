package com.peakstamina.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.peakstamina.peakStaminaMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.util.*;

public class CustomIconRegistry {

    public static final int CANVAS_SIZE = 16;

    public static class CustomIcon {
        public final String name;
        public final String desc;
        public final byte[] pixels;

        public CustomIcon(String name, String desc, byte[] pixels) {
            this.name = name;
            this.desc = desc;
            this.pixels = pixels;
        }

        public String serialize() {
            return name + ";" + desc + ";" + Base64.getEncoder().encodeToString(pixels);
        }

        public static CustomIcon deserialize(String entry) {
            String[] parts = entry.split(";", 3);
            if (parts.length < 3) return null;
            String name = parts[0].trim();
            String desc = parts[1].trim();
            if (name.isEmpty()) return null;
            try {
                byte[] pixels = Base64.getDecoder().decode(parts[2].trim());
                if (pixels.length != CANVAS_SIZE * CANVAS_SIZE) return null;
                return new CustomIcon(name, desc, pixels);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private static final Map<String, CustomIcon> icons = new LinkedHashMap<>();
    private static final Map<String, DynamicTexture> textures = new HashMap<>();
    private static final Map<String, ResourceLocation> textureLocations = new HashMap<>();

    private static final java.io.File CONFIG_FILE = new java.io.File("config/peakstamina/custom_icons.json");
    private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();

    public static List<CustomIcon> getAllIcons() {
        return new ArrayList<>(icons.values());
    }

    public static boolean hasIcon(String name) {
        return icons.containsKey(name);
    }

    public static void reload() {
        for (DynamicTexture tex : textures.values()) tex.close();
        textures.clear();
        textureLocations.clear();
        icons.clear();

        if (!CONFIG_FILE.exists()) {
            java.util.Base64.Decoder decoder = java.util.Base64.getDecoder();
            icons.put("Fatigue", new CustomIcon("Fatigue", "Exhaustion Icon", decoder.decode("AAAAAAAAAAAA/////wAAAAAAAAAAAAAAAAAAAP8AAAAAAAAAAAAAAAAAAP8AAAAAAAAAAAAAAAAAAP8AAAAAAAAAAP//////AP8AAAAAAAAAAAAAAAAA/wD/////AAAAAAAAAAAA/wAAAAAAAAAAAAAAAAAA/wAAAAAAAAAAAAAAAAAA/wAAAAAAAAAAAAAAAAAA/wAAAAAAAAAAAAAAAAAAAP//////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==")));
            icons.put("Hunger", new CustomIcon("Hunger", "Hunger Icon", decoder.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP//AAAAAAAAAAAAAAAAAP8AAP8AAAAAAAAAAAAAAP8A/wD//wAAAAAAAAAAAAD/AAD/////AAAAAAAAAAAAAP///////wAAAAAAAAAAAAAA//////8AAAAAAAAAAAAAAAD///8A/wAAAAAAAAAAAAAAAAAA//8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==")));
            icons.put("Poison", new CustomIcon("Poison", "Poison Icon", decoder.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAP//////AAAAAAAAAAAAAP////////8AAAAAAAAAAAD/AAD/AAD/AAAAAAAAAAAA/wAA/wAA/wAAAAAAAAAAAP///wD///8AAAAAAAAAAAAA//////8AAAAAAAAAAAAAAP8A/wD/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==")));
            icons.put("Weight", new CustomIcon("Weight", "Weight Icon", decoder.decode("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD//wAAAAAAAP//AAAAAAD///8AAAAAAAD///8AAAAA////////////////AAAAAP///////////////wAAAAD///8AAAAAAAD///8AAAAAAP//AAAAAAAA//8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==")));
            
            saveToFile();
            return;
        }

        try (java.io.FileReader reader = new java.io.FileReader(CONFIG_FILE)) {
            com.google.gson.JsonArray array = com.google.gson.JsonParser.parseReader(reader).getAsJsonArray();
            for (com.google.gson.JsonElement elem : array) {
                com.google.gson.JsonObject obj = elem.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String desc = obj.has("desc") ? obj.get("desc").getAsString() : "";
                byte[] pixels = java.util.Base64.getDecoder().decode(obj.get("pixels").getAsString());
                
                if (pixels.length == CANVAS_SIZE * CANVAS_SIZE) {
                    icons.put(name, new CustomIcon(name, desc, pixels));
                }
            }
        } catch (Exception e) {
            System.err.println("[PeakStamina] Failed to load custom icons from JSON: " + e.getMessage());
        }
    }

    public static void saveToFile() {
        com.google.gson.JsonArray array = new com.google.gson.JsonArray();
        for (CustomIcon icon : icons.values()) {
            com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
            obj.addProperty("name", icon.name);
            obj.addProperty("desc", icon.desc);
            obj.addProperty("pixels", java.util.Base64.getEncoder().encodeToString(icon.pixels));
            array.add(obj);
        }

        try {
            if (CONFIG_FILE.getParentFile() != null) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (java.io.FileWriter writer = new java.io.FileWriter(CONFIG_FILE)) {
                GSON.toJson(array, writer);
            }
        } catch (Exception e) {
            System.err.println("[PeakStamina] Failed to save custom icons to JSON: " + e.getMessage());
        }
    }

    public static void putIcon(CustomIcon icon) {
        if (textures.containsKey(icon.name)) {
            textures.get(icon.name).close();
            textures.remove(icon.name);
            textureLocations.remove(icon.name);
        }
        icons.put(icon.name, icon);
        saveToFile();
    }

    public static void removeIcon(String name) {
        icons.remove(name);
        if (textures.containsKey(name)) {
            textures.get(name).close();
            textures.remove(name);
            textureLocations.remove(name);
        }
        saveToFile();
    }

    private static ResourceLocation getOrUploadTexture(String name) {
        if (textureLocations.containsKey(name)) return textureLocations.get(name);
        CustomIcon icon = icons.get(name);
        if (icon == null) return null;

        NativeImage img = new NativeImage(NativeImage.Format.RGBA, CANVAS_SIZE, CANVAS_SIZE, true);
        for (int row = 0; row < CANVAS_SIZE; row++) {
            for (int col = 0; col < CANVAS_SIZE; col++) {
                int idx = row * CANVAS_SIZE + col;
                if (idx < icon.pixels.length && icon.pixels[idx] != 0) {
                    img.setPixelRGBA(col, row, 0xFFFFFFFF);
                } else {
                    boolean hasNeighbor = false;
                    for (int oy = -1; oy <= 1; oy++) {
                        for (int ox = -1; ox <= 1; ox++) {
                            if (ox == 0 && oy == 0) continue;
                            int nx = col + ox, ny = row + oy;
                            if (nx >= 0 && nx < CANVAS_SIZE && ny >= 0 && ny < CANVAS_SIZE) {
                                if (icon.pixels[ny * CANVAS_SIZE + nx] != 0) {
                                    hasNeighbor = true; break;
                                }
                            }
                        }
                        if (hasNeighbor) break;
                    }
                    img.setPixelRGBA(col, row, hasNeighbor ? 0x88000000 : 0x00000000);
                }
            }
        }

        DynamicTexture tex = new DynamicTexture(img);
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(peakStaminaMod.MODID, "custom_icon/" + name.toLowerCase(java.util.Locale.ROOT));
        Minecraft.getInstance().getTextureManager().register(loc, tex);
        textures.put(name, tex);
        textureLocations.put(name, loc);
        return loc;
    }

    public static void drawIcon(GuiGraphics gfx, int x, int y, int w, int h,
                                String iconName, int color, float alpha) {
        ResourceLocation loc = getOrUploadTexture(iconName);
        if (loc == null) return;

        int a = (int)(((color >> 24) & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8)  & 0xFF;
        int b =  color        & 0xFF;

        RenderSystem.setShaderTexture(0, loc);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = gfx.pose().last().pose();
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder buf = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        buf.addVertex(matrix, x,     y,     0).setColor(r, g, b, a).setUv(0, 0);
        buf.addVertex(matrix, x,     y + h, 0).setColor(r, g, b, a).setUv(0, 1);
        buf.addVertex(matrix, x + w, y + h, 0).setColor(r, g, b, a).setUv(1, 1);
        buf.addVertex(matrix, x + w, y,     0).setColor(r, g, b, a).setUv(1, 0);

        BufferUploader.drawWithShader(buf.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawPreview(GuiGraphics gfx, byte[] pixels, int x, int y,
                                   int cellSize, int color) {
        int argb = 0xFF000000 | (color & 0xFFFFFF);
        for (int row = 0; row < CANVAS_SIZE; row++) {
            for (int col = 0; col < CANVAS_SIZE; col++) {
                int idx = row * CANVAS_SIZE + col;
                if (idx < pixels.length && pixels[idx] != 0) {
                    gfx.fill(x + col * cellSize, y + row * cellSize,
                             x + col * cellSize + cellSize, y + row * cellSize + cellSize,
                             argb);
                }
            }
        }
    }
}