package com.trend.cloudone.amaas;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;
import com.google.protobuf.ByteString;
import com.trend.cloudone.amaas.scan.ScanGrpc;
import com.trend.cloudone.amaas.scan.ScanOuterClass;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Stage;

/**
 * AMaaS Client connecting to AMaaS gRPC server to provide API for malware scanning services. User can use the API 
 * to scan a file or a byte buffer.
 */
public class AMaasClient {
    private static final Logger logger = Logger.getLogger(AMaasClient.class.getName());
    private static final long  DEFAULT_SCAN_TIMEOUT = 180;

    private ManagedChannel channel;
    private ScanGrpc.ScanStub asyncStub;
    private AMaasCallCredentials cred;
    private long timeoutSecs = DEFAULT_SCAN_TIMEOUT; // 3 minutes

    

     /**
     * AMaaSClient constructor. The enabledTLS is default to true.
     * @param region region you obtained your api key
     * @param apiKey api key to be used
     * @param timeoutInSecs number in seconds to wait for a scan. 0 default to 180 seconds.
     * @throws AMaasException if an exception is detected, it will convert to AMassException.
     *
     */
    public AMaasClient(String region, String apiKey, long timeoutInSecs) throws AMaasException {
        this(region, apiKey, timeoutInSecs, true);
    }

    /**
     * AMaaSClient constructor
     * @param region region we obtained your api key
     * @param apiKey api key to be used
     * @param timeoutInSecs number in seconds to wait for a scan. 0 default to 180 seconds.
     * @param enabledTLS boolean flag ro enable or disable TLS
     * @throws AMaasException if an exception is detected, it will convert to AMassException.
     *
     */
    public AMaasClient(String region, String apiKey, long timeoutInSecs, boolean enabledTLS) throws AMaasException {
        String target = this.identifyHostAddr(region);
        if (target == null || target == "") {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_INVALID_REGION, region);
        }
        if (enabledTLS) {
            log(Level.FINE, "Using prod grpc service {0}", target);
            this.channel = Grpc.newChannelBuilder(target, TlsChannelCredentials.create()).build();
        } else {
            log(Level.FINE, "Using local grpc service");
            this.channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
        }
        if (apiKey != null) {
            this.cred = new AMaasCallCredentials(apiKey);
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

    private String identifyHostAddr(String region) {
        // overwrite region setting with host setting
        String target = System.getenv("TM_AM_SERVER_ADDR");
        if (target==null||target=="") {
            target = AMaasConstants.getHostForRegion(region);
        }
        return target;
    }

    private static void log(Level level, String msg, Object... params) {
        logger.log(level, msg, params);
    }

    /**
     * Class to handle the bidirection request and response messages on the gRPC channel between the SDK client and the scan server.
     * It implements the callback handlers of the response StreamObserver.
     */
    static class AMaasServerCallback implements StreamObserver<ScanOuterClass.S2C> {
        private StreamObserver<ScanOuterClass.C2S> requestObserver;
        private AMaasReader reader;
        private CountDownLatch finishCond = new CountDownLatch(1);
        private Boolean done = false;
        private String scanResult = null;
        private Status.Code grpcStatus = Status.Code.UNKNOWN;
        private int fetchCount = 0;
        private long fetchSize = 0;

        AMaasServerCallback() {
        }

        private AMaasException processError() {
            AMaasException err = null;
            if (this.grpcStatus == Status.Code.UNAUTHENTICATED) {
                err = new AMaasException(AMaasErrorCode.MSG_ID_ERR_KEY_AUTH_FAILED);
            } else {
                err = new AMaasException( AMaasErrorCode.MSG_ID_GRPC_ERROR, this.grpcStatus.value(), this.grpcStatus.toString());
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
            
            if (this.grpcStatus ==Status.Code.OK) {
                return this.scanResult;
            } else {
                throw this.processError();
            } 
        }

        protected void setConext(StreamObserver<ScanOuterClass.C2S> requestObserver, AMaasReader reader) {
            this.requestObserver = requestObserver;
            this.reader = reader;
            this.done = false;
        }

        @Override
        public void onNext(ScanOuterClass.S2C s2cMsg) {
            log(Level.FINE, "Got message {0} at {1}, {2}", s2cMsg.getCmdValue(), s2cMsg.getOffset(), s2cMsg.getLength());
            switch (s2cMsg.getCmd()) {
                case CMD_RETR:
                    byte[] bytes = new byte[s2cMsg.getLength()];
                    try {
                        int rtnLength = reader.readBytes(s2cMsg.getOffset(), bytes);

                        ByteString bytestr = ByteString.copyFrom(bytes);
                        int curloc = s2cMsg.getOffset() + s2cMsg.getLength();
                        this.fetchCount++;
                        this.fetchSize += rtnLength;
                        ScanOuterClass.C2S request = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_RUN).setOffset(s2cMsg.getLength()).setChunk(bytestr).setOffset(curloc).build();
                        requestObserver.onNext(request);
                    } catch (IOException e) {
                        log(Level.WARNING, "Exception when processing server message", e.getMessage());
                        requestObserver.onError(new StatusRuntimeException(Status.ABORTED));
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
        public void onError(Throwable t) {
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

    /*
    * Private method to scan a AMaasReader and return the scanned result
    *
    * @param reader AMaasReader to be scanned.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    private String scanRun(AMaasReader reader) throws AMaasException {
       
        long fileSize = reader.getLength();

        AMaasServerCallback serverCallback = new AMaasServerCallback();
        StreamObserver<ScanOuterClass.C2S> requestObserver = this.asyncStub.withDeadlineAfter(this.timeoutSecs, TimeUnit.SECONDS).run(serverCallback);
        
        // initialize the callback context before proceeding
        serverCallback.setConext(requestObserver, reader);

        ScanOuterClass.C2S request = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_INIT).setFileName(reader.getIdentifier()).setRsSize((int)fileSize).setOffset(0).build();
        requestObserver.onNext(request);

        String scanResult = serverCallback.waitTilExit();

        
        return scanResult;
        
    }

    /**
    * Scan a file and return the scanned result
    *
    * @param fileName Full path of a file to be scanned.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanFile(String fileName) throws AMaasException {
        AMaasFileReader fileReader = new AMaasFileReader(fileName);
        return this.scanRun(fileReader);
    }

    /**
    * Scan a buffer and return the scanned result
    *
    * @param buffer the buffer to be scanned
    * @param identifier A unique name to identify the buffer.
    * @return String the scanned result in JSON format.
    * @throws AMaasException if an exception is detected, it will convert to AMassException.
    */
    public String scanBuffer(byte[] buffer, String identifier) throws AMaasException {
        AMaasBufferReader bufReader = new AMaasBufferReader(buffer, identifier);
        return this.scanRun(bufReader);
    }
}
