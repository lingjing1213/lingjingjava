package com.lingjing.ui.login;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.data.repository.LoginRepository;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;
import com.lingjing.utils.RSAUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.login
 * @Project：lingjingjava
 * @name：LoginViewModel
 * @Date：2024/10/25 下午10:48
 * @Filename：LoginViewModel
 * @Version：1.0.0
 */
public class LoginViewModel extends AndroidViewModel {

    private final LoginRepository loginRepository = new LoginRepository();

    private final MutableLiveData<Integer> loginResult = new MutableLiveData<>();

    private final SharedPreferences sharedPreferences;

    public LoginViewModel(@NonNull Application application) {
        super(application);
        sharedPreferences = getApplication().getSharedPreferences(LingJingConstants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public LiveData<Integer> getLoginResult() {
        return loginResult;
    }

    /**
     * 自动登录
     */
    public void autoLogin() {
        long expireTime = sharedPreferences.getLong(LingJingConstants.EXPIRE_TIME_KEY, 0);
        if (System.currentTimeMillis() > expireTime) {
            clearUserCredentials();
            loginResult.postValue(ErrorTypes.LOGIN_EXPIRED.getCode());
            return;
        }
        String encryptedUserId = sharedPreferences.getString(LingJingConstants.USER_ID_KEY, null);
        String encryptedCode = sharedPreferences.getString(LingJingConstants.CODE_KEY, null);
        if (encryptedUserId != null && encryptedCode != null) {
            try {
                String userId = RSAUtils.decrypt(encryptedUserId);
                String code = RSAUtils.decrypt(encryptedCode);
                performLogin(userId, code);
            } catch (LingJingException e) {
                clearUserCredentials();
                loginResult.postValue(e.getCode());
            }
        } else {
            clearUserCredentials();
            loginResult.postValue(ErrorTypes.LOGIN_REQUIRED.getCode());
        }

    }

    /**
     * 执行登录
     * @param userId
     * @param code
     */
    private void performLogin(String userId, String code) {
        loginRepository.loginUser(userId, code).observeForever(result -> {
            loginResult.postValue(result); // 更新登录结果
            if (result.equals(ErrorTypes.LOGIN_SUCCESS.getCode())) {
                updateExpirationTime();
            }
        });
    }

    /**
     * 更新过期时间
     */
    private void updateExpirationTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(LingJingConstants.EXPIRE_TIME_KEY, System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
        editor.apply();
    }

    /**
     * 清理用户凭证
     */
    private void clearUserCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(LingJingConstants.USER_ID_KEY);
        editor.remove(LingJingConstants.CODE_KEY);
        editor.remove(LingJingConstants.EXPIRE_TIME_KEY);
        editor.apply();
    }

    /**
     * 登录
     * @param userId
     * @param code
     */
    public void loginUser(String userId, String code) {
        loginRepository.loginUser(userId, code).observeForever(result -> {
            if (result.equals(ErrorTypes.LOGIN_SUCCESS.getCode())) {
                saveUserCredentials(userId, code);
            } else {
                clearUserCredentials();
            }
            loginResult.postValue(result);
        });
    }

    /**
     * 保存用户凭证
     * @param userId
     * @param code
     */
    private void saveUserCredentials(String userId, String code) {
        //rsa加密并存7天
        try {
            RSAUtils.generateKeyPair();
            String encryptedUserId = RSAUtils.encrypt(userId);
            String encryptedCode = RSAUtils.encrypt(code);
            SharedPreferences.Editor edited = sharedPreferences.edit();
            edited.putString(LingJingConstants.USER_ID_KEY, encryptedUserId);
            edited.putString(LingJingConstants.CODE_KEY, encryptedCode);
            edited.putLong(LingJingConstants.EXPIRE_TIME_KEY, System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000);
            edited.apply();
        } catch (LingJingException e) {
            loginResult.postValue(e.getCode());
        }
    }
}
