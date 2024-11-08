package com.lingjing.ui.dglab.fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lingjing.R;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.ui.dglab.DgLabV2ViewModel;
import com.lingjing.utils.RSAUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：DiyFragment
 * @Date：2024/11/7 下午8:14
 * @Filename：DiyFragment
 * @Version：1.0.0
 */
public class DiyFragment extends Fragment {
    private DgLabButtonsFragment.ButtonsFragmentCallBack buttonsFragmentCallBack;

    private ImageButton backBut;

    private Button connectButton; // 添加连接按钮

    private boolean isButtonSelected = false; // 用于跟踪按钮选择状态

    private DgLabV2ViewModel dgLabV2ViewModel;

    public static DiyFragment newInstance() {
        return new DiyFragment();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (DgLabButtonsFragment.ButtonsFragmentCallBack)getParentFragment();
        dgLabV2ViewModel = new ViewModelProvider(requireActivity()).get(DgLabV2ViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_control, container, false);
        backBut = view.findViewById(R.id.back_but);
        connectButton = view.findViewById(R.id.connect_but);
        connectButton.setEnabled(false);
        GridLayout gridLayout = view.findViewById(R.id.gridLayout);
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View childView = gridLayout.getChildAt(i);
            if (childView instanceof Button) {
                Button button = (Button) childView;
                button.setOnClickListener(v -> {
                    isButtonSelected = true;
                    connectButton.setEnabled(true);
                    String waveformText = button.getText().toString(); // 获取按钮文本
                    dgLabV2ViewModel.setSelectedWaveformText(waveformText); // 更新选中的波形文本
                });
            }
        }
//        返回按钮的监听器，返回时关闭连接的方法不建议写在里面，应写在返回的回调中 303493
        backBut.setOnClickListener((v)->{
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

}
