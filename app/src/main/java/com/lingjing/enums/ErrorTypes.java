package com.lingjing.enums;

/**
 * @Author：灵静
 * @Package：com.lingjing.enums
 * @Project：lingjingjava
 * @name：ErrorTypes
 * @Date：2024/10/26 下午2:09
 * @Filename：ErrorTypes
 * @Version：1.0.0
 */
public enum ErrorTypes {


    LOGIN_SUCCESS(10000, "登录成功"),

    LOGIN_FAIL(10001, "登录失败"),

    LOGIN_EXPIRED(10002, "登录已过期"),

    NO_USER(10003, "没有该用户"),

    LOGIN_REQUIRED(10004, "需要登录"),
    //生成密钥对失败
    GENERATE_KEY_PAIR_FAIL(20000, "生成密钥对失败"),

    //获取公钥失败
    GET_PUBLIC_KEY_FAIL(20001, "获取公钥失败"),

    //获取私钥失败
    GET_PRIVATE_KEY_FAIL(20002, "获取私钥失败"),

    //私钥签名失败
    PRIVATE_KEY_SIGN_FAIL(20003, "私钥签名失败"),

    //公钥验签失败
    PUBLIC_KEY_VERIFY_FAIL(20004, "公钥验签失败"),

    //加密失败
    ENCRYPT_FAIL(20005, "加密失败"),

    //解密失败
    DECRYPT_FAIL(20006, "解密失败"),

    //删除密钥对失败
    DELETE_KEY_PAIR_FAIL(20007, "删除密钥对失败"),

    //json 解析失败
    JSON_PARSE_FAIL(30000, "json 解析失败"),

    //操作成功
    OPERATION_SUCCESS(40000, "操作成功"),
    //操作失败
    OPERATION_FAIL(40001, "操作失败"),

    NETWORK_ERROR(99998, "网络连接失败"),

    UNKNOWN_ERROR(99999, "未知错误"),
    ;

    private Integer code;

    private String msg;

    ErrorTypes(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static String getMsgByCode(Integer code) {
        for (ErrorTypes errorType : ErrorTypes.values()) {
            if (errorType.getCode().equals(code)) {
                return errorType.getMsg();
            }
        }
        return ErrorTypes.UNKNOWN_ERROR.getMsg();
    }
}
