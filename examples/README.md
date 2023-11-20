# Examples to use File Security Java SDK

Examples for using the Vision One File Security Java SDK to access File Security gRPC scanning service.  
There are 4 examples under the following sub-folders:

1. filescan: scan a file or a folder sequentially
2. parallelscan: scan all files in a folder concurrently
3. s3app: download and scan a AWS S3 key
4. s3lambda: AWS lambda to scan a AWS S3 object key or all object keys under a folder of a AWS S3 bucket

## Prerequisites

- Java 8 or newer
- [Apache Maven](https://maven.apache.org/download.cgi)
- [Trend Vision One API Key](https://docs.trendmicro.com/en-us/enterprise/trend-vision-one/administrative-setti/accountspartfoundati/api-keys.aspx)

## Build Examples

1. The build is using Maven. The following `file-security-java-sdk` dependency can be found in the `pom.xml` file. If you wish to change the version or repository to download the SDK, please follow the [instruction to modify](./DOWNLOAD_README.md).

   ```xml
   <dependency>
      <groupId>com.trend</groupId>
      <artifactId>file-security-java-sdk</artifactId>
      <version>[1.0,)</version>
   </dependency>
   ```

2. You can build all examples in the `client/examples` folder or one of the examples under the respective example folder by running:

   ```sh
   mvn package
   ```

3. After a build, the targeted jar(s) will be created under a newly created `target` folder under the respective example folder. For instance, `examples/filescan/target`.

### Use CLI examples

- `filescan`: This example is to scan a file or a folder sequentially. It takes 4 input options:

   ```sh
   -f a file or a directory to be scanned
   -k customer Vision One API key
   -r region where the Vision One API key was obtained. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 seconds.
   ```

   For example:

   ```sh
   java -cp file-security-sdk-example-simple-1.0.0.jar App -f /myhome/test/sample.txt -k my_vision_one_api_key -r vision_one_aws_region
   ```

- `parallelscan`: This example is to scan all files in a folder concurrently. It takes 4 input options:

   ```sh
   -f a file or a directory to be scanned
   -k customer Vision One API key
   -r region where the Vision One API key was obtained. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 seconds.
   ```

   For example:

   ```sh
   java -cp file-security-sdk-example-parallel-1.0.0.jar App -f /myhome/test/sample.txt -k my_vision_one_api_key -r vision_one_aws_region
   ```

- `s3app`: This example download and scan a AWS S3 object key. It takes 6 input options:

   ```sh
   -a target AWS region
   -b target AWS S3 bucket name
   -f target AWS S3 object key to be scanned
   -k customer Vision One API key
   -r region where the Vision One API key was obtained. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 seconds.
   ```

   For example:

   ```sh
   java -cp file-security-sdk-example-s3app-1.0.0.jar App -k my_vision_one_api_key -r vision_one_aws_region -a my_aws_region -b my_s3_bucket -f my_s3_key
   ```

### Use Lambda example

1. The AWS Lambda example is under `client/examples/s3lambda` folder. It is to scan a AWS S3 object key or all object keys under a folder of a AWS S3 bucket.

2. Create a Lambda Function on AWS with following runtime settings.

   | Runtime                         | Handler  | Architecture    |
   | :------------------------------ | :--------| :-------------- |
   | Java 8 on Amazon Linux or above | S3Lambda | x86_64 or arm64 |


3. Configure Lambda Function environment variables on AWS with following settings. Substitute API KEY with Vision One API Key.

   | Key                     | Value                                                                    | Optional |
   | :---------------------- | :----------------------------------------------------------------------- | :------- |
   | TM_AM_REGION            | \<Vision One region\>                                                         | No       |
   | TM_AM_AUTH_KEY          | \<Vision One API KEY\>                                                              | No       |
   | TM_AM_SCAN_TIMEOUT_SECS | per scan timeout (180 sec)                                               | Yes      |
   | S3_BUCKET_NAME          | target S3 bucket name                                                    | No       |
   | S3_FOLDER_NAME          | target S3 folder name                                                    | No       |
   | S3_KEY_NAME             | target S3 object key. If missing, all keys in the folder will be scanned | Yes      |
   | USER_TAG_LIST           | comma separated string consisting of user defined tags.  | Yes |

4. Deploy the newly built jar `/examples/s3lambda/target/file-security-sdk-example-s3lambda-1.0.0.jar` to Lambda Function.

5. Create a test in Lambda Function and run the test.
