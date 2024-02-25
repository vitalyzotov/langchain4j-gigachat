package ru.vzotov.langchain4j.gigachat.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigachatMessage {
    private String role;
    private String content;
}
