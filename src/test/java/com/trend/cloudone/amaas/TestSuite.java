package com.trend.cloudone.amaas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import io.grpc.inprocess.InProcessServerBuilder;


@RunWith(Suite.class)
@Suite.SuiteClasses({
   AMaasCallCredentialsTest.class,
   AMaasClientTest.class,
   AMaasConstantsTest.class,
   AMaasErrorCodeTest.class,
   AMaasExceptionTest.class,
   AMaasFileBufferTest.class,
   AMaasFileReaderTest.class,
   AMaasScanResultTest.class,
   MalwareItemTest.class,
})


public class TestSuite {
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        
        DataFileCreator.createDataFile();
        MockScanServicer servicer = new MockScanServicer();
        String uniqueName = InProcessServerBuilder.generateName();
        InProcessServerBuilder.forName(uniqueName).directExecutor().addService(servicer).build().start();
    }

    
    @AfterClass
    public static void tearDownClass() {
        
        DataFileCreator.deleteDataFile();
    }
    
}
