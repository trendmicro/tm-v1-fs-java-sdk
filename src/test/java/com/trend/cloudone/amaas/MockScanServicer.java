package com.trend.cloudone.amaas;

import io.grpc.Status;
import com.trend.cloudone.amaas.scan.ScanOuterClass.S2C;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Stage;
import com.google.gson.Gson;
import com.trend.cloudone.amaas.scan.ScanGrpc;
import com.trend.cloudone.amaas.scan.ScanOuterClass.C2S;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Command;


import io.grpc.stub.StreamObserver;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;


public final class MockScanServicer extends ScanGrpc.ScanImplBase {
    static final String IDENTIFIER_VIRUS = "virus";
    static final String IDENTIFIER_UNKNOWN_CMD = "unknown_cmd";
    static final String IDENTIFIER_MISMATCHED = "mismatched";
    static final String IDENTIFIER_GRPC_ERROR = "grpc_error";
    static final String IDENTIFIER_EXCEED_RATE = "exceed_rate";
    static final String IDENTIFIER_CHECK_HASH = "check_hash";
    static final Command UNKNOWN_CMD = Command.UNRECOGNIZED;
    static final String[] MYTAGS = new String[] {"tag1", "tag2"};

    private static final int MAX_RUN = 5;
    private final S2C finalMsg = S2C.newBuilder()
            .setStage(Stage.STAGE_FINI)
            .setCmd(Command.CMD_QUIT)
            .build();

    private int fsize = 0;
    private String identifier = "";
    private Random random = new Random();
    private int count = 0;


    private S2C getS2CMsg() {
        int start = random.nextInt(fsize);
        int end = random.nextInt(fsize - start) + start;
        return S2C.newBuilder()
                .setStage(Stage.STAGE_RUN)
                .setCmd(Command.CMD_RETR)
                .setOffset(start)
                .setLength(end - start)
                .build();
    }


    private S2C getUnknwonCmd() {
        int start = random.nextInt(fsize);
        int end = random.nextInt(fsize - start) + start;
        return S2C.newBuilder()
                .setStage(Stage.STAGE_RUN)
                .setCmd(Command.UNRECOGNIZED)
                .setOffset(start)
                .setLength(end - start)
                .build();
    }


    private S2C getMismatchedCmdStage() {
        int start = random.nextInt(fsize);
        int end = random.nextInt(fsize - start) + start;
        return S2C.newBuilder()
                .setStage(Stage.STAGE_RUN)
                .setCmd(Command.CMD_QUIT)
                .setOffset(start)
                .setLength(end - start)
                .build();
    }

    private S2C processRequest(final C2S req) {
        S2C msg = null;
        if (req.getStage() == Stage.STAGE_INIT) {
            this.fsize = req.getRsSize();
            this.identifier = req.getFileName();

            if (req.getTagsCount() > 0) {
                for (int indx = 0; indx < req.getTagsCount(); indx++) {
                    assertEquals(req.getTags(indx), MYTAGS[indx]);
                }
            }

            if (this.identifier == IDENTIFIER_CHECK_HASH) {
                String sha1 = "sha1:e4d5a3a79140b1c141f947efef6a7372c8f2bbc4";
                String sha256 = "sha256:c24f43025bc40b53e4e5948cf69cb59498b47d2127cf358de846cda6fefccc63";
                if ((sha1.equals(req.getFileSha1())) && (sha256.equals(req.getFileSha256()))) {
                    msg = this.getS2CMsg();
                } else {
                    msg = null;
                }
            } else {
                msg = this.getS2CMsg();
            }
        } else if (req.getStage() == Stage.STAGE_RUN) {
            if (this.identifier == IDENTIFIER_UNKNOWN_CMD) {
                msg = this.getUnknwonCmd();
            } else if (this.identifier == IDENTIFIER_MISMATCHED) {
                msg = this.getMismatchedCmdStage();
            } else if (this.identifier == IDENTIFIER_GRPC_ERROR) {
                msg = null;
            } else if (this.identifier == IDENTIFIER_EXCEED_RATE) {
                //context.set_details("Http Error Code: 429");
                //context.set_code(grpc.StatusCode.INTERNAL);
                //msg = "";
                msg = null;
            } else if (this.count >= MAX_RUN) {
                msg = finalMsg.toBuilder()
                    .setResult(getResultJson())
                    .build();
            } else {
                msg = this.getS2CMsg();
            }
        } else {
            msg = S2C.newBuilder()
                    .setStage(Stage.STAGE_FINI)
                    .setCmd(Command.CMD_QUIT)
                    .build();
        }
        this.count++;
        return msg;
    }

    @Override
    public StreamObserver<C2S> run(final StreamObserver<S2C> responseObserver) {

        return new StreamObserver<C2S>() {

            @Override
            public void onNext(final C2S request) {
                S2C resp = processRequest(request);
                if (resp == null) {
                    Status status = Status.INTERNAL;
                    responseObserver.onError(status.asRuntimeException());
                }
                responseObserver.onNext(resp);
            }

            @Override
            public void onError(final Throwable t) {
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }


    private String getResultJson() {
        String version = "1.0";
        int scanResult = 0;
        MalwareItem[] foundMalwares = new MalwareItem[2];

        if (IDENTIFIER_VIRUS.equals(identifier)) {
            scanResult = 1;
            foundMalwares[0] = new MalwareItem("virus1", "file1");
            foundMalwares[1] = new MalwareItem("virus2", "file2");
        }
        Date date = new Date();
        String scanTimestamp = date.toString();

        AMaasScanResult scanResultObj = new AMaasScanResult(version, scanResult, identifier, scanTimestamp, identifier, foundMalwares);
        return new Gson().toJson(scanResultObj);
    }
}
