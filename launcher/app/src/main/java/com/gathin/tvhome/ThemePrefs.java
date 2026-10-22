package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

/** Persistent visual theme + icon style. Independent of dock/widget prefs and config.json pushes. */
final class ThemePrefs {

    private static final String PREFS = "visual_theme";
    private static final String KEY_THEME = "theme_id";
    private static final String KEY_ICON = "icon_style";
    private static final String KEY_PREV = "previous_theme_id";
    private static final String KEY_ANIM_BG = "animate_backdrop";

    private ThemePrefs() { }

    static SharedPreferences prefs(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static VisualTheme load(Context ctx) {
        return VisualTheme.byId(prefs(ctx).getString(KEY_THEME, VisualTheme.ALL[0].id));
    }

    static VisualTheme.IconStyle loadIconStyle(Context ctx) {
        return VisualTheme.iconStyleById(prefs(ctx).getString(KEY_ICON, VisualTheme.IconStyle.ORIGINAL.name()));
    }

    static boolean animateBackdrop(Context ctx) {
        return prefs(ctx).getBoolean(KEY_ANIM_BG, false);
    }

    static void save(Context ctx, VisualTheme theme) {
        SharedPreferences p = prefs(ctx);
        String current = p.getString(KEY_THEME, VisualTheme.ALL[0].id);
        p.edit()
                .putString(KEY_PREV, current)
                .putString(KEY_THEME, theme.id)
                .apply();
    }

    static void saveIconStyle(Context ctx, VisualTheme.IconStyle style) {
        prefs(ctx).edit().putString(KEY_ICON, style.name()).apply();
    }

    static void saveAnimateBackdrop(Context ctx, boolean on) {
        prefs(ctx).edit().putBoolean(KEY_ANIM_BG, on).apply();
    }

    static VisualTheme previous(Context ctx) {
        return VisualTheme.byId(prefs(ctx).getString(KEY_PREV, VisualTheme.ALL[0].id));
    }
}
