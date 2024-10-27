package com.lingjing.exceptions;

import com.lingjing.enums.ErrorTypes;

/**
 * @Author：灵静
 * @Package：com.lingjing.exceptions
 * @Project：lingjingjava
 * @name：LingJingException
 * @Date：2024/10/27 上午1:59
 * @Filename：LingJingException
 * @Version：1.0.0
 */
public class LingJingException extends Exception {

    private final Integer code;

    private final String msg;


    public LingJingException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public LingJingException(ErrorTypes errorTypes,Throwable throwable) {
        super(throwable);
        this.code = errorTypes.getCode();
        this.msg = errorTypes.getMsg();
    }

    public LingJingException(Integer code ,Throwable throwable) {
        super(throwable);
        this.code = code;
        this.msg = ErrorTypes.getMsgByCode(code);
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }


}
