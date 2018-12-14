package com.edlplan.audiov;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.edlplan.audiov.core.visual.BaseVisualizer;
import com.edlplan.audiov.core.visual.LegacyAudioVisualizer;
import com.edlplan.audiov.platform.android.AndroidTexture;

public class AudioView extends View {

    BaseVisualizer visualizer;

    public AudioView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public BaseVisualizer getVisualizer() {
        return visualizer;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (visualizer == null) {
            visualizer = new LegacyAudioVisualizer();
            visualizer.setBaseSize(Math.min(getWidth(), getHeight()));
            visualizer.prepare();
            if (EdlAudioService.getAudioService().getAudioEntry() != null) {
                visualizer.changeAudio(EdlAudioService.getAudioService().getAudioEntry());
            }
            EdlAudioService.getAudioService().registerOnAudioChangeListener((pre, next) -> {
                if (next != null) {
                    visualizer.changeAudio(next);
                }
            });
        }

        visualizer.update();
        visualizer.draw();

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        Bitmap result = ((AndroidTexture) visualizer.getResult()).getBitmap();
        int hsize = Math.min(canvas.getWidth(), canvas.getHeight()) / 2;
        float cx = canvas.getWidth() / 2;
        float cy = canvas.getHeight() / 2;
        canvas.drawBitmap(
                result,
                new Rect(0, 0, result.getWidth(), result.getHeight()),
                new RectF(cx - hsize, cy - hsize, cx + hsize, cy + hsize),
                paint
        );
        invalidate();
    }
}
