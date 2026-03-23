package com.system.ai.predict.taos;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.ai.predict.config.TaosProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaosRestClient {

    private final TaosProperties props;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final ProxySelector NO_PROXY = new ProxySelector() {
        @Override
        public List<Proxy> select(URI uri) {
            return List.of(Proxy.NO_PROXY);
        }

        @Override
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            /* no-op */
        }
    };

    private HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(props.getTimeoutMs()))
                .proxy(NO_PROXY)
                .build();
    }

    public TaosQueryResult query(String sql) {
        String endpoint = props.getBaseUrl().replaceAll("/+$", "") + "/rest/sql";
        String basic = props.getUsername() + ":" + props.getPassword();
        String basicHeader = "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(props.getTimeoutMs()))
                .header("Accept", "application/json")
                .header("Content-Type", "text/plain; charset=UTF-8")
                .header("Authorization", basicHeader)
                // TDengine REST 接口只支持单语句执行，因此上游需保证 SQL 已经携带完整的库名。
                .POST(HttpRequest.BodyPublishers.ofString(sql, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> resp = httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            String body = resp.body();
            if (body == null || body.isBlank()) {
                throw new IOException("empty response body, http=" + resp.statusCode());
            }
            JsonNode root = mapper.readTree(body);
            TaosQueryResult r = TaosQueryResult.fromJson(root);
            if (r.getStatus() == null) {
                log.warn("TDengine unknown response format: {}", root);
            }
            return r;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("TDengine REST query interrupted: " + endpoint, e);
        } catch (IOException e) {
            String msg = e.getMessage();
            if (msg == null || msg.isBlank()) {
                msg = e.getClass().getSimpleName();
            }
            throw new RuntimeException("TDengine REST query failed: " + endpoint + " - " + msg, e);
        }
    }

    public boolean ping() {
        try {
            TaosQueryResult r = query("select now();");
            return r != null && ("succ".equalsIgnoreCase(r.getStatus()) || (r.getRows() != null && r.getRows() > 0));
        } catch (Exception e) {
            log.error("TDengine ping failed", e);
            return false;
        }
    }
}
