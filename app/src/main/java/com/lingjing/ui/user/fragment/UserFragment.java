package com.lingjing.ui.user.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.lingjing.utils.ToastUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.user.fragment
 * @Project：lingjingjava
 * @name：UserFragment
 * @Date：2024/10/27 下午7:15
 * @Filename：UserFragment
 * @Version：1.0.0
 */
public class UserFragment extends Fragment {

    private static final String TAG = "UserFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ToastUtils.showToast(requireContext(), "用户界面");
    }
}
