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
    public static final List<int[]> breatheDataV2 = Arrays.asList(
            new int[]{1, 9, 0}, new int[]{1, 9, 4},
            new int[]{1, 9, 8}, new int[]{1, 9, 16},
            new int[]{1, 9, 20}, new int[]{1, 9, 20},
            new int[]{1, 9, 20}, new int[]{1, 9, 0},
            new int[]{1, 9, 0}, new int[]{1, 9, 0},
            new int[]{1, 9, 0});

    /**
     * 潮汐
     */
    public static final List<int[]> tidalDataV2 = Arrays.asList(
            new int[]{1, 9, 0}, new int[]{1, 10, 3},
            new int[]{1, 12, 6}, new int[]{1, 13, 10},
            new int[]{1, 15, 20}, new int[]{1, 17, 20},
            new int[]{1, 18, 20}, new int[]{1, 20, 0},
            new int[]{1, 21, 0}, new int[]{1, 23, 0},
            new int[]{1, 25, 0}, new int[]{1, 25, 0},
            new int[]{1, 26, 0}, new int[]{1, 28, 0},
            new int[]{1, 29, 0}, new int[]{1, 31, 0},
            new int[]{1, 33, 0}, new int[]{1, 34, 0},
            new int[]{1, 36, 0}, new int[]{1, 37, 0},
            new int[]{1, 39, 0}, new int[]{1, 41, 0},
            new int[]{1, 9, 0});

    /**
     * 不断变快
     */
    public static final List<int[]> accelerateDataV2 = Arrays.asList(
            new int[]{5, (byte) 135, 20}, new int[]{5, 125, 20},
            new int[]{5, 115, 20}, new int[]{5, 105, 20},
            new int[]{5, 95, 20}, new int[]{4, 86, 20},
            new int[]{4, 76, 20}, new int[]{4, 66, 20},
            new int[]{3, 57, 20}, new int[]{3, 47, 20},
            new int[]{3, 37, 20}, new int[]{2, 28, 20},
            new int[]{2, 18, 20}, new int[]{1, 14, 20},
            new int[]{1, 9, 20});

    /**
     * 推力
     */
    public static final List<int[]> thrustDataV2 = Arrays.asList(
            new int[]{1, 9, 4}, new int[]{1, 9, 8},
            new int[]{1, 9, 12}, new int[]{1, 9, 16},
            new int[]{1, 9, 18}, new int[]{1, 9, 19},
            new int[]{1, 9, 20}, new int[]{1, 9, 0},
            new int[]{1, 9, 0}, new int[]{1, 9, 0});
}
