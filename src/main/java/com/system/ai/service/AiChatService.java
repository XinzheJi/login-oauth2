package com.system.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.system.ai.tools.ToolRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;
    private final ToolRegistry toolRegistry;

    public AiChatService(ChatClient.Builder chatClientBuilder, ToolRegistry toolRegistry) {
        this.chatClient = chatClientBuilder.build();
        this.toolRegistry = toolRegistry;
    }

    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    /**
     * 本地工具直调（用于无网络/同进程 Function Calling 场景）
     */
    public JsonNode executeTool(String name, JsonNode arguments) {
        return toolRegistry.execute(name, arguments);
    }
}
