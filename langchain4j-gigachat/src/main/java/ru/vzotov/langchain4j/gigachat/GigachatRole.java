package ru.vzotov.langchain4j.gigachat;

import lombok.Getter;

@Getter
enum GigachatRole {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant"),
    SEARCH_RESULT("search_result");

    private final String value;

    GigachatRole(String value) {
        this.value = value;
    }
}
