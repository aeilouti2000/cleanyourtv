package com.gathin.tvhome;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Premium TV theme browser. Focused controls use accent-rimmed dark fills + white text
 * (never white-on-white). Preview is live; Apply persists; Revert restores; Back closes.
 */
final class ThemePickerDialog {

    interface Host {
        void previewTheme(VisualTheme theme);
        void applyTheme(VisualTheme theme);
        void revertTheme();
        VisualTheme currentTheme();
        VisualTheme.IconStyle iconStyle();
        void setIconStyle(VisualTheme.IconStyle style);
        boolean animateBackdrop();
        void setAnimateBackdrop(boolean on);
    }

    private ThemePickerDialog() { }

    static void show(MainActivity activity, Host host) {
        float d = activity.getResources().getDisplayMetrics().density;
        VisualTheme shell = host.currentTheme();
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(activity);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(d, 32), dp(d, 24), dp(d, 32), dp(d, 24));
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(0xF2101218);
        panel.setCornerRadius(22f * d);
        panel.setStroke(Math.round(2 * d), shell.accent);
        root.setBackground(panel);

        TextView title = label(activity, "Themes", 24, 0xFFFFFFFF, true);
        TextView hint = label(activity,
                "← → preview   ·   OK apply   ·   Back close", 13, 0x99FFFFFF, false);
        hint.setPadding(0, dp(d, 6), 0, dp(d, 16));
        root.addView(title);
        root.addView(hint);

        HorizontalScrollView scroll = new HorizontalScrollView(activity);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        LinearLayout row = new LinearLayout(activity);
        row.setOrientation(LinearLayout.HORIZONTAL);
        scroll.addView(row);

        final VisualTheme[] selected = {host.currentTheme()};
        View focusCard = null;
        for (VisualTheme theme : VisualTheme.ALL) {
            View card = themeCard(activity, theme, d, theme.id.equals(selected[0].id));
            card.setTag(theme);
            card.setFocusable(true);
            card.setClickable(true);
            card.setOnFocusChangeListener((v, hasFocus) -> {
                VisualTheme t = (VisualTheme) v.getTag();
                v.setBackground(ThemeDrawables.themePreviewCard(t, d, hasFocus));
                if (hasFocus) {
                    selected[0] = t;
                    host.previewTheme(t);
                    scroll.post(() -> scroll.smoothScrollTo(Math.max(0, v.getLeft() - dp(d, 40)), 0));
                }
            });
            card.setOnClickListener(v -> {
                host.applyTheme((VisualTheme) v.getTag());
                dialog.dismiss();
            });
            card.setOnKeyListener((v, code, event) -> {
                if (event.getAction() != KeyEvent.ACTION_UP) return false;
                if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER) {
                    host.applyTheme((VisualTheme) v.getTag());
                    dialog.dismiss();
                    return true;
                }
                return false;
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(d, 240), dp(d, 168));
            lp.setMarginEnd(dp(d, 14));
            row.addView(card, lp);
            if (theme.id.equals(host.currentTheme().id)) focusCard = card;
            if (focusCard == null) focusCard = card;
        }
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout actions = new LinearLayout(activity);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.setPadding(0, dp(d, 18), 0, 0);
        TextView apply = actionBtn(activity, "Apply", d, shell);
        TextView revert = actionBtn(activity, "Revert", d, shell);
        TextView icons = actionBtn(activity, "Icon: " + VisualTheme.iconStyleLabel(host.iconStyle()), d, shell);
        TextView motion = actionBtn(activity, host.animateBackdrop() ? "Motion: On" : "Motion: Off", d, shell);

        apply.setOnClickListener(v -> {
            host.applyTheme(selected[0]);
            dialog.dismiss();
        });
        revert.setOnClickListener(v -> {
            host.revertTheme();
            dialog.dismiss();
        });
        icons.setOnClickListener(v -> {
            VisualTheme.IconStyle[] all = VisualTheme.IconStyle.values();
            VisualTheme.IconStyle next = all[(host.iconStyle().ordinal() + 1) % all.length];
            host.setIconStyle(next);
            icons.setText("Icon: " + VisualTheme.iconStyleLabel(next));
            Toast.makeText(activity, "Icon style: " + VisualTheme.iconStyleLabel(next), Toast.LENGTH_SHORT).show();
        });
        motion.setOnClickListener(v -> {
            boolean next = !host.animateBackdrop();
            host.setAnimateBackdrop(next);
            motion.setText(next ? "Motion: On" : "Motion: Off");
            host.previewTheme(selected[0]);
        });

        actions.addView(apply);
        actions.addView(gap(activity, d));
        actions.addView(revert);
        actions.addView(gap(activity, d));
        actions.addView(icons);
        actions.addView(gap(activity, d));
        actions.addView(motion);
        root.addView(actions);

        dialog.setContentView(root);
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
        }
        dialog.setOnCancelListener(di -> host.revertTheme());
        dialog.show();
        if (focusCard != null) focusCard.requestFocus();
    }

    private static View themeCard(MainActivity activity, VisualTheme theme, float d, boolean selected) {
        LinearLayout card = new LinearLayout(activity);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(d, 12), dp(d, 12), dp(d, 12), dp(d, 12));
        card.setBackground(ThemeDrawables.themePreviewCard(theme, d, selected));

        ThemeBackdropView preview = new ThemeBackdropView(activity, null);
        preview.setTheme(theme, false);
        LinearLayout.LayoutParams prevLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(d, 72));
        card.addView(preview, prevLp);

        // Mini tile row hinting icon treatment
        LinearLayout tiles = new LinearLayout(activity);
        tiles.setOrientation(LinearLayout.HORIZONTAL);
        tiles.setPadding(0, dp(d, 8), 0, 0);
        for (int i = 0; i < 3; i++) {
            View chip = new View(activity);
            chip.setBackground(ThemeDrawables.tilePlate(theme, theme.defaultIconStyle, d, i == 0));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(dp(d, 36), dp(d, 22));
            clp.setMarginEnd(dp(d, 6));
            tiles.addView(chip, clp);
        }
        card.addView(tiles);

        TextView name = label(activity, theme.name, 15, 0xFFFFFFFF, true);
        name.setPadding(0, dp(d, 8), 0, 0);
        TextView desc = label(activity, theme.description, 11, 0xAAFFFFFF, false);
        desc.setMaxLines(2);
        card.addView(name);
        card.addView(desc);
        return card;
    }

    private static TextView actionBtn(MainActivity activity, String text, float d, VisualTheme theme) {
        TextView t = new TextView(activity);
        t.setText(text);
        t.setTextSize(14);
        t.setTypeface(activity.getResources().getFont(R.font.outfit_medium));
        t.setPadding(dp(d, 18), dp(d, 12), dp(d, 18), dp(d, 12));
        t.setBackground(ThemeDrawables.pill(theme, d));
        // Critical: focused = white text on dark accent plate (never dark text / white fill).
        t.setTextColor(ThemeDrawables.pillText(theme));
        t.setFocusable(true);
        t.setClickable(true);
        t.setOnFocusChangeListener((v, hasFocus) -> {
            // Keep drawable in sync; ColorStateList handles text.
            v.setBackground(ThemeDrawables.pill(theme, d));
        });
        return t;
    }

    private static View gap(MainActivity activity, float d) {
        View v = new View(activity);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(d, 10), 1));
        return v;
    }

    private static TextView label(MainActivity activity, String text, int sp, int color, boolean medium) {
        TextView t = new TextView(activity);
        t.setText(text);
        t.setTextColor(color);
        t.setTextSize(sp);
        t.setTypeface(activity.getResources().getFont(medium ? R.font.outfit_medium : R.font.outfit_regular));
        return t;
    }

    private static int dp(float density, int v) {
        return Math.round(v * density);
    }
}
