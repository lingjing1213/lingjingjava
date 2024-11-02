package com.lingjing.controller;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.lingjing.constants.DGLabConstants;
import com.lingjing.utils.BluetoothGattManager;
import com.lingjing.utils.ToastUtils;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @Author：hifter
 * @Package：com.lingjing.controller
 * @Project：lingjingjava
 * @name：DgLabV2Controller
 * @Date：2024/11/2 下午9:03
 * @Filename：DgLabV2Controller
 * @Version：1.0.0
 */
public class DgLabV2Controller {
    private static final String TAG = "DgLabV2Controller";
    private BluetoothGatt bluetoothGatt = BluetoothGattManager.getInstance().getBluetoothGatt();
    private void writeIntensityToDevice(int aIntensityValue, int bIntensityValue) {
        if (bluetoothGatt != null) {
            BluetoothGattCharacteristic pwmAB2Characteristic = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE))
                    .getCharacteristic(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_STRENGTH_CHARACTERISTIC));
            if (pwmAB2Characteristic != null) {
                int aStrength = aIntensityValue * 7;
                int bStrength = bIntensityValue * 7;
                if (aStrength < 0 || aStrength > 2047 || bStrength < 0 || bStrength > 2047) {
                    Log.d("DgLabV2Controller", "强度值设置错误");
                }
                byte[] abPowerToByte = StrengthAndWave.abPowerToByte(aStrength, bStrength);
                pwmAB2Characteristic.setValue(abPowerToByte);
                @SuppressLint("MissingPermission") boolean writeSuccess = bluetoothGatt.writeCharacteristic(pwmAB2Characteristic);
                Log.d(TAG, "Write success: " + writeSuccess + ", Values: " + Arrays.toString(abPowerToByte));
            }
        }
    }
    private void handleStartOrPause() {
        if (bluetoothGatt == null) return;
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(DGLabConstants.DG_LAB_V2_PWM_AB_SERVICE));
        if (service != null) {
            UUID aChannelCharacteristicUUid = UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_A_DIRECTION_CHARACTERISTIC);
            UUID bChannelCharacteristicUUid = UUID.fromString(DGLabConstants.DG_LAB_V2_WAVE_B_DIRECTION_CHARACTERISTIC);
            BluetoothGattCharacteristic aChannelCharacteristic = null;
            BluetoothGattCharacteristic bChannelCharacteristic = null;
            aChannelCharacteristic = service.getCharacteristic(aChannelCharacteristicUUid);
            bChannelCharacteristic = service.getCharacteristic(bChannelCharacteristicUUid);

            if (aChannelCharacteristic != null || bChannelCharacteristic != null) {
                BluetoothGattCharacteristic finalAChannelCharacteristic = aChannelCharacteristic;
                BluetoothGattCharacteristic finalBChannelCharacteristic = bChannelCharacteristic;
                scheduler = Executors.newScheduledThreadPool(1);
                Log.d(TAG, "开始发送波形数据");
                ScheduledFuture<?> scheduledFuture = scheduler.scheduleWithFixedDelay(() ->
                        sendPulseParameters(finalAChannelCharacteristic, finalBChannelCharacteristic,
                                StrengthAndWave.wave(3, 90, 10)), 0, 100, TimeUnit.MILLISECONDS);
                scheduler.schedule(() -> {
                    scheduledFuture.cancel(false);
                    scheduler.shutdown(); // 关闭调度器
                }, 10, TimeUnit.SECONDS);
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void sendPulseParameters(BluetoothGattCharacteristic aChannelCharacteristic, BluetoothGattCharacteristic bChannelCharacteristic, byte[] bytes) {

        if (aChannelCharacteristic != null) {
            aChannelCharacteristic.setValue(bytes);
            if (bChannelCharacteristic != null) {
                bluetoothGatt.setCharacteristicNotification(bChannelCharacteristic, true);
            }
            boolean writeAChannelIsSuccess = bluetoothGatt.writeCharacteristic(aChannelCharacteristic);
            Log.d(TAG, "A通道是否写入成功: " + writeAChannelIsSuccess + "  bytes: " + Arrays.toString(bytes));
        }
        if (bChannelCharacteristic != null) {
            bChannelCharacteristic.setValue(bytes);
            if (aChannelCharacteristic != null) {
                bluetoothGatt.setCharacteristicNotification(aChannelCharacteristic, true);
            }
            boolean writeBChannelIsSuccess = bluetoothGatt.writeCharacteristic(bChannelCharacteristic);
            Log.d(TAG, "B通道是否写入成功: " + writeBChannelIsSuccess + "  bytes: " + Arrays.toString(bytes));
        }

        if (aChannelCharacteristic == null) {
            return;
        }
        if (bChannelCharacteristic == null) {
            return;
        }
    }
}
