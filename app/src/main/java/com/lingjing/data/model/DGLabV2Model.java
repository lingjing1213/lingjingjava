package com.lingjing.data.model;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.model
 * @Project：lingjingjava
 * @name：DGLabV2Model
 * @Date：2024/10/30 下午8:25
 * @Filename：DGLabV2Model
 * @Version：1.0.0
 */
public class DGLabV2Model {

    private int strengthAValue;
    private int strengthBValue;

    public int getStrengthAValue() {
        return strengthAValue;
    }

    public int getStrengthBValue() {
        return strengthBValue;
    }

    public boolean setStrengthAValue(int value) {
        if (value >= 0 && value <= 290) {
            strengthAValue = value;
            return true;
        }
        return false;
    }

    public boolean setStrengthBValue(int value) {
        if (value >= 0 && value <= 290) {
            strengthBValue = value;
            return true;
        }
        return false;
    }
}
