package com.trend.cloudone.amaas;


import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import static org.junit.Assert.assertEquals;

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
import java.util.Arrays;


public class AMaasClientTest {
    private static final int TIMEOUT_SEC = 5000;
    private static final int BUFF_LENGTH = 64;
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private ScanGrpc.ScanStub client;
    private Gson gson;

    /**
     * Rule for grpc cleanup.
     */
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    /**
     * Rule for expected exception.
     */
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


    private void prepareTestObj(final AMaasClient.AMaasServerCallback serverCallback, final AMaasReader reader, final String[] tagList) {
        MockScanServicer servicer = new MockScanServicer();
        serviceRegistry.addService(servicer);

        StreamObserver<ScanOuterClass.C2S> requestObserver =  this.client.run(serverCallback);
        // initialize the callback context before proceeding

        long fileSize = reader.getLength();
        String sha1Str = reader.getHash(AMaasReader.HashType.HASH_SHA1);
        String sha256Str = reader.getHash(AMaasReader.HashType.HASH_SHA256);
        serverCallback.setConext(requestObserver, reader);

        ScanOuterClass.C2S.Builder builder = ScanOuterClass.C2S.newBuilder().setStage(Stage.STAGE_INIT).setFileName(reader.getIdentifier()).setRsSize((int) fileSize).setOffset(0).setFileSha1(sha1Str).setFileSha256(sha256Str);
        if (tagList != null) {
            builder.addAllTags(Arrays.asList(tagList));
        }
        ScanOuterClass.C2S request = builder.build();

        requestObserver.onNext(request);
    }


    @Test
    public void testGrpcClientRetrieveCorrectly() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            AMaasFileReader reader = new AMaasFileReader(DataFileCreator.TEST_DATA_FILE_NAME);
            this.prepareTestObj(serverCallback, reader, null);

            String scanResultjson = serverCallback.waitTilExit();
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 0);
        } catch (AMaasException err) {
            System.out.println(err.getMessage());
            err.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGrpcServerReceiveHashCorrectly() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            String str = "This is a very long long long test";
            byte[] data = str.getBytes();
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_CHECK_HASH);
            this.prepareTestObj(serverCallback, reader, null);

            String scanResultjson = serverCallback.waitTilExit();
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 0);
        } catch (AMaasException err) {
            err.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGrpcServerReceiveWrongHash() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            String str = "This is a very long long long test error";
            byte[] data = str.getBytes();
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_CHECK_HASH);
            this.prepareTestObj(serverCallback, reader, null);

            String scanResultjson = serverCallback.waitTilExit();
            this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            //assertEquals(scanResult.getScanResult(), 0);
            assert false;
        } catch (AMaasException err) {
            err.printStackTrace();
            assert true;
        }
    }


    @Test
    public void testGrpcDetectedVirusCorrectly() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_VIRUS);
            this.prepareTestObj(serverCallback, reader, null);

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
        exceptionRule.expectMessage(AMaasErrorCode.MSG_ID_ERR_INVALID_REGION.getMessage(region, AMaasRegion.getAllRegionsAsString()));
        new AMaasClient(region, "AAAPPPKet", (long) TIMEOUT_SEC, false, AMaasConstants.V1FS_APP);
    }


    @Test
    public void testMissingAuthentication() throws Exception {
        exceptionRule.expect(AMaasException.class);
        exceptionRule.expectMessage(AMaasErrorCode.MSG_ID_ERR_MISSING_AUTH.getMessage());
        new AMaasClient("us-east-1", null, (long) TIMEOUT_SEC, false, AMaasConstants.V1FS_APP);
    }


    @Test
    public void testGrpcException() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader reader = new AMaasBufferReader(data, MockScanServicer.IDENTIFIER_UNKNOWN_CMD);
            this.prepareTestObj(serverCallback, reader, null);

            String scanResultjson = serverCallback.waitTilExit();
            System.out.println(scanResultjson);
            this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            // expecting exception to happen before this point. If reaches here, it is an error.
            assert false;
        } catch (AMaasException err) {
            System.out.println(err.getMessage());
            assertEquals(err.getErrorCode(), AMaasErrorCode.MSG_ID_GRPC_ERROR);
            assertEquals(err.getMessage(), "Received gRPC status code: 2, msg: UNKNOWN.");
        } catch (Exception err) {
            assert false;
        }
    }

    @Test
    public void testTags() {
        try {
            AMaasClient.AMaasServerCallback serverCallback = new AMaasClient.AMaasServerCallback();
            Path path = Paths.get(DataFileCreator.TEST_DATA_FILE_NAME);
            byte[] data = Files.readAllBytes(path);
            AMaasBufferReader reader = new AMaasBufferReader(data, "test");
            this.prepareTestObj(serverCallback, reader, MockScanServicer.MYTAGS);

            String scanResultjson = serverCallback.waitTilExit();
            System.out.println(scanResultjson);
            AMaasScanResult scanResult = this.gson.fromJson(scanResultjson, AMaasScanResult.class);
            assertEquals(scanResult.getScanResult(), 0);
        } catch (Exception err) {
            err.printStackTrace();
            assert false;
        }
    }

    @Test
    public void testGetTagListErrors() {
        String[] taglist = new String[]{"t1", "t2", "t3", "4", "t5", "t6", "t7", "t8", "t9"};
        AMaasException err = AMaasClient.getTagListErrors(taglist);
        assertEquals(err.getErrorCode(), AMaasErrorCode.MSG_ID_ERR_MAX_NUMBER_OF_TAGS);

        String space64 = new String(new char[BUFF_LENGTH]);
        taglist = new String[]{space64};
        err = AMaasClient.getTagListErrors(taglist);
        assertEquals(err.getErrorCode(), AMaasErrorCode.MSG_ID_ERR_LENGTH_OF_TAG);
    }
}

