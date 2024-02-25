package ru.vzotov.langchain4j.gigachat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsRequest;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsResponse;
import ru.vzotov.langchain4j.gigachat.api.GigachatMessage;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.RetryUtils.withRetry;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;

public class GigachatChatModel implements ChatLanguageModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(GigachatChatModel.class);
    private final GigachatClient gigachatClient;
    private final Double temperature;
    private final Integer maxTokens;
    private final Double topP;
    private final String modelName;
    private final Integer maxRetries;

    /**
     * @param clientId     the Client Id for authentication
     * @param clientSecret the Client Secret for authentication
     * @param scope        API version to which access is granted.
     * @param temperature  the temperature parameter for generating chat responses
     * @param maxTokens    the maximum number of new tokens to generate in a chat response
     * @param topP         the top-p parameter for generating chat responses
     * @param modelName    the name of the model to use. Examples: GigaChat-Plus, GigaChat-Pro, GigaChat:latest, GigaChat.
     *                     Default: GigaChat
     * @param timeout      the timeout duration for API requests
     *                     <p>
     *                     The default value is 60 seconds
     * @param logRequests  a flag indicating whether to log API requests
     * @param logResponses a flag indicating whether to log API responses
     */
    @Builder
    public GigachatChatModel(
            String clientId,
            String clientSecret,
            GigachatScope scope,
            Double temperature,
            Integer maxTokens,
            Double topP,
            String modelName,
            Duration timeout,
            Boolean logRequests,
            Boolean logResponses,
            Integer maxRetries,
            boolean useGrpc
    ) {
        this.maxRetries = getOrDefault(maxRetries, 3);

        this.gigachatClient = useGrpc
                ?
                GrpcGigachatClient.builder()
                        .baseAuthUrl(DefaultGigachatHelper.GIGACHAT_AUTH_URL)
                        .baseApiUrl(DefaultGigachatHelper.GIGACHAT_API_URL)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .scope(scope)
                        .timeout(getOrDefault(timeout, Duration.ofSeconds(60)))
                        .logRequests(getOrDefault(logRequests, false))
                        .logResponses(getOrDefault(logResponses, false))
                        .build()
                :
                RestGigachatClient.builder()
                        .baseAuthUrl(DefaultGigachatHelper.GIGACHAT_AUTH_URL)
                        .baseApiUrl(DefaultGigachatHelper.GIGACHAT_API_URL)
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .scope(scope)
                        .timeout(getOrDefault(timeout, Duration.ofSeconds(60)))
                        .logRequests(getOrDefault(logRequests, false))
                        .logResponses(getOrDefault(logResponses, false))
                        .build();
        this.temperature = getOrDefault(temperature, 0.87);
        this.maxTokens = getOrDefault(maxTokens, 1024);
        this.topP = getOrDefault(topP, 0.47);
        this.modelName = getOrDefault(modelName, "GigaChat");
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        ensureNotEmpty(messages, "messages");
        LOGGER.debug("prompt: {}", messages);

        GigachatCompletionsRequest request = GigachatCompletionsRequest.builder()
                .model(this.modelName)
                .temperature(this.temperature)
                .maxTokens(this.maxTokens.longValue())
                .topP(this.topP)
                .messages(messages.stream().map(msg -> GigachatMessage.builder()
                        .role(DefaultGigachatHelper.toGigachatRole(msg.type()).getValue())
                        .content(DefaultGigachatHelper.toGigachatMessageContent(msg))
                        .build()).collect(Collectors.toList()))
                .build();

        GigachatCompletionsResponse result = withRetry(() -> gigachatClient.completion(request), maxRetries);

        GigachatCompletionsResponse.Choice choice = result.getChoices().get(0);
        AiMessage response = AiMessage.from(choice.getMessage().getContent());
        LOGGER.debug("response: {}", response);
        return Response.from(
                response,
                DefaultGigachatHelper.tokenUsageFrom(Collections.singleton(result.getUsage())),
                DefaultGigachatHelper.finishReasonFrom(choice.getFinishReason())
        );
    }
}
