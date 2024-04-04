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

    AMaasBufferReader(final byte[] byteBuf, final String identifier) throws AMaasException {
        this.readerBuf = byteBuf;
        this.identifier = identifier;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            this.setHash(HashType.HASH_SHA1, md.digest(this.readerBuf));
            md = MessageDigest.getInstance("SHA-256");
            this.setHash(HashType.HASH_SHA256, md.digest(this.readerBuf));
        } catch (NoSuchAlgorithmException err) {
            // this exception is not possible as the algorithms are hard coded.
        }
    }

    public long getLength() {
        return this.readerBuf.length;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int readBytes(final int offset, final byte[] buf) throws IOException {
        int chunkLength = buf.length;
        if (chunkLength + offset > this.readerBuf.length) {
            chunkLength = this.readerBuf.length - offset;
        }
        System.arraycopy(readerBuf, offset, buf, 0, chunkLength);
        return chunkLength;
    }
}
