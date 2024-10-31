package com.lingjing.ui.dglab.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.lingjing.R;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dglab_buttons, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }
}
