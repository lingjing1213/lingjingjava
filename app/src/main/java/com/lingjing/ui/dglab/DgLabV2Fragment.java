package com.lingjing.ui.dglab;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lingjing.R;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.ui.dglab.fragment.DgLabButtonsFragment;
import com.lingjing.ui.dglab.fragment.DiyFragment;
import com.lingjing.utils.ToastUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab
 * @Project：lingjingjava
 * @name：DgLabV2Fragmet
 * @Date：2024/10/29 下午11:17
 * @Filename：DgLabV2Fragmet
 * @Version：1.0.0
 */
public class DgLabV2Fragment extends Fragment implements DgLabButtonsFragment.ButtonsFragmentCallBack {

    private DgLabV2ViewModel dgLabV2ViewModel;

    private TextView strengthAValueText;

    private TextView strengthBValueText;

    private TextView batteryText;

    private String waveformName;

    private boolean isAStart = false;

    private boolean isBStart = false;

    private DiyFragment diyFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);
        // 在这里开始连接并读取电量

        dgLabV2ViewModel.connectAndReadBattery(requireActivity().getApplicationContext());

        diyFragment = DiyFragment.newInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dglabv2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        batteryText = view.findViewById(R.id.power);
        observeViewModel();
        setupButtons(view);
    }

    private void observeViewModel() {
        dgLabV2ViewModel.getStrengthAValue().observe(getViewLifecycleOwner(), value -> strengthAValueText.setText(String.valueOf(value)));

        dgLabV2ViewModel.getStrengthBValue().observe(getViewLifecycleOwner(), value -> strengthBValueText.setText(String.valueOf(value)));

        dgLabV2ViewModel.getBatteryLevel().observe(getViewLifecycleOwner(), batteryLevel -> batteryText.setText(MessageFormat.format("电量：{0}%", batteryLevel)));

        dgLabV2ViewModel.getSelectedWaveformText().observe(getViewLifecycleOwner(), waveformText -> {
            if (StringUtils.isNotBlank(waveformText)) {
                waveformName = waveformText;
            } else {
                ToastUtils.showToast(requireContext(), ErrorTypes.WAVE_NOT_SELECTED.getMsg());
            }
        });


    }

    private void setupButtons(View view) {
        strengthAValueText = view.findViewById(R.id.strengthAValue);
        strengthBValueText = view.findViewById(R.id.strengthBValue);

        ImageButton startPauseButtonA = view.findViewById(R.id.startPauseButtonA);
        ImageButton strengthAPlus = view.findViewById(R.id.strengthAPlus);
        ImageButton strengthAMinus = view.findViewById(R.id.strengthAMinus);

        ImageButton startPauseButtonB = view.findViewById(R.id.startPauseButtonB);
        ImageButton strengthBPlus = view.findViewById(R.id.strengthBPlus);
        ImageButton strengthBMinus = view.findViewById(R.id.strengthBMinus);
        updatePlayPauseButtonA(startPauseButtonA);
        updatePlayPauseButtonB(startPauseButtonB);
        // 设置 A 通道按钮
        setupButtonA(startPauseButtonA, strengthAPlus, strengthAMinus);

        // 设置 B 通道按钮
        setupButtonB(startPauseButtonB, strengthBPlus, strengthBMinus);
    }

    private void setupButtonA(ImageButton startPauseButtonA, ImageButton strengthAPlus, ImageButton strengthAMinus) {
        startPauseButtonA.setOnClickListener(v -> {
            if (isAStart) {
                // 如果当前是播放状态，点击暂停
                isAStart = false;
                updatePlayPauseButtonA(startPauseButtonA);
                dgLabV2ViewModel.stopSendWaveDataA();
               // 设置为暂停状态
            } else {
                // 如果当前是暂停状态，点击开始
                if (StringUtils.isBlank(waveformName)){
                    ToastUtils.showToast(getContext(), ErrorTypes.WAVE_NOT_SELECTED.getMsg());
                }else {
                    isAStart = true;
                    updatePlayPauseButtonA(startPauseButtonA);
                    dgLabV2ViewModel.sendWaveDataA();  // 发送波形数据
                     // 设置为播放状态
                }

            }
        });

        strengthAPlus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthA(dgLabV2ViewModel.getStrengthAValue().getValue() + 1));

        strengthAMinus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthA(dgLabV2ViewModel.getStrengthAValue().getValue() - 1));

        strengthAValueText.setOnClickListener(v -> showAInputDialog());
    }

    private void updatePlayPauseButtonA(ImageButton button) {
        if (isAStart) {
            button.setImageResource(R.mipmap.ic_pause);
        } else {
            button.setImageResource(R.mipmap.ic_start);
        }
    }

    private void setupButtonB(ImageButton startPauseButtonB, ImageButton strengthBPlus, ImageButton strengthBMinus) {
        startPauseButtonB.setOnClickListener(v -> {
            if (isBStart) {
                // 如果当前是播放状态，点击暂停
                isBStart = false;
                updatePlayPauseButtonB(startPauseButtonB);
                dgLabV2ViewModel.stopSendWaveDataB();
                 // 设置为暂停状态
            } else {
                // 如果当前是暂停状态，点击开始
                if (StringUtils.isBlank(waveformName)){
                    ToastUtils.showToast(getContext(), ErrorTypes.WAVE_NOT_SELECTED.getMsg());
                }else {
                    isBStart = true;  // 设置为播放状态
                    updatePlayPauseButtonB(startPauseButtonB);
                    dgLabV2ViewModel.sendWaveDataB();  // 发送波形数据

                }

            }
        });

        strengthBPlus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthB(dgLabV2ViewModel.getStrengthBValue().getValue() + 1));

        strengthBMinus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthB(dgLabV2ViewModel.getStrengthBValue().getValue() - 1));

        strengthBValueText.setOnClickListener(v -> showBInputDialog());
    }

    private void updatePlayPauseButtonB(ImageButton button) {
        if (isBStart) {
            button.setImageResource(R.mipmap.ic_pause);
        } else {
            button.setImageResource(R.mipmap.ic_start);
        }
    }

    private void showAInputDialog() {
        showInputDialog("设置A通道值", dgLabV2ViewModel.getStrengthAValue().getValue(), newValue -> {
            dgLabV2ViewModel.updateStrengthA(newValue);
        });
    }

    private void showBInputDialog() {
        showInputDialog("设置B通道值", dgLabV2ViewModel.getStrengthBValue().getValue(), newValue -> {
            dgLabV2ViewModel.updateStrengthB(newValue);
        });
    }

    private void showInputDialog(String title, int currentValue, InputValueListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(currentValue));
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String userInput = input.getText().toString();
            try {
                int newValue = Integer.parseInt(userInput);
                listener.onInputValue(newValue);
            } catch (NumberFormatException e) {
                ToastUtils.showToast(getContext(), "请输入有效的数字");
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    // 接口用于监听用户输入
    interface InputValueListener {
        void onInputValue(int value);
    }

    @Override
    public void addFragment(Fragment fragment) {
//        getChildFragmentManager()方法可以获取自己的FragmentManager，而不是activity的FragmentManager，因为要管理的是子fragment
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.buttonsFragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void popFragment() {
        getChildFragmentManager().popBackStack();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 停止读取电量（如果需要的话）
        dgLabV2ViewModel.disconnect();
    }
}





