package ru.vzotov.langchain4j.gigachat.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GigachatEmbeddingRequest {
    private String model;
    private List<String> input;
}
