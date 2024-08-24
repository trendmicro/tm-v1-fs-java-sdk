package com.trend.cloudone.amaas;

/*
 * The abstract base class implements the hash methods of the AMaasReader interface.
 */
public abstract class AMaasBaseReader implements AMaasReader {
    private byte[] sha1;
    private byte[] sha256;

    /*
     * Method to convert a bytes array to Hex string.
     * @param bytes the given byte array to convert.
     * @return the converted hex string.
     */
    private static String bytesToHex(final byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public final String getHash(final HashType htype) {
        switch (htype) {
            case HASH_SHA1:
                return (this.sha1 == null) ? "" : "sha1:" + bytesToHex(this.sha1);
            case HASH_SHA256:
                return (this.sha256 == null) ? "" : "sha256:" + bytesToHex(this.sha256);
            default:
                return "";
        }
    }

    /*
     * Method to set the reader with a hash of the given type.
     * @param htype the type of the hash value.
     * @param hash the value of the hash to set.
     */
    protected final void setHash(final HashType htype, final byte[] hash) {
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
