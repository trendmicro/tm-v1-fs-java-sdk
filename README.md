# Trend Vision One™ File Security Java SDK User Guide

Trend Vision One™ - File Security is a scanner app for files and cloud storage. This scanner can detect all types of malicious software (malware) including trojans, ransomware, spyware, and more. Based on fragments of previously seen malware, File Security detects obfuscated or polymorphic variants of malware.
File Security can assess any file type or size for malware and display real-time results. With the latest file reputation and variant protection technologies backed by leading threat research, File Security automates malware scanning.
File Security can also scan objects across your environment in any application, whether on-premises or in the cloud.

The Java software development kit (SDK) for Trend Vision One™ File Security empowers you to craft applications which seamlessly integrate with File Security. With this SDK you can perform a thorough scan of data and artifacts within your applications to identify potential malicious elements.
Follow the steps below to set up your development environment and configure your project, laying the foundation to effectively use File Security.

## Checking prerequisites

- Have Java 8 and above installed in your dev/build environment.
- Trend Vision One account with a chosen region - for more information, see the [Trend Vision One document](https://docs.trendmicro.com/en-us/documentation/article/trend-vision-one-trend-micro-xdr-abou_001).
- A Trend Vision One API key with proper role - for more information, see the [Trend Vision One API key documentation](https://docs.trendmicro.com/en-us/documentation/article/trend-vision-one-api-keys).

## Download

Download the jar from [Maven Central Repository](https://mvnrepository.com/repos/central). Or for Maven, add this dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.trend</groupId>
  <artifactId>file-security-java-sdk</artifactId>
  <version>[1.1,)</version>
</dependency>
```

## Obtain an API Key

The File Security SDK requires a valid API Key provided as parameter to the SDK client object. It can accept Trend Vision One API keys.

When obtaining the API Key, ensure that the API Key is associated with the region that you plan to use. It is important to note that Trend Vision One API Keys are associated with different regions, please refer to the region flag below to obtain a better understanding of the valid regions associated with the respective API Key.

If you plan on using a Trend Vision One region, be sure to pass in region parameter when running custom program with File Security SDK to specify the region of that API key and to ensure you have proper authorization. The list of supported Trend Vision One regions can be found at API Reference section below.

1. Login to the Trend Vision One.
2. Create a new Trend Vision One API key:

- Navigate to the Trend Vision One User Roles page.
- Verify that there is a role with the "Run file scan via SDK" permissions enabled. If not, create a role by clicking on "Add Role" and "Save" once finished.
- Directly configure a new key on the Trend Vision One API Keys page, using the role which contains the "Run file scan via SDK" permission. It is advised to set an expiry time for the API key and make a record of it for future reference.

You can manage these keys from the Trend Vision One API Keys Page.

## Using File Security Java SDK

Using File Security Java SDK to scan for malware involves the following basic steps:

1. Creating an AMaaS Client object by specifying preferred Vision One region where scanning should be done and a valid API key.
2. Invoking file scan or buffer scan method to scan the target data.
3. Parsing the JSON response returned by the scan APIs to determine whether the scanned data contains malware or not.

### Sample Code

```java
import com.trend.fs.AMaasClient;

public static void main(String[] args) {
    try {
        // 1. Create an AMaaS Client object and configure it to carry out the scans in Vision One "us-east-1" region.
        AMaasClient client = new AMaasClient("us-east-1", "your-api-key");

        // 2. Call ScanFile() to scan the content of a file.
        String scanResult = client.scanFile("path-of-file-to-scan");

        if (scanResult != null) {
            // 3. Print out the JSON response from ScanFile()
            System.out.println("scan result " + scanResult);
        }
    } catch (AMaasException err) {
        info("Exception {0}", err.getMessage());
    }
}
```

### Sample JSON Response

```json
{
  "version": "1.0",
  "scanId": "25072030425f4f4d68953177d0628d0b",
  "scanResult": 1,
  "scanTimestamp": "2022-11-02T00:55:31Z",
  "fileName": "EICAR_TEST_FILE-1.exe",
  "filePath": "AmspBvtTestSamples/BVT_RightClickScan_DS/unclean/EICAR_TEST_FILE-1.exe",
  "foundMalwares": [
    {     
      "fileName": "Eicar.exe",
      "malwareName": "Eicar_test_file"
    }
  ]
}
```

When malicious content is detected in the scanned object, `scanResult` will show a non-zero value. Otherwise, the value will be `null`. Moreover, when malware is detected, `foundMalwares` will be non-empty containing one or more name/value pairs of `fileName` and `malwareName`. `fileName` will be filename of malware detected while `malwareName` will be the name of the virus/malware found.

## Java SDK API Reference

### ```AmaasClient```

The AmaasClient class is the main class of the SDK and provides methods to use the AMaaS scanning services.

#### ```public AMaasClient(final String region, final String apiKey, final long timeoutInSecs, final boolean enabledTLS) throws AMaasException```

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key. Value provided must be one of the Vision One regions, e.g. `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, `ap-south-1`, etc. |
| apikey        | Your own Vision One API Key.                                                              |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. Valid value is 0, 1, 2, ... ; default to 300 seconds.         |
| enabledTLS    | Enable or disable TLS. TLS should always be enabled when connecting to the AMaaS server. For more information, see the 'Ensuring Secure Communication with TLS' |

**_Return_**
An AmaasClient instance

#### ```public AMaasClient(final String region, final String apiKey, final long timeoutInSecs) throws AMaasException```

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc. The enabledTLS is default to true.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key. Value provided must be one of the Vision One regions, e.g. `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, `ap-south-1`, etc. |
| apikey        | Your own Vision One API Key.                                                              |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. Valid value is 0, 1, 2, ... ; default to 300 seconds.         |

**_Return_**
An AmaasClient instance

#### ```public String scanFile(final String fileName) throws AMaasException```

Scan a file for malware and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| fileName      | The name of the file with path of directory containing the file to scan.                 |

**_Return_**
String the scanned result in JSON format.

#### ```public String scanFile(final String fileName, final String[] tagList, final boolean pml, final boolean feedback) throws AMaasException```

Scan a file for malware, add a list of tags to the scan result and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| fileName      | The name of the file with path of directory containing the file to scan.                 |
| tagList       | A list of strings to be used to tag the scan result. At most 8 tags with the maximum length of 63 characters.                                  |
| pml           | A flag to indicate whether to enable predictive machine learning detection.                  |
| feedback      | A flag to indicate whether to enable Trend Micro Smart Protection Network's Smart Feedback.  |

**_Return_**
String the scanned result in JSON format.

#### ```public String scanBuffer(final byte[] buffer, final String identifier) throws AMaasException```

Scan a buffer for malware and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                               |
| ------------- | ----------------------------------------------------------------------------------------- |
| buffer        | The byte buffer to scan.                                                                  |
| identifier    | A unique name to identify the buffer.                                                     |

**_Return_**
String the scanned result in JSON format.

#### ```public String scanBuffer(final byte[] buffer, final String identifier, final String[] tagList, final boolean pml, final boolean feedback) throws AMaasException```

Scan a buffer for malware, add a list of tags to the scan result, and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                               |
| ------------- | ----------------------------------------------------------------------------------------- |
| buffer        | The byte buffer to scan.                                                                  |
| identifier    | A unique name to identify the buffer.                                                     |
| tagList       | A list of strings to be used to tag the scan result. At most 8 tags with maximum length of 63 characters.                                     |
| pml           | A flag to indicate whether to enable predictive machine learning detection.                  |
| feedback      | A flag to indicate whether to enable Trend Micro Smart Protection Network's Smart Feedback.  |

**_Return_**
String the scanned result in JSON format.

---

### ```AmaasScanResult```

The AmaasScanResult has the data elements of the response data that is retrieved from our API.
The class has the following private members. There are getter and setter methods for each of the members.

```java
public class AmaasScanResult {
  private String version;               // API version
  private int scanResult;               // Number of malwares found. A value of 0 means no malware was found
  private String scanId;                // ID of the scan
  private String scanTimestamp;         // Timestamp of the scan in ISO 8601 format
  private String fileName:              // Name of the file scanned
  private MalwareItem[] foundMalwares;  // A list of malware names and the filenames found by AMaaS

  // getter and seter methods for the above private variables.
}
```

---

### ```MalwareItem```

The MalwareItem contains a detected malware information in the response data that is retrieved from our API.
The class has the following private members. There are getter and setter methods for each of the members.

```java
public class MalwareItem {
  private String malwareName;           // A detected Malware name
  private String fileName:              // File name that the malware is detected.

  // getter and seter methods for the above private variables.
}
```

---

### ```AMaasException```

The AMaasException class is the AMaaS SDK exception class.

```java
public final class AMaasException extends Exception {
  private AMaasErrorCode erroCode;

  public AMaasException(final AMaasErrorCode erroCode, final Object... params) {
    ...
  }
}
```

---

### ```AMaasErrorCode```

AMaasErrorCode is a enum type containing all the error conditions thrown by the `AMaasException` class. The error conditions are as follows:

| Enum Type                       | Error Message Templates                                | Description |
| --------------------------------|------------------------------------------------------- | ----------- |
| MSG_ID_ERR_INVALID_REGION       | %s is not a supported region.                          | The region code provided to the AMaasClient constructor is not a valid region. |
| MSG_ID_ERR_MISSING_AUTH         | Must provide an API key to use the client.             | The API Key provided to the AMaasClient constructor cannot be empty or `null`. |
| MSG_ID_ERR_KEY_AUTH_FAILED      | You are not authenticated. Invalid C1 token or Api Key | The API key is invalid. Please make sure a correct Vision One Api key is used. |
| MSG_ID_ERR_FILE_NOT_FOUND       | Failed to open file. No such file or directory %s.     | The given file cannot be found. Please make sure the file exists. |
| MSG_ID_ERR_FILE_NO_PERMISSION   | Failed to open file. Permission denied to open %s.     | There is a file access permission issue. Please make sure the SDK has read permission to the file. |
| MSG_ID_GRPC_ERROR               | Received gRPC status code: %d, msg: %s.                | gRpc error was reported with the status code. For details, please refer to published [gRPC Status Codes](https://grpc.github.io/grpc/core/md_doc_statuscodes.html) |
| MSG_ID_ERR_UNEXPECTED_INTERRUPT | Unexpected interrupt encountered.                      | An unexpected interrupt signal was received at the client. |

## Thread Safety

- scanFile() or scanBuffer() are designed to be thread-safe. It should be able to invoke scanFile() concurrently from multiple threads without protecting scanFile() with mutex or other synchronization mechanisms.

## Ensuring Secure Communication with TLS

The communication channel between the client program or SDK and the Trend Vision One™ File Security service is fortified with robust server-side TLS encryption. This ensures that all data transmitted between the client and Trend service remains thoroughly encrypted and safeguarded.
The certificate employed by server-side TLS is a publicly-signed certificate from Trend Micro Inc, issued by a trusted Certificate Authority (CA), further bolstering security measures.

The File Security SDK consistently adopts TLS as the default communication channel, prioritizing security at all times. It is strongly advised not to disable TLS in a production environment while utilizing the File Security SDK, as doing so could compromise the integrity and confidentiality of transmitted data.
