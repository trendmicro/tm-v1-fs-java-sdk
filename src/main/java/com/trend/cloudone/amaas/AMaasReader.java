package com.trend.cloudone.amaas;

import java.io.IOException;

/*
 * Interface for implementing a reader class to be scanned.
 */
interface AMaasReader {
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

}
