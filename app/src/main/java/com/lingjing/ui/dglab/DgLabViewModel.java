package com.lingjing.ui.dglab;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lingjing.service.WebSocketMessageListener;
import com.lingjing.utils.BluetoothGattManager;
import com.lingjing.utils.BluetoothUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab
 * @Project：lingjingjava
 * @name：DgLabViewModel
 * @Date：2024/11/1 下午5:50
 * @Filename：DgLabViewModel
 * @Version：1.0.0
 */
public class DgLabViewModel extends ViewModel {

    private static final String TAG = "DgLabViewModel";

    private final BluetoothUtils bluetoothUtils;

    private BluetoothGattManager bluetoothGattManager;

    public final MutableLiveData<BluetoothDevice> connectedDevice = new MutableLiveData<>();

    public final MutableLiveData<Boolean> connectionStatus = new MutableLiveData<>();

    private final Application application;

    // 可以使用 LiveData 来跟踪权限请求的结果
    private final MutableLiveData<Boolean> permissionGranted = new MutableLiveData<>();

    public DgLabViewModel(Application application, ActivityResultLauncher<Intent> bluetoothActivityResultLauncher) {
        // 使用 Application Context
        this.application= application;
        bluetoothUtils = new BluetoothUtils(application, bluetoothActivityResultLauncher);
    }

    public void checkBluetoothAndRequestPermissions(Activity activity) {
        bluetoothUtils.checkBluetoothAndRequestPermissions(activity);
    }


    public void handleRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        bluetoothUtils.handleRequestPermissionsResult(activity, requestCode, permissions, grantResults);
        // 根据权限请求的结果更新 LiveData
        if (requestCode == BluetoothUtils.REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            permissionGranted.setValue(allGranted);
        }
    }

    public LiveData<Boolean> getPermissionGranted() {
        return permissionGranted;
    }

    public void handleEnableBluetoothResult(int resultCode, FragmentActivity activity) {
        bluetoothUtils.handleEnableBluetoothResult(resultCode, activity);
    }

    public void startBluetoothScan(String targetDeviceName, BluetoothUtils.ScanCallback callback) {
        bluetoothUtils.startBluetoothScan(targetDeviceName, new BluetoothUtils.ScanCallback() {
            @Override
            public void onDeviceFound(BluetoothDevice device) {
                // 当找到设备时，触发回调
                connectedDevice.setValue(device); // 更新 LiveData，通知观察者
                callback.onDeviceFound(device);
            }
        });
    }
    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device) {
        bluetoothUtils.connectToDevice(device, isConnected -> {
            Log.d(TAG, "连接状态: " + (isConnected ? "连接成功" : "连接失败"));
            if (isConnected) {
                BluetoothGatt gatt = device.connectGatt(application, false, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        if (newState == BluetoothGatt.STATE_CONNECTED) {
                            Log.d(TAG, "设备连接成功");
                            BluetoothGattManager.getInstance().setBluetoothGatt(gatt);
                            gatt.discoverServices();
                            connectionStatus.postValue(true);
                        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                            Log.d(TAG, "设备断开连接");
                            connectionStatus.postValue(false);
                        }
                    }
                });
            }
        });
    }

    public LiveData<Boolean> getConnectionStatus() {
        return connectionStatus;
    }


}

