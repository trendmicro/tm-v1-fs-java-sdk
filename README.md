# Trend Vision One File Security Java SDK User Guide

The Trend Vision One File Security (FS) Java SDK allows developers to build applications that interface with cloud-based Trend Vision One FS anti-malware file scanning service, so data and artifacts handled by the applications can be scanned to determine whether they are malicious or not.

This guide shows how to set up your dev environment and project before using the FS Java SDK.

## Prerequisites

- Have Java 8 and above installed in your dev/build environment.
- Trend Vision One account with a chosen region - for more information, see the [Trend Visioin One document](https://docs.trendmicro.com/en-us/enterprise/trend-micro-xdr-help/Home).
- A Trend Vision One API key - for more information, see the [Trend Vision One API key documentation](https://docs.trendmicro.com/en-us/enterprise/trend-vision-one/administrative-setti/accountspartfoundati/api-keys.aspx).

## Download

Download the jar from [Maven Central Repository](https://mvnrepository.com/repos/central). Or for Maven, add this dependency to your `pom.xml`:

```xml
<dependency>
  <groupId>com.trend</groupId>
  <artifactId>file-security-java-sdk</artifactId>
  <version>1.0.0</version>
</dependency>
```
## Obtain an API Key

The FS SDK requires a valid API Key stored in the environment variable. It can accept Trend Vision One API keys. Please add the API Key associated with the Trend Vision One region that you wish to call as an environment variable named

Example:

```
export TMFS_API_KEY=<your_vision_one_api_key>
```

When obtaining the API Key, ensure that the API Key is associated with the region that you plan to use. It is important to note that Trend Vision One API Keys are associated with different regions, please refer to the region flag below to obtain a better understanding of the valid regions associated with the respective API Key

If you plan on using a Trend Vision One region, be sure to use the --region flag when running program with FS SDK to specify the region of that API key and to ensure you have proper authorization. The list of supported Trend Vision One regions can be found under the region flag

1. Login to the Trend Vision One.
2. Create a new Trend Vision One API key:

* Navigate to the Trend Vision One User Roles page.
* Verify that there is a role with the "Run artifact scan" permissions enabled. If not, create a role by clicking on "Add Role" and "Save" once finished.
* Directly configure a new key on the Trend Vision One API Keys page, using the role which contains the "Run artifacts scan" permission. It is advised to set an expiry time for the API key and make a record of it for future reference.

You can manage these keys from the Trend Vision One API Keys Page

## Using FS Java SDK

Using FS Java SDK to scan for malwares involves the following basic steps:

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
        String scanResult = client.scanFile(""path-of-file-to-scan"");

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

#### ```public AMaasClient(String region, String apiKey, long timeoutInSecs, boolean enabledTLS) throws AMaasException```

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key.  Value provided must be one of the Vision One regions, e.g. `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, etc. |
| apikey        | Your own Vision One API Key.                                                              |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. 0 default to 180 seconds.         |
| enabledTLS    | Enable or disable TLS. TLS should always be enabled when connecting to the AMaaS server. |

**_Return_**
An AmaasClient instance

#### ```public AMaasClient(String region, String apiKey, long timeoutInSecs) throws AMaasException```

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc. The enabledTLS is default to true.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key. Value provided must be one of the Vision One regions, e.g.  `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, etc. |
| apikey        | Your own Vision One API Key.                                                              |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. 0 default to 180 seconds.         |

**_Return_**
An AmaasClient instance

#### ```public String scanFile(String fileName) throws AMaasException```

Scan a file for malware and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                              |
| ------------- | ---------------------------------------------------------------------------------------- |
| fileName      | The name of the file with path of directory containing the file to scan.                 |

**_Return_**
String the scanned result in JSON format.

#### ```public String scanBuffer(byte[] buffer, String identifier) throws AMaasException```

Scan a buffer for malware and retrieves response data from the API.

**_Parameters_**

| Parameter     | Description                                                                               |
| ------------- | ----------------------------------------------------------------------------------------- |
| buffer        | The byte buffer to scan.                                                                  |
| identifier    | A unique name to identify the buffer.                                                     |

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

  public AMaasException(AMaasErrorCode erroCode, Object... params) {
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

* scanFile() or scanBuffer() are designed to be thread-safe. It should be able to invoke scanFile() concurrently from multiple threads without protecting scanFile() with mutex or other synchronization mechanisms.

