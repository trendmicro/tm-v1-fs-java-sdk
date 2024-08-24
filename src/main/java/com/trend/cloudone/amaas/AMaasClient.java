package com.trend.cloudone.amaas;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLException;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.stub.CallStreamObserver;
import io.grpc.StatusRuntimeException;
import com.google.protobuf.ByteString;
import com.trend.cloudone.amaas.scan.ScanGrpc;
import com.trend.cloudone.amaas.scan.ScanOuterClass;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Stage;

/**
 * AMaaS Client connecting to AMaaS gRPC server to provide API for malware scanning services. User can use the API
 * to scan a file or a byte buffer.
 */
public final class AMaasClient {
    private static final Logger logger = Logger.getLogger(AMaasClient.class.getName());
    private static final long DEFAULT_SCAN_TIMEOUT = 300;
    private static final int MAX_NUM_OF_TAGS = 8;
    private static final int MAX_LENGTH_OF_TAG = 63;

    private ManagedChannel channel;
    private ScanGrpc.ScanStub asyncStub;
    private AMaasCallCredentials cred;
    private long timeoutSecs = DEFAULT_SCAN_TIMEOUT; // 3 minutes
    private boolean bulk = true;


     /**
     * AMaaSClient constructor. The enabledTLS is default to true and the appName is default to V1FS.
     * @param region region you obtained your api key
     * @param apiKey api key to be used
     * @param timeoutInSecs number in seconds to wait for a scan. 0 default to 180 seconds.
     * @throws AMaasException if an exception is detected, it will convert to AMassException.
     *
     */
    public AMaasClient(final String region, final String apiKey, final long timeoutInSecs) throws AMaasException {
        this(region, null, apiKey, timeoutInSecs, true, null);
    }

    /**
     * AMaaSClient constructor.
     * @deprecated
     * @param region region we obtained your api key
     * @param apiKey api key to be used
     * @param timeoutInSecs number in seconds to wait for a scan. 0 default to 180 seconds.
     * @param enabledTLS boolean flag to enable or disable TLS
     * @param appName application name to use.
     * @throws AMaasException if an exception is detected, it will convert to AMassException.
     *
     */
    @Deprecated
    public AMaasClient(final String region, final String apiKey, final long timeoutInSecs, final boolean enabledTLS, final String appName) throws AMaasException {
        this(region, null, apiKey, timeoutInSecs, enabledTLS, null);
    }

    /**
     * AMaaSClient constructor.
     * @param region region we obtained your api key. If host is given, region is ignored.
     * @param host AMaas scanner host address. Null if to use Trend AMaaS service specified in region.
     * @param apiKey api key to be used
     * @param timeoutInSecs number in seconds to wait for a scan. 0 default to 180 seconds.
     * @param enabledTLS boolean flag to enable or disable TLS
     * @param caCertPath File path of the CA certificate for hosted AMaaS Scanner server. null if using Trend AMaaS service.
     * @throws AMaasException if an exception is detected, it will convert to AMassException.
     *
     */
    public AMaasClient(final String region, final String host, final String apiKey, final long timeoutInSecs, final boolean enabledTLS, final String caCertPath) throws AMaasException {
        String target = this.identifyHostAddr(region, host);
        if (target == null || target == "") {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_INVALID_REGION, region, AMaasRegion.getAllRegionsAsString());
        }
        if (enabledTLS) {
            log(Level.FINE, "Using prod grpc service {0}", target);
            if (caCertPath != null && !caCertPath.isEmpty()) {
                // Bring Your Own Certificate case
                try {
                    File certFile = Paths.get(caCertPath).toFile();
                    this.channel = NettyChannelBuilder.forTarget(target)
                            .sslContext(GrpcSslContexts.forClient().trustManager(certFile).build())
                            .build();
                } catch (SSLException | UnsupportedOperationException e) {
                    throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_LOAD_SSL_CERT);
                }
            } else {
                // Default SSL credentials case
                try {
                    log(Level.FINE, "Using prod grpc service {0}", target);
                    this.channel = NettyChannelBuilder.forTarget(target)
                            .sslContext(GrpcSslContexts.forClient().build())
                            .build();
                } catch (SSLException e) {
                    throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_LOAD_SSL_CERT);
                }
            }
        } else {
            log(Level.FINE, "Using grpc service with TLS disenabled {0}", target);
            this.channel = NettyChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
        }
        if (apiKey != null) {
            this.cred = new AMaasCallCredentials(apiKey, AMaasConstants.V1FS_APP);
        } else {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_MISSING_AUTH);
        }
        if (timeoutInSecs > 0) {
            this.timeoutSecs = timeoutInSecs;
        }
        this.asyncStub = ScanGrpc.newStub(this.channel).withCallCredentials(this.cred);
    }

    @Override
    protected void finalize() {
        try {
            this.channel.shutdownNow().awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log(Level.WARNING, "Finalizing AMaaSClient throws exception: {0}", e.getMessage());
        }
    }

    private String identifyHostAddr(final String region, final String host) {
        if (host != null && !host.isEmpty()) {
            return host;
        }
        // TBD: keep this to pass BVT. Remove in next PR
        // overwrite region setting with host setting
        String target = System.getenv("TM_AM_SERVER_ADDR");
        if (target == null || target == "") {
            target = AMaasRegion.getServiceFqdn(region);
        }
        return target;
    }

    private static void log(final Level level, final String msg, final Object... params) {
        logger.log(level, msg, params);
    }

    /**
     * Class to handle the bidirection request and response messages on the gRPC channel between the SDK client and the scan server.
     * It implements the callback handlers of the response StreamObserver.
     */
    static class AMaasServerCallback implements StreamObserver<ScanOuterClass.S2C> {
        private static final int POLL_TIME_MILLIS = 200;

        private StreamObserver<ScanOuterClass.C2S> requestObserver;
        private AMaasReader reader;
        private CountDownLatch finishCond = new CountDownLatch(1);
        private Boolean done = false;
        private String scanResult = null;
        private Status.Code grpcStatus = Status.Code.UNKNOWN;
        private int fetchCount = 0;
        private long fetchSize = 0;
        private boolean bulk = true;
        private long start = System.currentTimeMillis();
        private long timeoutSecs;

        AMaasServerCallback() {
        }

        private AMaasException processError() {
            AMaasException err = null;
            if (this.grpcStatus == Status.Code.UNAUTHENTICATED) {
                err = new AMaasException(AMaasErrorCode.MSG_ID_ERR_KEY_AUTH_FAILED);
            } else {
                err = new AMaasException(AMaasErrorCode.MSG_ID_GRPC_ERROR, this.grpcStatus.value(), this.grpcStatus.toString());
            }
            return err;
        }

        protected String waitTilExit() throws AMaasException {
            try {
                while (!this.done) {
                    this.finishCond.await();
                }
            } catch (InterruptedException err) {
                throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_UNEXPECTED_INTERRUPT);
            }

            if (this.grpcStatus == Status.Code.OK) {
                return this.scanResult;
            } else {
                throw this.processError();
            }
        }

        protected void setContext(final StreamObserver<ScanOuterClass.C2S> requestObserver, final AMaasReader reader, final boolean bulk, final long timeoutSecs) {
            this.requestObserver = requestObserver;
            this.reader = reader;
            this.done = false;
            this.bulk = bulk;
            this.timeoutSecs = timeoutSecs;
        }

        @Override
        public void onNext(final ScanOuterClass.S2C s2cMsg) {
            log(Level.FINE, "Got message {0} at {1}", s2cMsg.getCmdValue(), s2cMsg.getBulkLengthCount());
            final CallStreamObserver<ScanOuterClass.C2S> callObserver = (CallStreamObserver<ScanOuterClass.C2S>) requestObserver;

            switch (s2cMsg.getCmd()) {
                case CMD_RETR:
                    if (s2cMsg.getStage() != Stage.STAGE_RUN) {
                        log(Level.INFO, "Received unexpected command RETR at stage {0}", s2cMsg.getStage());
                        requestObserver.onError(new StatusRuntimeException(Status.ABORTED));
                    }
                    java.util.List<java.lang.Integer> bulkLength;
                    java.util.List<java.lang.Integer> bulkOffset;
                    if (this.bulk) {
                        log(Level.FINE, "enter bulk mode");
                        int bulkCount = s2cMsg.getBulkLengthCount();
                        if (bulkCount > 1) {
                          log(Level.INFO, "bulk transfer triggered");
                        }
                        bulkLength = s2cMsg.getBulkLengthList();
                        bulkOffset = s2cMsg.getBulkOffsetList();
                    } else {
                        bulkLength = Arrays.asList(new Integer[]{s2cMsg.getLength()});
                        bulkOffset = Arrays.asList(new Integer[]{s2cMsg.getOffset()});
                    }
                    for (int i = 0; i < bulkLength.size(); i++) {
                        log(Level.INFO, "Bulk read length={0} at offset={1}", bulkLength.get(i).intValue(), bulkOffset.get(i).intValue());
                        byte[] bytes = new byte[bulkLength.get(i).intValue()];
                        try {
                            int rtnLength = reader.readBytes(bulkOffset.get(i).intValue(), bytes);
                            ByteString bytestr = ByteString.copyFrom(bytes);
                            this.fetchCount++;
                            this.fetchSize += rtnLength;
                            ScanOuterClass.C2S request = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_RUN).setChunk(bytestr).setOffset(bulkOffset.get(i).intValue()).build();

                            while (!callObserver.isReady()) {
                                try {
                                    Thread.sleep(this.POLL_TIME_MILLIS);
                                    log(Level.FINE, "stream is not ready yet, sleep {0}ms", this.POLL_TIME_MILLIS);
                                } catch (InterruptedException e) {
                                    log(Level.INFO, "Receive interrupt during callObserver wait, reason: " + e.getMessage());
                                }

                                long duration = System.currentTimeMillis() - this.start;

                                if (TimeUnit.MILLISECONDS.toSeconds(duration) > this.timeoutSecs) {
                                    log(Level.INFO, "DEADLINE_EXCEEDED {0}", duration);
                                    requestObserver.onError(new StatusRuntimeException(Status.DEADLINE_EXCEEDED));
                                    return;
                                }
                            }
                            requestObserver.onNext(request);
                        } catch (IOException e) {
                            log(Level.SEVERE, "Exception when processing server message", e.getMessage());
                            requestObserver.onError(new StatusRuntimeException(Status.ABORTED));
                        }
                    }
                    break;
                case CMD_QUIT:
                    this.scanResult = s2cMsg.getResult();
                    log(Level.INFO, "Scan succeeded: result={0} fetchCount={1} fetchSize={2}.", this.scanResult, this.fetchCount, this.fetchSize);
                    requestObserver.onCompleted();
                    break;
                default:
                    log(Level.WARNING, "Unknown command");
                    requestObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT));
            }
        }

        @Override
        public void onError(final Throwable t) {
            log(Level.WARNING, "scan Failed: {0}", Status.fromThrowable(t));
            this.done = true;
            this.grpcStatus = Status.fromThrowable(t).getCode();
            this.finishCond.countDown();
        }

        @Override
        public void onCompleted() {
            log(Level.INFO, "File successfully scanned.");
            this.done = true;
            this.grpcStatus = Status.Code.OK;
            this.finishCond.countDown();
        }
    }

    static AMaasException getTagListErrors(final String[] tagList) {
        AMaasException except = null;
        if (tagList.length > MAX_NUM_OF_TAGS) {
            except = new AMaasException(AMaasErrorCode.MSG_ID_ERR_MAX_NUMBER_OF_TAGS, MAX_NUM_OF_TAGS);
        }
        for (String tag: tagList) {
            if (tag == null || tag == "" || (tag.length() > MAX_LENGTH_OF_TAG)) {
                except = new AMaasException(AMaasErrorCode.MSG_ID_ERR_LENGTH_OF_TAG, MAX_LENGTH_OF_TAG, tag);
                break;
            }
        }
        return except;
    }

    /*
    * Private method to scan a AMaasReader and return the scanned result
    *
    * @param reader AMaasReader to be scanned.
    * @param tagList List of tags.
    * @param pml flag to indicate whether to use predictive machine learning detection.
    * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
    * @param verbose flag to enable log verbose mode
    * @param digest flag to enable calculation of digests for cache search and result lookup.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    private String scanRun(final AMaasReader reader, final String[] tagList, final boolean pml, final boolean feedback, final boolean verbose, final boolean digest) throws AMaasException {

        long fileSize = reader.getLength();

        AMaasServerCallback serverCallback = new AMaasServerCallback();
        StreamObserver<ScanOuterClass.C2S> requestObserver = this.asyncStub.withDeadlineAfter(this.timeoutSecs, TimeUnit.SECONDS).run(serverCallback);

        // initialize the callback context before proceeding
        serverCallback.setContext(requestObserver, reader, this.bulk, this.timeoutSecs);

        String sha1Str = reader.getHash(AMaasReader.HashType.HASH_SHA1);
        String sha256Str = reader.getHash(AMaasReader.HashType.HASH_SHA256);
        log(Level.FINE, "sha1={0} sha256={1}", sha1Str, sha256Str);

        ScanOuterClass.C2S.Builder builder = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_INIT).setFileName(reader.getIdentifier()).setRsSize(fileSize).setOffset(0).setFileSha1(sha1Str).setFileSha256(sha256Str).setTrendx(pml).setSpnFeedback(feedback).setBulk(this.bulk).setVerbose(verbose);
        if (tagList != null) {
            AMaasException except = getTagListErrors(tagList);
            if (except != null) {
                throw except;
            }
            builder.addAllTags(Arrays.asList(tagList));
        }
        ScanOuterClass.C2S request = builder.build();

        requestObserver.onNext(request);

        String scanResult = serverCallback.waitTilExit();

        return scanResult;

    }

    /**
    * Scan a file and return the scanned result.
    *
    * @param fileName Full path of a file to be scanned.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanFile(final String fileName) throws AMaasException {
        return this.scanFile(fileName, null, false, false, false, true);
    }

    /**
    * Scan a file and return the scanned result.
    *
    * @deprecated
    * @param fileName Full path of a file to be scanned.
    * @param tagList List of tags.
    * @param pml flag to indicate whether to enable predictive machine learning detection.
    * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    @Deprecated
    public String scanFile(final String fileName, final String[] tagList, final boolean pml, final boolean feedback) throws AMaasException {
        AMaasFileReader fileReader = new AMaasFileReader(fileName, true);
        return this.scanRun(fileReader, tagList, pml, feedback, false, true);
    }

    /**
    * Scan a file and return the scanned result.
    *
    * @param fileName Full path of a file to be scanned.
    * @param tagList List of tags.
    * @param pml flag to indicate whether to enable predictive machine learning detection.
    * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
    * @param verbose flag to enable log verbose mode
    * @param digest flag to enable calculation of digests for cache search and result lookup.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanFile(final String fileName, final String[] tagList, final boolean pml, final boolean feedback, final boolean verbose, final boolean digest) throws AMaasException {
        AMaasFileReader fileReader = new AMaasFileReader(fileName, digest);
        return this.scanRun(fileReader, tagList, pml, feedback, verbose, digest);
    }

    /**
    * Scan a buffer and return the scanned result.
    *
    * @param buffer the buffer to be scanned.
    * @param identifier A unique name to identify the buffer.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanBuffer(final byte[] buffer, final String identifier) throws AMaasException {
        return this.scanBuffer(buffer, identifier, null, false, false, false, true);
    }

    /**
    * Scan a buffer and return the scanned result. (TBD: LSK remove this API).
    *
    * @deprecated
    * @param buffer the buffer to be scanned.
    * @param identifier A unique name to identify the buffer.
    * @param tagList List of tags.
    * @param pml flag to indicate whether to use predictive machine learning detection.
    * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    @Deprecated
    public String scanBuffer(final byte[] buffer, final String identifier, final String[] tagList, final boolean pml, final boolean feedback) throws AMaasException {
        AMaasBufferReader bufReader = new AMaasBufferReader(buffer, identifier, true);
        return this.scanRun(bufReader, tagList, pml, feedback, false, true);
    }

    /**
    * Scan a buffer and return the scanned result.
    *
    * @param buffer the buffer to be scanned.
    * @param identifier A unique name to identify the buffer.
    * @param tagList List of tags.
    * @param pml flag to indicate whether to use predictive machine learning detection.
    * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
    * @param verbose flag to enable log verbose mode
    * @param digest flag to enable calculation of digests for cache search and result lookup.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanBuffer(final byte[] buffer, final String identifier, final String[] tagList, final boolean pml, final boolean feedback, final boolean verbose, final boolean digest) throws AMaasException {
        AMaasBufferReader bufReader = new AMaasBufferReader(buffer, identifier, digest);
        return this.scanRun(bufReader, tagList, pml, feedback, verbose, digest);
    }
}
