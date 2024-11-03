package com.lingjing.ui.dglab.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.lingjing.R;
import com.lingjing.data.model.DGLabV2Model;
import com.lingjing.service.DGLabWebSocketClient;
import com.lingjing.service.WebSocketMessageListener;

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

    private DGLabWebSocketClient dgLabWebSocketClient;

    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();

    private Button diyBtn;

    public static DgLabButtonsFragment newInstance(){
        return new DgLabButtonsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viwe = inflater.inflate(R.layout.fragment_dglab_buttons, container, false);
//        获取父fragment的对象，然后转化为buttonsFragmentCallBack对象来存储
        buttonsFragmentCallBack = (ButtonsFragmentCallBack)getParentFragment();

        groupControlBut = (Button) viwe.findViewById(R.id.groupControl);
        groupControlBut.setOnClickListener((v)->{
            buttonsFragmentCallBack.addFragment(GroupControlkFragment.newInstance());
        });
        return viwe;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    //    接口，在父fragment里面实现，然后可以进行调用，使得替换fragment的方法可以直接写在父fragment中
    public interface ButtonsFragmentCallBack {
//        新增
        void addFragment(Fragment fragment);
//        返回
        void popFragment();
    }
}
