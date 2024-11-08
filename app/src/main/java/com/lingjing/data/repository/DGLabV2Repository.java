package com.lingjing.data.repository;

import com.lingjing.constants.LingJingConstants;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.repository
 * @Project：lingjingjava
 * @name：DGLabSaveWaveRepository
 * @Date：2024/11/5 下午10:13
 * @Filename：DGLabSaveWaveRepository
 * @Version：1.0.0
 */
public class DGLabV2Repository {

    private final OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * 保存json格式的波形
     * @param jsonData
     * @param callback
     */
    public void sendJsonData(String jsonData, Callback callback) {
        RequestBody body = RequestBody.create(jsonData, LingJingConstants.JSON);
        Request request = new Request.Builder()
                .url(LingJingConstants.SAVE_WAVE_URL)
                .post(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }

    /**
     * 删除波形
     * @param data
     * @param callback
     */
    public void deleteWaveData(String data, Callback callback) {
        RequestBody body = RequestBody.create(data, LingJingConstants.JSON);
        Request request = new Request.Builder()
                .url(LingJingConstants.DELETE_WAVE_URL)
                .delete(body)
                .build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
