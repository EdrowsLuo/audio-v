package com.edlplan.audiov.core.audio;

import java.io.File;

public interface IAudioEntry {

    int FLAG_FFT1024 = 1;

    int FLAG_FFT512 = 2;

    /**
     * 播放前的准备（部分库需要缓冲设置的）
     */
    void prepare();

    /**
     * 播放，从中止位置开始或者是从开头开始
     */
    void play();

    /**
     * 暂停播放，下次开始时是从中断点开始
     */
    void pause();

    /**
     * 停止播放，下次开始重置进度从头开始
     */
    void stop();

    /**
     * 释放内存，播放音频一般涉及到native层，于是添加手动释放内存的方法
     */
    void release();

    /**
     * @return 当前歌曲播放的位置，单位ms
     */
    double position();

    /**
     * @return 歌曲长度，单位ms
     */
    double length();

    /**
     * 跳转进度
     * @param ms 跳转到的位置，单位ms
     */
    void seekTo(double ms);

    /**
     * @param volume 要设置的音量
     */
    void setVolume(float volume);

    /**
     * @return 设置的音量
     */
    float getVolume();

    /**
     * 获取频谱数据
     * @param array 接受数据的数组
     * @param type 要获取的数据长度，属于FLAG_FFTxxxx格式的全局变量之一
     */
    void getFFT(float[] array, int type);


    abstract class AFactory{
        /**
         * @param file 音频文件
         * @return 返回的音频对象
         */
        public abstract IAudioEntry create(File file);
    }
}
