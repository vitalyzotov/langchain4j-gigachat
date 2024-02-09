package ru.vzotov;

import dev.langchain4j.model.chat.ChatLanguageModel;
import ru.vzotov.langchain4j.gigachat.GigachatChatModel;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

public class Main {
    public static void main(String[] args) {
        // Create an instance of a model
//        ChatLanguageModel model = OpenAiChatModel.withApiKey("demo");
        ChatLanguageModel model = GigachatChatModel.builder()
                .clientId(System.getenv("GIGACHAT_CLIENT_ID"))
                .clientSecret(System.getenv("GIGACHAT_CLIENT_SECRET"))
                .scope(GigachatScope.GIGACHAT_API_PERS)
                .logRequests(true)
                .build();

        // Start interacting
        String answer = model.generate("Tell me a joke about Java");

        System.out.println(answer); // Hello! How can I assist you today?
    }
}