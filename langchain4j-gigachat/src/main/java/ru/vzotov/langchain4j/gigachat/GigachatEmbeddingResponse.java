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
public class GigachatEmbeddingResponse {
    private String object;
    private List<GigachatEmbedding> data;
    private String model;
}
