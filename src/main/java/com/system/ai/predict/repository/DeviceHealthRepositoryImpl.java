package com.system.ai.predict.repository;

import com.system.ai.predict.config.TaosProperties;
import com.system.ai.predict.exception.TaosAccessException;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceMetadata;
import com.system.ai.predict.model.DeviceSnapshot;
import com.system.ai.predict.taos.TaosQueryResult;
import com.system.ai.predict.taos.TaosRestClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * TDengine 数据访问实现。
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class DeviceHealthRepositoryImpl implements DeviceHealthRepository {

    private final TaosRestClient taos;
    private final TaosProperties props;

    private static final DateTimeFormatter SQL_TS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH).withZone(ZoneOffset.UTC);

    @Override
    @Cacheable(cacheNames = "aiPredict:deviceSnapshot", key = "#deviceId", cacheManager = "aiPredictCacheManager")
    public Optional<DeviceSnapshot> fetchLatestSnapshot(String deviceId) {
        try {
            return fetchLatestSnapshotInternal(deviceId, null);
        } catch (RuntimeException ex) {
            throw new TaosAccessException("获取设备最新快照失败，deviceId=" + deviceId, ex);
        }
    }

    @Override
    public List<DeviceHistoryPoint> fetchHistorySeries(String deviceId, Instant start, Instant end, int limit) {
        int effectiveLimit = limit > 0 ? limit : props.getHistoryDefaultLength();
        String table = resolveStableName();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ts, temperature, voltage, memoryUsage, cpuUsage, portErrorRate, icmpLoss ")
                .append("FROM ").append(table)
                .append(" WHERE ").append(deviceCondition(deviceId));
        if (start != null) {
            sql.append(" AND ts >= '").append(formatInstant(start)).append("'");
        }
        if (end != null) {
            sql.append(" AND ts <= '").append(formatInstant(end)).append("'");
        }
        sql.append(" ORDER BY ts ASC");
        if (effectiveLimit > 0) {
            sql.append(" LIMIT ").append(effectiveLimit);
        }
        sql.append(";");

        try {
            String historySql = sql.toString();
            log.debug("执行TDengine历史查询: {}", historySql);
            TaosQueryResult result = taos.query(historySql);
            List<DeviceHistoryPoint> out = new ArrayList<>();
            if (result != null && result.getData() != null) {
                for (List<Object> row : result.getData()) {
                    out.add(mapHistoryRow(row));
                }
            }
            log.debug("历史查询返回行数: {}", out.size());
            return out;
        } catch (RuntimeException ex) {
            throw new TaosAccessException("获取设备历史序列失败，deviceId=" + deviceId, ex);
        }
    }

    @Override
    public Map<String, DeviceSnapshot> fetchBatchLatestSnapshots(Collection<String> deviceIds, Instant windowStart) {
        Map<String, DeviceSnapshot> snapshots = new HashMap<>();
        if (deviceIds == null || deviceIds.isEmpty()) {
            return snapshots;
        }

        Instant effectiveStart = windowStart;
        for (String deviceId : deviceIds) {
            if (deviceId == null || deviceId.isBlank()) {
                continue;
            }
            Optional<DeviceSnapshot> snapshot = fetchLatestSnapshotWithWindow(deviceId, effectiveStart);
            snapshot.ifPresent(value -> snapshots.put(deviceId, value));
        }
        return snapshots;
    }

    @Override
    @Cacheable(cacheNames = "aiPredict:deviceMetadata", key = "#deviceId", cacheManager = "aiPredictCacheManager")
    public Optional<DeviceMetadata> fetchMetadata(String deviceId) {
        String sql = """
                SELECT deviceId, deviceName, vendor, model, batch, region,
                       swVersion, hwVersion, installDate, warrantyEnd
                FROM %s
                WHERE %s
                LIMIT 1;
                """.formatted(resolveStableName(), deviceCondition(deviceId));

        try {
            TaosQueryResult result = taos.query(sql);
            if (result == null || result.getData().isEmpty()) {
                return Optional.empty();
            }
            List<Object> row = result.getData().get(result.getData().size() - 1);
            return Optional.of(mapMetadataRow(row));
        } catch (RuntimeException ex) {
            throw new TaosAccessException("获取设备元数据失败，deviceId=" + deviceId, ex);
        }
    }

    private Optional<DeviceSnapshot> fetchLatestSnapshotWithWindow(String deviceId, Instant windowStart) {
        try {
            return fetchLatestSnapshotInternal(deviceId, windowStart);
        } catch (RuntimeException ex) {
            throw new TaosAccessException("批量获取设备快照失败，deviceId=" + deviceId, ex);
        }
    }

    private Optional<DeviceSnapshot> fetchLatestSnapshotInternal(String deviceId, Instant windowStart) {
        Optional<DeviceSnapshot> snapshot = runLatestSnapshotQuery(deviceId, windowStart, props.isUseSubTables());
        if (snapshot.isPresent() || !props.isUseSubTables()) {
            return snapshot;
        }
        log.warn("子表快照查询为空，自动回退到超级表，deviceId={}", deviceId);
        return runLatestSnapshotQuery(deviceId, windowStart, false);
    }

    private Optional<DeviceSnapshot> runLatestSnapshotQuery(String deviceId, Instant windowStart, boolean useSubTable) {
        String sql = useSubTable
                ? buildLatestFromSubTableSql(deviceId, windowStart)
                : buildLatestFromStableSql(deviceId, windowStart);
        log.debug("执行TDengine快照查询: {}", sql);
        TaosQueryResult result = taos.query(sql);
        if (result == null || result.getData().isEmpty()) {
            return Optional.empty();
        }
        DeviceSnapshot snapshot = mapSnapshotRow(result.getData().get(result.getData().size() - 1));
        if (snapshot.getTimestamp() == null) {
            log.warn("快照时间戳解析失败，忽略当前结果，deviceId={}", deviceId);
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    private DeviceSnapshot mapSnapshotRow(List<Object> row) {
        return DeviceSnapshot.builder()
                .timestamp(parseInstant(row, 0))
                .voltage(toDouble(row, 1))
                .temperature(toDouble(row, 2))
                .memoryUsage(toDouble(row, 3))
                .cpuUsage(toDouble(row, 4))
                .portErrorRate(toDouble(row, 5))
                .icmpLoss(toDouble(row, 6))
                .healthIndexHint(toDouble(row, 7))
                .build();
    }

    private DeviceHistoryPoint mapHistoryRow(List<Object> row) {
        return DeviceHistoryPoint.builder()
                .timestamp(parseInstant(row, 0))
                .temperature(toDouble(row, 1))
                .voltage(toDouble(row, 2))
                .memoryUsage(toDouble(row, 3))
                .cpuUsage(toDouble(row, 4))
                .portErrorRate(toDouble(row, 5))
                .icmpLoss(toDouble(row, 6))
                .build();
    }

    private DeviceMetadata mapMetadataRow(List<Object> row) {
        return DeviceMetadata.builder()
                .deviceId(asString(row, 0))
                .deviceName(asString(row, 1))
                .vendor(asString(row, 2))
                .model(asString(row, 3))
                .batch(asString(row, 4))
                .region(asString(row, 5))
                .swVersion(asString(row, 6))
                .hwVersion(asString(row, 7))
                .installDate(parseInstant(row, 8))
                .warrantyEnd(parseInstant(row, 9))
                .build();
    }

    private String buildLatestFromStableSql(String deviceId, Instant windowStart) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ts, voltage, temperature, memoryUsage, cpuUsage, portErrorRate, icmpLoss, healthIndexHint ")
                .append("FROM ").append(resolveStableName())
                .append(" WHERE ").append(deviceCondition(deviceId));
        if (windowStart != null) {
            sql.append(" AND ts >= '").append(formatInstant(windowStart)).append("'");
        }
        sql.append(" ORDER BY ts DESC LIMIT 1;");
        return sql.toString();
    }

    private String buildLatestFromSubTableSql(String deviceId, Instant windowStart) {
        String tableName = formatSubTableName(deviceId);
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ts, voltage, temperature, memoryUsage, cpuUsage, portErrorRate, icmpLoss, healthIndexHint ")
                .append("FROM ").append(qualifyTable(tableName));
        if (windowStart != null) {
            sql.append(" WHERE ts >= '").append(formatInstant(windowStart)).append("'");
        }
        sql.append(" ORDER BY ts DESC LIMIT 1;");
        return sql.toString();
    }

    private String resolveStableName() {
        String stable = Objects.requireNonNullElse(props.getHealthStable(), "t_device_health");
        return qualifyTable(stable);
    }

    private String deviceCondition(String deviceId) {
        if (deviceId == null) {
            return "1=0";
        }
        String trimmed = deviceId.trim();
        if (trimmed.isEmpty()) {
            return "1=0";
        }
        if (trimmed.matches("^-?\\d+(\\.\\d+)?$")) {
            return "deviceId=" + trimmed;
        }
        return "deviceId='" + escape(trimmed) + "'";
    }

    private String formatSubTableName(String deviceId) {
        String safe = sanitizeIdentifier(deviceId);
        return safe.startsWith("d_") ? safe : "d_" + safe;
    }

    private String qualifyTable(String tableName) {
        if (tableName == null || tableName.isBlank()) {
            return tableName;
        }
        String database = props.getDatabase();
        if (database == null || database.isBlank()) {
            return tableName;
        }
        return database + "." + tableName;
    }

    private String escape(String value) {
        return value.replace("'", "''");
    }

    private String sanitizeIdentifier(String value) {
        String normalized = value.replaceAll("[^A-Za-z0-9_]", "_");
        while (normalized.contains("__")) {
            normalized = normalized.replace("__", "_");
        }
        return normalized;
    }

    private String formatInstant(Instant instant) {
        return SQL_TS_FORMAT.format(instant);
    }

    private Instant parseInstant(List<Object> row, int index) {
        if (row == null || row.size() <= index) {
            return null;
        }
        Object raw = row.get(index);
        if (raw == null) {
            return null;
        }

        // JSON driver 可能直接返回 Long/Double（epoch 秒或毫秒）
        if (raw instanceof Number number) {
            return parseEpochNumber(number.longValue());
        }

        if (raw instanceof java.util.Date date) {
            return date.toInstant();
        }
        if (raw instanceof Instant instant) {
            return instant;
        }

        String text = raw.toString();
        if (text == null || text.isBlank()) {
            return null;
        }

        String trimmed = text.trim();
        if (trimmed.matches("^-?\\d+$")) {
            try {
                long epoch = Long.parseLong(trimmed);
                return parseEpochNumber(epoch);
            } catch (NumberFormatException ignored) {
                // fall through to formatted parsing
            }
        }

        String normalized = trimmed.replace(' ', 'T');
        if (!normalized.endsWith("Z")) {
            normalized = normalized + "Z";
        }
        try {
            return Instant.parse(normalized);
        } catch (Exception e) {
            log.warn("无法解析 TDengine 时间戳文本: {}, normalized={}", trimmed, normalized, e);
            return null;
        }
    }

    private Instant parseEpochNumber(long epochValue) {
        long abs = Math.abs(epochValue);
        if (abs >= 1_000_000_000_000L) {
            return Instant.ofEpochMilli(epochValue);
        }
        return Instant.ofEpochSecond(epochValue);
    }

    private Double toDouble(List<Object> row, int index) {
        if (row == null || row.size() <= index) {
            return null;
        }
        return toDouble(row.get(index));
    }

    private Double toDouble(Object raw) {
        if (raw == null) {
            return null;
        }
        if (raw instanceof Number number) {
            return number.doubleValue();
        }
        if (raw instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private String asString(List<Object> row, int index) {
        if (row == null || row.size() <= index) {
            return null;
        }
        Object raw = row.get(index);
        return raw == null ? null : raw.toString();
    }
}
