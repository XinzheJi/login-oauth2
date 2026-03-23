package com.system.ai.predict.scheduler;

import com.system.ai.predict.client.dto.TimemoeBatchPredictResponse;
import com.system.ai.predict.config.PredictSchedulerProperties;
import com.system.ai.predict.service.BatchPredictService;
import com.system.ai.predict.service.PredictionResultService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PredictBatchScheduler {

    private final PredictSchedulerProperties props;
    private final BatchPredictService batchPredictService;
    private final PredictionResultService predictionResultService;
    private final MeterRegistry meterRegistry;

    private Timer batchTimer() {
        return Timer.builder("ai_predict.batch.duration").register(meterRegistry);
    }

    @Scheduled(cron = "${predict.scheduler.cron:0 0/5 * * * ?}")
    public void runBatch() {
        if (!props.isEnabled()) {
            return;
        }
        List<String> all = props.getDeviceIds();
        if (all == null || all.isEmpty()) {
            log.warn("预测调度：未配置 deviceIds，跳过");
            return;
        }
        int size = Math.max(1, props.getBatchSize());
        List<List<String>> pages = paginate(all, size);
        for (List<String> page : pages) {
            Timer.Sample sample = Timer.start(meterRegistry);
            try {
                TimemoeBatchPredictResponse resp = batchPredictService.batchPredict(
                        page,
                        null, null,
                        props.getHistoryLength(),
                        props.getMinimumHistorySize(),
                        null,
                        props.getMode()
                );
                predictionResultService.saveBatch(resp);
                meterRegistry.counter("ai_predict.batch.success").increment();
            } catch (Exception ex) {
                log.warn("预测调度执行失败: size={}, reason={}", page.size(), ex.getMessage(), ex);
                meterRegistry.counter("ai_predict.batch.failed").increment();
            } finally {
                sample.stop(batchTimer());
            }
        }
    }

    private List<List<String>> paginate(List<String> input, int pageSize) {
        List<List<String>> pages = new ArrayList<>();
        for (int i = 0; i < input.size(); i += pageSize) {
            pages.add(new ArrayList<>(input.subList(i, Math.min(i + pageSize, input.size()))));
        }
        return pages;
    }
}
