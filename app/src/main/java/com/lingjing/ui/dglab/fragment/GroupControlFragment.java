package com.lingjing.ui.dglab.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;

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
 * @Author：hifter
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：GroupControlkFragment
 * @Date：2024/11/2 下午1:00
 * @Filename：GroupControlkFragment
 * @Version：1.0.0
 */

public class GroupControlFragment extends Fragment {

    private DgLabButtonsFragment.ButtonsFragmentCallBack buttonsFragmentCallBack;

    private ImageButton backBut;

    private Button connectButton; // 添加连接按钮

    private boolean isButtonSelected = false; // 用于跟踪按钮选择状态

    private DgLabV2ViewModel dgLabV2ViewModel;

    private GridLayout gridLayout;

    public static GroupControlFragment newInstance() {
        return new GroupControlFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (DgLabButtonsFragment.ButtonsFragmentCallBack) getParentFragment();
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);

/*        dgLabV2ViewModel.getSendWaveResult().observe(this, sendWaveResult -> {
            if (ErrorTypes.ADD_WAVE_SUCCESS.getCode().equals(sendWaveResult)) {
                fetchDataAndAddButtons();
                ToastUtils.showToast(requireContext(), ErrorTypes.getMsgByCode(sendWaveResult));
            } else {
                ToastUtils.showToast(requireContext(), ErrorTypes.getMsgByCode(sendWaveResult));
            }

        });*/

        dgLabV2ViewModel.getDeleteWaveResult().observe(this, deleteResult -> {
            ToastUtils.showToast(requireContext(), ErrorTypes.getMsgByCode(deleteResult));
        });
    }

    private void fetchDataAndAddButtons() {
        // 这里示例为从 SharedPreferences 获取波形数据
        JSONArray waveDataArray = getWaveDataFromPreferences(); // 从 SharedPreferences 获取存储的数据

        if (waveDataArray != null) {
            addButtonsToGrid(waveDataArray);
        }
    }

    private void addButtonsToGrid(JSONArray waveDataArray) {
        // 遍历从 SharedPreferences 获取到的数据，动态添加按钮
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
                connectButton.setEnabled(true);
                List<int[]> waveList = JsonArrayUtils.convertJsonToIntArrayList(waveDataJson);
                dgLabV2ViewModel.setSelectedWaveformText(buttonName);
                Map<String, List<int[]>> waveformData = dgLabV2ViewModel.getWaveformData();
                waveformData.clear();
                dgLabV2ViewModel.setWaveformDataMap(buttonName, waveList); // 更新选中的波形文本
            });

            //长安按钮删除并且删除SharedPreferences里面存的那个数据
            button.setOnLongClickListener(v -> {
                showDeleteConfirmationDialog(button); // 长按时弹出删除确认框
                return true; // 返回 true 表示长按事件被消费
            });

            // 将新按钮添加到 GridLayout 中
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_control, container, false);
        gridLayout = view.findViewById(R.id.gridLayout);
        backBut = view.findViewById(R.id.back_but);
        connectButton = view.findViewById(R.id.connect_but);
        connectButton.setEnabled(false);
        gridLayout = view.findViewById(R.id.gridLayout);
        setupPredefinedButtons();

        fetchDataAndAddButtons();

//        返回按钮的监听器，返回时关闭连接的方法不建议写在里面，应写在返回的回调中
        backBut.setOnClickListener((v) -> {
            buttonsFragmentCallBack.popFragment();
        });

        connectButton.setOnClickListener(v -> {
            if (isButtonSelected) {
                // 执行连接操作
                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, MODE_PRIVATE);
                String rsaUserId = sharedPreferences.getString(LingJingConstants.USER_ID_KEY, "");
                String userId = null;
                try {
                    userId = RSAUtils.decrypt(rsaUserId);
                } catch (LingJingException e) {
                    Log.e("GroupControlkFragment", "解密失败", e);
                }
                dgLabV2ViewModel.connectWebSocket(userId);
                // 这里可以添加你的连接逻辑
            }
        });

        return view;
    }

    private void setupPredefinedButtons() {
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View childView = gridLayout.getChildAt(i);
            if (childView instanceof Button button) {
                button.setOnClickListener(v -> {
                    isButtonSelected = true;
                    connectButton.setEnabled(true);
                    String waveformText = button.getText().toString(); // 获取按钮文本
                    dgLabV2ViewModel.setSelectedWaveformText(waveformText); // 更新选中的波形文本
                });
            }
        }
    }
}