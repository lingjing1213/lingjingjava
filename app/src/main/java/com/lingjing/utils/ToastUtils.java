package com.lingjing.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：ToastUtils
 * @Date：2024/10/26 下午6:03
 * @Filename：ToastUtils
 * @Version：1.0.0
 */
public class ToastUtils {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
