package com.lingjing.ui.dglab.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lingjing.R;
import com.lingjing.ui.dglab.DgLabV2ViewModel;
import com.lingjing.utils.ToastUtils;

import java.text.MessageFormat;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：DiyFragment
 * @Date：2024/11/9 下午7:50
 * @Filename：DiyFragment
 * @Version：1.0.0
 */
public class DiyFragment extends Fragment {

    private SeekBar pulseTimes;

    private SeekBar pulseWidth;

    private EditText pulseIntervalTime;

    private DgLabButtonsFragment.ButtonsFragmentCallBack buttonsFragmentCallBack;

    private DgLabV2ViewModel dgLabV2ViewModel;

    private static final int MIN_PULSE_TIMES = 1;   // 最小脉冲次数
    private static final int MAX_PULSE_TIMES = 31;  // 最大脉冲次数
    private static final int MIN_PULSE_WIDTH = 1;   // 最小脉冲宽度
    private static final int MAX_PULSE_WIDTH = 31;  // 最大脉冲宽度
    private static final int MIN_PULSE_INTERVAL = 0;// 最小脉冲间隔时间
    private static final int MAX_PULSE_INTERVAL = 1000;
    private TextView pulseTimesLabel;
    private TextView pulseWidthLabel;


    public static DiyFragment newInstance() {
        return new DiyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);
        buttonsFragmentCallBack = (DgLabButtonsFragment.ButtonsFragmentCallBack) getParentFragment();
        View view = inflater.inflate(R.layout.fragment_diy, container, false);
        pulseTimes = view.findViewById(R.id.pulseTimes);
        pulseWidth = view.findViewById(R.id.pulseWidth);
        pulseTimesLabel = view.findViewById(R.id.pulseTimesLabel);
        pulseWidthLabel = view.findViewById(R.id.pulseWidthLabel);
        pulseIntervalTime = view.findViewById(R.id.pulseIntervalTime);
        Button backButton = view.findViewById(R.id.backButton);
        observeViewModel();
        backButton.setOnClickListener((v) -> {
            buttonsFragmentCallBack.popFragment();
        });


        pulseTimes.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dgLabV2ViewModel.setPulseTimesValue(progress);
                pulseTimesLabel.setText(MessageFormat.format("脉冲次数：{0}", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // 更新脉冲宽度
        pulseWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dgLabV2ViewModel.setPulseWidthValue(progress);
                pulseWidthLabel.setText(MessageFormat.format("脉冲宽度：{0}", progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        pulseIntervalTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String pulseInterval = editable.toString();
                int value = pulseInterval.isEmpty() ? 0 : Integer.parseInt(pulseInterval);
                dgLabV2ViewModel.setPulseIntervalTimeValue(value); // 更新 ViewModel 中的脉冲间隔时间
            }
        });


        return view;
    }
    private void observeViewModel() {
        dgLabV2ViewModel.getPulseTimesValue().observe(getViewLifecycleOwner(), pulseTimesValue -> {
            if (pulseTimesValue == null || pulseTimesValue < MIN_PULSE_TIMES || pulseTimesValue > MAX_PULSE_TIMES) {
                ToastUtils.showToast(getContext(), "脉冲次数必须在 " + MIN_PULSE_TIMES + " 到 " + MAX_PULSE_TIMES + " 之间");
            }
        });

        dgLabV2ViewModel.getPulseWidthValue().observe(getViewLifecycleOwner(), pulseWidthValue -> {
            if (pulseWidthValue == null || pulseWidthValue < MIN_PULSE_WIDTH || pulseWidthValue > MAX_PULSE_WIDTH) {
                ToastUtils.showToast(getContext(), "脉冲宽度必须在 " + MIN_PULSE_WIDTH + " 到 " + MAX_PULSE_WIDTH + " 之间");
            }
        });

        dgLabV2ViewModel.getPulseIntervalTimeValue().observe(getViewLifecycleOwner(), pulseIntervalTimeValue -> {
            if (pulseIntervalTimeValue == null || pulseIntervalTimeValue < MIN_PULSE_INTERVAL || pulseIntervalTimeValue > MAX_PULSE_INTERVAL) {
                ToastUtils.showToast(getContext(), "脉冲间隔时间必须在 " + MIN_PULSE_INTERVAL + " 到 " + MAX_PULSE_INTERVAL + " 之间");
            }
        });
    }


}
