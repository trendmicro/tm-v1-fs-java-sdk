package com.trend.cloudone.amaas;

import java.io.IOException;

/*
 * A byte buffer type implementation of the AMaasReader interface.
 */
final class AMaasBufferReader implements AMaasReader {
    private String identifier;
    private byte[] readerBuf;

    AMaasBufferReader(byte[] byteBuf, String identifier) {
        this.readerBuf = byteBuf;
        this.identifier = identifier;
    }

    public long getLength() {
        return this.readerBuf.length;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int readBytes(int offset, byte[] buf) throws IOException {
        int chunkLength = buf.length;
        if (chunkLength + offset > this.readerBuf.length) {
            chunkLength = this.readerBuf.length - offset;
        }
        System.arraycopy(readerBuf, offset, buf, 0, chunkLength);
        return chunkLength;
    }
}
