package com.trend.cloudone.amaas;


import com.trend.cloudone.amaas.scan.ScanOuterClass.S2C;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Stage;
import com.google.gson.Gson;
import com.trend.cloudone.amaas.scan.ScanGrpc;
import com.trend.cloudone.amaas.scan.ScanOuterClass.C2S;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Command;

import io.grpc.stub.StreamObserver;
import java.util.Date;
import java.util.Random;


public class MockScanServicer extends ScanGrpc.ScanImplBase {
    final static String IDENTIFIER_VIRUS = "virus";
    final static String IDENTIFIER_UNKNOWN_CMD = "unknown_cmd";
    final static String IDENTIFIER_MISMATCHED = "mismatched";
    final static String IDENTIFIER_GRPC_ERROR = "grpc_error";
    final static String IDENTIFIER_EXCEED_RATE = "exceed_rate";
    final static Command UNKNOWN_CMD = Command.UNRECOGNIZED;

    private static final int MAX_RUN = 5;
    private final S2C FINAL_MSG = S2C.newBuilder()
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

    private S2C processRequest(C2S req) {
        S2C msg = null;
        if (req.getStage() == Stage.STAGE_INIT) {
            this.fsize = req.getRsSize();
            this.identifier = req.getFileName();
            msg = this.getS2CMsg();
        } else if (req.getStage() == Stage.STAGE_RUN) {
            if (this.identifier == IDENTIFIER_UNKNOWN_CMD) {
                msg = this.getUnknwonCmd();
            } else if (this.identifier == IDENTIFIER_MISMATCHED) {
                msg = this.getMismatchedCmdStage();
            } else if (this.identifier == IDENTIFIER_GRPC_ERROR) {
                //context.set_details("Ouch!");
                //context.set_code(grpc.StatusCode.INTERNAL);
                //msg = "";
            } else if (this.identifier == IDENTIFIER_EXCEED_RATE) {
                //context.set_details("Http Error Code: 429");
                //context.set_code(grpc.StatusCode.INTERNAL);
                //msg = "";
                
            } else if (this.count >= MAX_RUN) {
                msg = FINAL_MSG.toBuilder()
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
    public StreamObserver<C2S> run(StreamObserver<S2C> responseObserver) {
        
        return new StreamObserver<C2S>() {

            @Override
            public void onNext(C2S request) {
                S2C resp = processRequest(request);
                responseObserver.onNext(resp);
            }

            @Override
            public void onError(Throwable t) {
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
