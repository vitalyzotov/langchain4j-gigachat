package ru.vzotov;

import dev.langchain4j.model.chat.ChatLanguageModel;
import ru.vzotov.langchain4j.gigachat.GigachatChatModel;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

public class Generate {
    public static void main(String[] args) {
        // Generation
        ChatLanguageModel model = GigachatChatModel.builder()
                .clientId(System.getenv("GIGACHAT_CLIENT_ID"))
                .clientSecret(System.getenv("GIGACHAT_CLIENT_SECRET"))
                .scope(GigachatScope.GIGACHAT_API_PERS)
                .logRequests(true)
                .build();
        String answer;

        answer = model.generate("Tell me a joke about Java");
        System.out.println(answer);

        answer = model.generate("Tell me a joke about Python");
        System.out.println(answer);
    }
}
