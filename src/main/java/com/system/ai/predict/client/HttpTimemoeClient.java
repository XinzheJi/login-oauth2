package com.system.ai.predict.client;

import com.system.ai.predict.client.dto.AgingPredictResponse;
import com.system.ai.predict.client.dto.FaultPredictResponse;
import com.system.ai.predict.client.dto.TimemoeBatchPredictRequest;
import com.system.ai.predict.client.dto.TimemoeBatchPredictResponse;
import com.system.ai.predict.client.dto.TimemoeHealthResponse;
import com.system.ai.predict.client.dto.TimemoePredictRequest;
import com.system.ai.predict.client.exception.TimemoeClientException;
import com.system.ai.predict.client.exception.TimemoeClientException.ErrorType;
import com.system.ai.predict.config.TimemoeProperties;
import com.system.ai.predict.exception.PredictOverloadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 基于 RestTemplate 的 timemoe HTTP 客户端实现。
 */
@Slf4j
public class HttpTimemoeClient implements TimemoeClient {

    private final RestTemplate restTemplate;
    private final TimemoeProperties properties;
    private final Semaphore concurrentLimiter;

    public HttpTimemoeClient(RestTemplate restTemplate, TimemoeProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
        this.concurrentLimiter = new Semaphore(Math.max(1, properties.getMaxConcurrent()), true);
    }

    @Override
    public TimemoeHealthResponse checkHealth() {
        return get(properties.getHealthPath(), TimemoeHealthResponse.class);
    }

    @Override
    public AgingPredictResponse predictAging(TimemoePredictRequest request) {
        return post(properties.getAgingPath(), request, AgingPredictResponse.class);
    }

    @Override
    public FaultPredictResponse predictFault(TimemoePredictRequest request) {
        return post(properties.getFaultPath(), request, FaultPredictResponse.class);
    }

    @Override
    public TimemoeBatchPredictResponse batchPredict(TimemoeBatchPredictRequest request) {
        return post(properties.getBatchPath(), request, TimemoeBatchPredictResponse.class);
    }

    private <T> T get(String path, Class<T> responseType) {
        return execute(path, HttpMethod.GET, null, responseType);
    }

    private <T> T post(String path, Object body, Class<T> responseType) {
        log.info("发送到timemoe的请求体: {}", body);
        return execute(path, HttpMethod.POST, body, responseType);
    }

    private <T> T execute(String path, HttpMethod method, Object body, Class<T> responseType) {
        boolean acquired = acquirePermit(path);
        if (!acquired) {
            throw new PredictOverloadException("预测请求过多，请稍后重试");
        }
        int retries = Math.max(0, properties.getMaxRetries());
        TimemoeClientException lastException = null;
        try {
            for (int attempt = 0; attempt <= retries; attempt++) {
                try {
                    HttpHeaders headers = buildHeaders(method);
                    HttpEntity<Object> entity = new HttpEntity<>(body, headers);
                    String url = buildUrl(path);
                    ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new TimemoeClientException(
                                categorizeStatus(response.getStatusCode().value()),
                                response.getStatusCode().value(),
                                path,
                                null,
                                "调用 timemoe 接口失败，返回码 " + response.getStatusCode(),
                                null
                        );
                    }
                    return response.getBody();
                } catch (RestClientResponseException ex) {
                    TimemoeClientException mapped = mapResponseException(path, ex);
                    log.error("timemoe响应异常: status={}, body={}", ex.getRawStatusCode(), ex.getResponseBodyAsString());
                    if (!shouldRetry(mapped, attempt, retries)) {
                        throw mapped;
                    }
                    lastException = mapped;
                    log.warn("调用 timemoe 接口失败，准备重试，第 {} 次，path={}，status={}，message={}",
                            attempt + 1, path, mapped.getStatusCode(), mapped.getMessage());
                    sleepBackoff(attempt);
                } catch (ResourceAccessException ex) {
                    TimemoeClientException mapped = new TimemoeClientException(
                            ErrorType.NETWORK_ERROR,
                            null,
                            path,
                            null,
                            "timemoe 网络访问异常：" + ex.getMessage(),
                            ex
                    );
                    if (!shouldRetry(mapped, attempt, retries)) {
                        throw mapped;
                    }
                    lastException = mapped;
                    log.warn("timemoe 网络访问异常，准备重试，第 {} 次，path={}，message={}",
                            attempt + 1, path, ex.getMessage());
                    sleepBackoff(attempt);
                } catch (RestClientException ex) {
                    TimemoeClientException mapped = new TimemoeClientException(
                            ErrorType.UNKNOWN,
                            null,
                            path,
                            null,
                            "timemoe 客户端未知错误：" + ex.getMessage(),
                            ex
                    );
                    if (!shouldRetry(mapped, attempt, retries)) {
                        throw mapped;
                    }
                    lastException = mapped;
                    log.warn("timemoe 客户端未知错误，准备重试，第 {} 次，path={}，message={}",
                            attempt + 1, path, ex.getMessage());
                    sleepBackoff(attempt);
                }
            }
            if (lastException != null) {
                throw lastException;
            }
            throw new TimemoeClientException(
                    ErrorType.UNKNOWN,
                    null,
                    path,
                    null,
                    "timemoe 请求失败且未捕获具体异常",
                    null
            );
        } finally {
            concurrentLimiter.release();
        }
    }

    private boolean acquirePermit(String path) {
        long timeoutMs = Math.max(0L, properties.getAcquireTimeoutMs());
        try {
            boolean ok = timeoutMs == 0L
                    ? concurrentLimiter.tryAcquire()
                    : concurrentLimiter.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
            if (!ok) {
                log.warn("timemoe 并发闸门触发，path={}, maxConcurrent={}", path, properties.getMaxConcurrent());
            }
            return ok;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new PredictOverloadException("预测请求排队中断，请稍后重试");
        }
    }

    private HttpHeaders buildHeaders(HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        applyApiKey(headers);
        // 透传/生成请求ID，便于链路追踪
        if (!headers.containsKey("X-Request-ID")) {
            headers.set("X-Request-ID", UUID.randomUUID().toString());
        }
        return headers;
    }

    private void applyApiKey(HttpHeaders headers) {
        if (!StringUtils.hasText(properties.getApiKey())) {
            return;
        }
        String headerName = StringUtils.hasText(properties.getApiKeyHeader())
                ? properties.getApiKeyHeader()
                : "Authorization";
        String headerValue = properties.getApiKey();
        if ("Authorization".equalsIgnoreCase(headerName) && properties.isApiKeyUseBearer()
                && !headerValue.toLowerCase().startsWith("bearer ")) {
            headerValue = "Bearer " + headerValue;
        }
        headers.set(headerName, headerValue);
    }

    private boolean shouldRetry(TimemoeClientException exception, int attempt, int retries) {
        if (attempt >= retries) {
            return false;
        }
        ErrorType type = exception.getErrorType();
        return type == ErrorType.NETWORK_ERROR || type == ErrorType.SERVER_ERROR;
    }

    private TimemoeClientException mapResponseException(String path, RestClientResponseException ex) {
        ErrorType type = categorizeStatus(ex.getRawStatusCode());
        return new TimemoeClientException(
                type,
                ex.getRawStatusCode(),
                path,
                ex.getResponseBodyAsString(),
                "timemoe 接口响应异常：" + ex.getStatusText(),
                ex
        );
    }

    private ErrorType categorizeStatus(int status) {
        if (status >= 500) {
            return ErrorType.SERVER_ERROR;
        }
        if (status >= 400) {
            return ErrorType.CLIENT_ERROR;
        }
        return ErrorType.UNKNOWN;
    }

    private String buildUrl(String path) {
        String base = properties.getBaseUrl() != null ? properties.getBaseUrl().trim() : "";
        if (!StringUtils.hasText(path)) {
            return base;
        }
        String normalizedPath = path.startsWith("/") ? path : "/" + path;
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base + normalizedPath;
    }

    private void sleepBackoff(int attempt) {
        try {
            long backoff = (long) Math.min(1000L * (attempt + 1), 3000L);
            Thread.sleep(backoff);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
