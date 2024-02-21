package ru.vzotov.langchain4j.gigachat;

import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

/**
 * This class provides an implementation for applying request metadata using a Bearer token.
 * The Bearer token is supplied by the tokenSupplier.
 * This token is then used to create an Authorization header which is applied to the request metadata.
 */
class BearerToken extends CallCredentials {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerToken.class);

    private final Supplier<String> tokenSupplier;

    /**
     * This constructor creates a new instance of BearerToken with the specified tokenSupplier.
     *
     * @param tokenSupplier a Supplier function interface that supplies the Bearer token.
     * @throws NullPointerException if tokenSupplier is null.
     */
    public BearerToken(Supplier<String> tokenSupplier) {
        Objects.requireNonNull(tokenSupplier);
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    public void applyRequestMetadata(RequestInfo requestInfo, Executor executor, MetadataApplier applier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(Metadata.Key.of("Authorization", ASCII_STRING_MARSHALLER),
                        String.format("%s %s", "Bearer", tokenSupplier.get()));
                applier.apply(headers);
            } catch (Throwable e) {
                LOGGER.debug("Error when trying to get a token", e);
                applier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });
    }

    @Override
    public void thisUsesUnstableApi() {
    }
}
