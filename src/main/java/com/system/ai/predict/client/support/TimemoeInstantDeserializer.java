package com.system.ai.predict.client.support;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 兼容 timemoe 返回的多种时间格式（可能缺少时区或精度）的反序列化器。
 */
public class TimemoeInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.isBlank()) {
            return null;
        }
        String trimmed = text.trim();

        // 优先尝试标准 ISO-8601
        Instant parsed = tryParseInstant(trimmed);
        if (parsed != null) {
            return parsed;
        }

        // 如果缺少 Z，则按照 UTC 处理
        if (!trimmed.endsWith("Z")) {
            parsed = tryParseInstant(trimmed + "Z");
            if (parsed != null) {
                return parsed;
            }

            // timemoe 可能返回无时区但带微秒的时间，按 UTC 补齐
            try {
                LocalDateTime local = LocalDateTime.parse(trimmed, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return local.toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException ignored) {
                // fall through
            }
        }

        throw new IOException("无法解析预测时间: " + text);
    }

    private Instant tryParseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }
}
