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
public class GigachatCompletionsResponse {

    private List<Choice> choices;
    private Long created;
    private String model;
    private GigachatUsage usage;
    private String object;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Choice {
        private GigachatMessage message;
        private Integer index;
        private String finishReason;
    }
}
