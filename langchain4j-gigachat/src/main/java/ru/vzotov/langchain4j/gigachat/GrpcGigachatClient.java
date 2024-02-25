package ru.vzotov.langchain4j.gigachat;

import gigachat.v1.ChatServiceGrpc;
import gigachat.v1.Gigachatv1;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.Builder;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.vzotov.langchain4j.gigachat.api.GigachatClientApi;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsRequest;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsResponse;
import ru.vzotov.langchain4j.gigachat.api.GigachatEmbeddingRequest;
import ru.vzotov.langchain4j.gigachat.api.GigachatEmbeddingResponse;
import ru.vzotov.langchain4j.gigachat.api.GigachatMessage;
import ru.vzotov.langchain4j.gigachat.api.GigachatUsage;

import java.io.IOException;
import java.time.Duration;
import java.util.stream.Collectors;

import static dev.langchain4j.internal.RetryUtils.withRetry;

public class GrpcGigachatClient extends GigachatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcGigachatClient.class);

    private final GigachatClientApi gigachatClientApi;
    private final ChatServiceGrpc.ChatServiceBlockingStub grpcClient;

    @Builder
    public GrpcGigachatClient(String baseAuthUrl,
                              String baseApiUrl,
                              String clientId,
                              String clientSecret,
                              GigachatScope scope,
                              Duration timeout,
                              Boolean logRequests,
                              Boolean logResponses) {
        super(baseAuthUrl, clientId, clientSecret, scope, timeout, logRequests, logResponses);

        OkHttpClient.Builder apiClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(() -> getAccessToken().value(), this::authorize))
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);

        if (logRequests) {
            apiClientBuilder.addInterceptor(new GigachatRequestLoggingInterceptor());
        }

        if (logResponses) {
            apiClientBuilder.addInterceptor(new GigachatResponseLoggingInterceptor());
        }

        OkHttpClient apiClient = apiClientBuilder.build();

        gigachatClientApi = new Retrofit.Builder()
                .baseUrl(formattedUrlForRetrofit(baseApiUrl))
                .client(apiClient)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build().create(GigachatClientApi.class);

        final ManagedChannel channel = ManagedChannelBuilder.forTarget(DefaultGigachatHelper.GIGACHAT_TARGET).build();
        final BearerToken credentials = new BearerToken(() -> getAccessToken().value());
        grpcClient = ChatServiceGrpc.newBlockingStub(channel)
                .withInterceptors(logRequests ? new ClientInterceptor[]{new GrpcLoggingInterceptor()} :
                        new ClientInterceptor[0])
                .withCallCredentials(credentials);
    }

    GigachatEmbeddingResponse embedding(GigachatEmbeddingRequest request) {
        try {
            retrofit2.Response<GigachatEmbeddingResponse> retrofitResponse
                    = gigachatClientApi.embeddings(request).execute();
            if (retrofitResponse.isSuccessful()) {
                return retrofitResponse.body();
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    GigachatCompletionsResponse completion(GigachatCompletionsRequest request) {
        Gigachatv1.ChatResponse chat = withRetry(() -> {
            try {
                Gigachatv1.ChatOptions.Builder optionsBuilder = Gigachatv1.ChatOptions.newBuilder();
                if (request.getTemperature() != null) {
                    optionsBuilder.setTemperature(request.getTemperature().floatValue());
                }
                if (request.getMaxTokens() != null) {
                    optionsBuilder.setMaxTokens(request.getMaxTokens().intValue());
                }
                if (request.getTopP() != null) {
                    optionsBuilder.setTopP(request.getTopP().floatValue());
                }
                return grpcClient.chat(Gigachatv1.ChatRequest.newBuilder()
                        .setModel(request.getModel())
                        .setOptions(optionsBuilder.build())
                        .addAllMessages(request.getMessages().stream().map(msg -> Gigachatv1.Message.newBuilder()
                                        .setRole(msg.getRole())
                                        .setContent(msg.getContent())
                                        .build())
                                .collect(Collectors.toList()))
                        .build());
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.UNAUTHENTICATED) {
                    authorize();
                }
                throw e;
            }
        }, 2);

        return GigachatCompletionsResponse.builder()
                .choices(
                        chat.getAlternativesList().stream()
                                .map(ch -> GigachatCompletionsResponse.Choice.builder()
                                        .index(ch.getIndex())
                                        .finishReason(ch.getFinishReason())
                                        .message(GigachatMessage.builder()
                                                .role(ch.getMessage().getRole())
                                                .content(ch.getMessage().getContent())
                                                .build())
                                        .build())
                                .collect(Collectors.toList())
                )
                .created(chat.getTimestamp())
                .model(chat.getModelInfo().getName())
                .usage(GigachatUsage.builder()
                        .promptTokens(chat.getUsage().getPromptTokens())
                        .completionTokens(chat.getUsage().getCompletionTokens())
                        .totalTokens(chat.getUsage().getTotalTokens())
                        .build())
                .build();
    }
}
