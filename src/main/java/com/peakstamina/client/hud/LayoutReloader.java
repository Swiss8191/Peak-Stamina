package com.peakstamina.client.hud;

import com.google.gson.Gson;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import java.io.InputStreamReader;
import java.io.Reader;

public class LayoutReloader implements ResourceManagerReloadListener {
    private static final ResourceLocation LAYOUT_ID = new ResourceLocation("peakstamina", "gui_layout.json");
    private static final Gson GSON = new Gson();

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
        try {
            manager.getResource(LAYOUT_ID).ifPresent(resource -> {
                try (Reader reader = new InputStreamReader(resource.open())) {
                    StaminaLayout.Data loadedData = GSON.fromJson(reader, StaminaLayout.Data.class);
                    if (loadedData != null) {
                        
                        // Just directly assigns the data without zero-base shifting
                        StaminaLayout.INSTANCE = loadedData;
                        
                    }
                } catch (Exception e) {
                    System.out.println("[PeakStamina] Failed to parse gui_layout.json! Reverting to defaults.");
                }
            });
        } catch (Exception e) {
            System.out.println("[PeakStamina] gui_layout.json not found! Using default layout.");
        }
    }
}