# Examples to use Cloud One VSAPI Java SDK

Examples for using the Cloud One VSAPI Java SDK to access AMaaS gRPC scanning service.  
There are 4 examples under the following subfolders:

1. filescan: scan a file or a folder sequentially
2. parallelscan: scan all files in a folder concurrently
3. s3app: download and scan a AWS S3 key
4. s3lambda: AWS lambda to scan a AWS S3 object key or all object keys under a folder of a AWS S3 bucket

## Prerequisites

- Java 8 or newer
- [Apache Maven](https://maven.apache.org/download.cgi)
- [CloudOne API Key](https://cloudone.trendmicro.com/docs/identity-and-account-management/c1-api-key/)

## Build Examples

1. The build is using Maven. The following `cloudone-vsapi-sdk` dependency can be found in the `pom.xml` file. If you wish to change the version or repository to download the SDK, please follow the [instruction to modify](./DOWNLOAD_README.md).

   ```xml
   <dependency>
      <groupId>com.trend</groupId>
      <artifactId>cloudone-vsapi-sdk</artifactId>
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
   -k customer CloudOne API key or bearer authentication token
   -r region where the CouldOne key/token was applied. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 secsonds.
   ```

   For example:

   ```sh
   java -cp cloudone-vsapi-example-simple-1.0.0.jar App -f /myhome/test/sample.txt -k my_cloudone_api_key -r amaas_aws_region
   ```

- `parallelscan`: This example is to scan all files in a folder concurrently. It takes 4 input options:

   ```sh
   -f a file or a directory to be scanned
   -k customer CloudOne API key or bearer authentication token
   -r region where the CouldOne key/token was applied. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 secsonds.
   ```

   For example:

   ```sh
   java -cp cloudone-vsapi-example-parallel-1.0.0.jar App -f /myhome/test/sample.txt -k my_cloudone_api_key -r amaas_aws_region
   ```

- `s3app`: This example download and scan a AWS S3 object key. It takes 6 input options:

   ```sh
   -a target AWS region
   -b target AWS S3 bucket name
   -f target AWS S3 object key to be scanned
   -k customer CloudOne API key or bearer authentication token
   -r region where the CouldOne key/token was applied. eg, us-east-1
   -t optional client maximum waiting time in seconds for a scan. 0 or missing will default to 180 secsonds.
   ```

   For example:

   ```sh
   java -cp cloudone-vsapi-example-s3app-1.0.0.jar App -k my_cloudone_api_key -r amaas_aws_region -a my_aws_region -b my_s3_bucket -f my_s3_key
   ```

### Use Lambda example

1. The AWS Lambda example is under `client/examples/s3lambda` folder. It is to scan a AWS S3 object key or all object keys under a folder of a AWS S3 bucket.

2. Create a Lambda Function on AWS with following runtime settings.

   | Runtime                         | Handler  | Architecture    |
   | :------------------------------ | :--------| :-------------- |
   | Java 8 on Amazon Linux or above | S3Lambda | x86_64 or arm64 |


3. Configure Lambda Function environment variables on AWS with following settings. Substitute API KEY with CloudOne API Key.

   | Key                     | Value                                                                    | Optional |
   | :---------------------- | :----------------------------------------------------------------------- | :------- |
   | TM_AM_REGION            | \<AMaaS region\>                                                         | No       |
   | TM_AM_AUTH_KEY          | \<API KEY\>                                                              | No       |
   | TM_AM_SCAN_TIMEOUT_SECS | per scan timeout (180 sec)                                               | Yes      |
   | S3_BUCKET_NAME          | target S3 bucket name                                                    | No       |
   | S3_FOLDER_NAME          | target S3 folder name                                                    | No       |
   | S3_KEY_NAME             | target S3 object key. If missing, all keys in the folder will be scanned | Yes      |

4. Deploy the newly built jar `/examples/s3lambda/target/cloudone-vsapi-example-s3lambda-1.0.0.jar` to Lambda Function.

5. Create a test in Lambda Function and run the test.
