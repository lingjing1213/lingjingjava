package com.lingjing.data.model;

/**
 * @Author：灵静
 * @Package：com.lingjing.data.model
 * @Project：lingjingjava
 * @name：LoginInDto
 * @Date：2024/10/25 下午11:03
 * @Filename：LoginInDto
 * @Version：1.0.0
 */
public class LoginInDto {

    private final String userId;

    private final String code;

    public LoginInDto(String userId, String code) {
        this.userId = userId;
        this.code = code;
    }

    public String getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

}
