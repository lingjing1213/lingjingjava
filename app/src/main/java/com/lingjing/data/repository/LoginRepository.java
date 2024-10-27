package com.lingjing.data.repository;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.alibaba.fastjson2.JSONObject;
import com.lingjing.constants.LingJingConstants;
import com.lingjing.enums.ErrorTypes;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.repository
 * @Project：lingjingjava
 * @name：LoginRepository
 * @Date：2024/10/27 上午12:25
 * @Filename：LoginRepository
 * @Version：1.0.0
 */
public class LoginRepository {
    public LiveData<Integer> loginUser(String userId, String code) {
        MutableLiveData<Integer> loginResult = new MutableLiveData<>();
        OkHttpClient client = new OkHttpClient();
        JSONObject requestJson= new JSONObject();
        requestJson.put("userId", userId);
        requestJson.put("code", code);
        RequestBody requestBody = RequestBody.create(requestJson.toJSONString(), LingJingConstants.JSON);
        Request request = new Request.Builder()
                .url(LingJingConstants.LOGIN_URL)
                .post(requestBody)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        JSONObject jsonObject = JSONObject.parseObject(responseBody);
                        String code = jsonObject.getString("code");
                        if (ErrorTypes.LOGIN_SUCCESS.getCode().toString().equals(code)) {
                            loginResult.postValue(ErrorTypes.LOGIN_SUCCESS.getCode());
                        }
                        if (ErrorTypes.NO_USER.getCode().toString().equals(code)){
                            loginResult.postValue(ErrorTypes.NO_USER.getCode());
                        }

                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                loginResult.postValue(ErrorTypes.NETWORK_ERROR.getCode());
            }
        });
        return loginResult;
    }
}
