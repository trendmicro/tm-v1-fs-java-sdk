package com.trend.cloudone.amaas;

import java.io.IOException;

/*
 * Interface for implementing a reader class to be scanned.
 */
public interface AMaasReader {
    /**
     * Supported hash types for content hashing.
     */
    enum HashType {
        /** SHA-1 hash type. */
        HASH_SHA1,
        /** SHA-256 hash type. */
        HASH_SHA256
    }

    /**
     * Get Length of the content of the reader.
     * @return length of the content in bytes.
     */
    long getLength();

    /**
     * Get the identifier of the reader.
     * @return identifier of the reader.
     */
    String getIdentifier();

    /**
     * Method to fill the given buffer with part of the reader's content starting from the offset.
     * @param offset starting offset to be read
     * @param buff byte array to be filled with reader's content.
     * @return number of bytes read into the buffer.
     */
    int readBytes(int offset, byte[] buff) throws IOException;

    /**
     * Method to return the hashes as a Hex string for the content read by the reader.
     * @param htype one of HashType: sha-1, sha-256
     * @return a hex string represent the hash of a given type
     */
    String getHash(HashType htype);
}
