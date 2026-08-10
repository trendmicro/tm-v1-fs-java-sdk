package com.trend.cloudone.amaas;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/*
 * A byte buffer type implementation of the AMaasReader interface extends from the base implementation.
 */
final class AMaasBufferReader extends AMaasBaseReader {
    private String identifier;
    private byte[] readerBuf;

    AMaasBufferReader(final byte[] byteBuf, final String identifier, final boolean digest) throws AMaasException {
        this.readerBuf = byteBuf;
        this.identifier = identifier;
        if (digest) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                this.setHash(HashType.HASH_SHA1, md.digest(this.readerBuf));
                md = MessageDigest.getInstance("SHA-256");
                this.setHash(HashType.HASH_SHA256, md.digest(this.readerBuf));
            } catch (NoSuchAlgorithmException err) {
                // this exception is not possible as the algorithms are hard coded.
            }
        }
    }

    public long getLength() {
        return this.readerBuf.length;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int readBytes(final long offset, final byte[] buf) throws IOException {
        // The in-memory buffer is backed by a Java array, which is indexed by int, so a buffer larger than Integer.MAX_VALUE bytes (~2GiB)
        // is not representable here. Casting to int is safe for this implementation; only the on-disk file reader (AMaasFileReader) needs
        // the full long range. In normal operation the scan engine never requests an offset beyond the declared buffer length,
        // so offset always fits in int.
        if (offset < 0 || offset > this.readerBuf.length) {
            throw new IOException("offset out of range for buffer reader: " + offset + " (buffer length " + this.readerBuf.length + ")");
        }
        int intOffset = (int) offset;
        int chunkLength = buf.length;
        if (chunkLength + intOffset > this.readerBuf.length) {
            chunkLength = this.readerBuf.length - intOffset;
        }
        System.arraycopy(readerBuf, intOffset, buf, 0, chunkLength);
        return chunkLength;
    }
}
