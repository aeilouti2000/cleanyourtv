package com.gathin.tvhome;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Offline prayer times for a Jordan city + calculation method.
 */
final class PrayerTimes {

    private static final TimeZone TZ = TimeZone.getTimeZone("Asia/Amman");

    /** Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha — minutes from midnight local. */
    final int[] minutes = new int[6];
    final String[] names = {"Fajr", "Sunrise", "Dhuhr", "Asr", "Maghrib", "Isha"};
    final String cityLabel;

    static PrayerTimes forToday(JordanPrefs.City city, JordanPrefs.Method method) {
        Calendar now = Calendar.getInstance(TZ);
        return forDate(city, method,
                now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH));
    }

    static PrayerTimes forDate(JordanPrefs.City city, JordanPrefs.Method method, int year, int month, int day) {
        PrayerTimes p = new PrayerTimes(city.label);
        double jd = julian(year, month, day);
        double t = (jd - 2451545.0) / 36525.0;
        double eps = Math.toRadians(23.4393 - 0.0130 * t);
        double L = Math.toRadians(norm(280.466 + 36000.77 * t));
        double g = Math.toRadians(norm(357.529 + 35999.05 * t));
        double lambda = L + Math.toRadians(1.915) * Math.sin(g) + Math.toRadians(0.020) * Math.sin(2 * g);
        double alpha = Math.atan2(Math.cos(eps) * Math.sin(lambda), Math.cos(lambda));
        double delta = Math.asin(Math.sin(eps) * Math.sin(lambda));
        double eqT = (L - alpha) * 4.0 * 180.0 / Math.PI;
        eqT = ((eqT + 720) % 1440) - 720;

        double lat = Math.toRadians(city.lat);
        double noonUtcMin = 720.0 - 4.0 * city.lng - eqT;

        p.minutes[0] = roundTime(noonUtcMin - hourAngle(lat, delta, -method.fajrAngle) * 4.0);
        p.minutes[1] = roundTime(noonUtcMin - hourAngle(lat, delta, -0.833) * 4.0);
        p.minutes[2] = roundTime(noonUtcMin);
        double asrAlt = Math.toDegrees(Math.atan(1.0 / (1.0 + Math.tan(Math.abs(lat - delta)))));
        p.minutes[3] = roundTime(noonUtcMin + hourAngle(lat, delta, asrAlt) * 4.0);
        p.minutes[4] = roundTime(noonUtcMin + hourAngle(lat, delta, -0.833) * 4.0);
        if (method.ishaMinutesAfterMaghrib > 0) {
            p.minutes[5] = roundTime(p.minutes[4] + method.ishaMinutesAfterMaghrib);
        } else {
            p.minutes[5] = roundTime(noonUtcMin + hourAngle(lat, delta, -method.ishaAngle) * 4.0);
        }

        int offsetMin = TZ.getOffset(System.currentTimeMillis()) / 60000;
        for (int i = 0; i < 6; i++) {
            p.minutes[i] = ((p.minutes[i] + offsetMin) % 1440 + 1440) % 1440;
        }
        return p;
    }

    private PrayerTimes(String cityLabel) {
        this.cityLabel = cityLabel;
    }

    int nextIndex(int nowMinutes) {
        for (int i = 0; i < 6; i++) {
            if (i == 1) continue;
            if (minutes[i] > nowMinutes) return i;
        }
        return 0;
    }

    String formatCountdown(Calendar now) {
        int next = nextIndex(nowMinutes(now));
        return String.format(Locale.US, "%s in %s", names[next], countdownOnly(now));
    }

    static final int[] CARD_INDICES = {0, 2, 3, 4, 5};

    String countdownOnly(Calendar now) {
        int nowMin = nowMinutes(now);
        int next = nextIndex(nowMin);
        int diff = minutes[next] - nowMin;
        if (diff <= 0) diff += 1440;
        int h = diff / 60;
        int m = diff % 60;
        return h > 0
                ? String.format(Locale.US, "%dh %dm", h, m)
                : String.format(Locale.US, "%dm", m);
    }

    private static int nowMinutes(Calendar now) {
        return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
    }

    /** 12-hour clock without leading zero on the hour, e.g. 5:22 or 12:21. */
    static String hm(int mins) {
        int h24 = (mins / 60) % 24;
        int m = mins % 60;
        int h12 = h24 % 12;
        if (h12 == 0) h12 = 12;
        return String.format(Locale.US, "%d:%02d", h12, m);
    }

    private static double hourAngle(double lat, double delta, double angleDeg) {
        double a = Math.toRadians(angleDeg);
        double cosH = (Math.sin(a) - Math.sin(lat) * Math.sin(delta))
                / (Math.cos(lat) * Math.cos(delta));
        cosH = Math.max(-1, Math.min(1, cosH));
        return Math.toDegrees(Math.acos(cosH));
    }

    private static int roundTime(double utcMinutes) {
        return ((int) Math.round(utcMinutes) % 1440 + 1440) % 1440;
    }

    private static double julian(int y, int m, int d) {
        if (m <= 2) {
            y -= 1;
            m += 12;
        }
        int A = y / 100;
        int B = 2 - A + A / 4;
        return Math.floor(365.25 * (y + 4716)) + Math.floor(30.6001 * (m + 1)) + d + B - 1524.5;
    }

    private static double norm(double deg) {
        deg %= 360.0;
        if (deg < 0) deg += 360.0;
        return deg;
    }
}
