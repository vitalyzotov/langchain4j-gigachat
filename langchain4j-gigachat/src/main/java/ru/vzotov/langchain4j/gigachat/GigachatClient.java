package ru.vzotov.langchain4j.gigachat;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static dev.langchain4j.internal.Utils.isNullOrBlank;

public class GigachatClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GigachatClient.class);
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private final GigachatAuthClientApi gigachatAuthClientApi;
    private final GigachatClientApi gigachatClientApi;
    private final GigachatScope scope;
    private final AtomicReference<AccessToken> accessToken = new AtomicReference<>();

    @Builder
    public GigachatClient(String baseAuthUrl,
                          String baseApiUrl,
                          String clientId,
                          String clientSecret,
                          GigachatScope scope,
                          Duration timeout,
                          Boolean logRequests,
                          Boolean logResponses
    ) {
        if (isNullOrBlank(clientId) || isNullOrBlank(clientSecret) || scope == null) {
            throw new IllegalArgumentException("Gigachat clientId, clientSecret, scope must be defined. " +
                    "They can be generated here: https://developers.sber.ru/studio/");
        }
        this.scope = scope;

        OkHttpClient.Builder authClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(clientId, clientSecret))
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);

        OkHttpClient.Builder apiClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(() -> getAccessToken().value(), this::authorize))
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);

        if (logRequests) {
            authClientBuilder.addInterceptor(new GigachatRequestLoggingInterceptor());
            apiClientBuilder.addInterceptor(new GigachatRequestLoggingInterceptor());
        }

        if (logResponses) {
            authClientBuilder.addInterceptor(new GigachatResponseLoggingInterceptor());
            apiClientBuilder.addInterceptor(new GigachatResponseLoggingInterceptor());
        }

        OkHttpClient authClient = authClientBuilder.build();
        OkHttpClient apiClient = apiClientBuilder.build();

        gigachatAuthClientApi = new Retrofit.Builder()
                .baseUrl(formattedUrlForRetrofit(baseAuthUrl))
                .client(authClient)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build().create(GigachatAuthClientApi.class);

        gigachatClientApi = new Retrofit.Builder()
                .baseUrl(formattedUrlForRetrofit(baseApiUrl))
                .client(apiClient)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build().create(GigachatClientApi.class);
    }

    private static String formattedUrlForRetrofit(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    AuthResponse token() {
        try {
            retrofit2.Response<AuthResponse> retrofitResponse
                    = gigachatAuthClientApi.getToken(scope, UUID.randomUUID().toString()).execute();
            if (retrofitResponse.isSuccessful()) {
                return retrofitResponse.body();
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private RuntimeException toException(retrofit2.Response<?> retrofitResponse) throws IOException {
        int code = retrofitResponse.code();
        if (code >= 400) {
            try (ResponseBody errorBody = retrofitResponse.errorBody()) {
                if (errorBody != null) {
                    String errorBodyString = errorBody.string();
                    String errorMessage = String.format("status code: %s; body: %s", code, errorBodyString);
                    LOGGER.error("Error response: {}", errorMessage);
                    return new RuntimeException(errorMessage);
                }
            }
        }
        return new RuntimeException(retrofitResponse.message());
    }

    void authorize() {
        accessToken.set(refreshToken());
    }

    AccessToken getAccessToken() {
        return accessToken.updateAndGet(t -> {
            Instant expiresAt = t == null ? null : t.expiresAt();
            LOGGER.debug("Token expires at {}", expiresAt);
            return expiresAt != null && Instant.now().isBefore(expiresAt) ? t : refreshToken();
        });
    }

    private AccessToken refreshToken() {
        AuthResponse token = token();
        return new AccessToken(Instant.ofEpochMilli(token.getExpiresAt()), token.getAccessToken());
    }

}
