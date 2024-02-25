package ru.vzotov.langchain4j.gigachat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import gigachat.v1.Gigachatv1;
import okhttp3.Headers;
import ru.vzotov.langchain4j.gigachat.api.GigachatUsage;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.langchain4j.model.output.FinishReason.LENGTH;
import static dev.langchain4j.model.output.FinishReason.STOP;

class DefaultGigachatHelper {
    static final String GIGACHAT_AUTH_URL = "https://ngw.devices.sberbank.ru:9443";
    static final String GIGACHAT_API_URL = "https://gigachat.devices.sberbank.ru";
    static final String GIGACHAT_TARGET = "gigachat.devices.sberbank.ru";
    static final String GIGACHAT_EMBEDDINGS_MODEL_NAME = "Embeddings";

    static FinishReason finishReasonFrom(String gigachatFinishReason) {
        if (gigachatFinishReason == null) {
            return null;
        }
        switch (gigachatFinishReason) {
            case "stop":
                return STOP;
            case "length":
                return LENGTH;
            case "model_length":
            default:
                return null;
        }
    }

    static TokenUsage tokenUsageFrom(Gigachatv1.Usage usage) {
        if (usage == null) {
            return null;
        }
        return new TokenUsage(
                usage.getPromptTokens(),
                usage.getCompletionTokens(),
                usage.getTotalTokens()
        );
    }

    static TokenUsage tokenUsageFrom(Collection<GigachatUsage> usage) {
        if (usage == null) {
            return null;
        }
        return new TokenUsage(usage.stream().map(GigachatUsage::getPromptTokens).filter(Objects::nonNull)
                .reduce(Integer::sum).orElse(null));
    }

    static String toGigachatMessageContent(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return ((SystemMessage) message).text();
        }

        if (message instanceof AiMessage) {
            return ((AiMessage) message).text();
        }

        if (message instanceof UserMessage) {

            return message.text();
        }

        throw new IllegalArgumentException("Unknown message type: " + message.type());
    }

    static GigachatRole toGigachatRole(ChatMessageType chatMessageType) {
        switch (chatMessageType) {
            case SYSTEM:
                return GigachatRole.SYSTEM;
            case USER:
                return GigachatRole.USER;
            case AI:
                return GigachatRole.ASSISTANT;
            default:
                throw new IllegalArgumentException("Unknown chat message type: " + chatMessageType);
        }
    }

    static String getHeaders(Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false).map(header -> {
            String headerKey = header.component1();
            String headerValue = header.component2();
            if (headerKey.equals("Authorization")) {
                headerValue = "***";
            }
            return String.format("[%s: %s]", headerKey, headerValue);
        }).collect(Collectors.joining(", "));
    }


}
