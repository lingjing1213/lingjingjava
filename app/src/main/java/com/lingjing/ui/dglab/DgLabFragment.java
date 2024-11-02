package com.lingjing.ui.dglab;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.lingjing.R;
import com.lingjing.constants.DGLabConstants;
import com.lingjing.factory.DgLabViewModelFactory;
import com.lingjing.utils.BluetoothUtils;
import com.lingjing.utils.ToastUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab
 * @Project：lingjingjava
 * @name：DgLabFragment
 * @Date：2024/11/1 上午1:42
 * @Filename：DgLabFragment
 * @Version：1.0.0
 */
public class DgLabFragment extends Fragment {

    private DgLabViewModel dgLabViewModel;

    private boolean bluetoothResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dglab_version, container, false);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ActivityResultLauncher<Intent> bluetoothActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // 处理蓝牙启用的结果
                   dgLabViewModel.handleEnableBluetoothResult(result.getResultCode(), getActivity());

                }
        );

        // 使用 ViewModelFactory 创建 ViewModel
        DgLabViewModelFactory factory = new DgLabViewModelFactory(requireActivity().getApplication(), bluetoothActivityResultLauncher);
        dgLabViewModel = new ViewModelProvider(this, factory).get(DgLabViewModel.class);

        Button dgLabV2Btn = view.findViewById(R.id.dgLabV2Btn);
        Button dgLabV3Btn = view.findViewById(R.id.dgLabV3Btn);



        dgLabV2Btn.setOnClickListener(v -> {
            dgLabViewModel.checkBluetoothAndRequestPermissions(getActivity());

            dgLabViewModel.startBluetoothScan(DGLabConstants.DG_LAB_V2_NAME, new BluetoothUtils.ScanCallback() {
                @Override
                public void onDeviceFound(BluetoothDevice device) {
                    // 处理找到的设备
                    ToastUtils.showToast(getContext(), "找到设备: " + device.getName());
                    // 进行连接
                    dgLabViewModel.connectToDevice(device);
                }
            });

            dgLabViewModel.getPermissionGranted().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean granted) {
                    if (granted) {
                        // 权限被授予，开始扫描设备
                        dgLabViewModel.startBluetoothScan(DGLabConstants.DG_LAB_V2_NAME, new BluetoothUtils.ScanCallback() {
                            @Override
                            public void onDeviceFound(BluetoothDevice device) {
                                // 处理找到的设备
                                ToastUtils.showToast(getContext(), "找到设备: " + device.getName());
                                // 进行连接
                                dgLabViewModel.connectToDevice(device);
                            }
                        });
                    } else {
                        // 权限未授予，提示用户
                        ToastUtils.showToast(getContext(), "请授予蓝牙权限");
                    }
                }
            });

            // 观察连接状态
            // 观察连接状态
            dgLabViewModel.getConnectionStatus().observe(getViewLifecycleOwner(), isConnected -> {
                if (isConnected) {
                    // 跳转到下一个界面
                    navigateToNextScreen();
                } else {
                    ToastUtils.showToast(getContext(), "连接失败");
                }
            });
        });

        dgLabV3Btn.setOnClickListener(v -> {
            ToastUtils.showToast(getContext(), "待开发");
        });

    }

    private void navigateToNextScreen() {
        // 这里实现跳转逻辑，比如使用 NavController 或 Intent
        Intent intent = new Intent(getActivity(), DgLabV2Activity.class);
        startActivity(intent);

    }
}

