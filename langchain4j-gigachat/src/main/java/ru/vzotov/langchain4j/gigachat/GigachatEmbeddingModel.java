package ru.vzotov.langchain4j.gigachat;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.Builder;

import java.time.Duration;
import java.util.List;

import static dev.langchain4j.internal.RetryUtils.withRetry;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static java.util.stream.Collectors.toList;
import static ru.vzotov.langchain4j.gigachat.DefaultGigachatHelper.GIGACHAT_API_URL;
import static ru.vzotov.langchain4j.gigachat.DefaultGigachatHelper.GIGACHAT_AUTH_URL;
import static ru.vzotov.langchain4j.gigachat.DefaultGigachatHelper.GIGACHAT_EMBEDDINGS_MODEL_NAME;
import static ru.vzotov.langchain4j.gigachat.DefaultGigachatHelper.tokenUsageFrom;

/**
 * Represents a Gigachat embedding model.
 * You can find description of parameters <a href="https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/post-embeddings">here</a>.
 */
public class GigachatEmbeddingModel implements EmbeddingModel {

    private final GigachatClient client;
    private final String modelName;
    private final Integer maxRetries;

    /**
     * Constructs a new GigachatEmbeddingModel instance.
     *
     * @param clientId     the Client Id for authentication
     * @param clientSecret the Client Secret for authentication
     * @param scope        API version to which access is granted.
     * @param modelName    the name of the embedding model. It uses a default value if not specified
     * @param timeout      the timeout duration for API requests. It uses a default value of 60 seconds if not specified
     *                     <p>
     *                     The default value is 60 seconds
     * @param logRequests  a flag indicating whether to log API requests
     * @param logResponses a flag indicating whether to log API responses
     * @param maxRetries   the maximum number of retries for API requests. It uses a default value of 3 if not specified
     */
    @Builder
    public GigachatEmbeddingModel(
            String clientId,
            String clientSecret,
            GigachatScope scope,
            String modelName,
            Duration timeout,
            Boolean logRequests,
            Boolean logResponses,
            Integer maxRetries) {
        this.client = GigachatClient.builder()
                .baseAuthUrl(GIGACHAT_AUTH_URL)
                .baseApiUrl(GIGACHAT_API_URL)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(scope)
                .timeout(getOrDefault(timeout, Duration.ofSeconds(60)))
                .logRequests(getOrDefault(logRequests, false))
                .logResponses(getOrDefault(logResponses, false))
                .build();
        this.modelName = getOrDefault(modelName, GIGACHAT_EMBEDDINGS_MODEL_NAME);
        this.maxRetries = getOrDefault(maxRetries, 3);
    }

    @Override
    public Response<List<Embedding>> embedAll(List<TextSegment> textSegments) {
        GigachatEmbeddingRequest request = GigachatEmbeddingRequest.builder()
                .model(modelName)
                .input(textSegments.stream().map(TextSegment::text).collect(toList()))
                .build();

        GigachatEmbeddingResponse response = withRetry(() -> client.embedding(request), maxRetries);

        List<Embedding> embeddings = response.getData().stream()
                .map(gigachatEmbedding -> Embedding.from(gigachatEmbedding.getEmbedding()))
                .collect(toList());

        return Response.from(
                embeddings,
                tokenUsageFrom(response.getData().stream().map(GigachatEmbedding::getUsage).collect(toList()))
        );
    }
}
