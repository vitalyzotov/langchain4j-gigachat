package ru.vzotov.langchain4j.gigachat.spring;

import lombok.Getter;
import lombok.Setter;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

import java.time.Duration;

@Getter
@Setter
class ChatModelProperties {
    String clientId;
    String clientSecret;
    GigachatScope scope;
    String modelName;
    Double temperature;
    Double topP;
    Integer maxTokens;
    Duration timeout;
    Integer maxRetries;
}