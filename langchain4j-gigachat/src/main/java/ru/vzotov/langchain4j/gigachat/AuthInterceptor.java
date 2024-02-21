package ru.vzotov.langchain4j.gigachat;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Supplier;

public class AuthInterceptor implements Interceptor {

    private static final int HTTP_UNAUTHORIZED = 401;

    private final Supplier<String> credentials;

    private final Runnable unauthorizedHandler;

    public AuthInterceptor(Supplier<String> bearer) {
        this(bearer, null);
    }

    public AuthInterceptor(Supplier<String> bearer, Runnable unauthorizedHandler) {
        this.credentials = () -> String.format("Bearer %s", bearer.get());
        this.unauthorizedHandler = unauthorizedHandler;
    }

    public AuthInterceptor(String user, String password) {
        String basic = Credentials.basic(user, password);
        this.credentials = () -> basic;
        this.unauthorizedHandler = null;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder()
                .header("Authorization", credentials.get()).build();
        Response response = chain.proceed(authenticatedRequest);

        if(response.code() == HTTP_UNAUTHORIZED && unauthorizedHandler != null) {
            unauthorizedHandler.run();
        }

        return response;
    }

}
