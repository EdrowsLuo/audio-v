package com.edlplan.audiov.platform.android;

import com.edlplan.audiov.core.AudioVCore;
import com.edlplan.audiov.core.graphics.ACanvas;
import com.edlplan.audiov.core.graphics.ATexture;

public class AndroidPlugin implements AudioVCore.PlatformGraphics {

    public static final AndroidPlugin INSTANCE = new AndroidPlugin();

    @Override
    public ATexture.AFactory getTextureFactory() {
        return AndroidTexture.FACTORY;
    }

    @Override
    public ACanvas.AFactory getCanvasFactory() {
        return AndroidCanvas.FACTORY;
    }
}
