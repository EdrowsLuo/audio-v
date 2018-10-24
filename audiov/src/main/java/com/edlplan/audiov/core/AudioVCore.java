package com.edlplan.audiov.core;

import com.edlplan.audiov.core.audio.IAudioEntry;
import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;

public class AudioVCore {

    private static AudioVCore instance;

    private PlatformGraphics graphicsPlugin;

    private PlatformAudio audioPlugin;

    public static void initial(PlatformGraphics graphicsPlugin, PlatformAudio audioPlugin) {
        instance = new AudioVCore();
        instance.graphicsPlugin = graphicsPlugin;
        instance.audioPlugin = audioPlugin;
    }

    public static AudioVCore getInstance() {
        return instance;
    }

    public PlatformGraphics graphics() {
        return graphicsPlugin;
    }

    public PlatformAudio audio() {
        return audioPlugin;
    }

    /**
     * 获取对应平台的Graphics相关的类的接口
     */
    public interface PlatformGraphics{
        ATexture.AFactory getTextureFactory();
        ACanvas.AFactory getCanvasFactory();
    }

    /**
     * 获取音频相关的封装
     */
    public interface PlatformAudio{
        IAudioEntry.AFactory getAudioFactory();
    }
}
