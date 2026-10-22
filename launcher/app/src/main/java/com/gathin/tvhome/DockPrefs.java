package com.gathin.tvhome;

import android.content.Context;
import android.util.AtomicFile;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owner's dock package list (the apps in the bottom bar), kept across restarts.
 * Falls back to config.xml / theme dock_apps when no file exists yet.
 */
final class DockPrefs {

    private static final String TAG = "TvHome.DockPrefs";
    private static final String FILE_NAME = "dock_apps.txt";
    private static final String MAGIC = "tvhome-dock 1";
    private static final int MAX = 8;

    static File file(Context ctx) {
        return new File(ctx.getFilesDir(), FILE_NAME);
    }

    /** Saved dock packages, or {@code defaults} when none / invalid. */
    static List<String> load(Context ctx, List<String> defaults) {
        File file = file(ctx);
        if (!file.exists()) return new ArrayList<>(defaults);
        try {
            String text = new String(new AtomicFile(file).readFully(), StandardCharsets.UTF_8);
            List<String> parsed = decode(text);
            return parsed != null && !parsed.isEmpty() ? parsed : new ArrayList<>(defaults);
        } catch (IOException e) {
            Log.w(TAG, "cannot read " + FILE_NAME + ": " + e);
            return new ArrayList<>(defaults);
        }
    }

    static boolean save(Context ctx, List<String> packages) {
        List<String> clean = new ArrayList<>();
        for (String p : packages) {
            if (p == null || p.isEmpty() || clean.contains(p)) continue;
            clean.add(p);
            if (clean.size() >= MAX) break;
        }
        AtomicFile af = new AtomicFile(file(ctx));
        FileOutputStream out = null;
        try {
            out = af.startWrite();
            out.write(encode(clean).getBytes(StandardCharsets.UTF_8));
            af.finishWrite(out);
            return true;
        } catch (IOException e) {
            Log.w(TAG, "cannot write " + FILE_NAME + ": " + e);
            if (out != null) af.failWrite(out);
            return false;
        }
    }

    private static String encode(List<String> packages) {
        StringBuilder sb = new StringBuilder(MAGIC).append('\n');
        for (String p : packages) sb.append(p).append('\n');
        return sb.toString();
    }

    private static List<String> decode(String text) {
        if (text == null) return null;
        String[] lines = text.split("\n", -1);
        if (lines.length < 1 || !lines[0].equals(MAGIC)) return null;
        List<String> out = new ArrayList<>();
        for (int i = 1; i < lines.length; i++) {
            String p = lines[i].trim();
            if (p.isEmpty()) {
                if (i == lines.length - 1) break;
                continue;
            }
            if (!out.contains(p)) out.add(p);
            if (out.size() >= MAX) break;
        }
        return out;
    }
}
