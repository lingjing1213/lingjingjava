package com.lingjing.ui.dglab.fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lingjing.R;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.ui.dglab.DgLabV2ViewModel;
import com.lingjing.utils.JsonArrayUtils;
import com.lingjing.utils.ToastUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：DgLabButtonsFragment
 * @Date：2024/10/30 下午7:37
 * @Filename：DgLabButtonsFragment
 * @Version：1.0.0
 */
public class DgLabButtonsFragment extends Fragment {

    public static final String TAG = "DgLabButtonsFragment";

    private Button groupControlBut;

    private ButtonsFragmentCallBack buttonsFragmentCallBack;

    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    private Button diyBtn;

    private Button importWaveBtn;

    private DgLabV2ViewModel dgLabV2ViewModel;

    private EditText editTextJsonInput;

    public static DgLabButtonsFragment newInstance() {
        return new DgLabButtonsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viwe = inflater.inflate(R.layout.fragment_dglab_buttons, container, false);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (ButtonsFragmentCallBack) getParentFragment();
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);
        groupControlBut = viwe.findViewById(R.id.groupControl);
        groupControlBut.setOnClickListener((v) -> {
            buttonsFragmentCallBack.addFragment(GroupControlFragment.newInstance());
        });

        diyBtn = viwe.findViewById(R.id.diy);

        diyBtn.setOnClickListener(v -> {
            buttonsFragmentCallBack.addFragment(SelfControlFragment.newInstance());
        });
        return viwe;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        importWaveBtn = view.findViewById(R.id.importWaveform);
        importWaveBtn.setOnClickListener(v -> {
            showJsonInputDialog();
        });
        observeViewModel();
    }

    private void observeViewModel() {
        dgLabV2ViewModel.getSendWaveResult().observe(getViewLifecycleOwner(), sendWaveResult -> ToastUtils.showToast(getContext(), ErrorTypes.getMsgByCode(sendWaveResult)));
    }



    private void showJsonInputDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        dialog.setContentView(R.layout.dialog_input);
        dialog.setCancelable(true);

        // 设置 BottomSheetDialog 的高度，使其适应屏幕的一半
        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        dialog.getBehavior().setPeekHeight(getResources().getDisplayMetrics().heightPixels / 2);
        dialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);

        editTextJsonInput = dialog.findViewById(R.id.editTextJsonInput);
        Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);

        editTextJsonInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                buttonConfirm.setEnabled(isValidJson(requireContext(), s.toString()));

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        if (buttonConfirm != null) {
            buttonConfirm.setOnClickListener(v -> {

                String jsonInput = editTextJsonInput.getText().toString();
                // 处理 JSON 数据
                dgLabV2ViewModel.sendWaveData(jsonInput);
                dialog.dismiss();
            });
        }

        dialog.show();
    }


    // 验证输入是否为有效的 JSON 格式
    public static boolean isValidJson(Context context, String jsonString) {
        try {
            boolean flag = false;
            JSONObject jsonObject = JSON.parseObject(jsonString);
            if (jsonObject == null) {
                return false;
            }
            String wave = jsonObject.get("wave").toString();
            String name = jsonObject.get("name").toString();
            if (StringUtils.isNotBlank(name) && StringUtils.isNotBlank(wave)) {
                if (name.length() > 8) {
                    ToastUtils.showToast(context, "格式错误:name不能超过8个字。");
                } else {
                    flag = JsonArrayUtils.validateAndAssign(wave);
                }

                if (!flag) {
                    ToastUtils.showToast(context, "格式错误:wave必须是一个有效的 JSON 数组。");
                }
            }
            return flag;
        } catch (JSONException | LingJingException e) {
            return false; // 解析失败，返回 false
        }
    }

    //    接口，在父fragment里面实现，然后可以进行调用，使得替换fragment的方法可以直接写在父fragment中
    public interface ButtonsFragmentCallBack {
        //        新增
        void addFragment(Fragment fragment);

        //        返回
        void popFragment();
    }
}
