package ru.vzotov.langchain4j.gigachat;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static ru.vzotov.langchain4j.gigachat.DefaultGigachatHelper.getHeaders;

public class GigachatResponseLoggingInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(GigachatResponseLoggingInterceptor.class);

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        this.log(response);
        return response;
    }

    private void log(Response response) {
        try {
            LOGGER.debug("Response:\n- status code: {}\n- headers: {}\n- body: {}",
                    response.code(), getHeaders(response.headers()), this.getBody(response));
        } catch (Exception e) {
            LOGGER.warn("Error while logging response: {}", e.getMessage());
        }
    }

    private String getBody(Response response) throws IOException {
        return isEventStream(response)
                ? "[skipping response body due to streaming]"
                : response.peekBody(Long.MAX_VALUE).string();
    }

    private static boolean isEventStream(Response response) {
        String contentType = response.header("Content-Type");
        return contentType != null && contentType.contains("event-stream");
    }
}
