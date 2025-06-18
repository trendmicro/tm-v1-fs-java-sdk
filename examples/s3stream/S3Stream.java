package com.trend.cloudone.amaas;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import com.trend.cloudone.amaas.AMaasReader.HashType;

/**
 * S3Stream class implements the AMaasReader interface for S3 objects.
 * It supports only S3 URI format (s3://bucket/key) and gets hash results directly from AWS SDK.
 */
public class S3Stream implements AMaasReader {
    private static final Logger logger = Logger.getLogger(S3Stream.class.getName());
    private static final int CHUNK_SIZE_KB = 8;
    private static final int BYTES_PER_KB = 1024;

    private String region;
    private String bucket;
    private String key;
    private long fileSize;
    private final S3Client s3Client;
    private String sha1Hash;
    private String sha256Hash;

    /**
     * Create an S3 stream reader. Automatically creates S3Client with default configuration.
     *
     * @param region The AWS region where the S3 bucket is located.
     * @param bucket The S3 bucket name.
     * @param key The S3 object key.
     * @throws AMaasException If the S3 client cannot be created or if the S3 object cannot be accessed.
     */
    public S3Stream(final String region, final String bucket, final String key) throws AMaasException {
        if (region == null || region.isEmpty() || bucket == null || bucket.isEmpty() || key == null || key.isEmpty()) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, "s3://" + bucket + "/" + key);
        }

        this.region = region;
        this.bucket = bucket;
        this.key = key;

        try {
            // Create S3Client with default configuration and specified region
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .build();
        } catch (Exception ex) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_UNEXPECTED, ex);
        }

        try {
            determineObjectMetadata();
        } catch (SecurityException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, "s3://" + this.bucket + "/" + this.key);
        }
    }

    /**
     * Get the S3 object identifier (URI).
     * @return the S3 URI as string
     */
    @Override
    public String getIdentifier() {
        return "s3://" + this.bucket + "/" + this.key;
    }

    /**
     * Get the size of the S3 object.
     * @return the size in bytes
     */
    @Override
    public long getLength() {
        return this.fileSize;
    }

    /**
     * Read bytes from the S3 object starting from the given offset.
     * @param offset starting offset to read from
     * @param buff byte array to fill with content
     * @return number of bytes read
     * @throws IOException if read fails
     */
    @Override
    public int readBytes(final int offset, final byte[] buff) throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buff);
        return readByteRange(offset, byteBuffer);
    }

    /**
     * Get hash from S3 object metadata.
     * @param htype the type of hash (HASH_SHA1 or HASH_SHA256)
     * @return the hash value as hex string with prefix (e.g., "sha1:abc123...")
     */
    @Override
    public String getHash(final HashType htype) {
        switch (htype) {
            case HASH_SHA1:
                return (this.sha1Hash == null) ? "" : "sha1:" + this.sha1Hash;
            case HASH_SHA256:
                return (this.sha256Hash == null) ? "" : "sha256:" + this.sha256Hash;
            default:
                return "";
        }
    }

    /**
     * Gets the sha1, sha256 digest and size of the given S3 object in bytes.
     */
    private void determineObjectMetadata() throws AMaasException {
        try {
            final HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(this.key)
                    .build();

            final HeadObjectResponse response = s3Client.headObject(request);
            this.fileSize = response.contentLength();

            // Get hash results directly from AWS SDK
            if (response.checksumSHA1() != null) {
                this.sha1Hash = response.checksumSHA1();
            }
            if (response.checksumSHA256() != null) {
                this.sha256Hash = response.checksumSHA256();
            }
        } catch (SdkException ex) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_UNEXPECTED);
        }
    }

    /**
     * Reads a byte range from a file served via S3 URI.
     *
     * @param startByte The starting byte (inclusive)
     * @param byteBuffer The ByteBuffer to fill with the read data
     * @return The number of bytes read
     * @throws IOException if the connection fails or read fails
     */
    private int readByteRange(final long startByte, final ByteBuffer byteBuffer) throws IOException {
        if (startByte < 0 || fileSize < startByte) {
            throw new IllegalArgumentException("Invalid byte range");
        }

        final long endByte = Math.min(startByte + byteBuffer.remaining() - 1, fileSize - 1);

        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.bucket)
                .key(this.key)
                .range(String.format("bytes=%d-%d", startByte, endByte))
                .build();

        int totalBytesRead = 0;
        try (ResponseInputStream<GetObjectResponse> s3Stream = this.s3Client.getObject(getObjectRequest)) {
            byte[] chunk = new byte[CHUNK_SIZE_KB * BYTES_PER_KB];
            int bytesRead;
            while ((bytesRead = s3Stream.read(chunk)) != -1) {
                byteBuffer.put(chunk, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        } catch (SdkException | IOException ex) {
            throw new RuntimeException("Failed to download S3 object range", ex);
        }
        return totalBytesRead;
    }

    private static void info(final String msg, final Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private static Options getCmdOptions() {
        Options optionList = new Options();
        optionList.addRequiredOption("a", "awsregion", true, "AWS region");
        optionList.addRequiredOption("b", "bucket", true, "S3 bucket name");
        optionList.addRequiredOption("f", "S3key", true, "S3 key to be scanned");
        optionList.addRequiredOption("k", "apikey", true, "Vision One API key");
        optionList.addRequiredOption("r", "region", true, "AMaaS service region");
        optionList.addOption("t", "timeout", true, "Per scan timeout in seconds");
        return optionList;
    }

    /**
      * The program takes 6 options and respecive values to configure the AMaaS SDK client.
      * @param args Input options:
      *                  -a AWS region
      *                  -b S3 bucket name
      *                  -f S3 key to be scanned
      *                  -k the API key or bearer authentication token
      *                  -r region where the V1 key/token was applied. eg, us-east-1
      *                  -t optional client maximum waiting time in seconds for a scan. 0 or missing means default.
    */
    public static void main(final String[] args) {
        String awsRegion = "";
        String bucketName = "";
        String keyName = "";
        String apikey = null;
        String amaasRegion = "";
        long timeout = 0;

        DefaultParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();
        Options optionList = getCmdOptions();
        try {
            CommandLine cmd = parser.parse(optionList, args);
            if (cmd.hasOption("a")) {
                awsRegion = cmd.getOptionValue("a");
            }
            if (cmd.hasOption("b")) {
                bucketName = cmd.getOptionValue("b");
            }
            if (cmd.hasOption("f")) {
                keyName = cmd.getOptionValue("f");
            }
            if (cmd.hasOption("r")) {
                amaasRegion = cmd.getOptionValue("r");
            }
            if (cmd.hasOption("k")) {
                apikey = cmd.getOptionValue("k");
            }
            if (cmd.hasOption("t")) {
                timeout = Long.parseLong(cmd.getOptionValue("t"));
            }
            info("Creating S3Stream for S3 Object....");
            S3Stream s3Stream = new S3Stream(awsRegion, bucketName, keyName);
            info("Completed creating S3Stream for S3 Object....");
            AMaasClient client = new AMaasClient(amaasRegion, apikey, timeout);
            try {
                long totalStartTs = System.currentTimeMillis();

                // Use scanRun with the S3Stream AMaasReader
                String scanResult = client.scanRun(s3Stream, null, false, false, false, true);
                long totalEndTs = System.currentTimeMillis();
                info("Scan Result: {0}", scanResult);
                info("*************** Total scan time {0}", totalEndTs - totalStartTs);
            } finally {
                client.close();
            }
        } catch (ParseException err) {
            helper.printHelp("Usage:", optionList);
        } catch (NumberFormatException err) {
            info("Exception parsing -t value must be a number");
        } catch (AMaasException err) {
            info("AMaaS SDK Exception encountered: {0}", err.getMessage());
        } catch (Exception err) {
            info("Unexpected exception encountered: {0}", err.getMessage());
        }
    }
}
