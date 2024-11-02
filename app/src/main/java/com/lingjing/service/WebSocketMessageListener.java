package com.lingjing.service;

/**
 * @Author：灵静
 * @Package：com.lingjing.service
 * @Project：lingjingjava
 * @name：WebSocketMessageListener
 * @Date：2024/11/2 下午3:57
 * @Filename：WebSocketMessageListener
 * @Version：1.0.0
 */
public interface WebSocketMessageListener {

    void onMessageReceived(String message);
}
