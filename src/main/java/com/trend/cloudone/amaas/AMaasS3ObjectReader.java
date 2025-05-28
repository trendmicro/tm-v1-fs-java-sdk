package com.trend.cloudone.amaas;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;


public class AMaasS3ObjectReader extends AMaasBaseReader {
    private static final Logger logger = Logger.getLogger(AMaasS3ObjectReader.class.getName());

    private URI s3Uri;
    private long fileSize;
    private final S3Client s3Client;

    /**
     * Create an S3 object reader.
     *
     * @param s3Client AWS S3 client used to access the S3 object. Only required if the s3Uri is in the S3 URI format.
     * @param s3Uri Location of the S3 object to read. This should be in the S3 URI format s3://bucket/key if you want
     *              to take advantage of the digest, otherwise it should be in the pre-signed S3 Object URL format
     *              https://bucket.s3.region.amazonaws.com/key
     * @param digest If true and the s3Uri is a s3 URI, the SHA1 and SHA256 digests will be calculated.
     * @throws AMaasException If the S3 client cannot be created or if the S3 object cannot be accessed.
     */
    AMaasS3ObjectReader(final S3Client s3Client, final URI s3Uri, final boolean digest) throws AMaasException {
        try {
            this.s3Client = s3Client;
            this.s3Uri = s3Uri;

            // Check if the URI is an S3 URI and if so then we need the S3Client to be provided
            if (this.s3Client == null && isS3Uri()) {
                throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, this.s3Uri);
            }

            determineObjectMetadata(digest);
        } catch (SecurityException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, this.s3Uri);
        }
    }

    @Override
    protected void finalize() {
        this.s3Client.close();
    }

    public String getIdentifier() {
        return this.s3Uri.toString();
    }

    public long getLength() {
        return this.fileSize;
    }

    public int readBytes(final int offset, final byte[] buff) throws IOException {
        final ByteBuffer byteBuffer = ByteBuffer.wrap(buff);
        return readByteRange(offset, byteBuffer);
    }

    /**
     * Gets the sha1, sha256 digest and size of the given S3 object in bytes.
     */
    void determineObjectMetadata(final boolean digest) throws AMaasException {
        if (isS3Uri()) {
            final String bucket = this.s3Uri.getHost();
            final String path = this.s3Uri.getPath();

            final HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(path)
                    .build();

            final HeadObjectResponse response = s3Client.headObject(request);
            this.fileSize = response.contentLength();
            if (digest) {
                this.setHash(HashType.HASH_SHA1, hexToBytes(response.checksumSHA1()));
                this.setHash(HashType.HASH_SHA256, hexToBytes(response.checksumSHA256()));
            }
        } else if (isS3ObjectUrl()) {
            try {
                final HttpURLConnection connection = (HttpURLConnection) this.s3Uri.toURL().openConnection();
                connection.setRequestMethod("HEAD");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_UNEXPECTED);
                }
                this.fileSize = Long.parseLong(connection.getHeaderField("Content-Length"));
            } catch (IOException ex) {
                throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_UNEXPECTED);
            }
         } else {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, this.s3Uri);
        }
    }

    /**
     * Reads a byte range from a file served via a S3 URI or Object URL.
     *
     * @param startByte The starting byte (inclusive)
     * @throws IOException if the connection fails or read fails
     */
    int readByteRange(final long startByte, final ByteBuffer byteBuffer) throws IOException {
        if (startByte < 0 || fileSize < startByte) {
            throw new IllegalArgumentException("Invalid byte range");
        }

        final long endByte = Math.min(startByte + byteBuffer.remaining() - 1, fileSize - 1);

        if (isS3ObjectUrl()) {
            return readByteRangeViaS3ObjectUrl(startByte, byteBuffer, endByte);
        } else {
            return readyByteRangeViaS3Uri(startByte, byteBuffer, endByte);
        }
    }

    private int readByteRangeViaS3ObjectUrl(long startByte, ByteBuffer byteBuffer, long endByte) throws IOException {
        final HttpURLConnection connection = (HttpURLConnection) this.s3Uri.toURL().openConnection();
        final String rangeHeader = String.format("bytes=%d-%d", startByte, endByte);
        connection.setRequestProperty("Range", rangeHeader);
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode != 200 && responseCode != 206) {
            throw new IOException("Expected HTTP 206 Partial Content, got: " + responseCode);
        }

        int totalBytesRead = 0;
        try (InputStream in = connection.getInputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                byteBuffer.put(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
            }
        }
        return totalBytesRead;
    }

    private int readyByteRangeViaS3Uri(long startByte, ByteBuffer byteBuffer, long endByte) {
        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(this.s3Uri.getHost())
                .key(this.s3Uri.getPath())
                .range(String.format("bytes=%d-%d", startByte, endByte))
                .build();

        int totalBytesRead = 0;
        try (ResponseInputStream<GetObjectResponse> s3Stream = this.s3Client.getObject(getObjectRequest)) {
            byte[] chunk = new byte[8 * 1024];
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

    boolean isS3Uri() {
        return this.s3Uri.getScheme().equalsIgnoreCase("s3");
    }

    boolean isS3ObjectUrl() {
        return this.s3Uri.getScheme().equalsIgnoreCase("https");
    }

    byte[] hexToBytes(final String hex) {
        int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Hex string must have even length");
        }
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);
            if (high == -1 || low == -1) {
                throw new IllegalArgumentException("Invalid hex character at position " + i);
            }
            data[i / 2] = (byte) ((high << 4) + low);
        }
        return data;
    }
}
