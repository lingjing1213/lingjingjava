package com.lingjing.service;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONObject;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.data.model.DGLabV2Model;

import java.lang.ref.PhantomReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

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

    private final OkHttpClient okHttpClient;

    private WebSocketMessageListener messageListener;

    private final String userId;

    private final DGLabV2Model dgLabV2Model;


    public boolean isConnected = false; // 连接状态跟踪

    public DGLabWebSocketClient(String userId, DGLabV2Model dgLabV2Model) {
        this.userId = userId;
        this.dgLabV2Model = dgLabV2Model;
        this.okHttpClient = new OkHttpClient();
    }

    public void setMessageListener(WebSocketMessageListener listener) {
        this.messageListener = listener;
    }

    public void connect() {

        Request request = new Request.Builder().url(LingJingConstants.DG_LAB_SOCKET_URL).build();

        webSocket = okHttpClient.newWebSocket(request, new WebSocketListener() {

            @Override
            public void onOpen(@NonNull WebSocket webSocket, @NonNull okhttp3.Response response) {
                // 连接成功，发送登录信息
                isConnected=true;
                JSONObject jsonObject = JSONObject.of("userId", userId, "dgLabV2Model", dgLabV2Model);
                webSocket.send(jsonObject.toJSONString());
            }
            @Override
            public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
                // 接收到消息，调用回调方法
                Log.d(TAG, "接收到的消息: " + text);
                messageListener.onMessageReceived(text);
            }
            @Override
            public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, Response response) {
                // 连接失败，尝试重连
              //  reconnect();
                Log.e("DGLabWebSocketClient", "WebSocket connection failed: " + t.getMessage());
            }

            @Override
            public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
                super.onClosed(webSocket, code, reason);

            }
        });



    }

    private void reconnect() {
        // 延时重连逻辑
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                connect();
            } catch (InterruptedException e) {
                Log.e("DGLabWebSocketClient", "重连失败：" + e.getMessage());
            }
        }).start();
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "客户端断开连接");
            isConnected = false; // 更新连接状态
        }
    }
}
