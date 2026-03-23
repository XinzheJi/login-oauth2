package com.system.ai.predict.client;

import com.system.ai.predict.client.dto.AgingPredictResponse;
import com.system.ai.predict.client.dto.FaultPredictResponse;
import com.system.ai.predict.client.dto.TimemoeBatchPredictRequest;
import com.system.ai.predict.client.dto.TimemoeBatchPredictResponse;
import com.system.ai.predict.client.dto.TimemoeHealthResponse;
import com.system.ai.predict.client.dto.TimemoePredictRequest;

import java.util.List;

/**
 * timemoe 推理服务客户端接口。
 */
public interface TimemoeClient {

    TimemoeHealthResponse checkHealth();

    AgingPredictResponse predictAging(TimemoePredictRequest request);

    FaultPredictResponse predictFault(TimemoePredictRequest request);

    TimemoeBatchPredictResponse batchPredict(TimemoeBatchPredictRequest request);

    default TimemoeBatchPredictResponse batchPredict(List<TimemoePredictRequest> devices) {
        if (devices == null) {
            throw new IllegalArgumentException("devices 不能为空");
        }
        return batchPredict(TimemoeBatchPredictRequest.builder().devices(devices).build());
    }
}
