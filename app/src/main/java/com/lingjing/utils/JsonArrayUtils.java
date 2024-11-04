package com.lingjing.utils;

import android.content.Context;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONException;
import com.lingjing.enums.ErrorTypes;
import com.lingjing.exceptions.LingJingException;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：JsonArrayUtils
 * @Date：2024/11/4 下午1:11
 * @Filename：JsonArrayUtils
 * @Version：1.0.0
 */
public class JsonArrayUtils {

    private static final int X_MIN = 1;
    private static final int X_MAX = 31;
    private static final int Y_MIN = 1;
    private static final int Y_MAX = 1000;
    private static final int Z_MIN = 0;
    private static final int Z_MAX = 31;
    private static final int MAX_ELEMENTS = 30;


    public static boolean validateAndAssign(Context context, String jsonString) throws LingJingException {
        try {
            JSONArray jsonArray = JSONArray.parseArray(jsonString);
            if (jsonArray.size() > MAX_ELEMENTS) {
                ToastUtils.showToast(context, "格式错误：数组最多只能包含 30 个元素。");
                return false;
            }
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONArray innerArray = jsonArray.getJSONArray(i);
                if (innerArray == null || innerArray.size() < 2 || innerArray.size() > 3) {
                    ToastUtils.showToast(context, "格式错误：每个元素必须是一个包含两个或三个元素的数组。");
                    return false;
                }
                Integer x = innerArray.getInteger(0);
                Integer y = innerArray.getInteger(1);
                if (x == null || y == null) {
                    ToastUtils.showToast(context, "格式错误：数组中的元素必须是整数。");
                    return false;
                }
                if (x < X_MIN || x > X_MAX) {
                    ToastUtils.showToast(context, "格式错误：x 值必须在 " + X_MIN + " 和 " + X_MAX + " 之间。");
                    return false;
                }
                if (y < Y_MIN || y > Y_MAX) {
                    ToastUtils.showToast(context, "格式错误：y 值必须在 " + Y_MIN + " 和 " + Y_MAX + " 之间。");
                    return false;
                }
                int z = 0; // 默认值
                if (innerArray.size() == 3) {
                    Integer zValue = innerArray.getInteger(2);
                    if (zValue == null) {
                        ToastUtils.showToast(context, "格式错误：数组中的第三个元素必须是整数。");
                        return false;
                    }
                    z = zValue;
                    if (z < Z_MIN || z > Z_MAX) {
                        ToastUtils.showToast(context, "格式错误：z 值必须在 " + Z_MIN + " 和 " + Z_MAX + " 之间。");
                        return false;
                    }
                }
            }
            return true; // 所有数组都校验通过
        }catch (JSONException e){
            throw new LingJingException(ErrorTypes.JSON_PARSE_FAIL, e);
        }
    }
}
