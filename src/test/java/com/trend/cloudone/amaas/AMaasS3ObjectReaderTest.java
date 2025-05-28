package com.trend.cloudone.amaas;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import com.trend.cloudone.amaas.AMaasReader.HashType;
import org.junit.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;


public class AMaasS3ObjectReaderTest {

    @Test
    public void testIsS3Uri() throws AMaasException {
        final AMaasS3ObjectReader readerWithDigest = new AMaasS3ObjectReader(S3Client.create(),
                URI.create("s3://my-bucket.ap-southeast-2.aws.com.au/my-key"), false) {
            @Override
            void determineObjectMetadata(boolean digest) {
                // Mock implementation
            }
        };
        assertNotNull(readerWithDigest);
        assertTrue(readerWithDigest.isS3Uri());
        assertFalse(readerWithDigest.isS3ObjectUrl());
    }

    @Test
    public void testIsS3ObjectUrl() throws AMaasException {
        final AMaasS3ObjectReader readerWithDigest = new AMaasS3ObjectReader(S3Client.create(),
                URI.create("https://my-bucket.ap-southeast-2.aws.com.au/my-key"), false) {
            @Override
            void determineObjectMetadata(boolean digest) {
                // Mock implementation
            }
        };
        assertNotNull(readerWithDigest);
        assertFalse(readerWithDigest.isS3Uri());
        assertTrue(readerWithDigest.isS3ObjectUrl());
    }


    @Test
    public void testAMaasS3ObjectReaderInitialised() throws AMaasException {
        final String sha1Hash = "1234567890abcdef1234567890abcdef12345678";
        final String sha256Hash = "1234567890abcdef1234567890abcdef12345678";

        final S3Client s3Client = new MockS3Client() {
            @Override
            public HeadObjectResponse headObject(HeadObjectRequest headObjectRequest) {
                return HeadObjectResponse.builder()
                        .contentLength(100L)
                        .checksumSHA1(sha1Hash)
                        .checksumSHA256(sha256Hash)
                        .build();
            }
        };

        final String s3Uri = "s3://my-bucket/my-prefix/my-object.txt";
        final AMaasS3ObjectReader readerWithDigest = new AMaasS3ObjectReader(s3Client, URI.create(s3Uri), true);
        assertNotNull(readerWithDigest);
        assertEquals(100L, readerWithDigest.getLength());
        assertEquals("sha1:" + sha1Hash, readerWithDigest.getHash(HashType.HASH_SHA1));
        assertEquals("sha256:" + sha256Hash, readerWithDigest.getHash(HashType.HASH_SHA256));

        final AMaasS3ObjectReader readerWithoutDigest = new AMaasS3ObjectReader(s3Client, URI.create(s3Uri), false);
        assertNotNull(readerWithoutDigest);
        assertEquals(100L, readerWithoutDigest.getLength());
        assertEquals("", readerWithoutDigest.getHash(HashType.HASH_SHA1));
        assertEquals("", readerWithoutDigest.getHash(HashType.HASH_SHA256));
    }

    @Test
    public void testAMaasS3ObjectReaderRandomAccess() throws AMaasException, IOException {
        final String sha1Hash = "1234567890abcdef1234567890abcdef12345678";
        final String sha256Hash = "1234567890abcdef1234567890abcdef12345678";

        final S3Client s3Client = new MockS3Client() {
            @Override
            public HeadObjectResponse headObject(final HeadObjectRequest headObjectRequest) {
                return HeadObjectResponse.builder()
                        .contentLength(1000000L)
                        .checksumSHA1(sha1Hash)
                        .checksumSHA256(sha256Hash)
                        .build();
            }

            @Override
            public ResponseInputStream<GetObjectResponse> getObject(final GetObjectRequest request) {
                final ByteRange byteRange = parse(request.range());

                // Mock implementation to return a dummy object
                final GetObjectResponse response = GetObjectResponse.builder()
                        .contentLength(byteRange.end - byteRange.start + 1)
                        .contentRange(String.format("bytes=%d-%d/%d", byteRange.start, byteRange.end, 1000000L))
                        .build();

                return new ResponseInputStream<>(response, new ByteArrayInputStream(fillByteArray(byteRange.start, byteRange.end)));
            }
        };

        final String s3Uri = "s3://my-bucket/my-prefix/my-object.txt";
        final AMaasS3ObjectReader readerWithDigest = new AMaasS3ObjectReader(s3Client, URI.create(s3Uri), true);
        assertNotNull(readerWithDigest);
        assertEquals(1000000L, readerWithDigest.getLength());
        assertEquals("sha1:" + sha1Hash, readerWithDigest.getHash(HashType.HASH_SHA1));
        assertEquals("sha256:" + sha256Hash, readerWithDigest.getHash(HashType.HASH_SHA256));

        ByteBuffer scanData = ByteBuffer.wrap(new byte[100]);
        int bytesRead = readerWithDigest.readByteRange(0, scanData);
        assertEquals(100, bytesRead);
        validateByteArray(0, 99, scanData);

        scanData = ByteBuffer.wrap(new byte[200]);
        bytesRead = readerWithDigest.readByteRange(100, scanData);
        assertEquals(200, bytesRead);
        validateByteArray(100, 299, scanData);
    }

    public void validateByteArray(final long startNumber, final long endNumber, final ByteBuffer buffer) {
        byte[] byteArray = buffer.array();

        IntStream.range((int)startNumber, (int)endNumber)
                .forEach(i -> assertEquals(byteArray[i - (int)startNumber], (byte) (i - startNumber)));
    }

    public byte[] fillByteArray(final long startNumber, final long endNumber) {
        byte[] byteArray = new byte[(int)endNumber - (int)startNumber + 1];

        // Use IntStream.range to iterate from 0 to 255 (exclusive of 256) and fill the array.
        IntStream.range((int)startNumber, (int)endNumber)
                .forEach(i -> byteArray[i - (int)startNumber] = (byte) (i - startNumber));

        return byteArray;
    }

    public static class ByteRange {
        public ByteRange(final long start, final long end) {
            this.start = start;
            this.end = end;
        }

        long start;
        long end;
    }

    public ByteRange parse(final String byteRangeString) {
        final Pattern pattern = Pattern.compile("bytes=(\\d+)-(\\d+)");
        final Matcher matcher = pattern.matcher(byteRangeString);

        if (matcher.matches()) {
            return new ByteRange(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)));
        }
        return null;
    }
}
