package com.edlplan.audiov.platform.android;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;

public class AndroidCanvas extends ACanvas {

    public static AndroidCanvasFactory FACTORY = new AndroidCanvasFactory();

    private AndroidTexture texture;

    private Canvas canvas;

    public AndroidCanvas(AndroidTexture texture) {
        super(texture);
        canvas = new Canvas(texture.getBitmap());
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void clear(float r, float g, float b, float a) {
        texture.getBitmap().eraseColor(argb(a, r, g, b));
    }

    public static int argb(float r, float g, float b, float a) {
        return ((int) (a * 255.0f + 0.5f) << 24) |
                ((int) (r   * 255.0f + 0.5f) << 16) |
                ((int) (g * 255.0f + 0.5f) <<  8) |
                (int) (b  * 255.0f + 0.5f);
    }

    public static int color255(float v) {
        return (int) (v * 255.0f + 0.5f);
    }

    @Override
    public void drawTexture(ATexture texture, int ox, int oy, int ow, int oh, float cx, float cy, float cw, float ch, float alpha) {
        Paint paint = new Paint();
        paint.setARGB(255, 255, 255, 255);
        paint.setAlpha(color255(alpha));
        canvas.drawBitmap(
                ((AndroidTexture) texture).getBitmap(),
                new Rect(ox, oy, ow, oh),
                new RectF(cx, cy, cw, ch),
                paint);
    }

    @Override
    public void drawLines(float[] lineData, float lineWidth, float r, float g, float b, float a) {
        Paint paint = new Paint();
        paint.setColor(argb(a, r, g, b));
        paint.setStrokeWidth(lineWidth);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawLines(lineData, paint);
    }

    public static class AndroidCanvasFactory extends AFactory {
        @Override
        public ACanvas create(ATexture texture) {
            return new AndroidCanvas((AndroidTexture) texture);
        }
    }


}
