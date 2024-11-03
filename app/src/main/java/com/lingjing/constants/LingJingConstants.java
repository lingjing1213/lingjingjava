package com.lingjing.constants;

import com.lingjing.R;

import okhttp3.MediaType;

/**
 * @Author：灵静
 * @Package：com.lingjing.constants
 * @Project：lingjingjava
 * @name：LingJingConstants
 * @Date：2024/10/26 下午2:14
 * @Filename：LingJingConstants
 * @Version：1.0.0
 */
public class LingJingConstants {

    public static final Integer CODE_LENGTH = 6;

    public static final String DG_LAB_SOCKET_URL = "wss://noticeably-positive-bird.ngrok-free.app/appV1";

    public static final String LOGIN_URL = "https://noticeably-positive-bird.ngrok-free.app/app/login";

    public static final String CHECK_USER_URL = "https://noticeably-positive-bird.ngrok-free.app/app/checkUser";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static final String KEYSTORE_ALIAS = "lingJing_rsa_key";

    public static final String ANDROID_KEYSTORE = "AndroidKeyStore";

    public static final String SHARED_PREFS_NAME="LingJingSharedPrefs";

    public static final String USER_ID_KEY="userId";

    public static final String CODE_KEY="code";

    public static final String EXPIRE_TIME_KEY="expireTime";

}
