package ru.vzotov.langchain4j.gigachat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import gigachat.v1.ChatServiceGrpc;
import gigachat.v1.Gigachatv1;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Builder;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;

public class GigachatChatModel implements ChatLanguageModel {

    private final ChatServiceGrpc.ChatServiceBlockingStub client;
    private final GigachatClient gigachatClient;
    private final Double temperature;
    private final Integer maxTokens;
    private final Double topP;
    private final String modelName;

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
            Boolean logResponses
    ) {
        Boolean logReq = getOrDefault(logRequests, false);
        this.gigachatClient = GigachatClient.builder()
                .baseAuthUrl(DefaultGigachatHelper.GIGACHAT_AUTH_URL)
                .baseApiUrl(DefaultGigachatHelper.GIGACHAT_API_URL)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope(scope)
                .timeout(getOrDefault(timeout, Duration.ofSeconds(60)))
                .logRequests(logReq)
                .logResponses(getOrDefault(logResponses, false))
                .build();
        this.temperature = getOrDefault(temperature, 0.87);
        this.maxTokens = getOrDefault(maxTokens, 1024);
        this.topP = getOrDefault(topP, 0.47);
        this.modelName = getOrDefault(modelName, "GigaChat");
        final ManagedChannel channel = ManagedChannelBuilder.forTarget(DefaultGigachatHelper.GIGACHAT_TARGET).build();
        final BearerToken credentials = new BearerToken(() -> gigachatClient.getAccessToken().value());
        client = ChatServiceGrpc.newBlockingStub(channel)
                .withInterceptors(logReq ? new ClientInterceptor[]{new GrpcLoggingInterceptor()} :
                        new ClientInterceptor[0])
                .withCallCredentials(credentials);
    }

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        ensureNotEmpty(messages, "messages");
        Gigachatv1.ChatResponse chat = client.chat(Gigachatv1.ChatRequest.newBuilder()
                .setModel(this.modelName)
                .setOptions(Gigachatv1.ChatOptions.newBuilder()
                        .setTemperature(this.temperature.floatValue())
                        .setMaxTokens(this.maxTokens)
                        .setTopP(this.topP.floatValue())
                        .build())
                .addAllMessages(messages.stream().map(msg -> Gigachatv1.Message.newBuilder()
                                .setRole(DefaultGigachatHelper.toGigachatRole(msg.type()).getValue())
                                .setContent(DefaultGigachatHelper.toGigachatMessageContent(msg))
                                .build())
                        .collect(Collectors.toList()))
                .build());

        Gigachatv1.Alternative choice = chat.getAlternativesList().get(0);
        return Response.from(
                AiMessage.from(choice.getMessage().getContent()),
                DefaultGigachatHelper.tokenUsageFrom(chat.getUsage()),
                DefaultGigachatHelper.finishReasonFrom(choice.getFinishReason())
        );
    }

}
