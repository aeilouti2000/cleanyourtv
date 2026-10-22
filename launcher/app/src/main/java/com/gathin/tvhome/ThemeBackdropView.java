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
 * Lightweight theme backdrop: gradients and a few vector accents. No bitmaps, no blur —
 * safe for TCL Google TV GPUs. Optional slow alpha pulse when animation is enabled.
 */
public class ThemeBackdropView extends View {

    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accent = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private VisualTheme theme = VisualTheme.ALL[0];
    private boolean animate;
    private float pulse;
    private long startMs;
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!animate || getVisibility() != VISIBLE) return;
            long t = android.os.SystemClock.uptimeMillis() - startMs;
            pulse = 0.5f + 0.5f * (float) Math.sin(t / 4200.0);
            invalidate();
            postOnAnimationDelayed(this, 48);
        }
    };

    public ThemeBackdropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        accent.setStyle(Paint.Style.STROKE);
        accent.setStrokeCap(Paint.Cap.ROUND);
    }

    void setTheme(VisualTheme theme, boolean animate) {
        this.theme = theme != null ? theme : VisualTheme.ALL[0];
        this.animate = animate;
        removeCallbacks(tick);
        startMs = android.os.SystemClock.uptimeMillis();
        rebuildShader(getWidth(), getHeight());
        invalidate();
        if (animate) postOnAnimation(tick);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rebuildShader(w, h);
    }

    private void rebuildShader(int w, int h) {
        if (w <= 0 || h <= 0) return;
        fill.setShader(new LinearGradient(0, 0, 0, h, theme.background, theme.backgroundEnd, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas c) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        c.drawRect(0, 0, w, h, fill);
        float dens = getResources().getDisplayMetrics().density;
        float a = animate ? (0.55f + 0.45f * pulse) : 1f;
        switch (theme.backdrop) {
            case MIDNIGHT_LINES:
                drawLines(c, w, h, dens, a);
                break;
            case LIQUID_NAVY:
                drawSoftBlob(c, w * 0.75f, h * 0.2f, w * 0.55f, theme.accent, 0x28 * a);
                drawSoftBlob(c, w * 0.2f, h * 0.7f, w * 0.4f, theme.accent, 0x18 * a);
                break;
            case AURORA:
                drawSoftBlob(c, w * 0.3f, h * 0.25f, w * 0.5f, theme.accent, 0x30 * a);
                drawSoftBlob(c, w * 0.7f, h * 0.35f, w * 0.45f, 0xFF6B8CFF, 0x22 * a);
                drawSoftBlob(c, w * 0.5f, h * 0.75f, w * 0.55f, 0xFF2AFFB0, 0x18 * a);
                break;
            case CRIMSON:
                drawSoftBlob(c, w * 0.85f, h * 0.15f, w * 0.5f, theme.accent, 0x2A * a);
                drawSoftBlob(c, w * 0.15f, h * 0.85f, w * 0.4f, 0xFF6A1020, 0x40 * a);
                break;
            case GALAXY:
                drawSoftBlob(c, w * 0.8f, h * 0.3f, w * 0.55f, theme.accent, 0x32 * a);
                drawSoftBlob(c, w * 0.25f, h * 0.55f, w * 0.4f, 0xFF4A2AFF, 0x24 * a);
                break;
            case AMBER_SUNSET:
                drawSoftBlob(c, w * 0.5f, h * 0.05f, w * 0.7f, theme.accent, 0x28 * a);
                drawSoftBlob(c, w * 0.2f, h * 0.9f, w * 0.5f, 0xFF3A2010, 0x50 * a);
                break;
            case FOREST:
                drawSoftBlob(c, w * 0.85f, h * 0.4f, w * 0.45f, theme.accent, 0x22 * a);
                drawLeaves(c, w, h, dens, a);
                break;
            case GRAPHITE:
            default:
                break;
        }
    }

    private void drawSoftBlob(Canvas c, float cx, float cy, float r, int color, float alpha) {
        int a = Math.max(0, Math.min(255, Math.round(alpha)));
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setShader(new RadialGradient(cx, cy, Math.max(1f, r),
                (a << 24) | (color & 0xFFFFFF), 0x00000000, Shader.TileMode.CLAMP));
        c.drawCircle(cx, cy, r, p);
    }

    private void drawLines(Canvas c, int w, int h, float dens, float a) {
        accent.setStrokeWidth(1.2f * dens);
        accent.setColor((Math.round(0x55 * a) << 24) | (theme.accent & 0xFFFFFF));
        path.reset();
        path.moveTo(0, h * 0.22f);
        path.cubicTo(w * 0.25f, h * 0.05f, w * 0.55f, h * 0.4f, w, h * 0.18f);
        c.drawPath(path, accent);
        path.reset();
        path.moveTo(0, h * 0.55f);
        path.cubicTo(w * 0.3f, h * 0.35f, w * 0.6f, h * 0.7f, w, h * 0.48f);
        c.drawPath(path, accent);
        path.reset();
        path.moveTo(0, h * 0.82f);
        path.cubicTo(w * 0.35f, h * 0.7f, w * 0.7f, h * 0.95f, w, h * 0.78f);
        c.drawPath(path, accent);
    }

    private void drawLeaves(Canvas c, int w, int h, float dens, float a) {
        accent.setStyle(Paint.Style.FILL);
        accent.setColor((Math.round(0x28 * a) << 24) | (theme.accent & 0xFFFFFF));
        float x = w * 0.82f, y = h * 0.35f;
        for (int i = 0; i < 5; i++) {
            float ox = x + (i % 2) * 40 * dens;
            float oy = y + i * 55 * dens;
            path.reset();
            path.moveTo(ox, oy);
            path.quadTo(ox + 50 * dens, oy - 30 * dens, ox + 20 * dens, oy + 70 * dens);
            path.quadTo(ox - 10 * dens, oy + 20 * dens, ox, oy);
            c.drawPath(path, accent);
        }
        accent.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }
}
