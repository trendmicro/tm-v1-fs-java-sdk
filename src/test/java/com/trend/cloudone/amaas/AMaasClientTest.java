package com.trend.cloudone.amaas;


import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.trend.cloudone.amaas.scan.ScanGrpc;
import com.trend.cloudone.amaas.scan.ScanOuterClass;
import com.trend.cloudone.amaas.scan.ScanOuterClass.Stage;

import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class AMaasClientTest {
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private ScanGrpc.ScanStub client;
    protected Gson gson;


    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();


    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    
    @Before
    public void setUp() throws Exception {
        this.gson = new GsonBuilder().create();
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Use a mutable service registry for later registering the service impl for each test case.
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
            .fallbackHandlerRegistry(serviceRegistry).directExecutor().build().start());

        this.client = ScanGrpc.newStub(grpcCleanup.register(
            InProcessChannelBuilder.forName(serverName).directExecutor().build()));
        
    }


    private void prepareTestObj(AMaasClient.AMaasServerCallback serverCallback, AMaasReader reader) {
        MockScanServicer servicer = new MockScanServicer();
        serviceRegistry.addService(servicer);
        
        StreamObserver<ScanOuterClass.C2S> requestObserver =  this.client.run(serverCallback);
        // initialize the callback context before proceeding

        long fileSize = reader.getLength();
        serverCallback.setConext(requestObserver, reader);
        ScanOuterClass.C2S request = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_INIT).setFileName(reader.getIdentifier()).setRsSize((int)fileSize).setOffset(0).build();
        requestObserver.onNext(request);
    }


    @Test
    public void testGrpcClientRetrieveCorrectly() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            AMaasFileReader reader = new AMaasFileReader(DataFileCreator.TEST_DATA_FILE_NAME);
            this.prepareTestObj(serverCallback, reader);

            String scanResultjson = serverCallback.waitTilExit();
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 0);
            
        } catch (AMaasException err) {
            System.out.println(err.getMessage());
            assert false;
        }
    }


    @Test
    public void testGrpcDetectedVirusCorrectly() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_VIRUS);
            this.prepareTestObj(serverCallback, reader);

            String scanResultjson = serverCallback.waitTilExit();
            System.out.println(scanResultjson);
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 1);
            assertEquals(scanResult.getFoundMalwares().length, 2);
            assertEquals(scanResult.getFoundMalwares()[0].getMalwareName(), "virus1");
            assertEquals(scanResult.getFoundMalwares()[1].getMalwareName(), "virus2");
            
        } catch (AMaasException err) {
            System.out.println(err.getMessage());
            assert false;
        } catch (Exception err) {
            assert false;
        }
    }


    @Test
    public void testWrongRegion() throws AMaasException {
        String region = "aa-1";
        exceptionRule.expect(AMaasException.class);
        exceptionRule.expectMessage(AMaasErrorCode.MSG_ID_ERR_INVALID_REGION.getMessage(region));
        new AMaasClient(region, "AAAPPPKet", (long)5000, false);
    }


    @Test
    public void testMissingAuthentication() throws Exception {
        exceptionRule.expect(AMaasException.class);
        exceptionRule.expectMessage(AMaasErrorCode.MSG_ID_ERR_MISSING_AUTH.getMessage());
        new AMaasClient("us-1", null, (long)5000, false);
    }


    @Test
    public void testGrpcException() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_UNKNOWN_CMD);
            this.prepareTestObj(serverCallback, reader);

            String scanResultjson = serverCallback.waitTilExit();
            System.out.println(scanResultjson);
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 1);
            
        } catch (AMaasException err) {
            System.out.println(err.getMessage());
            assertEquals(err.getErrorCode(), AMaasErrorCode.MSG_ID_GRPC_ERROR);
            assertEquals(err.getMessage(), "Received gRPC status code: 2, msg: UNKNOWN.");
        } catch (Exception err) {
            //err.printStackTrace();
            assert false;
        }
    }
}

