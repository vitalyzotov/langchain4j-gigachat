package ru.vzotov.langchain4j.gigachat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigachatEmbeddingUsage {
    private Integer promptTokens;
}
