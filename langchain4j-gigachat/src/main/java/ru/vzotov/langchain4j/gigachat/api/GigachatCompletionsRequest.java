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
public class GigachatCompletionsRequest {
    private String model;
    private List<GigachatMessage> messages;
    private Double temperature;
    private Double topP;
    private Long n;
    private Boolean stream;
    private Long maxTokens;
    private Double repetitionPenalty;
    private Integer updateInterval;

}
