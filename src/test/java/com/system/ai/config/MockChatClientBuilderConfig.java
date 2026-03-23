package com.system.ai.config;

import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 提供被测服务可用的 ChatClient.Builder mock，避免依赖真实大模型。
 */
@TestConfiguration
public class MockChatClientBuilderConfig {

    @Bean
    public ChatClient.Builder chatClientBuilder() {
        ChatClient.Builder builder = Mockito.mock(ChatClient.Builder.class);
        ChatClient chatClient = Mockito.mock(ChatClient.class);
        Mockito.when(builder.build()).thenReturn(chatClient);
        return builder;
    }
}
