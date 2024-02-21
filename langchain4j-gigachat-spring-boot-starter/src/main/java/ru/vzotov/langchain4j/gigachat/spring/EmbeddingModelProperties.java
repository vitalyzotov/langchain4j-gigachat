package ru.vzotov.langchain4j.gigachat.spring;

import lombok.Getter;
import lombok.Setter;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

import java.time.Duration;

@Getter
@Setter
class EmbeddingModelProperties {
    String clientId;
    String clientSecret;
    GigachatScope scope;
    String modelName;
    Duration timeout;
    Integer maxRetries;
    Boolean logRequests;
    Boolean logResponses;
}