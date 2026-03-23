package com.system.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<LocalTool> tools;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<String, LocalTool> cache = new ConcurrentHashMap<>();

    public LocalTool get(String name) {
        return cache.computeIfAbsent(name, n -> tools.stream()
                .filter(t -> t.name().equals(n))
                .findFirst()
                .orElse(null));
    }

    public JsonNode execute(String name, JsonNode args) {
        LocalTool tool = get(name);
        if (tool == null) {
            ObjectNode err = objectMapper.createObjectNode();
            err.put("error", "tool_not_found");
            err.put("name", name);
            return err;
        }
        return tool.execute(args == null ? objectMapper.createObjectNode() : args);
    }

    public List<LocalTool> list() { return tools; }
}

