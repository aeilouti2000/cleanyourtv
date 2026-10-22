package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * Soft atmospheric plane behind Home (neon streaks, nebula, ridges, foliage silhouettes).
 * Drawn once per theme change; cheap enough for TV GPUs.
 */
public class ThemeBackdrop extends View {

    private HomeTheme.Atmosphere atmosphere = HomeTheme.Atmosphere.NEON_STREAKS;
    private int accent = 0xFF2EE6D6;
    private int background = 0xFF070A14;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    public ThemeBackdrop(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    void setTheme(HomeTheme theme) {
        atmosphere = theme.atmosphere;
        accent = theme.accent;
        background = theme.background;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        c.drawColor(background);
        switch (atmosphere) {
            case NEON_STREAKS:
                drawNeonStreaks(c, w, h);
                break;
            case NEBULA:
                drawNebula(c, w, h);
                break;
            case GOLDEN_RIDGE:
                drawRidges(c, w, h);
                break;
            case JUNGLE:
                drawJungle(c, w, h);
                break;
            case CRIMSON_GLOW:
                drawCrimson(c, w, h);
                break;
            case ICE_SHEET:
                drawIce(c, w, h);
                break;
        }
        // Bottom vignette so the dock stays readable.
        paint.setShader(new LinearGradient(0, h * 0.55f, 0, h,
                0x00000000, 0xCC000000, Shader.TileMode.CLAMP));
        c.drawRect(0, h * 0.55f, w, h, paint);
        paint.setShader(null);
    }

    private void drawNeonStreaks(Canvas c, int w, int h) {
        // Soft cyan glow blobs + flowing streaks (image 2).
        paint.setShader(new RadialGradient(w * 0.15f, h * 0.35f, w * 0.45f,
                accentWithAlpha(0x55), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(new RadialGradient(w * 0.85f, h * 0.2f, w * 0.4f,
                accentWithAlpha(0x40), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, w / 900f));
        paint.setColor(accentWithAlpha(0x66));
        for (int i = 0; i < 7; i++) {
            float y0 = h * (0.12f + i * 0.08f);
            path.reset();
            path.moveTo(-40, y0);
            path.cubicTo(w * 0.25f, y0 - h * 0.06f, w * 0.55f, y0 + h * 0.08f, w + 40, y0 - h * 0.02f);
            c.drawPath(path, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawNebula(Canvas c, int w, int h) {
        paint.setShader(new RadialGradient(w * 0.6f, h * 0.35f, w * 0.55f,
                accentWithAlpha(0x70), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(new RadialGradient(w * 0.25f, h * 0.55f, w * 0.4f,
                0x55FF4D9E, 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(new RadialGradient(w * 0.8f, h * 0.7f, w * 0.35f,
                0x406B5CFF, 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
    }

    private void drawRidges(Canvas c, int w, int h) {
        paint.setShader(new RadialGradient(w * 0.5f, h * 0.25f, w * 0.5f,
                accentWithAlpha(0x45), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        paint.setColor(0xFF1A120A);
        path.reset();
        path.moveTo(0, h);
        path.lineTo(0, h * 0.72f);
        path.lineTo(w * 0.18f, h * 0.55f);
        path.lineTo(w * 0.38f, h * 0.68f);
        path.lineTo(w * 0.55f, h * 0.48f);
        path.lineTo(w * 0.72f, h * 0.62f);
        path.lineTo(w, h * 0.5f);
        path.lineTo(w, h);
        path.close();
        c.drawPath(path, paint);
        paint.setColor(0xFF0E0906);
        path.reset();
        path.moveTo(0, h);
        path.lineTo(0, h * 0.82f);
        path.lineTo(w * 0.22f, h * 0.7f);
        path.lineTo(w * 0.45f, h * 0.78f);
        path.lineTo(w * 0.7f, h * 0.65f);
        path.lineTo(w, h * 0.74f);
        path.lineTo(w, h);
        path.close();
        c.drawPath(path, paint);
        paint.setColor(accentWithAlpha(0x33));
        c.drawCircle(w * 0.72f, h * 0.42f, w * 0.08f, paint);
    }

    private void drawJungle(Canvas c, int w, int h) {
        paint.setShader(new RadialGradient(w * 0.3f, h * 0.3f, w * 0.5f,
                accentWithAlpha(0x40), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        paint.setColor(0xFF0A1A10);
        for (int i = 0; i < 5; i++) {
            float x = w * (0.05f + i * 0.2f);
            path.reset();
            path.moveTo(x, h);
            path.quadTo(x - w * 0.08f, h * 0.55f, x + w * 0.02f, h * 0.25f);
            path.quadTo(x + w * 0.12f, h * 0.5f, x + w * 0.06f, h);
            path.close();
            c.drawPath(path, paint);
        }
        paint.setColor(0xFF07140C);
        for (int i = 0; i < 4; i++) {
            float x = w * (0.15f + i * 0.22f);
            path.reset();
            path.moveTo(x, h);
            path.quadTo(x + w * 0.1f, h * 0.6f, x - w * 0.02f, h * 0.35f);
            path.quadTo(x - w * 0.1f, h * 0.55f, x - w * 0.04f, h);
            path.close();
            c.drawPath(path, paint);
        }
    }

    private void drawCrimson(Canvas c, int w, int h) {
        paint.setShader(new RadialGradient(w * 0.5f, h * 0.2f, w * 0.55f,
                accentWithAlpha(0x50), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(new RadialGradient(w * 0.2f, h * 0.7f, w * 0.4f,
                0x40601018, 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
    }

    private void drawIce(Canvas c, int w, int h) {
        paint.setShader(new RadialGradient(w * 0.7f, h * 0.25f, w * 0.5f,
                accentWithAlpha(0x45), 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(new LinearGradient(0, 0, w, h * 0.6f,
                0x228EC5FF, 0x00000000, Shader.TileMode.CLAMP));
        c.drawRect(0, 0, w, h, paint);
        paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f);
        paint.setColor(accentWithAlpha(0x44));
        for (int i = 0; i < 5; i++) {
            float x = w * (0.1f + i * 0.18f);
            c.drawLine(x, 0, x + w * 0.08f, h * 0.5f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }

    private int accentWithAlpha(int a) {
        return (a << 24) | (accent & 0xFFFFFF);
    }
}
