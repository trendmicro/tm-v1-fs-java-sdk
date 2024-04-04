import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.S3Client;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.trend.cloudone.amaas.AMaasClient;
import com.trend.cloudone.amaas.AMaasException;

public final class S3Lambda implements RequestHandler<Object, String> {
    private static final Logger logger = Logger.getLogger(S3Lambda.class.getName());
    private static final int BUFFER_LENGTH = 16483;

    private static void info(final String msg, final Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private static byte[] serialize(final ResponseInputStream<GetObjectResponse> data) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] byteArray = new byte[BUFFER_LENGTH];

        try {
            while ((nRead = data.read(byteArray, 0, byteArray.length)) != -1) {
                buffer.write(byteArray, 0, nRead);
            }
        } catch (IOException e) {
            info("I/O error while serializing data: {} | {}", e.getMessage(), e);
        }

        return buffer.toByteArray();
    }

    private static byte[] downloadS3Object(final S3Client s3client, final String bucketName, final String key) throws S3Exception {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .build();

        ResponseInputStream<GetObjectResponse> response = s3client.getObject(getObjectRequest);
        return serialize(response);
    }

    private static ArrayList<String> listAllS3Keys(final S3Client s3client, final String bucketName, final String folderName) {
        ArrayList<String> arrayList = new ArrayList<>();
        ListObjectsRequest.Builder builder = ListObjectsRequest
            .builder()
            .bucket(bucketName);
        if (folderName != null && folderName != "") {
            builder.prefix(folderName);
        }
        ListObjectsRequest listObjects = builder.build();

        try {
            ListObjectsResponse res = s3client.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            for (S3Object os : objects) {
                if (os.key() != null && !(os.key().endsWith(("/")))) {
                    arrayList.add(os.key());
                }
            }

        } catch (S3Exception e) {
            info(e.awsErrorDetails().errorMessage());
        }
        return arrayList;
    }

    private static void sequentialScan(final AMaasClient client, final S3Client s3client, final String bucketName, final ArrayList<String> keyList, final String[] tagList) {

        for (String keyName: keyList) {
            try {
                byte[] bytes = downloadS3Object(s3client, bucketName, keyName);
                long startTs = System.currentTimeMillis();
                info("===============> Scanning S3 key {0}", keyName);
                String scanResult = "";
                if (tagList == null) {
                    scanResult = client.scanBuffer(bytes, keyName);
                } else {
                    scanResult = client.scanBuffer(bytes, keyName, tagList, false, false);
                }
                long endTs = System.currentTimeMillis();
                info("===============> scanResult {0}, scanTime {1}.", scanResult, endTs - startTs);
            } catch (S3Exception err) {
                info("S3Exception encountered. Skip scanning  {0} {1}", keyName, err.getMessage());
            } catch (AMaasException err) {
                info("AMaaSException encountered. Skip scanning  {0} {1}", keyName, err.getMessage());
            }
        }
    }

    /*
      * Lambda handler depends on 6 environment variables to configure the AMaaS SDK client and what to scan.
      * It can scan either a specific S3 key or a folder under a S3 bucket.
      *
      * TM_AM_AUTH_KEY the API key or bearer authentication token
      * TM_AM_REGION region where the C1 key/token was applied. eg, us-east-1
      * TM_AM_SCAN_TIMEOUT_SECS client maximum waiting time in seconds for a scan. 0 or missing means default.
      *
      * S3_BUCKET_NAME S3 bucket name
      * S3_FOLDER_NAME S3 key prefix as folder name
      * S3_KEY_NAME a particular S3 key to be scanned
      *
      * If S3_BUCKET_NAME and S3_KEY_NAME is set, that particular S3 key is scanned.
      * If S3_BUCKET_NAME and S3_FOLDER_NAME are set, all keys under the folder of the bucket are scanned.
      *
    */
    @Override
    public String handleRequest(final Object input, final Context context) {
        info("Testing scanning");

        String apikey = System.getenv("TM_AM_AUTH_KEY");
        String region = System.getenv("TM_AM_REGION");
        String timeoutStr = System.getenv("TM_AM_SCAN_TIMEOUT_SECS");

        String bucketName = System.getenv("S3_BUCKET_NAME");
        String folderName = System.getenv("S3_FOLDER_NAME");
        String keyName = System.getenv("S3_KEY_NAME");
        // tags separated by comma. This is user defined tags to be used to tag scan items.
        String tags = System.getenv("USER_TAG_LIST");
        long timeout = 0;
        try {
            timeout = Integer.parseInt(timeoutStr);
        } catch (NumberFormatException err) {
            info("Timeout setting ignored.");
        }

        try {
            S3Client s3client = S3Client.builder().build();
            ArrayList<String> keyList = null;
            if (keyName == null || keyName == "") {
                keyList = listAllS3Keys(s3client, bucketName, folderName);
            } else {
                keyList = new ArrayList<String>() {{ add(keyName); }};
            }

            if (keyList == null || keyList.isEmpty()) {
                info("No S3 object found in the S3 bucket folder");
                return "";
            }
            String[] tagList = null;
            if (tags != null) {
                info("tags to used {0}", tags);
                tagList = tags.split(",");
            }

            AMaasClient client = new AMaasClient(region, apikey, timeout);
            long totalStartTs = System.currentTimeMillis();

            sequentialScan(client, s3client, bucketName, keyList, tagList);
            long totalEndTs = System.currentTimeMillis();
            info("*************** Total scan time {0}", totalEndTs - totalStartTs);
        } catch (Exception err) {
            info("Exception encountered {0}", err);
        }
        return "";
    }
}
