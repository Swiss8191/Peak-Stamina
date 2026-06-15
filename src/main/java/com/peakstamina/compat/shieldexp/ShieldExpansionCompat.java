package com.peakstamina.compat.shieldexp;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;
import java.lang.reflect.Method;

public class ShieldExpansionCompat {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean isShieldExpLoaded = false;
    private static Method getParryWindowMethod = null;

    public static void init() {
        if (ModList.get().isLoaded("shieldexp")) {
            isShieldExpLoaded = true;

            try {
                LOGGER.info("Peak Stamina: Attempting to reflect Shield Expansion parry window...");
                getParryWindowMethod = Player.class.getMethod("getParryWindow");
                LOGGER.info("Peak Stamina: Successfully accessed Shield Expansion method.");
            } catch (Exception e) {
                LOGGER.warn("Peak Stamina: Failed to initialize Shield Expansion compat reflection.", e);
                isShieldExpLoaded = false;
            }
        }
    }

    public static boolean isLoaded() {
        return isShieldExpLoaded;
    }

    public static boolean isParrying(Player player) {
        if (!isShieldExpLoaded || getParryWindowMethod == null) return false;

        try {
            int window = (int) getParryWindowMethod.invoke(player);
            return window > 0;
        } catch (Exception ignored) {
            return false;
        }
    }
}