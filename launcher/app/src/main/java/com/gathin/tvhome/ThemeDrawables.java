package com.gathin.tvhome;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

/**
 * Theme-aware surfaces. Focused controls use accent-tinted dark fills — never near-white
 * backgrounds that collide with white labels (the Icon Style white-on-white bug).
 */
final class ThemeDrawables {

    private ThemeDrawables() { }

    static StateListDrawable pill(VisualTheme t, float density) {
        StateListDrawable s = new StateListDrawable();
        // Focused: dark plate + accent rim (readable with white or near-white text).
        s.addState(new int[]{android.R.attr.state_focused},
                round(mix(t.background, t.accent, 0.22f) | 0xEE000000, t.accent, 21f * density, 2f * density));
        s.addState(new int[]{android.R.attr.state_activated},
                round(t.accentSoft, t.accent, 21f * density, 1.5f * density));
        s.addState(new int[]{},
                round(t.pillFill, t.pillStroke, 21f * density, 1f * density));
        return s;
    }

    /** ColourStateList for labels/icons on themed pills: bright on focus, never dark-on-dark. */
    static ColorStateList pillText(VisualTheme t) {
        return new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_focused},
                        new int[]{android.R.attr.state_activated},
                        new int[]{}
                },
                new int[]{0xFFFFFFFF, t.accent, t.textPrimary});
    }

    static StateListDrawable card(VisualTheme t, float density) {
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused},
                round(t.cardFillFocus, t.cardStrokeFocus, 18f * density, 2.5f * density));
        s.addState(new int[]{},
                round(t.cardFill, t.cardStroke, 18f * density, 1.25f * density));
        return s;
    }

    static GradientDrawable datePill(VisualTheme t, float density) {
        return round(t.datePillFill, t.datePillStroke, 999f * density, 1.25f * density);
    }

    static GradientDrawable themePreviewCard(VisualTheme t, float density, boolean focused) {
        return round(focused ? t.cardFillFocus : t.cardFill,
                focused ? t.accent : t.cardStroke,
                18f * density, focused ? 3f * density : 1.25f * density);
    }

    /** App tile plate: dark / glass / tinted surfaces for icon styles. */
    static GradientDrawable tilePlate(VisualTheme t, VisualTheme.IconStyle style, float density, boolean focused) {
        int fill;
        int stroke;
        switch (style) {
            case GLASS:
                fill = mix(0xFF1A1E28, t.accent, 0.12f) & 0x00FFFFFF | 0xB3000000;
                stroke = focused ? t.accent : (0x55FFFFFF);
                break;
            case DARK:
                fill = 0xFF12141A;
                stroke = focused ? t.accent : 0x33FFFFFF;
                break;
            case MONOCHROME:
                fill = 0xFF16181E;
                stroke = focused ? 0xFFD0D4DC : 0x44FFFFFF;
                break;
            case THEME_TINTED:
                fill = mix(0xFF101018, t.accent, 0.35f) | 0xFF000000;
                stroke = focused ? t.accent : t.accentSoft;
                break;
            case ORIGINAL:
            default:
                fill = t.tileFill;
                stroke = focused ? t.accent : t.tileStroke;
                break;
        }
        return round(fill, stroke, 16f * density, focused ? 2.5f * density : 1.25f * density);
    }

    static GradientDrawable iconGlassPlate(int accent, float density) {
        return round(0xB3181C24, (accent & 0x00FFFFFF) | 0x66000000, 14f * density, 1.25f * density);
    }

    static int mix(int base, int tint, float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        int br = (base >> 16) & 0xFF, bg = (base >> 8) & 0xFF, bb = base & 0xFF;
        int tr = (tint >> 16) & 0xFF, tg = (tint >> 8) & 0xFF, tb = tint & 0xFF;
        int r = Math.round(br + (tr - br) * amount);
        int g = Math.round(bg + (tg - bg) * amount);
        int b = Math.round(bb + (tb - bb) * amount);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    private static GradientDrawable round(int fill, int stroke, float radius, float strokeW) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.RECTANGLE);
        g.setColor(fill);
        g.setCornerRadius(radius);
        g.setStroke(Math.max(1, Math.round(strokeW)), stroke);
        return g;
    }
}
