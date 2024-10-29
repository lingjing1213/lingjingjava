package com.lingjing.ui.home.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.lingjing.R;
import com.lingjing.utils.ToastUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.home.fragment
 * @Project：lingjingjava
 * @name：HomeFragment
 * @Date：2024/10/27 下午7:10
 * @Filename：HomeFragment
 * @Version：1.0.0
 */
public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button keyBoxBtn = view.findViewById(R.id.keyBoxBtn);
        Button dgLabBtn = view.findViewById(R.id.dgLabBtn);
        Button button3 = view.findViewById(R.id.button3);
        Button button4 = view.findViewById(R.id.button4);
        Button button5 = view.findViewById(R.id.button5);
        Button button6 = view.findViewById(R.id.button6);

        keyBoxBtn.setOnClickListener(v -> {
            // Button 1 点击事件
            ToastUtils.showToast(getContext(), "待开发");
        });

        dgLabBtn.setOnClickListener(v -> {
            // Button 2 点击事件

        });

        // 其他按钮的点击事件...
    }
}
