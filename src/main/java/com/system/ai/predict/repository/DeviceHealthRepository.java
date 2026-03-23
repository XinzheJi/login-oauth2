package com.system.ai.predict.repository;

import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceMetadata;
import com.system.ai.predict.model.DeviceSnapshot;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 从 TDengine 拉取预测所需的设备指标与元数据。
 */
public interface DeviceHealthRepository {

    Optional<DeviceSnapshot> fetchLatestSnapshot(String deviceId);

    List<DeviceHistoryPoint> fetchHistorySeries(String deviceId, Instant start, Instant end, int limit);

    Map<String, DeviceSnapshot> fetchBatchLatestSnapshots(Collection<String> deviceIds, Instant windowStart);

    Optional<DeviceMetadata> fetchMetadata(String deviceId);
}

