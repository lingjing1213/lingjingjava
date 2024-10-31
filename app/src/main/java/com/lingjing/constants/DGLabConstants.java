package com.lingjing.constants;

import java.util.Arrays;
import java.util.List;

/**
 * @Author：灵静
 * @Package：com.lingjing.constants
 * @Project：lingjingjava
 * @name：DGLabTwo
 * @Date：2024/10/29 上午1:41
 * @Filename：DGLabTwo
 * @Version：1.0.0
 */
public class DGLabConstants {

    /**
     * 郊狼2.0 设备
     */
    public static final String DG_LAB_V2_NAME = "D-LAB ESTIM01";

    public static final String DG_LAB_V2_BATTERY_SERVICE = "955a180a-0fe2-f5aa-a094-84b8d4f3e8ad";

    public static final String DG_LAB_V2_BATTERY_CHARACTERISTIC = "955a1500-0fe2-f5aa-a094-84b8d4f3e8ad";

    public static final String DG_LAB_V2_PWM_AB_SERVICE = "955a180b-0fe2-f5aa-a094-84b8d4f3e8ad";

    public static final String DG_LAB_V2_PWM_AB_STRENGTH_CHARACTERISTIC = "955a1504-0fe2-f5aa-a094-84b8d4f3e8ad";

    public static final String DG_LAB_V2_WAVE_A_DIRECTION_CHARACTERISTIC = "955a1506-0fe2-f5aa-a094-84b8d4f3e8ad";

    public static final String DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC = "955a1505-0fe2-f5aa-a094-84b8d4f3e8ad";


    /**
     * 郊狼3.0 设备
     */
    public static final String DG_LAB_V3_NAME = "47L121000";


    /**
     * 呼吸波形
     */
    public static final List<byte[]> breatheDataV2 = Arrays.asList(
            new byte[]{1, 9, 0}, new byte[]{1, 9, 4},
            new byte[]{1, 9, 8}, new byte[]{1, 9, 16},
            new byte[]{1, 9, 20}, new byte[]{1, 9, 20},
            new byte[]{1, 9, 20}, new byte[]{1, 9, 0},
            new byte[]{1, 9, 0}, new byte[]{1, 9, 0},
            new byte[]{1, 9, 0});

    /**
     * 潮汐
     */
    public static final List<byte[]> tidalDataV2 = Arrays.asList(
            new byte[]{1, 9, 0}, new byte[]{1, 10, 3},
            new byte[]{1, 12, 6}, new byte[]{1, 13, 10},
            new byte[]{1, 15, 20}, new byte[]{1, 17, 20},
            new byte[]{1, 18, 20}, new byte[]{1, 20, 0},
            new byte[]{1, 21, 0}, new byte[]{1, 23, 0},
            new byte[]{1, 25, 0}, new byte[]{1, 25, 0},
            new byte[]{1, 26, 0}, new byte[]{1, 28, 0},
            new byte[]{1, 29, 0}, new byte[]{1, 31, 0},
            new byte[]{1, 33, 0}, new byte[]{1, 34, 0},
            new byte[]{1, 36, 0}, new byte[]{1, 37, 0},
            new byte[]{1, 39, 0}, new byte[]{1, 41, 0},
            new byte[]{1, 9, 0});

    /**
     * 不断变快
     */
    public static final List<byte[]> accelerateDataV2 = Arrays.asList(
            new byte[]{5, (byte) 135, 20}, new byte[]{5, 125, 20},
            new byte[]{5, 115, 20}, new byte[]{5, 105, 20},
            new byte[]{5, 95, 20}, new byte[]{4, 86, 20},
            new byte[]{4, 76, 20}, new byte[]{4, 66, 20},
            new byte[]{3, 57, 20}, new byte[]{3, 47, 20},
            new byte[]{3, 37, 20}, new byte[]{2, 28, 20},
            new byte[]{2, 18, 20}, new byte[]{1, 14, 20},
            new byte[]{1, 9, 20});

    /**
     * 推力
     */
    public static final List<byte[]> thrustDataV2 = Arrays.asList(
            new byte[]{1, 9, 4}, new byte[]{1, 9, 8},
            new byte[]{1, 9, 12}, new byte[]{1, 9, 16},
            new byte[]{1, 9, 18}, new byte[]{1, 9, 19},
            new byte[]{1, 9, 20}, new byte[]{1, 9, 0},
            new byte[]{1, 9, 0}, new byte[]{1, 9, 0});
}
