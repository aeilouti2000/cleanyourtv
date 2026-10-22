package com.gathin.tvhome;

/**
 * Built-in home looks the Themes pill can switch between. Accent tints glass, pills, widgets and
 * the clock's AM/PM; {@link #atmosphere} drives {@link ThemeBackdrop}.
 */
final class HomeTheme {

    enum Atmosphere {
        NEON_STREAKS,   // image 2 / cyan mock
        NEBULA,         // purple
        GOLDEN_RIDGE,   // amber mountains
        JUNGLE,         // green leaves
        CRIMSON_GLOW,   // deep red (current burgundy vibe)
        ICE_SHEET       // cool silver-blue
    }

    final String id;
    final String label;
    final int accent;       // ARGB
    final int background;   // ARGB opaque
    final Atmosphere atmosphere;

    private HomeTheme(String id, String label, int accent, int background, Atmosphere atmosphere) {
        this.id = id;
        this.label = label;
        this.accent = accent;
        this.background = background;
        this.atmosphere = atmosphere;
    }

    /** Teal / cyan — the look from the user's mock (image 2). Default. */
    static final HomeTheme AURORA = new HomeTheme(
            "aurora", "Aurora", 0xFF2EE6D6, 0xFF070A14, Atmosphere.NEON_STREAKS);

    static final HomeTheme NEBULA = new HomeTheme(
            "nebula", "Nebula", 0xFFC084FC, 0xFF120818, Atmosphere.NEBULA);

    static final HomeTheme AMBER = new HomeTheme(
            "amber", "Amber Ridge", 0xFFE8A54B, 0xFF100A06, Atmosphere.GOLDEN_RIDGE);

    static final HomeTheme FOREST = new HomeTheme(
            "forest", "Forest", 0xFF5DDB8A, 0xFF06140C, Atmosphere.JUNGLE);

    static final HomeTheme CRIMSON = new HomeTheme(
            "crimson", "Crimson", 0xFFE85A5A, 0xFF140608, Atmosphere.CRIMSON_GLOW);

    static final HomeTheme ICE = new HomeTheme(
            "ice", "Ice", 0xFF8EC5FF, 0xFF060A12, Atmosphere.ICE_SHEET);

    static final HomeTheme[] ALL = { AURORA, NEBULA, AMBER, FOREST, CRIMSON, ICE };

    static HomeTheme byId(String id) {
        if (id == null || id.isEmpty()) return AURORA;
        for (HomeTheme t : ALL) if (t.id.equals(id)) return t;
        return AURORA;
    }
}
