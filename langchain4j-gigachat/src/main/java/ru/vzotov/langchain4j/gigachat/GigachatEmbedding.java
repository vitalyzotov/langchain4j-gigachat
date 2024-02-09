package ru.vzotov.langchain4j.gigachat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigachatEmbedding {
    private String object;
    private List<Float> embedding;
    private Integer index;
    private GigachatEmbeddingUsage usage;
}
