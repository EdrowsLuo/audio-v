package com.edlplan.audiov.core.visual;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;

import java.io.IOException;

public class EdlAudioVisualizer extends BaseVisualizer {

    /**
     * 混合后的数据，介于0~127之间
     */
    private float[] mixedFFT;

    /**
     * 定义绘制的大小
     */
    private float width = 400;
    private float height = 400;


    @Override
    protected void onPrepare() {
        super.onPrepare();
        try {
            centerTexture = ATexture.getFactory().createFromAssets("logo_white.png");
            backBuffer = ATexture.getFactory().create(720, 720);
            currentView = ATexture.getFactory().create(720, 720);
            System.out.println("EDL::LOG create buffers " + currentView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onUpdateFFT() {
        super.onUpdateFFT();
        mixedFFT = new float[fftData.length / 2 + 1];
        mixedFFT[0] = Math.abs(fftData[0]);
        for (int i = 2, j = 1; j < mixedFFT.length && (i + 1) < fftData.length; ) {
            mixedFFT[j] = (float) Math.sqrt((fftData[i] * fftData[i] + fftData[i + 1] * fftData[i + 1]) * 127 * 127 / 2);
            i+=2;
            j++;
        }
    }



    float ang = 0;
    int barCount;

    private long drawTime = -1;
    private int deltaTime = -1;

    private float lk;

    private float[] preRfdata;

    private ATexture currentView;
    private ATexture backBuffer;
    private ATexture centerTexture;

    /**
     * 更新绘制数据
     * 历史遗留代码，有时间再整理
     */
    protected void updateDrawdata() {
        if (drawTime != -1) {
            deltaTime = (int) (System.currentTimeMillis() - drawTime);
            drawTime = System.currentTimeMillis();
        } else {
            drawTime = System.currentTimeMillis();
            return;
        }

        barCount = mixedFFT.length - 20;

        /*float mul = 0;
        for(int i=0;i<mixedFFT.length;i++) {
            mul += mixedFFT[i];
        }
        mul = (float) Math.sqrt(0.15f * mul / mixedFFT.length);*/

        float k = 0;
        for (int i = 0; i < mixedFFT.length; i++) {
            k += (i + 4) * mixedFFT[i];
        }
        k /= mixedFFT.length;
        k = (float) Math.sqrt(Math.sqrt(k) / 7);

        float[] rfData = new float[barCount];

        for (int i = 0; i < rfData.length; i++) {
            rfData[i] = mixedFFT[i] * (2 + i * 0.11f) / 2;
        }
        rfData = applyVolume(rfData);

        if (preRfdata == null || preRfdata.length != rfData.length) {
            preRfdata = new float[rfData.length];
        }

        float change = 0;
        for (int i = 0; i < rfData.length; i++) {
            change += ((preRfdata[i] < rfData[i]) ? (rfData[i] - preRfdata[i]) : 0);
            preRfdata[i] = rfData[i];
        }
        change = (float) (Math.pow(change, 1.5) / 1500);


        rfData = resortData(rfData);
        for (int i = 0; i < rfData.length; i++) {
            rfData[i] *= k;
        }

        if (deltaTime > 0 && deltaTime < 200) {
            ang += (Math.abs(k - lk) + 0.08 * k) * 0.006 * deltaTime;
        }

        //根据节拍产生的波动附加值
        float ryAdd = 0;
        boolean beat = false;
        if (k > lk * 1.14 + 0.04) {
            ryAdd = (float) Math.min(Math.pow(k - lk, 0.3) * 20, 25);
            beat = true;
        }

        lk = k;

        ACanvas currentCanvas = ACanvas.of(currentView);
        currentCanvas.start();
        {
            currentCanvas.clear(0, 0, 0, 0);
            currentCanvas.drawTexture(backBuffer, 0, 0, 180f / 255);
        }
        currentCanvas.end();

        ACanvas bufferCanvas = ACanvas.of(backBuffer);
        bufferCanvas.start();
        bufferCanvas.clear(0, 0, 0, 0);
        bufferCanvas.end();

        float rng = k * 60 + 137 + ryAdd;

        float deltaAngle = (float) (2 * Math.PI / (barCount - 1));
        float angi = ((int) (ang / deltaAngle)) * deltaTime;
        int r = (int) (165 + k * 25);
        float[] points = new float[barCount * 4];
        //绘制条状图
        for (int i = 0; i < barCount; i++) {
            points[i * 4] = (float) (currentView.getWidth() / 2 + Math.cos(angi + i * deltaAngle) * r);
            points[i * 4 + 1] = (float) (currentView.getHeight() / 2 - Math.sin(angi + i * deltaAngle) * r);
            points[i * 4 + 2] = (float) (currentView.getWidth() / 2 + Math.cos(angi + i * deltaAngle) * (rfData[i] + r));
            points[i * 4 + 3] = (float) (currentView.getHeight() / 2 - Math.sin(angi + i * deltaAngle) * (rfData[i] + r));
        }

        currentCanvas.start();
        {
            currentCanvas.drawLines(points, 3.2f, 0, 128f / 255, 1, (float) Math.sin(k));
            currentCanvas.drawTexture(
                    centerTexture,
                    0, 0, centerTexture.getWidth(), centerTexture.getHeight(),
                    currentView.getWidth() / 2 - rng, currentView.getHeight() / 2 - rng, rng * 2, rng * 2,
                    beat ? (150f / 255) : (210f / 255)
            );
        }
        currentCanvas.end();

        bufferCanvas.start();
        {
            bufferCanvas.drawTexture(currentView, 0, 0, 1);
        }
        bufferCanvas.end();

    }

    protected float[] resortData(float[] data) {
        float[] ra = new float[data.length];
        for (int i = 0; i < ra.length; i++) {
            ra[i] = data[(5 * i) % data.length];
        }
        return ra;
    }

    public static int range = 2;

    protected float[] applyVolume(float[] data) {
        float[] r = new float[data.length];
        int start = 0;
        int end = 0;
        int count = 0;

        for (int i = 0; i < r.length; i++) {
            float rv = 0;
            start = (i - range < 0) ? 0 : (i - range);
            end = (i + range >= r.length) ? (r.length - 1) : (i + range);
            count = end - start + 1;
            for (int j = start; j <= end; j++) {
                rv += data[j];
            }
            rv = rv / (count * 255);
            rv = (float) Math.pow(rv, 0.15);
            r[i] = rv * data[i] * 10;
        }

        return r;
    }

    @Override
    public void draw() {
        updateDrawdata();
    }

    @Override
    public ATexture getResult() {
        return currentView;
    }
}
