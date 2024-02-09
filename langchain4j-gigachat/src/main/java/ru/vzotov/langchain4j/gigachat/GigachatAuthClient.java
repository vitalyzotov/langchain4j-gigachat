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
import java.util.UUID;

import static dev.langchain4j.internal.Utils.isNullOrBlank;

public class GigachatAuthClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GigachatAuthClient.class);
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();

    private final GigachatAuthClientApi gigachatAuthClientApi;
    private final GigachatScope scope;

    @Builder
    public GigachatAuthClient(String baseUrl,
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

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new BasicAuthInterceptor(clientId, clientSecret))
                .callTimeout(timeout)
                .connectTimeout(timeout)
                .readTimeout(timeout)
                .writeTimeout(timeout);

        if (logRequests) {
            okHttpClientBuilder.addInterceptor(new GigachatRequestLoggingInterceptor());
        }

        if (logResponses) {
            okHttpClientBuilder.addInterceptor(new GigachatResponseLoggingInterceptor());
        }

        OkHttpClient okHttpClient = okHttpClientBuilder.build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(formattedUrlForRetrofit(baseUrl))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(GSON))
                .build();

        gigachatAuthClientApi = retrofit.create(GigachatAuthClientApi.class);

    }

    private static String formattedUrlForRetrofit(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }

    AuthResponse getToken() {
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

    private RuntimeException toException(retrofit2.Response<?> retrofitResponse) throws IOException {
        int code = retrofitResponse.code();
        if (code >= 400) {
            ResponseBody errorBody = retrofitResponse.errorBody();
            if (errorBody != null) {
                String errorBodyString = errorBody.string();
                String errorMessage = String.format("status code: %s; body: %s", code, errorBodyString);
                LOGGER.error("Error response: {}", errorMessage);
                return new RuntimeException(errorMessage);
            }
        }
        return new RuntimeException(retrofitResponse.message());
    }

}
