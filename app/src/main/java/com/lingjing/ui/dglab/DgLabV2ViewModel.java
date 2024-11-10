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
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.lingjing.constants.DGLabConstants;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.data.model.DGLabSocketMsg;
import com.lingjing.data.model.DGLabV2Model;
import com.lingjing.data.repository.DGLabV2Repository;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.service.DGLabWebSocketClient;
import com.lingjing.service.WebSocketMessageListener;
import com.lingjing.utils.BluetoothGattManager;
import com.lingjing.utils.RSAUtils;
import com.lingjing.utils.SingleLiveEventUtils;
import com.lingjing.utils.StrengthAndWaveUtils;
import com.lingjing.utils.ToastUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PipedReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
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

    private final Queue<DGLabSocketMsg> dgLabSocketMsgQueue = new LinkedList<>();

    private BluetoothGatt bluetoothGatt;

    private final Handler handler = new Handler();

    private Runnable batteryLevelRunnable;

    private boolean isProcessingMessage = false; // 标志当前是否正在处理消息

    private final MutableLiveData<String> selectedWaveformText = new MutableLiveData<>();

    private DGLabWebSocketClient client;

    private final DGLabV2Repository dgLabV2Repository = new DGLabV2Repository();

    private final SingleLiveEventUtils<Integer> sendWaveResult = new SingleLiveEventUtils<>();

    private final SharedPreferences sharedPreferences;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> currentWaveformTask;

    private final Map<String, List<int[]>> waveformDataMap = new HashMap<>(1);

    private final SingleLiveEventUtils<Integer> deleteWaveResult = new SingleLiveEventUtils<>();

    private final SingleLiveEventUtils<Boolean> socketFlag = new SingleLiveEventUtils<>();



    public DgLabV2ViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = getApplication().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public SingleLiveEventUtils<Integer> getDeleteWaveResult() {
        return deleteWaveResult;
    }

    public void setWaveformDataMap(String waveName, List<int[]> wave) {
        waveformDataMap.put(waveName, wave);
    }

    public Map<String, List<int[]>> getWaveformData() {
        return waveformDataMap;
    }

    public LiveData<String> getSelectedWaveformText() {
        return selectedWaveformText;
    }


    public SingleLiveEventUtils<Integer> getSendWaveResult() {
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

    public SingleLiveEventUtils<Boolean> getSocketFlag() {
        return socketFlag;
    }

    public void updateStrengthA(int value) {
        if (dgLabV2Model.setStrengthAValue(value)) {
            strengthAValue.setValue(dgLabV2Model.getStrengthAValue());
            writeIntensityToDevice(dgLabV2Model.getStrengthAValue(), dgLabV2Model.getStrengthBValue());
        }
    }

    public void updateStrengthB(int value) {
        if (dgLabV2Model.setStrengthBValue(value)) {
            strengthBValue.setValue(dgLabV2Model.getStrengthBValue());
            writeIntensityToDevice(dgLabV2Model.getStrengthAValue(), dgLabV2Model.getStrengthBValue());
        }
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
            socketFlag.setValue(true);
        } else {
            socketFlag.setValue(false);
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
                if (waveformData.isEmpty()) {
                    waveformData = waveformDataMap.get(value);
                }
                Log.d(TAG, "Waveform data : " + waveformData);
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

    public List<int[]> getWaveformData(String waveformText) {
        switch (waveformText) {
            case "呼吸":
                return new ArrayList<>(breatheDataV2);
            case "潮汐":
                return new ArrayList<>(tidalDataV2);
            case "加快":
                return new ArrayList<>(accelerateDataV2);
            case "推力":
                return new ArrayList<>(thrustDataV2);
            default:
                return Collections.emptyList(); // 或者返回 null，根据你的需求
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
                writeWaveformDataSequentially(aWave, bWave);
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
            if (strengthAValue.getValue() > 0) {
                if (aChannelCharacteristic != null) {
                    aChannelCharacteristic.setValue(aWave);
                    boolean writeSuccessA = bluetoothGatt.writeCharacteristic(aChannelCharacteristic);
                    Log.d(TAG, "写入A通道是否成功: " + writeSuccessA + ", Values: " + Arrays.toString(aWave));
                }
            }

            // 延迟后写入 B 通道
            handler.postDelayed(() -> {
                BluetoothGattCharacteristic bChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC));
                if (strengthBValue.getValue() > 0) {
                    if (bChannelCharacteristic != null) {
                        bChannelCharacteristic.setValue(bWave);
                        boolean writeSuccessB = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
                        Log.d(TAG, "写入B通道是否成功: " + writeSuccessB + ", Values: " + Arrays.toString(bWave));
                    }
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

    /**
     * 向服务端发送波形数据
     *
     * @param jsonData
     */
    public void sendWaveData(String jsonData) {
        String encryptedUserId = sharedPreferences.getString(LingJingConstants.USER_ID_KEY, null);
        if (StringUtils.isBlank(encryptedUserId)) {
            sendWaveResult.setValue(ErrorTypes.UNKNOWN_ERROR.getCode());
            return;
        }
        String userId = "";
        try {
            userId = RSAUtils.decrypt(encryptedUserId);
        } catch (LingJingException e) {
            sendWaveResult.setValue(ErrorTypes.UNKNOWN_ERROR.getCode());
        }
        JSONObject jsonWaveObject = JSON.parseObject(jsonData);
        jsonWaveObject.put("userId", userId);
        String jsonString = jsonWaveObject.toJSONString();

        dgLabV2Repository.sendJsonData(jsonString, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendWaveResult.setValue(ErrorTypes.NETWORK_ERROR.getCode()); // 网络请求失败
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = JSONObject.parseObject(responseBody);
                        String code = jsonObject.getString("code");
                        Log.d(TAG, "新增波形: " + code);
                        if (ErrorTypes.ADD_WAVE_SUCCESS.getCode().toString().equals(code)) {
                            String existingDataJson = sharedPreferences.getString(LingJingConstants.WAVE_DATA_KEY, "[]");
                            JSONArray existingDataArray = JSON.parseArray(existingDataJson);
                            existingDataArray.add(jsonWaveObject);
                            sharedPreferences.edit()
                                    .putString(LingJingConstants.WAVE_DATA_KEY, existingDataArray.toJSONString())
                                    .apply();

                            sendWaveResult.postValue(Integer.parseInt(code));
                        } else {
                            sendWaveResult.postValue(Integer.parseInt(code));
                        }
                    }
                } else {
                    sendWaveResult.setValue(ErrorTypes.ADD_WAVE_FAIL.getCode()); // 发送失败
                }
            }
        });
    }

    /**
     * 删除服务器上的波形数据
     *
     * @param userId
     * @param buttonName
     */
    public void deleteWaveData(String userId, String buttonName) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("userId", userId);
        jsonObject.put("name", buttonName);

        dgLabV2Repository.deleteWaveData(jsonObject.toJSONString(), new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = JSONObject.parseObject(responseBody);
                        String code = jsonObject.getString("code");
                        Log.d(TAG, "删除结果: " + code);
                        deleteWaveResult.postValue(Integer.parseInt(code));
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                deleteWaveResult.postValue(ErrorTypes.NETWORK_ERROR.getCode());
            }
        });
    }


    private MutableLiveData<Integer> pulseTimesValue = new MutableLiveData<>(0); // 默认为0
    private MutableLiveData<Integer> pulseWidthValue = new MutableLiveData<>(0); // 默认为0
    private MutableLiveData<Integer> pulseIntervalTimeValue = new MutableLiveData<>(0); // 默认为0


    public MutableLiveData<Integer> getPulseTimesValue() {
        return pulseTimesValue;
    }

    public MutableLiveData<Integer> getPulseWidthValue() {
        return pulseWidthValue;
    }

    public MutableLiveData<Integer> getPulseIntervalTimeValue() {
        return pulseIntervalTimeValue;
    }

    // 设置脉冲次数、脉冲宽度、脉冲间隔时间
    public void setPulseTimesValue(int value) {
        pulseTimesValue.setValue(value);
    }

    public void setPulseWidthValue(int value) {
        pulseWidthValue.setValue(value);
    }

    public void setPulseIntervalTimeValue(int value) {
        pulseIntervalTimeValue.setValue(value);
    }

    private final Handler handlerA = new Handler();

    private final Handler handlerB = new Handler();

    private Runnable waveformRunnableA;

    private Runnable waveformRunnableB;
    public void sendWaveDataA() {
        String value = selectedWaveformText.getValue();
        List<int[]> waveformData = getWaveformData(value);
        if (waveformData.isEmpty()) {
            waveformData = waveformDataMap.get(value);
        }
        Log.d(TAG, "Waveform data : " + waveformData);

        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE));
        if (service == null) {
            return;
        }
        // 当前索引，用于记录从waveformData中取出的位置
        final int[] currentIndex = {0};
        List<int[]> finalWaveformData = waveformData;
        // 定时器每100ms执行一次任务
        waveformRunnableA = new Runnable() {
            @Override
            public void run() {
                // 获取当前索引位置的数据
                int[] data = finalWaveformData.get(currentIndex[0]);

                // 处理数据
                byte[] wave = StrengthAndWaveUtils.wave(data);
                BluetoothGattCharacteristic aChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_A_DIRECTION_CHARACTERISTIC));

                if (strengthAValue.getValue() > 0) {
                    if (aChannelCharacteristic != null) {
                        aChannelCharacteristic.setValue(wave);
                        @SuppressLint("MissingPermission")
                        boolean writeSuccessA = bluetoothGatt.writeCharacteristic(aChannelCharacteristic);
                        Log.d(TAG, "写入A通道是否成功: " + writeSuccessA + ", Values: " + Arrays.toString(wave));
                    }
                }
                // 更新索引，循环使用waveformData
                currentIndex[0] = (currentIndex[0] + 1) % finalWaveformData.size();

                // 100ms后继续执行
                handlerA.postDelayed(this, 100);
            }
        };
        // 启动定时任务
        handlerA.post(waveformRunnableA);
    }

    public  void stopSendWaveDataA() {
        handlerA.removeCallbacks(waveformRunnableA);
    }

    public void sendWaveDataB() {
        String value = selectedWaveformText.getValue();
        List<int[]> waveformData = getWaveformData(value);
        if (waveformData.isEmpty()) {
            waveformData = waveformDataMap.get(value);
        }

        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE));
        if (service == null) {
            return;
        }
        // 当前索引，用于记录从waveformData中取出的位置
        final int[] currentIndex = {0};
        List<int[]> finalWaveformData = waveformData;
        // 定时器每100ms执行一次任务
        waveformRunnableB = new Runnable() {
            @Override
            public void run() {
                // 获取当前索引位置的数据
                int[] data = finalWaveformData.get(currentIndex[0]);

                // 处理数据
                byte[] wave = StrengthAndWaveUtils.wave(data);
                BluetoothGattCharacteristic bChannelCharacteristic = service.getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC));
                if (strengthBValue.getValue() > 0) {
                    if (bChannelCharacteristic != null) {
                        bChannelCharacteristic.setValue(wave);
                        @SuppressLint("MissingPermission")
                        boolean writeSuccessB = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
                        Log.d(TAG, "写入B通道是否成功: " + writeSuccessB + ", Values: " + Arrays.toString(wave));
                    }
                }
                // 更新索引，循环使用waveformData
                currentIndex[0] = (currentIndex[0] + 1) % finalWaveformData.size();

                // 100ms后继续执行
                handlerB.postDelayed(this, 100);
            }
        };
        // 启动定时任务
        handlerB.post(waveformRunnableB);
    }


    public  void stopSendWaveDataB() {
        handlerB.removeCallbacks(waveformRunnableB);
    }
}
