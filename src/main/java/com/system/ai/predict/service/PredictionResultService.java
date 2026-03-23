package com.system.ai.predict.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.ai.predict.client.dto.AgingPredictResponse;
import com.system.ai.predict.client.dto.FaultPredictResponse;
import com.system.ai.predict.client.dto.TimemoeBatchPredictResponse;
import com.system.ai.predict.mapper.AiPredictResultMapper;
import com.system.ai.predict.persistence.AiPredictResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionResultService {

    private final AiPredictResultMapper resultMapper;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    private Counter savedCounter() { return meterRegistry.counter("ai_predict.results.saved"); }

    // 保存老化预测结果
    public void saveAging(AgingPredictResponse resp) {
        if (resp == null || !resp.isSuccess() || resp.getResult() == null) {
            return;
        }
        Instant pt = resp.getResult().getPredictionTime();
        LocalDateTime predictionTime = pt != null ? LocalDateTime.ofInstant(pt, ZoneOffset.UTC) : LocalDateTime.now(ZoneOffset.UTC);
        String json = toJsonUnchecked(resp);

        AiPredictResult entity = new AiPredictResult();
        entity.setDeviceId(resp.getResult().getDeviceId());
        entity.setPredictType("AGING");
        entity.setPredictionTime(predictionTime);
        entity.setResultJson(json);
        resultMapper.upsert(entity);
        savedCounter().increment();
    }

    public void saveFault(FaultPredictResponse resp) {
        if (resp == null || !resp.isSuccess() || resp.getResult() == null) {
            return;
        }
        Instant pt = resp.getResult().getPredictionTime();
        LocalDateTime predictionTime = pt != null ? LocalDateTime.ofInstant(pt, ZoneOffset.UTC) : LocalDateTime.now(ZoneOffset.UTC);
        String json = toJsonUnchecked(resp);

        AiPredictResult entity = new AiPredictResult();
        entity.setDeviceId(resp.getResult().getDeviceId());
        entity.setPredictType("FAULT");
        entity.setPredictionTime(predictionTime);
        entity.setResultJson(json);
        resultMapper.upsert(entity);
        savedCounter().increment();
    }

    public void saveBatch(TimemoeBatchPredictResponse batch) {
        if (batch == null || batch.getResults() == null) {
            return;
        }
        batch.getResults().forEach(item -> {
            if (item.getAging() != null) {
                saveAging(item.getAging());
            }
            if (item.getFault() != null) {
                saveFault(item.getFault());
            }
        });
    }

    private String toJsonUnchecked(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("序列化预测结果失败，fallback to toString()", e);
            return String.valueOf(o);
        }
    }
}
