package com.system.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 本地 Function Calling 工具接口（与具体大模型无关）。
 * 用于在同进程内将 LLM 的函数调用映射到业务服务。
 */
public interface LocalTool {
    /** 工具名称（如：health_score） */
    String name();

    /** 工具描述 */
    String description();

    /** JSON Schema（字符串）描述参数 */
    String jsonSchema();

    /** 执行工具，入参为 JSON（对象），返回 JSON（对象/基础类型） */
    JsonNode execute(JsonNode arguments);
}

