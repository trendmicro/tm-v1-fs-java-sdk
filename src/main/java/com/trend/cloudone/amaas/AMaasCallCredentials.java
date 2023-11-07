package com.trend.cloudone.amaas;

import java.util.concurrent.Executor;
import java.util.regex.*;
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

    public AMaasConstants.TokenType getTokenType() {
        return tokenType;
    }

    AMaasCallCredentials(String token) {
        this.token = token;
        Pattern p = Pattern.compile("ey[^.]+.ey[^.]+.ey[^.]+");
        Matcher m = p.matcher(token);
        if (m.matches()) {
            this.tokenType = AMaasConstants.TokenType.AUTH_TYPE_BEARER;
        } else {
            this.tokenType = AMaasConstants.TokenType.AUTH_TYPE_APIKEY;
        }
    }

    @Override
    public void applyRequestMetadata(
            RequestInfo requestInfo,
            Executor executor,
            MetadataApplier metadataApplier) {
        executor.execute(() -> {
            try {
                Metadata headers = new Metadata();
                String tokenTypeStr = "";
                switch (this.tokenType) {
                    case AUTH_TYPE_BEARER: {
                        tokenTypeStr = "Bearer ";
                        break;
                    }
                    case AUTH_TYPE_APIKEY: {
                        tokenTypeStr = "ApiKey ";
                        break;
                    }
                }
                headers.put(AMaasConstants.META_DATA_KEY, tokenTypeStr + token);
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
