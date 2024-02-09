package ru.vzotov.langchain4j.gigachat;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.time.Instant;

@Accessors(fluent = true)
@Getter
class AccessToken {
    private final Instant expiresAt;
    private final String value;

    public AccessToken(Instant expiresAt, String value) {
        this.expiresAt = expiresAt;
        this.value = value;
    }
}
