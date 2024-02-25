package ru.vzotov.langchain4j.gigachat;

import lombok.Builder;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.vzotov.langchain4j.gigachat.api.GigachatClientApi;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsRequest;
import ru.vzotov.langchain4j.gigachat.api.GigachatCompletionsResponse;
import ru.vzotov.langchain4j.gigachat.api.GigachatEmbeddingRequest;
import ru.vzotov.langchain4j.gigachat.api.GigachatEmbeddingResponse;

import java.io.IOException;
import java.time.Duration;

public class RestGigachatClient extends GigachatClient {
    private final GigachatClientApi gigachatClientApi;

    @Builder
    public RestGigachatClient(String baseAuthUrl,
                              String baseApiUrl,
                              String clientId,
                              String clientSecret,
                              GigachatScope scope,
                              Duration timeout,
                              Boolean logRequests,
                              Boolean logResponses
    ) {
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

    GigachatCompletionsResponse completion(GigachatCompletionsRequest request) {
        try {
            retrofit2.Response<GigachatCompletionsResponse> retrofitResponse
                    = gigachatClientApi.completions(request).execute();
            if (retrofitResponse.isSuccessful()) {
                return retrofitResponse.body();
            } else {
                throw toException(retrofitResponse);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
