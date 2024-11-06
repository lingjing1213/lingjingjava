package com.lingjing.data.model;

import java.io.Serializable;
import java.lang.ref.PhantomReference;
import java.util.List;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.model
 * @Project：lingjingjava
 * @name：DGLabJson
 * @Date：2024/11/5 下午9:20
 * @Filename：DGLabJson
 * @Version：1.0.0
 */
public class DGLabJson implements Serializable {

    private String name;

    private List<List<Integer>> wave;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<List<Integer>> getWave() {
        return wave;
    }

    public void setWave(List<List<Integer>> wave) {
        this.wave = wave;
    }
}
