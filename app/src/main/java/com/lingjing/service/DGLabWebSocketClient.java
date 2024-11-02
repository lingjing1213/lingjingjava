package com.lingjing.service;

import android.content.SharedPreferences;

import com.lingjing.constants.LingJingConstants;
import com.lingjing.data.model.DGLabV2Model;

import java.lang.ref.PhantomReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

/**
 * @Author：灵静
 * @Package：com.lingjing.service
 * @Project：lingjingjava
 * @name：DGLabWebSocketClient
 * @Date：2024/11/2 下午3:54
 * @Filename：DGLabWebSocketClient
 * @Version：1.0.0
 */
public class DGLabWebSocketClient {

    private static final String TAG = "DGLabWebSocketClient";

    private WebSocket webSocket;

    private OkHttpClient okHttpClient;

    private WebSocketMessageListener messageListener;

    private SharedPreferences sharedPreferences;

    private String userId;

    private DGLabV2Model dgLabV2Model;


    private DGLabWebSocketClient(String userId,DGLabV2Model dgLabV2Model) {
        this.userId = userId;
        this.dgLabV2Model = dgLabV2Model;
        this.okHttpClient = new OkHttpClient();
    }

    public void setMessageListener(WebSocketMessageListener listener) {
        this.messageListener = listener;
    }

    public void connect() {

       // new Request.Builder().url(LingJingConstants.DG_LAB_SOCKET_URL+ userId+dgLabV2Model)

    }
}
