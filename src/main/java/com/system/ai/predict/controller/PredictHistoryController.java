package com.system.ai.predict.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.system.ai.predict.controller.vo.ApiResponse;
import com.system.ai.predict.controller.vo.PagedResult;
import com.system.ai.predict.mapper.AiPredictResultMapper;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.persistence.AiPredictResult;
import com.system.ai.predict.repository.DeviceHealthRepository;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 历史预测结果查询接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tool/predict")
public class PredictHistoryController {

    private final AiPredictResultMapper resultMapper;
    private final ObjectMapper objectMapper;
    private final DeviceHealthRepository deviceHealthRepository;

    // 历史预测结果查询接口
    @GetMapping("/history")
    public ApiResponse<PagedResult<Map<String, Object>>> history(
            @RequestParam @NotBlank(message = "deviceId 不能为空") String deviceId,
            @RequestParam(required = false) String type, // AGING/FAULT 可选
            @RequestParam(required = false) Instant start,
            @RequestParam(required = false) Instant end,
            @RequestParam(defaultValue = "1") @Min(1) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) Integer size,
            @RequestParam(defaultValue = "true") Boolean asc
    ) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("end 不能早于 start");
        }
        int limit = Math.min(size, 200);
        int offset = (page - 1) * limit;

        LocalDateTime s = start != null ? LocalDateTime.ofInstant(start, ZoneOffset.UTC) : null;
        LocalDateTime e = end != null ? LocalDateTime.ofInstant(end, ZoneOffset.UTC) : null;

        Long total = resultMapper.countRange(deviceId, normType(type), s, e);
        List<AiPredictResult> rows = resultMapper.selectRange(deviceId, normType(type), s, e, offset, limit, asc != null && asc);

        List<Map<String, Object>> items = rows.stream()
                .map(r -> Map.of(
                        "deviceId", r.getDeviceId(),
                        "predictType", r.getPredictType(),
                        "predictionTime", r.getPredictionTime().toInstant(ZoneOffset.UTC),
                        "createdAt", r .getCreatedAt() != null ? r.getCreatedAt().toInstant(ZoneOffset.UTC) : null,
                        "result", parseJsonSafely(r.getResultJson())
                ))
                .collect(Collectors.toList());

        PagedResult<Map<String, Object>> body = new PagedResult<>(page, limit, total != null ? total : 0L, items);
        return ApiResponse.success(body);
    }

    private String normType(String type) {
        if (type == null || type.isBlank()) return null;
        String t = type.trim().toUpperCase();
        if ("AGING".equals(t) || "FAULT".equals(t)) return t;
        throw new IllegalArgumentException("type 仅支持 AGING 或 FAULT");
    }

    private Object parseJsonSafely(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return Map.of("raw", json);
        }
    }

    // 获取设备历史健康数据接口
    @GetMapping("/device-health-history")
    public ApiResponse<List<Map<String, Object>>> deviceHealthHistory(
            @RequestParam @NotBlank(message = "deviceId 不能为空") String deviceId,
            @RequestParam(required = false) Instant start,
            @RequestParam(required = false) Instant end,
            @RequestParam(required = false) @Min(1) Integer limit
    ) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new IllegalArgumentException("end 不能早于 start");
        }
        int effectiveLimit = limit != null && limit > 0 ? limit : 1000;
        
        List<DeviceHistoryPoint> history = deviceHealthRepository.fetchHistorySeries(
                deviceId, start, end, effectiveLimit);
        
        List<Map<String, Object>> items = history.stream()
//                .map(point -> Map.of(
//                        "timestamp", point.getTimestamp() != null ? point.getTimestamp() : Instant.now(),
//                        "temperature", point.getTemperature() != null ? point.getTemperature() : 0.0,
//                        "voltage", point.getVoltage() != null ? point.getVoltage() : 0.0,
//                        "memoryUsage", point.getMemoryUsage() != null ? point.getMemoryUsage() : 0.0,
//                        "cpuUsage", point.getCpuUsage() != null ? point.getCpuUsage() : 0.0,
//                        "portErrorRate", point.getPortErrorRate() != null ? point.getPortErrorRate() : 0.0,
//                        "icmpLoss", point.getIcmpLoss() != null ? point.getIcmpLoss() : 0.0
//                ))
//                .collect(Collectors.toList());
                .map(point -> Map.<String, Object>of(  // <--- 修改这里，强制指定 Value 为 Object
                        "timestamp", point.getTimestamp() != null ? point.getTimestamp() : Instant.now(),
                        "temperature", point.getTemperature() != null ? point.getTemperature() : 0.0,
                        "voltage", point.getVoltage() != null ? point.getVoltage() : 0.0,
                        "memoryUsage", point.getMemoryUsage() != null ? point.getMemoryUsage() : 0.0,
                        "cpuUsage", point.getCpuUsage() != null ? point.getCpuUsage() : 0.0,
                        "portErrorRate", point.getPortErrorRate() != null ? point.getPortErrorRate() : 0.0,
                        "icmpLoss", point.getIcmpLoss() != null ? point.getIcmpLoss() : 0.0
                ))
                .collect(Collectors.toList());
        return ApiResponse.success(items);
    }
}

