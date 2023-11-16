package com.trend.cloudone.amaas;

import java.io.IOException;

/*
 * Interface for implementing a reader class to be scanned.
 */
interface AMaasReader {
    public enum HashType {
        HASH_SHA1,
        HASH_SHA256
    }

    /*
     * Get Length of the content of the reader.
     */
    long getLength();

    /*
     * Get the identifier of the reader.
     */
    String getIdentifier();

    /*
     * Method to fill the given buffer with part of the reader's content starting from the offset.
     * @param offset starting offset to be read
     * @param buff byte array to be filled with reader's content.
     */
    int readBytes(int offset, byte[] buff) throws IOException;

    /*
     * Method to return the hashes as a Hex string for the content read by the reader.
     * @param htype one of HashType: sha-1, sha-256
     * @return a hex string represent the hash of a given type
     */
    String getHash(HashType htype);
}
