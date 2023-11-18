package com.trend.cloudone.amaas;

/*
 * The abstract base class implements the hash methods of the AMaasReader interface.
 */
public abstract class AMaasBaseReader implements AMaasReader {
    byte[] sha1;
    byte[] sha256;

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public String getHash(HashType htype) {
        switch (htype) {
            case HASH_SHA1:
                return "sha1:" + bytesToHex(this.sha1);
            case HASH_SHA256:
                return "sha256:" + bytesToHex(this.sha256);
            default:
                return null;
        }
    }
}
