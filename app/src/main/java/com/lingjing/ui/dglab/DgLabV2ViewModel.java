package com.lingjing.ui.dglab;

import static com.lingjing.constants.DGLabConstants.accelerateDataV2;
import static com.lingjing.constants.DGLabConstants.breatheDataV2;
import static com.lingjing.constants.DGLabConstants.thrustDataV2;
import static com.lingjing.constants.DGLabConstants.tidalDataV2;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lingjing.constants.DGLabConstants;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.data.model.DGLabSocketMsg;
import com.lingjing.data.model.DGLabV2Model;
import com.lingjing.data.repository.DGLabSaveWaveRepository;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.service.DGLabWebSocketClient;
import com.lingjing.service.WebSocketMessageListener;
import com.lingjing.ui.home.HomeActivity;
import com.lingjing.utils.BluetoothGattManager;
import com.lingjing.utils.RSAUtils;
import com.lingjing.utils.StrengthAndWaveUtils;
import com.lingjing.utils.ToastUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DgLabV2ViewModel extends AndroidViewModel implements WebSocketMessageListener {

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
    private DGLabSaveWaveRepository saveWaveRepository = new DGLabSaveWaveRepository();
    private MutableLiveData<Boolean> sendWaveResult = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> currentWaveformTask;
    private boolean isTaskRunning = false;

    /**
     * 测试新增线程控制另一个通道
     */




    public DgLabV2ViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = getApplication().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }


    public LiveData<String> getSelectedWaveformText() {
        return selectedWaveformText;
    }


    public LiveData<Boolean> getSendWaveResult() {
        return sendWaveResult;
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

    /**
     * 断开连接
     */
    @SuppressLint("MissingPermission")
    public void disconnect() {
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
            DGLabSocketMsg msg = dgLabSocketMsgQueue.poll();
            if (msg != null) {
                int aIntensityValue = msg.getaValue();
                int bIntensityValue = msg.getbValue();
                int time = msg.getTime();

                writeIntensityToDevice(aIntensityValue, bIntensityValue);
                String value = selectedWaveformText.getValue();
                Log.d(TAG, "Selected waveform: " + value);

                List<int[]> waveformData = getWaveformData(value);

                // 串行执行选中的波形数据
                processWaveformData(waveformData, time);

                Log.d(TAG, "开始执行波形，A通道强度: " + aIntensityValue + ", B通道强度: " + bIntensityValue + ", 执行时间: " + time + "秒");

                // 延迟 time 秒后继续处理下一条消息
                handler.postDelayed(() -> {
                    isProcessingMessage = false;
                    processSocketMsgQueue();
                }, time * 1000L);
            }
        } else if (!client.isConnected) {
            Log.d(TAG, "队列为空或未连接，无法处理消息");
        }
    }

    private void processWaveformData(List<int[]> waveformData, int durationInSeconds) {
        int totalIterations = durationInSeconds * 10;
        int[] currentIndex = {0};

        if (currentWaveformTask != null && !currentWaveformTask.isCancelled()) {
            currentWaveformTask.cancel(false); // 停止之前的任务
        }

        Runnable waveformRunnable = () -> {
            if (currentIndex[0] < totalIterations) {
                int[] currentWaveform = waveformData.get(currentIndex[0] % waveformData.size());

                // 执行写入 A 通道波形数据的逻辑
                byte[] aWave = StrengthAndWaveUtils.wave(currentWaveform);

                // 执行写入 B 通道波形数据的逻辑
                byte[] bWave = StrengthAndWaveUtils.wave(currentWaveform);
                writeWaveformDataSequentially(aWave,bWave);
                currentIndex[0]++;
            } else {
                Log.d(TAG, "波形执行完毕");
                if (currentWaveformTask != null) {
                    currentWaveformTask.cancel(false); // 停止任务调度
                }
            }
        };

        // 每 100 毫秒执行一次波形任务
        currentWaveformTask = scheduler.scheduleWithFixedDelay(waveformRunnable, 0, 100, TimeUnit.MILLISECONDS);
    }




    @SuppressLint("MissingPermission")
    private void writeWaveformDataSequentially(byte[] aWave, byte[] bWave) {
        if (bluetoothGatt == null) {
            Log.d(TAG, "BluetoothGatt 未连接");
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE));
        if (service != null) {
            // 写入 A 通道
            BluetoothGattCharacteristic aChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_A_DIRECTION_CHARACTERISTIC));
            if (aChannelCharacteristic != null) {
                aChannelCharacteristic.setValue(aWave);
                boolean writeSuccessA = bluetoothGatt.writeCharacteristic(aChannelCharacteristic);
                Log.d(TAG, "写入A通道是否成功: " + writeSuccessA + ", Values: " + Arrays.toString(aWave));
            }

            // 延迟后写入 B 通道
            handler.postDelayed(() -> {
                BluetoothGattCharacteristic bChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC));
                if (bChannelCharacteristic != null) {
                    bChannelCharacteristic.setValue(bWave);
                    boolean writeSuccessB = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
                    Log.d(TAG, "写入B通道是否成功: " + writeSuccessB + ", Values: " + Arrays.toString(bWave));
                }
            }, 50); // 50 毫秒延迟，可根据需要调整
        } else {
            Log.d(TAG, "未找到对应的Service");
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

    public void sendWaveData(String jsonData) {
        String encryptedUserId = sharedPreferences.getString(LingJingConstants.USER_ID_KEY, null);
        if (StringUtils.isBlank(encryptedUserId)) {
            sendWaveResult.setValue(false);
            return;
        }
        String userId = "";
        try {
            userId = RSAUtils.decrypt(encryptedUserId);
        } catch (LingJingException e) {
            sendWaveResult.postValue(false);
        }
        JSONObject jsonObject = JSON.parseObject(jsonData);
        jsonObject.put("userId", userId);
        String jsonString = jsonObject.toJSONString();

        saveWaveRepository.sendJsonData(jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendWaveResult.setValue(false); // 网络请求失败
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = JSONObject.parseObject(responseBody);
                        String code = jsonObject.getString("code");
                        if (ErrorTypes.LOGIN_SUCCESS.getCode().toString().equals(code)) {
                            sendWaveResult.postValue(true);
                        } else {
                            sendWaveResult.postValue(false);
                        }
                    }
                } else {
                    sendWaveResult.setValue(false); // 发送失败
                }
            }
        });
    }
}
