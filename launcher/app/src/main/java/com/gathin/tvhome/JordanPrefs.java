package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Locations (Jordan) and prayer calculation method for the home widgets.
 * Defaults: Irbid + Egyptian General Authority (common in Jordan).
 */
final class JordanPrefs {

    static final class City {
        final String id;
        final String label;
        final double lat;
        final double lng;

        City(String id, String label, double lat, double lng) {
            this.id = id;
            this.label = label;
            this.lat = lat;
            this.lng = lng;
        }
    }

    /** Fajr / Isha angles; ishaMinutesAfterMaghrib > 0 replaces Isha angle (Umm al-Qura style). */
    static final class Method {
        final String id;
        final String label;
        final double fajrAngle;
        final double ishaAngle;
        final int ishaMinutesAfterMaghrib;

        Method(String id, String label, double fajrAngle, double ishaAngle, int ishaMinutesAfterMaghrib) {
            this.id = id;
            this.label = label;
            this.fajrAngle = fajrAngle;
            this.ishaAngle = ishaAngle;
            this.ishaMinutesAfterMaghrib = ishaMinutesAfterMaghrib;
        }
    }

    static final City[] CITIES = {
            new City("irbid", "Irbid", 32.5556, 35.8500),
            new City("amman", "Amman", 31.9539, 35.9106),
            new City("zarqa", "Zarqa", 32.0728, 36.0880),
            new City("ajloun", "Ajloun", 32.3325, 35.7517),
            new City("jerash", "Jerash", 32.2808, 35.8993),
            new City("mafraq", "Mafraq", 32.3429, 36.2080),
            new City("salt", "Salt", 32.0392, 35.7272),
            new City("madaba", "Madaba", 31.7167, 35.8000),
            new City("karak", "Karak", 31.1850, 35.7047),
            new City("aqaba", "Aqaba", 29.5320, 35.0063),
    };

    static final Method[] METHODS = {
            new Method("egypt", "Egyptian (Jordan)", 19.5, 17.5, 0),
            new Method("mwl", "Muslim World League", 18.0, 17.0, 0),
            new Method("umm", "Umm al-Qura", 18.5, 0, 90),
            new Method("karachi", "Karachi", 18.0, 18.0, 0),
            new Method("isna", "ISNA", 15.0, 15.0, 0),
            // UOIF: Fajr 12°, Isha 12° (Union of Islamic Organisations of France).
            new Method("uoif", "Union of Islamic Organisations of France (UOIF)", 12.0, 12.0, 0),
    };

    private static final String PREFS = "jordan_widgets";
    private static final String KEY_PRAYER_CITY = "prayer_city";
    private static final String KEY_WEATHER_CITY = "weather_city";
    private static final String KEY_METHOD = "prayer_method";
    private static final String DEFAULT_CITY = "irbid";
    private static final String DEFAULT_METHOD = "egypt";

    private final SharedPreferences prefs;

    JordanPrefs(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    City prayerCity() {
        return cityById(prefs.getString(KEY_PRAYER_CITY, DEFAULT_CITY));
    }

    City weatherCity() {
        return cityById(prefs.getString(KEY_WEATHER_CITY, DEFAULT_CITY));
    }

    Method method() {
        return methodById(prefs.getString(KEY_METHOD, DEFAULT_METHOD));
    }

    void setPrayerCity(String id) {
        prefs.edit().putString(KEY_PRAYER_CITY, id).apply();
    }

    void setWeatherCity(String id) {
        prefs.edit().putString(KEY_WEATHER_CITY, id).apply();
    }

    void setMethod(String id) {
        prefs.edit().putString(KEY_METHOD, id).apply();
    }

    static City cityById(String id) {
        for (City c : CITIES) {
            if (c.id.equals(id)) return c;
        }
        return CITIES[0]; // Irbid
    }

    static Method methodById(String id) {
        for (Method m : METHODS) {
            if (m.id.equals(id)) return m;
        }
        return METHODS[0];
    }
}
