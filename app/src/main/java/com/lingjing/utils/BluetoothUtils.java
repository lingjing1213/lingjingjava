package com.lingjing.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.function.Consumer;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：BluetoothUtils
 * @Date：2024/10/29 上午12:50
 * @Filename：BluetoothUtils
 * @Version：1.0.0
 */
public class BluetoothUtils {

    public static final int REQUEST_ENABLE_BLUETOOTH = 1001;

    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1002;

    private static final int REQUEST_LOCATION_PERMISSION = 1003;

    private final BluetoothAdapter bluetoothAdapter;

    private final Context context;

    private final ActivityResultLauncher<Intent> bluetoothActivityResultLauncher;

    public BluetoothUtils(Context context, ActivityResultLauncher<Intent> bluetoothActivityResultLauncher) {
        this.context = context;
        this.bluetoothActivityResultLauncher = bluetoothActivityResultLauncher;
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }

    public void checkBluetoothAndRequestPermissions(Activity activity) {
        if (bluetoothAdapter == null) {
            ToastUtils.showToast(context, "蓝牙不可用");
            return;
        }
        // 请求蓝牙权限
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            bluetoothActivityResultLauncher.launch(enableBtIntent);
        } else {
            // 蓝牙已启用，检查权限
            requestPermissions(activity);
        }

    }

    private void requestPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_BLUETOOTH_PERMISSIONS
                );
            } else {
                onPermissionsGranted();
            }
        } else {
            // 处理旧版本的请求逻辑
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                onPermissionsGranted();
            }
        }
    }

    private void onPermissionsGranted() {
        ToastUtils.showToast(context, "权限已授予,请打开蓝牙设备");
    }

    public void handleRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && allPermissionsGranted(grantResults)) {
                // 所有请求的权限均已授予
                onPermissionsGranted();
            } else {
                ToastUtils.showToast(context, "蓝牙权限是必须的");
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onPermissionsGranted();
            } else {
                ToastUtils.showToast(context, "定位权限是必须的");
            }
        }
    }

    private boolean allPermissionsGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void handleEnableBluetoothResult(int resultCode, Activity activity) {
        if (resultCode == Activity.RESULT_OK) {
            // 用户允许启用蓝牙，继续请求权限
            requestPermissions(activity);
        } else {
            // 用户拒绝启用蓝牙，返回主界面
            ToastUtils.showToast(context, "蓝牙权限是必须的");

        }
    }

    @SuppressLint("MissingPermission")
    public void startBluetoothScan(String targetDeviceName, ScanCallback callback) {
        BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        if (bluetoothLeScanner == null) {
            ToastUtils.showToast(context, "蓝牙扫描器不可用");
            return;
        }

        bluetoothLeScanner.startScan(new android.bluetooth.le.ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();
                if (device.getName() != null && device.getName().equals(targetDeviceName)) {
                    ToastUtils.showToast(context, "找到设备: " + device.getName());
                    bluetoothLeScanner.stopScan(this);// 停止扫描
                    callback.onDeviceFound(device); // 调用回调处理找到的设备

                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                ToastUtils.showToast(context, "未找到设备，错误代码: " + errorCode);
            }
        });
    }

    @SuppressLint("MissingPermission")
    public void connectToDevice(BluetoothDevice device, Consumer<Boolean> callback) {
        device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                boolean isConnected = (newState == BluetoothGatt.STATE_CONNECTED);
                callback.accept(isConnected);
                if (isConnected) {
                    gatt.discoverServices();
                }
            }
        });
    }

    public interface ConnectionCallback {
        void onConnectionStateChange(boolean isConnected);
    }

    public interface ScanCallback {
        void onDeviceFound(BluetoothDevice device);
    }

    public Context getContext() {
        return context;
    }
}
