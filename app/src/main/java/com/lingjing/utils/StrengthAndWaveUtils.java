package com.lingjing.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：StrengthAndWaveUtils
 * @Date：2024/10/31 下午1:38
 * @Filename：StrengthAndWaveUtils
 * @Version：1.0.0
 */
public class StrengthAndWaveUtils {

    public static byte[] wave(int[] ints) {

        int x=ints[0];
        int y=ints[1];
        int z=ints[2];
        int xBits = x & 0x1F;
        int yBits = (y & 0x3FF) << 5;
        int zBits = (z & 0x1F) << 15;
        int data = zBits | yBits | xBits;

        return new byte[]{
                (byte) (data & 0xFF),          // 第一个字节
                (byte) ((data >> 8) & 0xFF),   // 第二个字节
                (byte) ((data >> 16) & 0xFF)   // 第三个字节
        };
    }

    public static byte[] abPowerToByte(int a, int b) {
        int bChannelBits = b & 0x7FF;
        int aChannelBits = (a & 0x7FF) << 11;
        int data = (aChannelBits | bChannelBits) & 0xFFFFFF;
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(data);
        byte[] bytes = new byte[3];
        byteBuffer.rewind();
        byteBuffer.get(bytes);
        return bytes;
 /*       return new byte[]{
                (byte) (data & 0xFF),         // 第一个字节
                (byte) ((data >> 8) & 0xFF),  // 第二个字节
                (byte) ((data >> 16) & 0xFF)  // 第三个字节
        };*/
    }

    public static byte[] wave(int x, int y, int z) {
        int xBits = x & 0x1F;
        int yBits = (y & 0x3FF) << 5;
        int zBits = (z & 0x1F) << 15;
        int data = zBits | yBits | xBits;

        return new byte[]{
                (byte) (data & 0xFF),          // 第一个字节
                (byte) ((data >> 8) & 0xFF),   // 第二个字节
                (byte) ((data >> 16) & 0xFF)   // 第三个字节
        };
    }
}
