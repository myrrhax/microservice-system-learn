package com.myrrhax.insightservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class GoogleAIBeans {
    private final ResourceLoader resourceLoader;

    @Value("${app.ai.system-prompt-filename:SystemPrompt.md}")
    private String systemPromptFilename;

    String loadSystemPrompt() {
        Resource resource = resourceLoader.getResource("classpath:" + systemPromptFilename);

        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            log.warn("No system prompt found for file {}", systemPromptFilename);

            return "";
        }
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(loadSystemPrompt())
                .build();
    }
}
