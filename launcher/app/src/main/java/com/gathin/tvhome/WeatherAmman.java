package com.gathin.tvhome;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/** Light weather via Open-Meteo for a chosen Jordan city. Cached ~30 minutes per city. */
final class WeatherAmman {

    static final class Snapshot {
        final String tempLabel;
        final String description;

        Snapshot(String tempLabel, String description) {
            this.tempLabel = tempLabel;
            this.description = description;
        }
    }

    interface Listener {
        void onWeather(Snapshot snap);
    }

    private static final String TAG = "WeatherAmman";
    private static final long CACHE_MS = 30 * 60 * 1000L;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicReference<Snapshot> cached = new AtomicReference<>(null);
    private String cachedCityId = "";
    private long cachedAt;

    void refresh(JordanPrefs.City city, Listener listener) {
        Snapshot hit = cached.get();
        if (hit != null && city.id.equals(cachedCityId)
                && System.currentTimeMillis() - cachedAt < CACHE_MS) {
            listener.onWeather(hit);
            return;
        }
        if (hit == null || !city.id.equals(cachedCityId)) {
            listener.onWeather(new Snapshot("…", "Updating"));
        }
        final String cityId = city.id;
        final double lat = city.lat;
        final double lng = city.lng;
        io.execute(() -> {
            Snapshot snap = fetch(lat, lng);
            if (snap != null) {
                cached.set(snap);
                cachedCityId = cityId;
                cachedAt = System.currentTimeMillis();
                main.post(() -> listener.onWeather(snap));
            } else if (hit != null && cityId.equals(cachedCityId)) {
                main.post(() -> listener.onWeather(hit));
            } else {
                main.post(() -> listener.onWeather(new Snapshot("—", "Offline")));
            }
        });
    }

    void shutdown() {
        io.shutdownNow();
    }

    private static Snapshot fetch(double lat, double lng) {
        HttpURLConnection c = null;
        try {
            String url = String.format(Locale.US,
                    "https://api.open-meteo.com/v1/forecast"
                            + "?latitude=%.4f&longitude=%.4f"
                            + "&current=temperature_2m,weather_code"
                            + "&timezone=Asia%%2FAmman",
                    lat, lng);
            c = (HttpURLConnection) new URL(url).openConnection();
            c.setConnectTimeout(4000);
            c.setReadTimeout(4000);
            c.setRequestProperty("Accept", "application/json");
            c.setRequestProperty("User-Agent", "CleanYourTV-Jordan/1.0");
            if (c.getResponseCode() != 200) return null;
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            return parse(sb.toString());
        } catch (Exception e) {
            Log.w(TAG, "weather fetch failed", e);
            return null;
        } finally {
            if (c != null) c.disconnect();
        }
    }

    private static Snapshot parse(String json) {
        int currentIdx = json.indexOf("\"current\":{");
        String scope = currentIdx >= 0 ? json.substring(currentIdx) : json;
        Double temp = extractNumber(scope, "\"temperature_2m\":");
        Double wcode = extractNumber(scope, "\"weather_code\":");
        if (temp == null) return null;
        int t = (int) Math.round(temp);
        String desc = describe(wcode == null ? -1 : (int) Math.round(wcode));
        return new Snapshot(String.format(Locale.US, "%d°", t), desc);
    }

    private static Double extractNumber(String json, String key) {
        int from = 0;
        while (true) {
            int i = json.indexOf(key, from);
            if (i < 0) return null;
            i += key.length();
            while (i < json.length() && json.charAt(i) == ' ') i++;
            int j = i;
            while (j < json.length()) {
                char ch = json.charAt(j);
                if ((ch >= '0' && ch <= '9') || ch == '-' || ch == '+' || ch == '.' || ch == 'e' || ch == 'E') {
                    j++;
                } else break;
            }
            if (j > i) {
                try {
                    return Double.parseDouble(json.substring(i, j));
                } catch (NumberFormatException ignored) {
                }
            }
            from = i;
        }
    }

    private static String describe(int code) {
        if (code == 0) return "Clear";
        if (code == 1 || code == 2) return "Partly cloudy";
        if (code == 3) return "Cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Showers";
        if (code >= 95) return "Thunder";
        return "—";
    }
}
