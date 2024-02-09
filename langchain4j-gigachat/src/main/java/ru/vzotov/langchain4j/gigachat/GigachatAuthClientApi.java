package ru.vzotov.langchain4j.gigachat;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

interface GigachatAuthClientApi {

    @FormUrlEncoded
    @POST("api/v2/oauth")
    @Headers({"Content-Type: application/x-www-form-urlencoded"})
    Call<AuthResponse> getToken(@Field("scope") GigachatScope scope, @Header("RqUID") String rqUid);
}
