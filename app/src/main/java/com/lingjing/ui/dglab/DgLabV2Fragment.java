package com.lingjing.ui.dglab;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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
import com.lingjing.ui.dglab.fragment.DgLabButtonsFragment;
import com.lingjing.utils.ToastUtils;

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
public class DgLabV2Fragment extends Fragment implements  DgLabButtonsFragment.ButtonsFragmentCallBack{
    public static final String TAG = "DgLabV2Fragment";

    private DgLabV2ViewModel dgLabV2ViewModel;

    private TextView strengthAValueText;

    private TextView strengthBValueText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dglabv2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dgLabV2ViewModel = new ViewModelProvider(this).get(DgLabV2ViewModel.class);
        if (savedInstanceState == null) {
            DgLabButtonsFragment buttonsFragment = DgLabButtonsFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.buttonsFragmentContainer, buttonsFragment) // 这里的 id 是您在 fragment_dglabv2.xml 中为容器设置的
                    .commit();
        }

        observeViewModel();

        setupButtons(view);
    }

    private void observeViewModel() {
        dgLabV2ViewModel.getStrengthAValue().observe(getViewLifecycleOwner(), value -> strengthAValueText.setText(String.valueOf(value)));

        dgLabV2ViewModel.getStrengthBValue().observe(getViewLifecycleOwner(), value -> strengthBValueText.setText(String.valueOf(value)));
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

        // 设置 A 通道按钮
        setupButtonA(startPauseButtonA, strengthAPlus, strengthAMinus);

        // 设置 B 通道按钮
        setupButtonB(startPauseButtonB, strengthBPlus, strengthBMinus);
    }

    private void setupButtonA(ImageButton startPauseButtonA, ImageButton strengthAPlus, ImageButton strengthAMinus) {
        updatePlayPauseButtonA(startPauseButtonA);

        startPauseButtonA.setOnClickListener(v -> {
            dgLabV2ViewModel.togglePlayPauseA();
            updatePlayPauseButtonA(startPauseButtonA);
        });

        strengthAPlus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthA(dgLabV2ViewModel.getStrengthAValue().getValue() + 1));

        strengthAMinus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthA(dgLabV2ViewModel.getStrengthAValue().getValue() - 1));

        strengthAValueText.setOnClickListener(v -> showAInputDialog());
    }

    private void updatePlayPauseButtonA(ImageButton button) {
        if (dgLabV2ViewModel.isPlayingA()) {
            button.setImageResource(R.mipmap.ic_start);
        } else {
            button.setImageResource(R.mipmap.ic_pause);
        }
    }

    private void setupButtonB(ImageButton startPauseButtonB, ImageButton strengthBPlus, ImageButton strengthBMinus) {
        updatePlayPauseButtonB(startPauseButtonB);

        startPauseButtonB.setOnClickListener(v -> {
            dgLabV2ViewModel.togglePlayPauseB();
            updatePlayPauseButtonB(startPauseButtonB);
        });

        strengthBPlus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthB(dgLabV2ViewModel.getStrengthBValue().getValue() + 1));

        strengthBMinus.setOnClickListener(v -> dgLabV2ViewModel.updateStrengthB(dgLabV2ViewModel.getStrengthBValue().getValue() - 1));

        strengthBValueText.setOnClickListener(v -> showBInputDialog());
    }

    private void updatePlayPauseButtonB(ImageButton button) {
        if (dgLabV2ViewModel.isPlayingB()) {
            button.setImageResource(R.mipmap.ic_start);
        } else {
            button.setImageResource(R.mipmap.ic_pause);
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
}





