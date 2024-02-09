package ru.vzotov.langchain4j.gigachat;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface GigachatClientApi {

    @POST("api/v1/embeddings")
    @Headers({"Content-Type: application/json"})
    Call<GigachatEmbeddingResponse> embeddings(@Body GigachatEmbeddingRequest request);
}
