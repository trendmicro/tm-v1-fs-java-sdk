package com.trend.cloudone.amaas;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * An file type implementation of the AMaasRedaer interface.
 */
final class AMaasFileReader implements AMaasReader {
    private static final Logger logger = Logger.getLogger(AMaasClient.class.getName());
    private RandomAccessFile randomFile;
    private String fileName;
    private long fileSize;

    AMaasFileReader (String fileName) throws AMaasException {
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
            this.randomFile = new RandomAccessFile(fileName, "r");
        } catch (FileNotFoundException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NOT_FOUND, this.fileName);
        } catch (SecurityException err) {
            throw new AMaasException(AMaasErrorCode.MSG_ID_ERR_FILE_NO_PERMISSION, this.fileName);
        }
    }

    @Override
    protected void finalize() {
        try {
            this.randomFile.close();
        } catch (IOException err) {
            logger.log(Level.INFO,"unexpected exception {0}", err);      
        }
    }

    public String getIdentifier() {
        return this.fileName;
    }

    public long getLength() {
        return this.fileSize;
    }

    public int readBytes(int offset, byte[] buff) throws IOException {
        this.randomFile.seek(offset);
        return this.randomFile.read(buff);
    }
}
