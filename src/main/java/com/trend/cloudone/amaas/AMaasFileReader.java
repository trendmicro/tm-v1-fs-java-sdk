package com.trend.cloudone.amaas;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * An file type implementation of the AMaasReader interface extends from the base implementation.
 */
class AMaasFileReader extends AMaasBaseReader {
    private static final Logger logger = Logger.getLogger(AMaasClient.class.getName());
    private RandomAccessFile randomFile;
    private String fileName;
    private long fileSize;

    private static final int ONE_KBYTE = 1024;

    AMaasFileReader(final String fileName, final boolean digest) throws AMaasException {
        try {
            this.fileName = fileName;
            File targetFile = new File(fileName);
            if (!targetFile.exists()) {
                throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, this.fileName);
            }
            if (!targetFile.canRead()) {
                throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, this.fileName);
            }
            this.fileSize = targetFile.length();
            if (digest) {
                MessageDigest md = MessageDigest.getInstance("SHA1");
                MessageDigest md256 = MessageDigest.getInstance("SHA-256");
                try (FileInputStream is = new FileInputStream(targetFile)) {
                    byte[] bytesBuffer = new byte[ONE_KBYTE];
                    int bytesRead = -1;

                    while ((bytesRead = is.read(bytesBuffer)) != -1) {
                        md.update(bytesBuffer, 0, bytesRead);
                        md256.update(bytesBuffer, 0, bytesRead);
                    }
                    this.setHash(HashType.HASH_SHA1, md.digest());
                    this.setHash(HashType.HASH_SHA256, md256.digest());
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }
            this.randomFile = new RandomAccessFile(fileName, "r");
        } catch (IOException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, this.fileName);
        } catch (SecurityException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, this.fileName);
        } catch (NoSuchAlgorithmException err) {
            // this exception is not possible as the algorithms are hard coded.
        }
    }

    @Override
    protected void finalize() {
        try {
            this.randomFile.close();
        } catch (IOException err) {
            logger.log(Level.INFO, "unexpected exception {0}", err);
        }
    }

    public String getIdentifier() {
        return this.fileName;
    }

    public long getLength() {
        return this.fileSize;
    }

    public int readBytes(final int offset, final byte[] buff) throws IOException {
        this.randomFile.seek(offset);
        return this.randomFile.read(buff);
    }
}
