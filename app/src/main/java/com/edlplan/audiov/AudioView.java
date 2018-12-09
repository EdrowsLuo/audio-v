package com.edlplan.audiov;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.visual.BaseVisualizer;
import com.edlplan.audiov.core.visual.EdlAudioVisualizer;
import com.edlplan.audiov.core.visual.LegacyAudioVisualizer;
import com.edlplan.audiov.platform.android.AndroidCanvas;
import com.edlplan.audiov.platform.android.AndroidTexture;

import java.io.File;

public class AudioView extends View {

    BaseVisualizer visualizer;

    public AudioView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (visualizer == null) {
            visualizer = new LegacyAudioVisualizer();
            visualizer.setBaseSize(Math.min(getWidth(), getHeight()));
            visualizer.prepare();
            if (EdAudioService.getAudioService().getAudioEntry() != null) {
                visualizer.changeAudio(EdAudioService.getAudioService().getAudioEntry());
            }
            EdAudioService.getAudioService().registerOnAudioChangeListener((pre, next) -> {
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
        canvas.drawBitmap(
                result,
                new Rect(0, 0, result.getWidth(), result.getHeight()),
                new RectF(0, 0, Math.min(canvas.getWidth(), canvas.getHeight()), Math.min(canvas.getWidth(), canvas.getHeight())),
                paint
        );
        invalidate();
    }
}
