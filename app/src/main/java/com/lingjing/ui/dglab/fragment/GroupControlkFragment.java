package com.lingjing.ui.dglab.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.lingjing.R;

/**
 * @Author：hifter
 * @Package：com.lingjing.ui.dglab.fragment
 * @Project：lingjingjava
 * @name：GroupControlkFragment
 * @Date：2024/11/2 下午1:00
 * @Filename：GroupControlkFragment
 * @Version：1.0.0
 */

public class GroupControlkFragment extends Fragment {
    private DgLabButtonsFragment.ButtonsFragmentCallBack buttonsFragmentCallBack;
    private ImageButton backBut;
    public static GroupControlkFragment newInstance() {
        GroupControlkFragment fragment = new GroupControlkFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (DgLabButtonsFragment.ButtonsFragmentCallBack)getParentFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group_controlk, container, false);
        backBut = view.findViewById(R.id.back_but);
//        返回按钮的监听器，返回时关闭连接的方法不建议写在里面，应写在返回的回调中
        backBut.setOnClickListener((v)->{
            buttonsFragmentCallBack.popFragment();
        });
        return view;
    }
}