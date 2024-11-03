package com.lingjing.ui.dglab;

import static com.lingjing.constants.DGLabConstants.accelerateDataV2;
import static com.lingjing.constants.DGLabConstants.breatheDataV2;
import static com.lingjing.constants.DGLabConstants.thrustDataV2;
import static com.lingjing.constants.DGLabConstants.tidalDataV2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson2.JSONObject;
import com.lingjing.constants.DGLabConstants;
import com.lingjing.data.model.DGLabSocketMsg;
import com.lingjing.data.model.DGLabV2Model;
import com.lingjing.service.DGLabWebSocketClient;
import com.lingjing.service.WebSocketMessageListener;
import com.lingjing.ui.home.HomeActivity;
import com.lingjing.utils.BluetoothGattManager;
import com.lingjing.utils.StrengthAndWaveUtils;
import com.lingjing.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

public class DgLabV2ViewModel extends ViewModel implements WebSocketMessageListener {

    private static final String TAG = "DgLabV2ViewModel";
    private final DGLabV2Model dgLabV2Model = new DGLabV2Model();

    private final MutableLiveData<Integer> strengthAValue = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> strengthBValue = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> batteryLevel = new MutableLiveData<>();
    private Queue<DGLabSocketMsg> dgLabSocketMsgQueue = new LinkedList<>();
    private boolean isPlayingA = false;
    private boolean isPlayingB = false;

    private BluetoothGatt bluetoothGatt;
    private Handler handler = new Handler();
    private Runnable batteryLevelRunnable;
    private boolean isProcessingMessage = false; // 标志当前是否正在处理消息
    private final MutableLiveData<String> selectedWaveformText = new MutableLiveData<>();
    private DGLabWebSocketClient client;


    public LiveData<String> getSelectedWaveformText() {
        return selectedWaveformText;
    }

    public void setSelectedWaveformText(String waveformText) {
        selectedWaveformText.setValue(waveformText);
    }

    public LiveData<Integer> getBatteryLevel() {
        return batteryLevel;
    }

    public LiveData<Integer> getStrengthAValue() {
        return strengthAValue;
    }

    public LiveData<Integer> getStrengthBValue() {
        return strengthBValue;
    }

    public void updateStrengthA(int value) {
        if (dgLabV2Model.setStrengthAValue(value)) {
            strengthAValue.setValue(dgLabV2Model.getStrengthAValue());
        }
    }

    public void updateStrengthB(int value) {
        if (dgLabV2Model.setStrengthBValue(value)) {
            strengthBValue.setValue(dgLabV2Model.getStrengthBValue());
        }
    }

    public void togglePlayPauseA() {
        isPlayingA = !isPlayingA;
    }

    public void togglePlayPauseB() {
        isPlayingB = !isPlayingB;
    }

    public boolean isPlayingA() {
        return isPlayingA;
    }

    public boolean isPlayingB() {
        return isPlayingB;
    }

    public void connectWebSocket(String userId) {
        if (userId == null) {
            return;
        }
        if (dgLabV2Model.getStrengthAValue() != 0 || dgLabV2Model.getStrengthBValue() != 0) {
            this.client = new DGLabWebSocketClient(userId, dgLabV2Model);
            client.setMessageListener(this);
            client.connect();
            client.isConnected = true;
        } else {
            Log.e(TAG, "请先设置A通道或B通道的值");
        }
    }

    @Override
    public void onMessageReceived(String message) {
        Log.d(TAG, "接收到的 WebSocket 消息: " + message);
        JSONObject jsonObject = JSONObject.parseObject(message);
        int strength = jsonObject.getIntValue("strength");
        int time = jsonObject.getIntValue("time");

        float decimalValue = strength / 100f;
        float aValue = dgLabV2Model.getStrengthAValue() * decimalValue;
        float bValue = dgLabV2Model.getStrengthBValue() * decimalValue;

        int intAValue = Math.round(aValue);
        int intBValue = Math.round(bValue);
        DGLabSocketMsg dgLabSocketMsg = new DGLabSocketMsg(intAValue, intBValue, time);
        dgLabSocketMsgQueue.add(dgLabSocketMsg);

        if (!isProcessingMessage && client.isConnected) {
            processSocketMsgQueue();

        }
    }

    @SuppressLint("MissingPermission")
    public void connectAndReadBattery(Context context) {
        bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt().getDevice().connectGatt(context, false, bluetoothGattCallback);
        if (bluetoothGatt != null) {
            bluetoothGatt.connect();
        } else {
            ToastUtils.showToast(context, "连接失败");
        }
    }

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e(TAG, "连接断开");
                handler.removeCallbacks(batteryLevelRunnable); // 停止电量读取
            }
        }

        @SuppressLint("MissingPermission")
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic batteryCharacteristic = gatt
                        .getService(UUID.fromString(DGLabConstants.DG_LAB_V2_BATTERY_SERVICE))
                        .getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_BATTERY_CHARACTERISTIC));

                if (batteryCharacteristic != null) {
                    readBatteryLevel(batteryCharacteristic);
                    startBatteryLevelUpdates(); // 启动定时读取
                } else {
                    Log.e(TAG, "特征未找到。");
                }
            }
        }

        @SuppressLint("MissingPermission")
        private void readBatteryLevel(BluetoothGattCharacteristic characteristic) {
            bluetoothGatt.readCharacteristic(characteristic);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (DGLabConstants.DG_LAB_V2_BATTERY_CHARACTERISTIC.equals(characteristic.getUuid().toString())) {
                    byte[] value = characteristic.getValue();
                    if (value != null && value.length > 0) {
                        int battery = value[0] & 0xFF;
                        Log.d(TAG, "Battery Level: " + battery);
                        batteryLevel.postValue(battery);
                    }
                }
            }
        }

        private void startBatteryLevelUpdates() {
            batteryLevelRunnable = new Runnable() {
                @Override
                public void run() {
                    BluetoothGattCharacteristic characteristic = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_BATTERY_SERVICE))
                            .getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_BATTERY_CHARACTERISTIC));
                    if (characteristic != null) {
                        readBatteryLevel(characteristic);
                    }
                    handler.postDelayed(this, 60000); // 每60秒读取一次电量
                }
            };
            handler.post(batteryLevelRunnable); // 初始调用
        }
    };

    @SuppressLint("MissingPermission")
    public void disconnect() {
        Log.d(TAG, "断开连接");
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null; // 释放资源
            handler.removeCallbacks(batteryLevelRunnable); // 停止电量读取
        }
    }

    //当用户启动了socket连接以后，监听队列(dgLabSocketMsgQueue)中是否有数据选择是否执行，有数据获取第一个数据，把 intAValue写入通道A intBValue写入通道B，time为执行用户选择的波形的时间

    public void processSocketMsgQueue() {
        if (!dgLabSocketMsgQueue.isEmpty() && client.isConnected) {
            isProcessingMessage = true;
            DGLabSocketMsg msg = dgLabSocketMsgQueue.poll(); // 获取并移除队列的第一个元素
            if (msg != null) {
                int aIntensityValue = msg.getaValue();
                int bIntensityValue = msg.getbValue();
                int time = msg.getTime();

                // 将强度值写入设备
                writeIntensityToDevice(aIntensityValue, bIntensityValue);
                String value = selectedWaveformText.getValue();
                Log.d(TAG, "Selected waveform: " + value);

                // 获取GroupControlkFragment上用户选中的波形
                List<int[]> waveformData = getWaveformData(value);

                // 每100毫秒从 breatheDataV2(list<int[]>) 中取出下一个元素，并在到达数组末尾时重新开始。执行time时间
                processWaveformData(waveformData, time);

                // 根据 time 值执行用户选择的波形的时间
                Log.d(TAG, "开始执行波形，A通道强度: " + aIntensityValue + ", B通道强度: " + bIntensityValue + ", 执行时间: " + time + "秒");

                // 延迟 time 秒后继续处理队列中的下一个消息
                handler.postDelayed(() -> {
                    isProcessingMessage = false; // 处理完成，允许新消息
                    processSocketMsgQueue(); // 继续处理下一条消息
                }, time * 1000L);
            }
        } else if (!client.isConnected) {
            Log.d(TAG, "队列为空或未连接，无法处理消息");
        }
    }

    /**
     * 写入强度到设备
     *
     * @param aIntensityValue
     * @param bIntensityValue
     */
    @SuppressLint("MissingPermission")
    private void writeIntensityToDevice(int aIntensityValue, int bIntensityValue) {
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic pwmAB2Characteristic = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE))
                    .getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_STRENGTH_CHARACTERISTIC));
            if (pwmAB2Characteristic != null) {
                int aStrength = aIntensityValue * 7;
                int bStrength = bIntensityValue * 7;
                if (aStrength < 0 || aStrength > 2047 || bStrength < 0 || bStrength > 2047) {
                    return;
                }
                byte[] abPowerToByte = StrengthAndWaveUtils.abPowerToByte(aStrength, bStrength);
                pwmAB2Characteristic.setValue(abPowerToByte);
                boolean writeSuccess = bluetoothGatt.writeCharacteristic(pwmAB2Characteristic);
                Log.d(TAG, "Write success: " + writeSuccess + ", Values: " + Arrays.toString(abPowerToByte));
            }
        }
    }

    public List<int[]> getWaveformData(String waveformText) {
        switch (waveformText) {
            case "呼吸":
                return new ArrayList<>(breatheDataV2);
            case "潮汐":
                return new ArrayList<>(tidalDataV2);
            case "变快":
                return new ArrayList<>(accelerateDataV2);
            case "推力":
                return new ArrayList<>(thrustDataV2);
            default:
                return Collections.emptyList(); // 或者返回 null，根据你的需求
        }
    }


    private void processWaveformData(List<int[]> waveformData, int durationInSeconds) {
        int totalIterations = durationInSeconds * 10; // 100ms 为一个周期，总周期数
        int[] currentIndex = {0}; // 使用数组以便在内部类中修改

        Runnable waveformRunnable = new Runnable() {
            @Override
            public void run() {
                // 检查是否还需要继续执行
                if (currentIndex[0] < totalIterations) {
                    // 获取当前波形数据
                    int[] currentWaveform = waveformData.get(currentIndex[0] % waveformData.size());

                    // 执行写入当前波形数据的逻辑
                    byte[] wave = StrengthAndWaveUtils.wave(currentWaveform);
                    writeWaveformData(wave);

                    // 更新索引
                    currentIndex[0]++;

                    // 安排下一次执行
                    handler.postDelayed(this, 100); // 每100毫秒执行一次
                } else {
                    Log.d(TAG, "波形执行完毕");
                }
            }
        };
        // 启动波形处理任务
        handler.post(waveformRunnable);
    }

    private void writeWaveformData(byte[] currentWaveform) {
        if (bluetoothGatt == null){
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE));
        if (service != null) {
            BluetoothGattCharacteristic bChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC));
            if (bChannelCharacteristic != null) {
                bChannelCharacteristic.setValue(currentWaveform);
                @SuppressLint("MissingPermission") boolean writeSuccess = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
                Log.d(TAG, "Write success: " + writeSuccess + ", Values: " + Arrays.toString(currentWaveform));
            }
        }else {
            Log.d(TAG, "service is null");
        }
    }
}
