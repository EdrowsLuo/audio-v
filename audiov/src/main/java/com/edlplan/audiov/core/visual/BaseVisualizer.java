package com.edlplan.audiov.core.visual;


import com.edlplan.audiov.core.audio.IAudioEntry;

/**
 * 对数据进行初始处理
 */
public abstract class BaseVisualizer extends AbstractVisualizer {

    /**
     * 获取的fft数据数组
     */
    protected float[] fftData;

    protected int baseSize;

    public BaseVisualizer() {
        fftData = new float[512];
    }

    public void setBaseSize(int baseSize) {
        if (Math.abs(baseSize - this.baseSize) > 0.5) {
            this.baseSize = baseSize;
            onBaseSizeChanged(baseSize);
        }
    }

    public float getBaseSize() {
        return baseSize;
    }

    protected void onBaseSizeChanged(int newSize) {

    }

    @Override
    public void update() {
        if (entry == null || !entry.isPlaying()) {
            return;
        }
        entry.getFFT(fftData, IAudioEntry.FLAG_FFT1024);
        onUpdateFFT();
    }

    /**
     * 在这个方法里处理fft数据，具体的数据在成员变量fftData里
     */
    protected void onUpdateFFT() {

    }
}
