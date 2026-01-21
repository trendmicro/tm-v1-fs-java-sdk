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
2. Configuring scan options using the `AMaasScanOptions` class with the builder pattern.
3. Invoking file scan or buffer scan method to scan the target data.
4. Parsing the JSON response returned by the scan APIs to determine whether the scanned data contains malware or not.

### Sample Code

```java
import com.trend.cloudone.amaas.AMaasClient;
import com.trend.cloudone.amaas.AMaasScanOptions;
import com.trend.cloudone.amaas.AMaasException;

public static void main(String[] args) {
    try {
        // 1. Create an AMaaS Client object and configure it to carry out the scans in Vision One "us-east-1" region.
        AMaasClient client = new AMaasClient("us-east-1", "your-api-key");
        try {
            // 2. Configure scan options using the builder pattern
            AMaasScanOptions options = AMaasScanOptions.builder()
                .pml(true)                    // Enable predictive machine learning
                .feedback(true)               // Enable Smart Feedback
                .verbose(false)               // Disable verbose logging
                .activeContent(false)         // Disable active content scanning
                .tagList(new String[]{"tag1", "tag2"})  // Optional tags
                .build();

            // 3. Call scanFile() to scan the content of a file
            String scanResult = client.scanFile("path-of-file-to-scan", true, options);

            if (scanResult != null) {
                // 4. Print out the JSON response from scanFile()
                System.out.println("scan result " + scanResult);
            }
        } finally {
            // 5. Always close the client to release resources
            client.close();
        }
    } catch (AMaasException err) {
        info("Exception {0}", err.getMessage());
    }
}
```

### Sample JSON Response

#### Concise Format

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

### Enable Active Content Detection

Enables active content detection for scanning operations. This feature allows the scanner to detect potentially malicious active content within files, specifically:

- **PDF scripts**: Detects embedded JavaScript and other scripting content in PDF files
- **Office macros**: Detects VBA macros and other executable content in Microsoft Office documents

When active content is detected, the scan result will include a type field with values of either `macro` or `script` to indicate the type of active content found.

#### Verbose Format

```json
{
  "scanType": "sdk",
  "objectType": "file",
  "timestamp": {
    "start": "2024-07-05T20:01:21.064Z",
    "end": "2024-07-05T20:01:21.069Z"
  },
  "schemaVersion": "1.0.0",
  "scannerVersion": "1.0.0-59",
  "fileName": "eicar.com",
  "rsSize": 68,
  "scanId": "40d7a38e-a1d3-400b-a09c-7aa9cd62658f",
  "accountId": "",
  "result": {
    "atse": {
      "elapsedTime": 4693,
      "fileType": 5,
      "fileSubType": 0,
      "version": {
        "engine": "23.57.0-1002",
        "lptvpn": 385,
        "ssaptn": 731,
        "tmblack": 253,
        "tmwhite": 239,
        "macvpn": 914
      },
      "malwareCount": 1,
      "malware": [
        {
          "name": "Eicar_test_file",
          "fileName": "eicar.com",
          "type": "",
          "fileType": 5,
          "fileSubType": 0,
          "fileTypeName": "COM",
          "fileSubTypeName": "VSDT_COM_DOS"
        }
      ],
      "error": null,
      "fileTypeName": "COM",
      "fileSubTypeName": "VSDT_COM_DOS"
    }
  },
  "fileSHA1": "3395856ce81f2b7382dee72602f798b642f14140",
  "fileSHA256": "275a021bbfb6489e54d471899f7db9d1663fc695ec2fe2a2c4538aabf651fd0f",
  "appName": "V1FS"
}
```

## Java SDK API Reference

### `AmaasClient`

The AmaasClient class is the main class of the SDK and provides methods to use the AMaaS scanning services.

#### `public AMaasClient(final String region, final String host, final String apiKey, final long timeoutInSecs, final boolean enabledTLS, final string caCert) throws AMaasException`

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc.

**_Parameters_**

| Parameter     | Description                                                                                                                                                                                                                                                                                 |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key. Value provided must be one of the Vision One regions, e.g. `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, `ap-south-1`, `me-central-1`,`eu-west-2`,`ca-central-1`, etc. If host is given, region will be ignored. |
| host          | The host ip address of self hosted AMaaS scanner. Ignore if to use Trend AMaaS service                                                                                                                                                                                                      |
| apikey        | Your own Vision One API Key.                                                                                                                                                                                                                                                                |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. Valid value is 0, 1, 2, ... ; default to 300 seconds.                                                                                                                                                                                |
| enabledTLS    | Enable or disable TLS. TLS should always be enabled when connecting to the AMaaS server. For more information, see the 'Ensuring Secure Communication with TLS'                                                                                                                             |
| caCert        | File path of the CA certificate for hosted AMaaS Scanner server. null if using Trend AMaaS service.                                                                                                                                                                                         |

**_Return_**
An AmaasClient instance

#### `public AMaasClient(final String region, final String apiKey, final long timeoutInSecs) throws AMaasException`

Creates a new instance of the `AmaasClient` class, and provisions essential settings, including authentication/authorization credentials (API key), preferred service region, etc. The enabledTLS is default to true.

**_Parameters_**

| Parameter     | Description                                                                                                                                                                                                                                       |
| ------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| region        | The region you obtained your api key. Value provided must be one of the Vision One regions, e.g. `us-east-1`, `eu-central-1`, `ap-northeast-1`, `ap-southeast-2`, `ap-southeast-1`, `ap-south-1`, `me-central-1`,`eu-west-2`,`ca-central-1` ,etc. |
| apikey        | Your own Vision One API Key.                                                                                                                                                                                                                      |
| timeoutInSecs | Timeout to cancel the connection to server in seconds. Valid value is 0, 1, 2, ... ; default to 300 seconds.                                                                                                                                      |

**_Return_**
An AmaasClient instance

#### `public String scanRun(final AMaasReader reader, final AMaasScanOptions options) throws AMaasException`

Scan an AMaasReader for malware and retrieves response data from the API. This is the core scanning method that provides the most flexibility by accepting an AMaasReader interface, allowing for different types of data sources.

**_Parameters_**

| Parameter | Description                                                                                                                                   |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| reader    | `AMaasReader` to be scanned. This can be an `AMaasFileReader` or any custom implementation you develop to support your specific data sources. |
| options   | Scan options containing configuration for the scan operation (PML, feedback, verbose, activeContent, tags).                                   |

**_Return_**
String the scanned result in JSON format.

**_Note_**: For an example of implementing a custom AMaasReader, please refer to the `examples/s3stream/S3Stream.java` code which demonstrates a streaming implementation of the AMaasReader interface.

#### `public String scanFile(final String fileName, final boolean digest, final AMaasScanOptions options) throws AMaasException`

Scan a file for malware and retrieves response data from the API.

**_Parameters_**

| Parameter | Description                                                                                                 |
| --------- | ----------------------------------------------------------------------------------------------------------- |
| fileName  | The name of the file with path of directory containing the file to scan.                                    |
| digest    | A flag to enable/disable calculation of digests for cache search and result lookup.                         |
| options   | Scan options containing configuration for the scan operation (PML, feedback, verbose, activeContent, tags). |

**_Return_**
String the scanned result in JSON format.

#### `public String scanBuffer(final byte[] buffer, final String identifier, final boolean digest, final AMaasScanOptions options) throws AMaasException`

Scan a buffer for malware and retrieves response data from the API.

**_Parameters_**

| Parameter  | Description                                                                                                 |
| ---------- | ----------------------------------------------------------------------------------------------------------- |
| buffer     | The byte buffer to scan.                                                                                    |
| identifier | A unique name to identify the buffer.                                                                       |
| digest     | A flag to enable/disable calculation of digests for cache search and result lookup.                         |
| options    | Scan options containing configuration for the scan operation (PML, feedback, verbose, activeContent, tags). |

**_Return_**
String the scanned result in JSON format.

---

### `AMaasScanOptions`

The AMaasScanOptions class provides a convenient way to configure scan parameters using the builder pattern. This class encapsulates all scan-related configuration options.

#### Creating Scan Options

```java
// Create scan options with default values (all flags disabled, no tags)
AMaasScanOptions defaultOptions = AMaasScanOptions.builder().build();

// Create scan options with specific configuration
AMaasScanOptions customOptions = AMaasScanOptions.builder()
    .pml(true)                              // Enable predictive machine learning
    .feedback(true)                         // Enable Smart Feedback
    .verbose(false)                         // Disable verbose logging
    .activeContent(true)                    // Enable active content scanning
    .tagList(new String[]{"tag1", "tag2"})  // Add custom tags
    .build();
```

**_Builder Methods_**

| Method                   | Parameter     | Description                                                                                            |
| ------------------------ | ------------- | ------------------------------------------------------------------------------------------------------ |
| `pml(boolean)`           | pml           | Enable or disable predictive machine learning detection. Default: false.                               |
| `feedback(boolean)`      | feedback      | Enable or disable Trend Micro Smart Protection Network's Smart Feedback. Default: false.               |
| `verbose(boolean)`       | verbose       | Enable or disable verbose logging mode. Default: false.                                                |
| `activeContent(boolean)` | activeContent | Enable or disable active content scanning. Default: false.                                             |
| `tagList(String[])`      | tagList       | Set the list of tags for the scan. At most 8 tags with maximum length of 63 characters. Default: null. |
| `build()`                | -             | Build and return the AMaasScanOptions instance.                                                        |

**_Getter Methods_**

| Method              | Return Type | Description                                            |
| ------------------- | ----------- | ------------------------------------------------------ |
| `isPml()`           | boolean     | Returns true if PML detection is enabled.              |
| `isFeedback()`      | boolean     | Returns true if Smart Feedback is enabled.             |
| `isVerbose()`       | boolean     | Returns true if verbose mode is enabled.               |
| `isActiveContent()` | boolean     | Returns true if active content scanning is enabled.    |
| `getTagList()`      | String[]    | Returns the array of tags, or null if no tags are set. |

---

### `AmaasScanResult`

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

  // getter and setter methods for the above private variables.
}
```

---

### `MalwareItem`

The MalwareItem contains a detected malware information in the response data that is retrieved from our API.
The class has the following private members. There are getter and setter methods for each of the members.

```java
public class MalwareItem {
  private String malwareName;           // A detected Malware name
  private String fileName:              // File name that the malware is detected.

  // getter and setter methods for the above private variables.
}
```

### `AMaasScanResultVerbose`

The AMaasScanResultVerbose has the data elements of the response data in verbose mode that is retrieved from our API. The class has the following private members. There are getter and setter methods for each of the members. See javaDoc for the class of each data element.

```java
public class AMaasScanResultVerbose {
    private String scanType;          // Type of scan
    private String objectType;        // Type of the object being scanned. e.g, file
    private StartEnd timestamp;       // begin and end time strings in ISO 8601 format
    private String schemaVersion;     // Version of the data schema
    private String scannerVersion;    // Scanner version
    private String fileName;          // Name of the file
    private long rsSize;              // Size of the scanned file
    private String scanId;            // ID of the scan
    private String accountId;         // ID of the customer
    private ScanResult result;        // Result for the current scan
    private String[] tags;            // Tags used for this scan
    private String fileSha1;          // Sha1 of the scanned file
    private String fileSha256;        // Sha256 of the scanned file
    private String appName;           // Name of the application

    // getter and setter methods for the above private variables.
}
```

---

### `AMaasException`

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

### `AMaasErrorCode`

AMaasErrorCode is a enum type containing all the error conditions thrown by the `AMaasException` class. The error conditions are as follows:

| Enum Type                       | Error Message Templates                                | Description                                                                                                                                                        |
| ------------------------------- | ------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| MSG_ID_ERR_INVALID_REGION       | %s is not a supported region.                          | The region code provided to the AMaasClient constructor is not a valid region.                                                                                     |
| MSG_ID_ERR_MISSING_AUTH         | Must provide an API key to use the client.             | The API Key provided to the AMaasClient constructor cannot be empty or `null`.                                                                                     |
| MSG_ID_ERR_KEY_AUTH_FAILED      | You are not authenticated. Invalid C1 token or Api Key | The API key is invalid. Please make sure a correct Vision One Api key is used.                                                                                     |
| MSG_ID_ERR_FILE_NOT_FOUND       | Failed to open file. No such file or directory %s.     | The given file cannot be found. Please make sure the file exists.                                                                                                  |
| MSG_ID_ERR_FILE_NO_PERMISSION   | Failed to open file. Permission denied to open %s.     | There is a file access permission issue. Please make sure the SDK has read permission to the file.                                                                 |
| MSG_ID_GRPC_ERROR               | Received gRPC status code: %d, msg: %s.                | gRpc error was reported with the status code. For details, please refer to published [gRPC Status Codes](https://grpc.github.io/grpc/core/md_doc_statuscodes.html) |
| MSG_ID_ERR_UNEXPECTED_INTERRUPT | Unexpected interrupt encountered.                      | An unexpected interrupt signal was received at the client.                                                                                                         |

## Thread Safety

- scanFile() or scanBuffer() are designed to be thread-safe. It should be able to invoke scanFile() concurrently from multiple threads without protecting scanFile() with mutex or other synchronization mechanisms.

## Ensuring Secure Communication with TLS

The communication channel between the client program or SDK and the Trend Vision One™ File Security service is fortified with robust server-side TLS encryption. This ensures that all data transmitted between the client and Trend service remains thoroughly encrypted and safeguarded.
The certificate employed by server-side TLS is a publicly-signed certificate from Trend Micro Inc, issued by a trusted Certificate Authority (CA), further bolstering security measures.

The File Security SDK consistently adopts TLS as the default communication channel, prioritizing security at all times. It is strongly advised not to disable TLS in a production environment while utilizing the File Security SDK, as doing so could compromise the integrity and confidentiality of transmitted data.

## Disabling certificate verification

For customers who need to enable TLS channel encryption without verifying the provided CA certificate, the `TM_AM_DISABLE_CERT_VERIFY` environment variable can be set. However, this option is only recommended for use in testing environments.

When `TM_AM_DISABLE_CERT_VERIFY` is set to `1`, certificate verification is disabled. By default, the certificate will be verified.

## Proxy Configuration

The File Security Java SDK supports HTTP and SOCKS5 proxy configurations through environment variables. This allows the SDK to work in enterprise environments that require proxy servers for internet access.

### Supported Environment Variables

| Environment Variable | Required/Optional | Description                                                                                                                     |
| -------------------- | ----------------- | ------------------------------------------------------------------------------------------------------------------------------- |
| `HTTP_PROXY`         | Optional          | HTTP proxy URL for HTTP connections (e.g., `http://proxy.example.com:8080`)                                                     |
| `HTTPS_PROXY`        | Optional          | Proxy URL for HTTPS connections (e.g., `https://proxy.example.com:8443` or `socks5://socks.example.com:1080`)                   |
| `NO_PROXY`           | Optional          | Comma-separated list of host names to bypass proxy (e.g., `localhost,127.0.0.1,*.local`). Use `*` to bypass proxy for all hosts |
| `PROXY_USER`         | Optional          | Username for proxy authentication (used with `Proxy-Authorization` header)                                                      |
| `PROXY_PASS`         | Optional          | Password for proxy authentication (used only when `PROXY_USER` is configured)                                                   |

### Proxy Configuration Examples

#### Basic HTTP Proxy

```bash
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=http://proxy.company.com:8080
```

#### SOCKS5 Proxy

```bash
export HTTPS_PROXY=socks5://socks-proxy.company.com:1080
```

**Important:** When using SOCKS5 proxy, ensure you call `client.close()` to properly release network resources. The SDK creates background threads for SOCKS5 connections that must be explicitly closed.

```java
AMaasClient client = new AMaasClient("us-east-1", "your-api-key");
try {
    // Perform scanning operations
    String result = client.scanFile("file.txt");
} finally {
    // Always close the client when using SOCKS5 proxy
    client.close();
}
```

#### Proxy with Authentication

```bash
export HTTP_PROXY=http://proxy.company.com:8080
export HTTPS_PROXY=https://secure-proxy.company.com:8443
export PROXY_USER=username
export PROXY_PASS=password
```

#### Bypassing Proxy for Specific Hosts

```bash
export HTTP_PROXY=http://proxy.company.com:8080
export NO_PROXY=localhost,127.0.0.1,*.internal.company.com
```

#### Disabling Proxy for All Connections

```bash
export NO_PROXY=*
```

### Notes

- The SDK automatically detects and uses proxy settings from environment variables
- For HTTPS connections, `HTTPS_PROXY` takes precedence over `HTTP_PROXY`
- SOCKS5 proxies are supported by specifying `socks5://` in the proxy URL
- Proxy authentication requires both `PROXY_USER` and `PROXY_PASS` to be set
- The `NO_PROXY` variable supports wildcards (e.g., `*.local`) and exact matches
- No code changes are required - simply set the appropriate environment variables before running your application
- **Resource Management:** Always call `client.close()` when finished, especially when using SOCKS5 proxies, to ensure proper cleanup of network resources and prevent applications from hanging
