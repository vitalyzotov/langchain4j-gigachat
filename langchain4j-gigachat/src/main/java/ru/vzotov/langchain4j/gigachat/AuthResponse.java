package ru.vzotov.langchain4j.gigachat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class AuthResponse {
    private String accessToken;
    private long expiresAt;
}
