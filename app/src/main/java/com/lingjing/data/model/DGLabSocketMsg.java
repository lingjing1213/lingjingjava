package com.lingjing.data.model;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.model
 * @Project：lingjingjava
 * @name：DGLabSocketMsg
 * @Date：2024/11/3 下午4:30
 * @Filename：DGLabSocketMsg
 * @Version：1.0.0
 */
public class DGLabSocketMsg {

    private int aValue;

    private int bValue;

    private int time;

    public DGLabSocketMsg(int aValue, int bValue, int time) {
        this.aValue = aValue;
        this.bValue = bValue;
        this.time = time;
    }

    public int getaValue() {
        return aValue;
    }

    public int getbValue() {
        return bValue;
    }

    public int getTime() {
        return time;
    }
}
