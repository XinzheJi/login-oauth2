package com.system.ai.predict.repository;

import com.system.ai.predict.config.TaosProperties;
import com.system.ai.predict.model.DeviceHistoryPoint;
import com.system.ai.predict.model.DeviceSnapshot;
import com.system.ai.predict.taos.TaosQueryResult;
import com.system.ai.predict.taos.TaosRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class DeviceHealthRepositoryImplTest {

    private TaosProperties properties;
    private DeviceHealthRepositoryImpl repository;
    private StubTaosRestClient stubClient;

    @BeforeEach
    void setUp() {
        properties = new TaosProperties();
        properties.setUseSubTables(false);
        properties.setHealthStable("t_device_health");
        properties.setHistoryDefaultLength(5);
        stubClient = new StubTaosRestClient(properties);
        repository = new DeviceHealthRepositoryImpl(stubClient, properties);
    }

    @Test
    void fetchLatestSnapshot_shouldMapRowCorrectly() {
        TaosQueryResult result = new TaosQueryResult();
        result.setStatus("succ");
        result.setData(List.of(List.of(
                "2023-04-17 19:00:00.000",
                255.0,
                18.7,
                0.3941,
                0.3109,
                0.00187,
                0.00259,
                77.86
        )));
        stubClient.setResponder(sql -> result);

        Optional<DeviceSnapshot> snapshotOpt = repository.fetchLatestSnapshot("sw-0199");
        assertThat(snapshotOpt).isPresent();
        DeviceSnapshot snapshot = snapshotOpt.get();
        assertThat(snapshot.getVoltage()).isEqualTo(255.0);
        assertThat(snapshot.getTemperature()).isEqualTo(18.7);
        assertThat(snapshot.getTimestamp()).isNotNull();
    }

    @Test
    void fetchLatestSnapshot_shouldFallbackToStableWhenSubTableEmpty() {
        properties.setUseSubTables(true);

        TaosQueryResult empty = new TaosQueryResult();
        empty.setStatus("succ");

        TaosQueryResult stableResult = new TaosQueryResult();
        stableResult.setStatus("succ");
        stableResult.setData(List.of(List.of(
                "2023-04-17 19:00:00.000",
                255.0,
                18.7,
                0.3941,
                0.3109,
                0.00187,
                0.00259,
                77.86
        )));

        stubClient.setResponder(sql -> {
            if (sql.contains("FROM tk.d_sw_0199")) {
                return empty;
            }
            return stableResult;
        });

        Optional<DeviceSnapshot> snapshotOpt = repository.fetchLatestSnapshot("sw-0199");
        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().getTemperature()).isEqualTo(18.7);
    }

    @Test
    void fetchLatestSnapshot_shouldParseEpochMillisTimestamp() {
        long epoch = Instant.parse("2023-04-17T19:00:00Z").toEpochMilli();
        TaosQueryResult result = new TaosQueryResult();
        result.setStatus("succ");
        result.setData(List.of(List.of(
                epoch,
                255.0,
                18.7,
                0.3941,
                0.3109,
                0.00187,
                0.00259,
                77.86
        )));
        stubClient.setResponder(sql -> result);

        Optional<DeviceSnapshot> snapshotOpt = repository.fetchLatestSnapshot("sw-0199");
        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().getTimestamp()).isEqualTo(Instant.ofEpochMilli(epoch));
    }

    @Test
    void fetchHistorySeries_shouldRespectLimitAndOrder() {
        TaosQueryResult result = new TaosQueryResult();
        result.setStatus("succ");
        result.setData(List.of(
                List.of("2023-04-17 15:00:00.000", 18.46, 255.0, 0.3952, 0.3269, 0.00326, 0.00208),
                List.of("2023-04-17 16:00:00.000", 18.71, 255.0, 0.3358, 0.3139, 0.00527, 0.00557)
        ));
        stubClient.setResponder(sql -> result);

        List<DeviceHistoryPoint> history = repository.fetchHistorySeries("sw-0199", null, Instant.parse("2023-04-18T00:00:00Z"), 2);
        assertThat(history).hasSize(2);
        assertThat(history.get(0).getTimestamp()).isNotNull();
        assertThat(history.get(0).getTemperature()).isEqualTo(18.46);
        assertThat(history.get(1).getTemperature()).isEqualTo(18.71);
    }

    private static class StubTaosRestClient extends TaosRestClient {
        private Function<String, TaosQueryResult> responder = sql -> new TaosQueryResult();

        StubTaosRestClient(TaosProperties props) {
            super(props);
        }

        void setResponder(Function<String, TaosQueryResult> responder) {
            this.responder = responder;
        }

        @Override
        public TaosQueryResult query(String sql) {
            return responder.apply(sql);
        }
    }
}
