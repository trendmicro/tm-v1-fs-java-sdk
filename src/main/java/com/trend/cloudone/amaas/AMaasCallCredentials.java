package com.trend.cloudone.amaas;

import java.util.concurrent.Executor;
import io.grpc.CallCredentials;
import io.grpc.Metadata;
import io.grpc.Status;

/*
 * Realization of the abstract CallCredential class to carries credential data that will be propagated to the server via
 * request metadata for each RPC.
 */
final class AMaasCallCredentials extends CallCredentials {
    private String token;
    private AMaasConstants.TokenType tokenType;
    private String appName;

    public AMaasConstants.TokenType getTokenType() {
        return tokenType;
    }

    AMaasCallCredentials(final String authToken, final String applicationName) {
        this.token = authToken;
        this.appName = applicationName;
        this.tokenType  = AMaasConstants.TokenType.AUTH_TYPE_APIKEY;
    }

    @Override
    public void applyRequestMetadata(
            final RequestInfo requestInfo,
            final Executor executor,
            final MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                headers.put(AMaasConstants.META_DATA_KEY, "ApiKey " + token);
                headers.put(AMaasConstants.META_APP_KEY, this.appName);
                metadataApplier.apply(headers);
            } catch (Throwable e) {
                metadataApplier.fail(Status.UNAUTHENTICATED.withCause(e));
            }
        });

    }

    @Override
    public void thisUsesUnstableApi() {
        // yes this is unstable :(
    }
}
