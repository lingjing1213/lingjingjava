package com.lingjing.ui.home.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ToastUtils.showToast(requireContext(), "首页");
    }
}
