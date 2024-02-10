package ru.vzotov.langchain4j.gigachat.spring;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import ru.vzotov.langchain4j.gigachat.GigachatChatModel;
import ru.vzotov.langchain4j.gigachat.GigachatEmbeddingModel;
import ru.vzotov.langchain4j.gigachat.GigachatScope;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AutoConfigIT {

    private static final String GIGACHAT_CLIENT_ID = System.getenv("GIGACHAT_CLIENT_ID");

    private static final String GIGACHAT_CLIENT_SECRET = System.getenv("GIGACHAT_CLIENT_SECRET");

    private static final String GIGACHAT_SCOPE = Optional.ofNullable(System.getenv("GIGACHAT_SCOPE"))
            .orElse(GigachatScope.GIGACHAT_API_PERS.name());

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AutoConfig.class));

    @Test
    void should_provide_chat_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.gigachat.chat-model.client-id=" + GIGACHAT_CLIENT_ID,
                        "langchain4j.gigachat.chat-model.client-secret=" + GIGACHAT_CLIENT_SECRET,
                        "langchain4j.gigachat.chat-model.scope=" + GIGACHAT_SCOPE,
                        "langchain4j.open-ai.chat-model.max-tokens=20"
                )
                .run(context -> {

                    ChatLanguageModel chatLanguageModel = context.getBean(ChatLanguageModel.class);
                    assertThat(chatLanguageModel).isInstanceOf(GigachatChatModel.class);
                    if (GIGACHAT_CLIENT_SECRET != null && !GIGACHAT_CLIENT_SECRET.isEmpty()) {
                        assertThat(chatLanguageModel.generate("What is the capital of Germany?")).contains("Berlin");
                        assertThat(context.getBean(GigachatChatModel.class)).isSameAs(chatLanguageModel);
                    }
                });
    }

    @Test
    void should_provide_embedding_model() {
        contextRunner
                .withPropertyValues(
                        "langchain4j.gigachat.embedding-model.client-id=" + GIGACHAT_CLIENT_ID,
                        "langchain4j.gigachat.embedding-model.client-secret=" + GIGACHAT_CLIENT_SECRET,
                        "langchain4j.gigachat.embedding-model.scope=" + GIGACHAT_SCOPE
                )
                .run(context -> {

                    EmbeddingModel embeddingModel = context.getBean(EmbeddingModel.class);
                    assertThat(embeddingModel).isInstanceOf(GigachatEmbeddingModel.class);
                    if (GIGACHAT_CLIENT_SECRET != null && !GIGACHAT_CLIENT_SECRET.isEmpty()) {
                        assertThat(embeddingModel.embed("hi").content().dimension()).isEqualTo(1024);
                        assertThat(context.getBean(GigachatEmbeddingModel.class)).isSameAs(embeddingModel);
                    }
                });
    }
}