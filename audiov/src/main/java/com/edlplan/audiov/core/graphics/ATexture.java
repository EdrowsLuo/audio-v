package com.edlplan.audiov.core.graphics;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 抽象绘制的目标材质，EdGameFramework对应GLTexture，android对应Bitmap
 */
public abstract class ATexture {


    /**
     * @return 材质的宽度
     */
    public abstract int getWidth();

    /**
     * @return 材质的高度
     */
    public abstract int getHeight();


    /**
     * 创建Atexture的工厂
     */
    public static abstract class AFactory {
        /**
         * 创建一个空材质，在反射中使用，子类必须继承
         * @param w 创建的材质的宽
         * @param h 创建的材质的高
         */
        public abstract ATexture create(int w, int h);

        /**
         * 通过解析一组数据来创建材质
         * @param data 源数据，可能是读取自文件
         * @param offset 开始位置偏移
         * @param length 数据长度
         */
        public abstract ATexture create(byte[] data, int offset, int length);


        /**
         * 从文件创建材质
         * @param f 文件
         * @return  创建的材质
         * @throws IOException 读取文件出错时抛出
         */
        public ATexture createFromFile(File f) throws IOException {
            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
            FileInputStream in = new FileInputStream(f);
            byte[] buffer = new byte[1024];
            int l;
            while ((l = in.read(buffer)) != -1) {
                byteOutput.write(buffer, 0, l);
            }
            byte[] out = byteOutput.toByteArray();
            return create(out, 0, out.length);
        }

    }

    /**
     * 通过反射创建的默认工厂，此时要求对应的ATexture有对应的构造方法
     */
    public static class Factory extends AFactory{

        private Class<? extends ATexture> klass;

        private Constructor<? extends ATexture> whConstructor;

        private Constructor<? extends ATexture> byteConstructor;

        public Factory(Class<? extends ATexture> klass) throws NoSuchMethodException {
            this.klass = klass;
            whConstructor = klass.getConstructor(int.class, int.class);
            byteConstructor = klass.getConstructor(byte[].class, int.class, int.class);
        }

        @Override
        public ATexture create(int w, int h) {
            try {
                return whConstructor.newInstance(w, h);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public ATexture create(byte[] data, int offset, int length) {
            try {
                return whConstructor.newInstance(data, offset, length);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
