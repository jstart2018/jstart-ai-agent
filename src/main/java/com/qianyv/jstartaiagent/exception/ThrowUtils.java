package com.qianyv.jstartaiagent.exception;


import com.qianyv.jstartaiagent.enums.ErrorEnum;
import lombok.Getter;

@Getter
public class ThrowUtils {

    /**
     *  条件成立则抛出异常
     * @param condition 条件
     * @param exception 异常
     */
    public static void throwIf(boolean condition,RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }
    /**
     *  条件成立则抛出异常
     * @param condition 条件
     * @param exceptionCode 异常code
     */
    public static void throwIf(boolean condition, ErrorEnum exceptionCode) {
        if (condition) {
            throwIf(condition,new BusinessException(exceptionCode));
        }
    }
    /**
     *  条件成立则抛出异常
     * @param condition 条件
     * @param exceptionCode 异常code
     * @param message   异常消息
     */
    public static void throwIf(boolean condition, ErrorEnum exceptionCode, String message) {
        if (condition) {
            throwIf(condition,new BusinessException(exceptionCode,message));
        }
    }

}