package ru.vzotov;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import ru.vzotov.langchain4j.gigachat.GigachatChatModel;
import ru.vzotov.langchain4j.gigachat.GigachatEmbeddingModel;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

public class Main {
    public static void main(String[] args) {
        // Embeddings
        EmbeddingModel embeddingModel = GigachatEmbeddingModel.builder()
                .clientId(System.getenv("GIGACHAT_CLIENT_ID"))
                .clientSecret(System.getenv("GIGACHAT_CLIENT_SECRET"))
                .scope(GigachatScope.GIGACHAT_API_PERS)
                .logRequests(true)
                .logResponses(false)
                .build();
        Response<Embedding> response = embeddingModel.embed("Hello, how are you?");
        System.out.println(response);

        // Generation
        ChatLanguageModel model = GigachatChatModel.builder()
                .clientId(System.getenv("GIGACHAT_CLIENT_ID"))
                .clientSecret(System.getenv("GIGACHAT_CLIENT_SECRET"))
                .scope(GigachatScope.GIGACHAT_API_PERS)
                .logRequests(true)
                .build();
        String answer = model.generate("Tell me a joke about Java");
        System.out.println(answer);
    }
}