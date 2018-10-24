package com.edlplan.libbass;

import com.un4seen.bass.BASS;
import java.nio.ByteBuffer;
import java.io.IOException;

public class BassChannel {
    private Type type;
    private int chaId;

    protected BassChannel(int chaId, Type type) {
        if (chaId == 0) {
            throw new RuntimeException("err bass: " + BASS.BASS_ErrorGetCode());
        }
        this.chaId = chaId;
        this.type = type;
    }


    public enum Type {
        Stream, Sample, Music
    }

    public int getChannelId() {
        return chaId;
    }

    public Type getType() {
        return type;
    }

    public boolean play(boolean loop) {
        return BASS.BASS_ChannelPlay(getChannelId(), loop);
    }

    public boolean play() {
        return play(false);
    }

    public double currentPlayTimeMS() {
        return currentPlayTimeS() * 1000;
    }

    public double currentPlayTimeS() {
        return BASS.BASS_ChannelBytes2Seconds(chaId, BASS.BASS_ChannelGetPosition(chaId, BASS.BASS_POS_BYTE));
    }

    public double getLength() {
        return BASS.BASS_ChannelBytes2Seconds(chaId, BASS.BASS_ChannelGetLength(chaId, BASS.BASS_POS_BYTE));
    }

    public void seekTo(double ms) {
        if (ms >= 0) {
            BASS.BASS_ChannelSetPosition(chaId, BASS.BASS_ChannelSeconds2Bytes(chaId, ms / 1000d), BASS.BASS_POS_BYTE);
        } else {

        }
    }


    public float getVolume() {
        Float f = new Float(0);
        BASS.BASS_ChannelGetAttribute(chaId, BASS.BASS_ATTRIB_VOL, f);
        return f;
    }

    public void setVolume(float volume) {
        BASS.BASS_ChannelSetAttribute(chaId, BASS.BASS_ATTRIB_VOL, volume);
    }

    private int getFFT(ByteBuffer buf, int fft_size) {
        return BASS.BASS_ChannelGetData(getChannelId(), buf, BASS.BASS_DATA_FFT512);
    }

    public int getFFT(float[] b) {
        ByteBuffer buf = ByteBuffer.allocateDirect(1024 * 2);
        buf.order(null);
        int r = getFFT(buf, BASS.BASS_DATA_FFT1024);
        buf.asFloatBuffer().get(b);
        return r;
    }

    public boolean pause() {
        return BASS.BASS_ChannelPause(getChannelId());
    }

    public boolean stop() {
        return BASS.BASS_ChannelStop(getChannelId());
    }

    public boolean free() {
        return BASS.BASS_StreamFree(getChannelId());
    }

    public static BassChannel createStreamFromFile(String file, int offset, int length, int flags) {
        return new BassChannel(BASS.BASS_StreamCreateFile(file, offset, length, flags), Type.Stream);
    }

    public static BassChannel createStreamFromFile(String file) {
        return createStreamFromFile(file, 0, 0, 0);
    }

    public static BassChannel createStreamFromBuffer(ByteBuffer buffer) {
        return new BassChannel(BASS.BASS_StreamCreateFile(buffer, 0, buffer.remaining(), 0), Type.Stream);
    }

    static {
        Bass.prepare();
    }
}
