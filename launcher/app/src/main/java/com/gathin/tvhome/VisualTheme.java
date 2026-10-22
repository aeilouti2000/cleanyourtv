package com.gathin.tvhome;

/**
 * In-launcher visual theme tokens. Separate from config.json push themes: these only change
 * colours, backdrop style, focus glow and icon presentation — never dock order or widgets.
 */
final class VisualTheme {

    enum Backdrop {
        MIDNIGHT_LINES,
        LIQUID_NAVY,
        AURORA,
        CRIMSON,
        GALAXY,
        AMBER_SUNSET,
        FOREST,
        GRAPHITE
    }

    enum IconStyle {
        ORIGINAL,
        DARK,
        MONOCHROME,
        THEME_TINTED,
        GLASS
    }

    final String id;
    final String name;
    final String description;
    final Backdrop backdrop;
    final int background;
    final int backgroundEnd;
    final int accent;
    final int accentSoft;
    final int textPrimary;
    final int textSecondary;
    final int cardFill;
    final int cardFillFocus;
    final int cardStroke;
    final int cardStrokeFocus;
    final int pillFill;
    final int pillFillFocus;
    final int pillStroke;
    final int datePillFill;
    final int datePillStroke;
    final int dockBand;
    final int dockEdge;
    final int amPm;
    final int tileFill;
    final int tileStroke;
    /** Preferred default icon style hint when user hasn't overridden (still overridable). */
    final IconStyle defaultIconStyle;

    private VisualTheme(String id, String name, String description, Backdrop backdrop,
                        int background, int backgroundEnd, int accent, int accentSoft,
                        int textPrimary, int textSecondary,
                        int cardFill, int cardFillFocus, int cardStroke, int cardStrokeFocus,
                        int pillFill, int pillFillFocus, int pillStroke,
                        int datePillFill, int datePillStroke, int dockBand, int dockEdge, int amPm,
                        int tileFill, int tileStroke, IconStyle defaultIconStyle) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.backdrop = backdrop;
        this.background = background;
        this.backgroundEnd = backgroundEnd;
        this.accent = accent;
        this.accentSoft = accentSoft;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.cardFill = cardFill;
        this.cardFillFocus = cardFillFocus;
        this.cardStroke = cardStroke;
        this.cardStrokeFocus = cardStrokeFocus;
        this.pillFill = pillFill;
        this.pillFillFocus = pillFillFocus;
        this.pillStroke = pillStroke;
        this.datePillFill = datePillFill;
        this.datePillStroke = datePillStroke;
        this.dockBand = dockBand;
        this.dockEdge = dockEdge;
        this.amPm = amPm;
        this.tileFill = tileFill;
        this.tileStroke = tileStroke;
        this.defaultIconStyle = defaultIconStyle;
    }

    private static VisualTheme of(String id, String name, String description, Backdrop backdrop,
                                  int bg, int bgEnd, int accent, IconStyle defaultIcons,
                                  int cardFill, int dockBand) {
        int soft = (accent & 0x00FFFFFF) | 0x55000000;
        return new VisualTheme(
                id, name, description, backdrop,
                bg, bgEnd, accent, soft,
                0xFFFFFFFF, 0xB3FFFFFF,
                cardFill, mixAlpha(cardFill, 0xE6), 0x40FFFFFF, accent,
                0x33182028, 0xEE1A222C, 0x55FFFFFF,
                0x44182028, soft, dockBand, (accent & 0x00FFFFFF) | 0x55FFFFFF,
                accent,
                0xFF14181F, 0x33FFFFFF, defaultIcons);
    }

    private static int mixAlpha(int argb, int alpha) {
        return (argb & 0x00FFFFFF) | (alpha << 24);
    }

    static final VisualTheme[] ALL = {
            of("midnight_cyan", "Midnight Cyan", "Futuristic black with electric cyan glow.",
                    Backdrop.MIDNIGHT_LINES, 0xFF05070C, 0xFF0A1220, 0xFF2EE6FF, IconStyle.DARK,
                    0xB3141820, 0xB010141C),
            of("liquid_glass_blue", "Liquid Glass Blue", "Navy glass panels with soft blue rims.",
                    Backdrop.LIQUID_NAVY, 0xFF07101C, 0xFF0E2744, 0xFF5BA8FF, IconStyle.GLASS,
                    0x99101C2C, 0x99101828),
            of("aurora_teal", "Aurora Teal", "Dark teal atmosphere with luminous borders.",
                    Backdrop.AURORA, 0xFF061412, 0xFF0A2A28, 0xFF3DFFC8, IconStyle.GLASS,
                    0xB010201C, 0xA00E1C18),
            of("crimson_noir", "Crimson Noir", "Cinematic near-black with ruby accents.",
                    Backdrop.CRIMSON, 0xFF0A0506, 0xFF2A0A10, 0xFFFF3B4A, IconStyle.DARK,
                    0xB3180C10, 0xB014080C),
            of("purple_galaxy", "Purple Galaxy", "Indigo nebula with violet glass.",
                    Backdrop.GALAXY, 0xFF090614, 0xFF1A0F36, 0xFFB26BFF, IconStyle.GLASS,
                    0xA0161028, 0xA0120E22),
            of("warm_amber", "Warm Amber", "Charcoal and gold, sunset warmth.",
                    Backdrop.AMBER_SUNSET, 0xFF0C0A08, 0xFF1C140C, 0xFFE8A54B, IconStyle.ORIGINAL,
                    0xB31A1410, 0xB014100C),
            of("forest_emerald", "Forest Emerald", "Deep greens with mint highlights.",
                    Backdrop.FOREST, 0xFF06100A, 0xFF0E2416, 0xFF45E09A, IconStyle.DARK,
                    0xB0101A14, 0xA00C1610),
            of("graphite_mono", "Graphite Monochrome", "Clean graphite with silver focus.",
                    Backdrop.GRAPHITE, 0xFF0C0C0E, 0xFF18181C, 0xFFD0D4DC, IconStyle.MONOCHROME,
                    0xCC16181C, 0xCC121418),
    };

    static VisualTheme byId(String id) {
        if (id == null || id.isEmpty()) return ALL[0];
        for (VisualTheme t : ALL) if (t.id.equals(id)) return t;
        return ALL[0];
    }

    static IconStyle iconStyleById(String id) {
        if (id == null) return IconStyle.ORIGINAL;
        try {
            return IconStyle.valueOf(id);
        } catch (IllegalArgumentException e) {
            return IconStyle.ORIGINAL;
        }
    }

    static String iconStyleLabel(IconStyle style) {
        switch (style) {
            case DARK: return "Dark";
            case MONOCHROME: return "Monochrome";
            case THEME_TINTED: return "Theme-tinted";
            case GLASS: return "Glass";
            default: return "Original";
        }
    }
}
