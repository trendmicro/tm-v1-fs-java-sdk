package com.trend.cloudone.amaas;

/*
 * The abstract base class implements the hash methods of the AMaasReader interface.
 */
public abstract class AMaasBaseReader implements AMaasReader {
    private byte[] sha1;
    private byte[] sha256;

    public static String bytesToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public final String getHash(final HashType htype) {
        switch (htype) {
            case HASH_SHA1:
                return "sha1:" + bytesToHex(this.sha1);
            case HASH_SHA256:
                return "sha256:" + bytesToHex(this.sha256);
            default:
                return null;
        }
    }

    public final void setHash(final HashType htype, final byte[] hash) {
        switch (htype) {
            case HASH_SHA1:
                this.sha1 = hash;
            case HASH_SHA256:
                this.sha256 = hash;
            default:
                break;
        }
    }
}
