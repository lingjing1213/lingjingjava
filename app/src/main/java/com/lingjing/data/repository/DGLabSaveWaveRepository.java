package com.lingjing.data.repository;

import com.lingjing.constants.LingJingConstants;

import okhttp3.Callback;
import okhttp3.MediaType;
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
public class DGLabSaveWaveRepository {


    private final OkHttpClient okHttpClient = new OkHttpClient();

    public void sendJsonData(String jsonData, Callback callback) {
        RequestBody body = RequestBody.create(jsonData, LingJingConstants.JSON);
        Request request = new Request.Builder()
                .url(LingJingConstants.SAVE_WAVE_URL) // 替换为你的服务端接口
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(callback); // 使用回调处理响应
    }
}
