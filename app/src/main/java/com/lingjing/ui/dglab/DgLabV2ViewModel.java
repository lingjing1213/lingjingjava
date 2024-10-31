package com.lingjing.ui.dglab;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lingjing.data.model.DGLabV2Model;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab
 * @Project：lingjingjava
 * @name：DgLabV2Model
 * @Date：2024/10/29 下午11:19
 * @Filename：DgLabV2Model
 * @Version：1.0.0
 */
public class DgLabV2ViewModel extends ViewModel {

    private final DGLabV2Model dgLabV2Model =new DGLabV2Model();
    private final MutableLiveData<Integer> strengthAValue = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> strengthBValue = new MutableLiveData<>(0);

    private boolean isPlayingA = false;
    private boolean isPlayingB = false;

    public LiveData<Integer> getStrengthAValue() {
        return strengthAValue;
    }

    public LiveData<Integer> getStrengthBValue() {
        return strengthBValue;
    }

    public void updateStrengthA(int value) {
        if (dgLabV2Model.setStrengthAValue(value)) {
            strengthAValue.setValue(dgLabV2Model.getStrengthAValue());
        }
    }

    public void updateStrengthB(int value) {
        if (dgLabV2Model.setStrengthBValue(value)) {
            strengthBValue.setValue(dgLabV2Model.getStrengthBValue());
        }
    }

    public void togglePlayPauseA() {
        isPlayingA = !isPlayingA;
    }

    public void togglePlayPauseB() {
        isPlayingB = !isPlayingB;
    }

    public boolean isPlayingA() {
        return isPlayingA;
    }

    public boolean isPlayingB() {
        return isPlayingB;
    }
}
