package ru.vzotov.langchain4j.gigachat.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import ru.vzotov.langchain4j.gigachat.GigachatChatModel;
import ru.vzotov.langchain4j.gigachat.GigachatEmbeddingModel;

import static ru.vzotov.langchain4j.gigachat.spring.Properties.PREFIX;

@AutoConfiguration
@EnableConfigurationProperties(Properties.class)
public class AutoConfig {

    @Bean
    @ConditionalOnProperty(PREFIX + ".chat-model.client-id")
    GigachatChatModel gigachatChatModel(Properties properties) {
        ChatModelProperties chatModelProperties = properties.getChatModel();
        return GigachatChatModel.builder()
                .clientId(chatModelProperties.getClientId())
                .clientSecret(chatModelProperties.getClientSecret())
                .scope(chatModelProperties.getScope())
                .modelName(chatModelProperties.getModelName())
                .temperature(chatModelProperties.getTemperature())
                .topP(chatModelProperties.getTopP())
                .maxTokens(chatModelProperties.getMaxTokens())
                .timeout(chatModelProperties.getTimeout())
                .build();
    }

    @Bean
    @ConditionalOnProperty(PREFIX + ".embedding-model.client-id")
    GigachatEmbeddingModel gigachatEmbeddingModel(Properties properties) {
        EmbeddingModelProperties embeddingModelProperties = properties.getEmbeddingModel();
        return GigachatEmbeddingModel.builder()
                .clientId(embeddingModelProperties.getClientId())
                .clientSecret(embeddingModelProperties.getClientSecret())
                .scope(embeddingModelProperties.getScope())
                .modelName(embeddingModelProperties.getModelName())
                .timeout(embeddingModelProperties.getTimeout())
                .maxRetries(embeddingModelProperties.getMaxRetries())
                .build();
    }
}