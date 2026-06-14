package com.peakstamina.client.hud;

public class StaminaLayout {
    public static Data INSTANCE = new Data();

    public static class Data {
        public UVRect empty_left = new UVRect(30, 33, 4, 8);
        public UVRect empty_mid = new UVRect(39, 33, 1, 8);
        public UVRect empty_right = new UVRect(45, 33, 4, 8);

        public UVRect fill_left = new UVRect(59, 33, 4, 8);
        public UVRect fill_mid = new UVRect(68, 33, 1, 8);
        public UVRect fill_right = new UVRect(74, 33, 4, 8);

        public UVRect bonus_fill_left = new UVRect(88, 33, 4, 8);
        public UVRect bonus_fill_mid = new UVRect(97, 33, 1, 8);
        public UVRect bonus_fill_right = new UVRect(103, 33, 4, 8);

        public UVRect single_stripe = new UVRect(30, 52, 10, 4);
        public UVRect penalty_sep = new UVRect(51, 52, 2, 4);
        public UVRect bonus_sep = new UVRect(63, 52, 2, 4);
        
        public UVRect icon_empty = new UVRect(27, 66, 16, 20);
        public UVRect icon_full = new UVRect(53, 66, 16, 20);
        public UVRect icon_bonus_full = new UVRect(79, 66, 16, 20);

        public UVRect icon_penalty = new UVRect(105, 68, 10, 16);
        public UVRect icon_penalty_sep = new UVRect(130, 68, 2, 16);
        public UVRect icon_bonus_sep = new UVRect(142, 68, 2, 16);

        public UVRect regen_pos_1 = new UVRect(27, 96, 4, 8);
        public UVRect regen_pos_2 = new UVRect(27, 106, 10, 8);
        public UVRect regen_pos_3 = new UVRect(27, 116, 16, 8);
        public UVRect regen_neg_1 = new UVRect(27, 126, 4, 8);
        public UVRect regen_neg_2 = new UVRect(27, 136, 10, 8);
        public UVRect regen_neg_3 = new UVRect(27, 146, 16, 8);
    }

    public static class UVRect {
        public int u, v, w, h;
        
        public UVRect() {} 
        
        public UVRect(int u, int v, int w, int h) {
            this.u = u; this.v = v; this.w = w; this.h = h;
        }
    }
}