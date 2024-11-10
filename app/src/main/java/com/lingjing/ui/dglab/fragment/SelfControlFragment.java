package com.lingjing.ui.dglab.fragment;

import static android.content.Context.MODE_PRIVATE;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lingjing.R;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.ui.dglab.DgLabV2ViewModel;
import com.lingjing.utils.JsonArrayUtils;
import com.lingjing.utils.RSAUtils;
import com.lingjing.utils.ToastUtils;
import java.util.List;
import java.util.Map;


/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：DiyFragment
 * @Date：2024/11/7 下午8:14
 * @Filename：DiyFragment
 * @Version：1.0.0
 */
public class SelfControlFragment extends Fragment {
    private DgLabButtonsFragment.ButtonsFragmentCallBack buttonsFragmentCallBack;

    private Button backBut;

    private boolean isButtonSelected = false;

    private DgLabV2ViewModel dgLabV2ViewModel;

    private GridLayout gridLayout;

    private Button selectedButton = null;

    private Button diyBtn;

    public static SelfControlFragment newInstance() {
        return new SelfControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (DgLabButtonsFragment.ButtonsFragmentCallBack) getParentFragment();
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_self_control, container, false);
        gridLayout = view.findViewById(R.id.gridLayout);
        backBut = view.findViewById(R.id.back_but);
        gridLayout = view.findViewById(R.id.gridLayout);
        diyBtn= view.findViewById(R.id.diy_button);
        setupPredefinedButtons();
        fetchDataAndAddButtons();
        observeViewModel();
        backBut.setOnClickListener((v) -> {
            buttonsFragmentCallBack.popFragment();
        });

        diyBtn.setOnClickListener(v -> {
            //打开一个新的Fragment页面 todo
            buttonsFragmentCallBack.addFragment(DiyFragment.newInstance());

        });
        return view;
    }

    /**
     * 从SharedPreferences中获取保存的波形数据，并将其添加到网格布局中
     */
    private void fetchDataAndAddButtons() {
        JSONArray waveDataArray = getWaveDataFromPreferences();
        if (waveDataArray != null) {
            addButtonsToGrid(waveDataArray);
        }
    }

    private void addButtonsToGrid(JSONArray waveDataArray) {
        for (int i = 0; i < waveDataArray.size(); i++) {
            JSONObject waveData = waveDataArray.getJSONObject(i);
            String buttonName = waveData.getString("name");
            String waveDataJson = waveData.getString("wave");
            // 创建新的按钮
            Button button = new Button(requireContext());
            button.setText(buttonName); // 设置按钮文本

            // 设置按钮点击事件
            button.setOnClickListener(v -> {
                isButtonSelected = true;
                if (selectedButton != null) {
                    // 恢复之前选中按钮的背景色
                    selectedButton.setBackgroundResource(R.drawable.default_button_background);
                }
                button.setBackgroundResource(R.drawable.selected_button_background);
                selectedButton = button;
                List<int[]> waveList = JsonArrayUtils.convertJsonToIntArrayList(waveDataJson);
                dgLabV2ViewModel.setSelectedWaveformText(buttonName);
                Map<String, List<int[]>> waveformData = dgLabV2ViewModel.getWaveformData();
                waveformData.clear();
                dgLabV2ViewModel.setWaveformDataMap(buttonName, waveList);
            });

            button.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(button);
                return true;
            });

            gridLayout.addView(button);
        }
    }

    private void showDeleteConfirmationDialog(Button button) {
        new AlertDialog.Builder(requireContext())
                .setTitle("删除按钮")
                .setMessage("你确定要删除这个按钮吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 删除按钮及其数据
                    deleteButton(button);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 删除按钮
     *
     * @param button
     */
    private void deleteButton(Button button) {
        gridLayout.removeView(button);
        JSONArray waveDataArray = getWaveDataFromPreferences();
        if (waveDataArray != null) {
            String buttonName = button.getText().toString();
            for (int i = 0; i < waveDataArray.size(); i++) {
                JSONObject waveData = waveDataArray.getJSONObject(i);
                if (waveData.getString("name").equals(buttonName)) {
                    waveDataArray.remove(i);
                    break;
                }
            }
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, MODE_PRIVATE);
            sharedPreferences.edit()
                    .putString(LingJingConstants.WAVE_DATA_KEY, waveDataArray.toJSONString())
                    .apply();
            String rsaUserId = sharedPreferences.getString(LingJingConstants.USER_ID_KEY, "");
            String userId = null;
            try {
                userId = RSAUtils.decrypt(rsaUserId);
            } catch (LingJingException e) {
                Log.e("GroupControlkFragment", "解密失败", e);
            }
            dgLabV2ViewModel.deleteWaveData(userId, buttonName);
        }
    }

    private JSONArray getWaveDataFromPreferences() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, MODE_PRIVATE);
        String waveDataJson = sharedPreferences.getString(LingJingConstants.WAVE_DATA_KEY, "[]");
        return JSON.parseArray(waveDataJson);
    }

    /**
     * 设置预置按钮的
     */
    private void setupPredefinedButtons() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View childView = gridLayout.getChildAt(i);
            if (childView instanceof Button button) {
                button.setOnClickListener(v -> {
                    isButtonSelected = true;
                    if (selectedButton != null) {
                        // 恢复之前选中按钮的背景色
                        selectedButton.setBackgroundResource(R.drawable.default_button_background);
                    }
                    button.setBackgroundResource(R.drawable.selected_button_background);
                    selectedButton = button;
                    String waveformText = button.getText().toString(); // 获取按钮文本
                    dgLabV2ViewModel.setSelectedWaveformText(waveformText); // 更新选中的波形文本
                });
            }
        }
    }

    /**
     * 观察ViewModel的数据
     */
    private void observeViewModel() {

        //观察删除波形结果
        dgLabV2ViewModel.getDeleteWaveResult().observe(getViewLifecycleOwner(), deleteResult -> {
            if (deleteResult != null) {
                ToastUtils.showToast(requireContext(), ErrorTypes.getMsgByCode(deleteResult));
            }
        });
    }
}
