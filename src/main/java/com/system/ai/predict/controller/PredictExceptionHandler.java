package com.system.ai.predict.controller;

import com.system.ai.predict.client.exception.TimemoeClientException;
import com.system.ai.predict.controller.vo.ApiResponse;
import com.system.ai.predict.exception.PredictOverloadException;
import com.system.ai.predict.exception.TaosAccessException;
import com.system.ai.predict.service.support.PredictionDataException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

/**
 * 预测模块异常统一处理。
 */
@Slf4j
@RestControllerAdvice(basePackageClasses = PredictController.class)
public class PredictExceptionHandler {

    // 参数校验异常处理
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleValidation(Exception ex) {
        String message = resolveValidationMessage(ex);
        return ApiResponse.failure(400, message);
    }

    // 预测数据异常处理
    @ExceptionHandler(PredictionDataException.class)
    public ApiResponse<Void> handlePredictionData(PredictionDataException ex) {
        log.warn("预测数据不足或缺失，deviceId={}, reason={}, expected={}, actual={}",
                ex.getDeviceId(), ex.getReason(), ex.getExpected(), ex.getActual());
        return ApiResponse.failure(422, ex.getMessage());
    }

    // timemoe 客户端异常处理
    @ExceptionHandler(TimemoeClientException.class)
    public ApiResponse<Void> handleTimemoeClient(TimemoeClientException ex) {
        log.warn("调用 timemoe 服务失败，path={}, status={}, type={}, message={}",
                ex.getPath(), ex.getStatusCode(), ex.getErrorType(), ex.getMessage(), ex);
        int code = switch (ex.getErrorType()) {
            case CLIENT_ERROR -> 400;
            case SERVER_ERROR -> 502;
            case NETWORK_ERROR -> 504;
            default -> 500;
        };
        return ApiResponse.failure(code, ex.getMessage());
    }

    @ExceptionHandler({PredictOverloadException.class, TaskRejectedException.class, java.util.concurrent.RejectedExecutionException.class})
    public ApiResponse<Void> handleOverload(Exception ex) {
        log.warn("预测服务触发过载保护: {}", ex.getMessage());
        return ApiResponse.failure(503, "预测请求过多，请稍后重试");
    }

    // TDengine 访问异常处理
    @ExceptionHandler(TaosAccessException.class)
    public ApiResponse<Void> handleTaosAccess(TaosAccessException ex) {
        log.warn("访问 TDengine 失败: {}", ex.getMessage());
        // 统一映射为后端依赖不可用/网关错误
        return ApiResponse.failure(502, "数据源暂不可用，请稍后重试");
    }

    // 非法参数异常处理
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ApiResponse.failure(400, ex.getMessage());
    }

    // 通用异常处理
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneric(Exception ex) {
        log.error("预测接口出现未预期异常", ex);
        return ApiResponse.failure(500, "预测服务内部错误");
    }

    // 解析参数校验异常信息
    private String resolveValidationMessage(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().hasErrors()) {
            return manv.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        if (ex instanceof BindException bind && bind.getBindingResult().hasErrors()) {
            return bind.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        if (ex instanceof ConstraintViolationException violation && !violation.getConstraintViolations().isEmpty()) {
            return violation.getConstraintViolations().iterator().next().getMessage();
        }
        if (ex instanceof HttpMessageNotReadableException) {
            return "请求体解析失败，请确认 JSON 格式";
        }
        return "请求参数校验失败";
    }
}
