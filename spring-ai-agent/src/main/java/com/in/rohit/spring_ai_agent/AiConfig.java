package com.in.rohit.spring_ai_agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
 * WHY:
 * Different AI providers ke ChatClient beans configure karne ke liye.
 */
@Configuration
public class AiConfig {

    @Bean("ollamaChatClient")
    public ChatClient ollamaChatClient(
            @Qualifier("ollamaChatModel")
            ChatModel ollamaChatModel) {

        return ChatClient.builder(ollamaChatModel)
                .build();
    }

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(
            @Qualifier("googleGenAiChatModel")
            ChatModel geminiChatModel) {

        return ChatClient.builder(geminiChatModel)
                .build();
    }

    @Bean("groqChatClient")
    public ChatClient groqChatClient(
            @Qualifier("openAiChatModel")
            ChatModel groqChatModel) {

        return ChatClient.builder(groqChatModel)
                .build();
    }

    @Bean("nvidiaChatClient")
    public ChatClient nvidiaChatClient() {

        OpenAiChatModel nvidiaChatModel =
                OpenAiChatModel.builder()
                        .options(
                                OpenAiChatOptions.builder()
                                        .baseUrl(
                                                "https://integrate.api.nvidia.com/v1"
                                        )
                                        .apiKey(
                                                System.getenv(
                                                        "NVIDIA_API_KEY"
                                                )
                                        )
                                        .model(
                                                "openai/gpt-oss-20b"
                                        )
                                        .maxTokens(1000)
                                        .build()
                        )
                        .build();

        return ChatClient
                .builder(nvidiaChatModel)
                .build();
    }
}