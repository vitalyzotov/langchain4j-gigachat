package ru.vzotov.langchain4j.gigachat;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Supplier;

public class AuthInterceptor implements Interceptor {

    private final Supplier<String> credentials;

    public AuthInterceptor(Supplier<String> bearer) {
        this.credentials = () -> String.format("Bearer %s", bearer.get());
    }

    public AuthInterceptor(String user, String password) {
        String basic = Credentials.basic(user, password);
        this.credentials = () -> basic;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials.get()).build();
        return chain.proceed(authenticatedRequest);
    }

}
