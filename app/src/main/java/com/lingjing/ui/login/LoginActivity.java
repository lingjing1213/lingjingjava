package com.lingjing.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.lingjing.R;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.ui.home.HomeActivity;
import com.lingjing.utils.NumberUtils;
import com.lingjing.utils.ToastUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.login
 * @Project：lingjingjava
 * @name：LoginActivity
 * @Date：2024/10/25 下午10:47
 * @Filename：LoginActivity
 * @Version：1.0.0
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editTextUserId;

    private EditText editTextCode;

    private Button loginButton;

    private LoginViewModel loginViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        observeLoginResult();
        loginViewModel.autoLogin();
    }

    private void observeLoginResult() {
        loginViewModel.getLoginResult().observe(this, loginResult -> {
            if (ErrorTypes.LOGIN_SUCCESS.getCode().equals(loginResult)) {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            }else {
                showLoginScreen(loginResult);
                ToastUtils.showToast(this, ErrorTypes.getMsgByCode(loginResult));
            }
        });
    }

    private void showLoginScreen(Integer loginResult) {
        setContentView(R.layout.activity_login);
        editTextUserId = findViewById(R.id.editTextUserId);
        editTextCode = findViewById(R.id.editTextCode);
        loginButton = findViewById(R.id.buttonLogin);
        if (loginResult != null) {
            ToastUtils.showToast(this, ErrorTypes.getMsgByCode(loginResult));
        }
        loginButton.setOnClickListener(view -> {
            String userId = editTextUserId.getText().toString();
            String code = editTextCode.getText().toString();

            if (StringUtils.isNoneBlank(userId, code)) {
                if (NumberUtils.isNumeric(userId) && NumberUtils.isSixDigitCode(code)) {
                    loginViewModel.loginUser(userId, code);
                } else {
                    ToastUtils.showToast(this, "请输入正确的id或验证码");
                }
            } else {
                ToastUtils.showToast(this, "id或验证码不能为空");
            }
        });
    }

}
