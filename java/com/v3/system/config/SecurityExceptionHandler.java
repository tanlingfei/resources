package com.v3.system.config;

import com.v3.common.result.Result;
import com.v3.common.result.ResultCodeEnum;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author tanlingfei
 * @version 1.0
 * @description TODO
 * @date 2023/4/3 8:45
 */
@ControllerAdvice
@Order(1)
public class SecurityExceptionHandler {
    /**
     * spring security异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) throws AccessDeniedException {
        return Result.build(null, ResultCodeEnum.PERMISSION);
    }
}
