package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;

/**
 * Prepares the real application logo for themed tiles.
 * The card background is the style plate — this class only places a fitCentered
 * logo (never geometric brand marks, never stretched into the tile).
 */
final class ThemedIcons {

    /** Logo fills this fraction of the tile width and height (fitCenter). */
    private static final float LOGO_FRACTION = 0.86f;

    private static final LruCache<String, Drawable> CACHE = new LruCache<String, Drawable>(64) {
        @Override
        protected int sizeOf(String key, Drawable value) {
            return 1;
        }
    };

    private ThemedIcons() { }

    /**
     * @return logo on a transparent WxH canvas matching the tile, or null for Original /
     *         when the caller should fall back to the stock drawable
     */
    static Drawable present(String pkg, Drawable logo, VisualTheme theme,
                            VisualTheme.IconStyle style, int widthPx, int heightPx) {
        if (style == VisualTheme.IconStyle.ORIGINAL) return null;
        if (logo == null || widthPx <= 0 || heightPx <= 0) return null;

        String key = pkg + '|' + style.name() + '|' + widthPx + 'x' + heightPx + '|'
                + Integer.toHexString(theme.accent) + "|v3";
        Drawable cached = CACHE.get(key);
        if (cached != null) return cached;

        int maxW = Math.max(1, Math.round(widthPx * LOGO_FRACTION));
        int maxH = Math.max(1, Math.round(heightPx * LOGO_FRACTION));
        Bitmap logoBmp = rasterizeFit(logo, maxW, maxH);
        if (logoBmp == null) return null;

        Bitmap treated = treat(logoBmp, style, theme.accent);
        Bitmap canvasBmp = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(canvasBmp);
        float left = (widthPx - treated.getWidth()) / 2f;
        float top = (heightPx - treated.getHeight()) / 2f;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        c.drawBitmap(treated, left, top, paint);
        if (treated != logoBmp) treated.recycle();
        logoBmp.recycle();

        BitmapDrawable d = new BitmapDrawable(null, canvasBmp);
        d.setBounds(0, 0, widthPx, heightPx);
        CACHE.put(key, d);
        return d;
    }

    static void clearCache() {
        CACHE.evictAll();
    }

    /**
     * Rasterize at intrinsic aspect, trim empty padding, flatten opaque plate backgrounds,
     * then uniformly scale to fit inside maxW×maxH (fitCenter).
     */
    private static Bitmap rasterizeFit(Drawable original, int maxW, int maxH) {
        Drawable d = original.getConstantState() != null
                ? original.getConstantState().newDrawable().mutate() : original;
        int iw = d.getIntrinsicWidth();
        int ih = d.getIntrinsicHeight();
        if (iw <= 0 || ih <= 0) {
            iw = maxW;
            ih = maxH;
        }

        int target = Math.max(maxW, maxH);
        int render = Math.min(Math.max(Math.max(iw, ih), target), target * 3);
        float rs = Math.min(render / (float) iw, render / (float) ih);
        int rw = Math.max(1, Math.round(iw * rs));
        int rh = Math.max(1, Math.round(ih * rs));

        Bitmap full = Bitmap.createBitmap(rw, rh, Bitmap.Config.ARGB_8888);
        Canvas fc = new Canvas(full);
        d.setBounds(0, 0, rw, rh);
        d.draw(fc);

        Bitmap content = trimTransparent(full);
        if (content != full) full.recycle();

        // Pull the mark off solid white/coloured plates (YouTube, Play wordmark tiles, etc.).
        Bitmap flat = flattenPlateBackground(content);
        if (flat != content) content.recycle();
        content = flat;

        content = trimTransparent(content);

        int cw = content.getWidth();
        int ch = content.getHeight();

        // Extreme banners → center-crop toward a TV-tile-friendly aspect so the mark isn't tiny.
        float tileAspect = maxW / (float) maxH;
        float contentAspect = cw / (float) ch;
        if (contentAspect > tileAspect * 1.55f || contentAspect < tileAspect / 1.55f) {
            int cropW = cw;
            int cropH = ch;
            if (contentAspect > tileAspect) {
                cropW = Math.max(1, Math.round(ch * tileAspect * 1.15f));
                cropW = Math.min(cw, cropW);
            } else {
                cropH = Math.max(1, Math.round(cw / (tileAspect * 1.15f)));
                cropH = Math.min(ch, cropH);
            }
            int x = (cw - cropW) / 2;
            int y = (ch - cropH) / 2;
            Bitmap cropped = Bitmap.createBitmap(content, x, y, cropW, cropH);
            if (cropped != content) content.recycle();
            content = cropped;
            cw = cropW;
            ch = cropH;
        }

        float scale = Math.min(maxW / (float) cw, maxH / (float) ch);
        int dw = Math.max(1, Math.round(cw * scale));
        int dh = Math.max(1, Math.round(ch * scale));
        Bitmap scaled = Bitmap.createScaledBitmap(content, dw, dh, true);
        if (scaled != content) content.recycle();
        return scaled;
    }

    /** Crop to the opaque/visible content so launcher icons with padding fill the tile. */
    private static Bitmap trimTransparent(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        int left = w, top = h, right = -1, bottom = -1;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                if (((px[row + x] >>> 24) & 0xFF) > 12) {
                    if (x < left) left = x;
                    if (x > right) right = x;
                    if (y < top) top = y;
                    if (y > bottom) bottom = y;
                }
            }
        }
        if (right < left || bottom < top) return src;
        int pad = Math.max(1, Math.round(Math.min(w, h) * 0.02f));
        left = Math.max(0, left - pad);
        top = Math.max(0, top - pad);
        right = Math.min(w - 1, right + pad);
        bottom = Math.min(h - 1, bottom + pad);
        int tw = right - left + 1;
        int th = bottom - top + 1;
        if (tw >= w * 0.94f && th >= h * 0.94f) return src;
        return Bitmap.createBitmap(src, left, top, tw, th);
    }

    /**
     * If the icon sits on a near-solid plate (sampled from corners), punch that plate to
     * transparent so the recognisable mark sits on the themed card instead of a nested box.
     */
    private static Bitmap flattenPlateBackground(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        if (w < 8 || h < 8) return src;
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);

        int c0 = px[0];
        int c1 = px[w - 1];
        int c2 = px[(h - 1) * w];
        int c3 = px[(h - 1) * w + (w - 1)];
        if (!nearColor(c0, c1, 18) || !nearColor(c0, c2, 18) || !nearColor(c0, c3, 18)) {
            return src; // corners disagree — already a freeform / transparent icon
        }
        if (((c0 >>> 24) & 0xFF) < 200) return src; // already transparent corners

        // Plate must dominate the edge ring (typical for adaptive / store icons).
        int edgeMatch = 0, edgeTotal = 0;
        for (int x = 0; x < w; x++) {
            edgeTotal += 2;
            if (nearColor(px[x], c0, 22)) edgeMatch++;
            if (nearColor(px[(h - 1) * w + x], c0, 22)) edgeMatch++;
        }
        for (int y = 1; y < h - 1; y++) {
            edgeTotal += 2;
            if (nearColor(px[y * w], c0, 22)) edgeMatch++;
            if (nearColor(px[y * w + (w - 1)], c0, 22)) edgeMatch++;
        }
        if (edgeMatch < edgeTotal * 0.72f) return src;

        int br = (c0 >> 16) & 0xFF, bg = (c0 >> 8) & 0xFF, bb = c0 & 0xFF;
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int i = 0; i < px.length; i++) {
            int p = px[i];
            int a = (p >>> 24) & 0xFF;
            if (a < 8) {
                px[i] = 0;
                continue;
            }
            int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
            int dr = r - br, dg = g - bg, db = b - bb;
            int dist = (int) Math.sqrt(dr * dr + dg * dg + db * db);
            if (dist <= 28) {
                // Soft fringe near the plate colour
                float t = dist / 28f;
                int na = Math.round(a * t * t);
                px[i] = (na << 24) | (r << 16) | (g << 8) | b;
            }
        }
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    private static boolean nearColor(int a, int b, int tol) {
        if (((a >>> 24) & 0xFF) < 160 || ((b >>> 24) & 0xFF) < 160) return false;
        int dr = ((a >> 16) & 0xFF) - ((b >> 16) & 0xFF);
        int dg = ((a >> 8) & 0xFF) - ((b >> 8) & 0xFF);
        int db = (a & 0xFF) - (b & 0xFF);
        return Math.abs(dr) <= tol && Math.abs(dg) <= tol && Math.abs(db) <= tol;
    }

    private static Bitmap treat(Bitmap src, VisualTheme.IconStyle style, int accent) {
        switch (style) {
            case MONOCHROME:
                return toMono(src, 0xFFE8E8EC);
            case THEME_TINTED:
                return softTint(src, accent | 0xFF000000);
            case DARK:
            case GLASS:
            default:
                return src;
        }
    }

    private static Bitmap toMono(Bitmap src, int color) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int tr = (color >> 16) & 0xFF, tg = (color >> 8) & 0xFF, tb = color & 0xFF;
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < px.length; i++) {
            int a = (px[i] >>> 24) & 0xFF;
            if (a < 8) {
                px[i] = 0;
                continue;
            }
            int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
            int lum = (r * 30 + g * 59 + b * 11) / 100;
            float k = Math.min(1f, (lum / 255f) * 1.15f + 0.08f);
            px[i] = (a << 24) | (Math.round(tr * k) << 16) | (Math.round(tg * k) << 8) | Math.round(tb * k);
        }
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    private static Bitmap softTint(Bitmap src, int accent) {
        int w = src.getWidth(), h = src.getHeight();
        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        int tr = (accent >> 16) & 0xFF, tg = (accent >> 8) & 0xFF, tb = accent & 0xFF;
        int[] px = new int[w * h];
        src.getPixels(px, 0, w, 0, 0, w, h);
        for (int i = 0; i < px.length; i++) {
            int a = (px[i] >>> 24) & 0xFF;
            if (a < 8) {
                px[i] = 0;
                continue;
            }
            int r = (px[i] >> 16) & 0xFF, g = (px[i] >> 8) & 0xFF, b = px[i] & 0xFF;
            int lum = (r * 30 + g * 59 + b * 11) / 100;
            float ak = lum / 255f;
            int nr = Math.round(r * 0.55f + tr * ak * 0.45f);
            int ng = Math.round(g * 0.55f + tg * ak * 0.45f);
            int nb = Math.round(b * 0.55f + tb * ak * 0.45f);
            px[i] = (a << 24) | (clamp(nr) << 16) | (clamp(ng) << 8) | clamp(nb);
        }
        out.setPixels(px, 0, w, 0, 0, w, h);
        return out;
    }

    private static int clamp(int v) {
        return v < 0 ? 0 : Math.min(255, v);
    }
}
