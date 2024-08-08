package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for converting AMaaS json scan result in verbose mode returned by AMaaS scanning APIs.
 */
public class AMaasScanResultVerbose {
    private String scanType;
    private String objectType;
    private StartEnd timestamp;
    private String schemaVersion;
    private String scannerVersion;
    private String fileName;
    private long rsSize;
    private String scanId;
    private String accountId;
    private ScanResult result;
    private String[] tags;
    private String fileSha1;
    private String fileSha256;
    private String appName;

    /**
     * Get scan type.
     * @return scan type
     */
    public String getScanType() {
        return scanType;
    }

    /**
     * Set scan type.
     * @param scanType scan type
     */
    public void setScanType(final String scanType) {
        this.scanType = scanType;
    }

    /**
     * Get object type such as file.
     * @return scanned object type
     */
    public String getObjectType() {
        return objectType;
    }

    /**
     * Set object type.
     * @param objectType object type.
     */
    public void setObjectType(final String objectType) {
        this.objectType = objectType;
    }

    /**
     * Get start and end time of the scan.
     * @return StartEnd object with start and end time string in ISO 8601 format.
     */
    public StartEnd getTimestamp() {
        return timestamp;
    }

    /**
     * Set start and end time of the scan.
     * @param timestamp StartEnd object with start and end time string in ISO 8601 format.
     */
    public void setTimestamp(final StartEnd timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the schema version.
     * @return schema version
     */
    public String getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Set the schema version.
     * @param schemaVersion schema version to set.
     */
    public void setSchemaVersion(final String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * Get the scanner version.
     * @return scanner version
     */
    public String getScannerVersion() {
        return scannerVersion;
    }

    /**
     * Set the scanner version.
     * @param scannerVersion version of the scanner.
     */
    public void setScannerVersion(final String scannerVersion) {
        this.scannerVersion = scannerVersion;
    }

    /**
     * Get the scanned file name.
     * @return file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * set the scanned file name.
     * @param fileName file name.
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * The size of the scanned file.
     * @return size of scanned file.
     */
    public long getRsSize() {
        return rsSize;
    }

    /**
     * Set the size of the scanned file.
     * @param rsSize size of scanned file.
     */
    public void setRsSize(final long rsSize) {
        this.rsSize = rsSize;
    }

    /**
     * Get the scanned id.
     * @return scanned id
     */
    public String getScanId() {
        return scanId;
    }

    /**
     * Set the scanned id.
     * @param scanId scanned id.
     */
    public void setScanId(final String scanId) {
        this.scanId = scanId;
    }

    /**
     * Get the account id.
     * @return account id.
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Set the account id.
     * @param accountId account id.
     */
    public void setAccountId(final String accountId) {
        this.accountId = accountId;
    }

    /**
     * Get the scan result.
     * @return scan result
     */
    public ScanResult getResult() {
        return result;
    }

    /**
     * Set the scan result.
     * @param result scan result.
     */
    public void setResult(final ScanResult result) {
        this.result = result;
    }

    /**
     * Get the tags used for the scan.
     * @return tags used for scan
     */
    public String[] getTags() {
        return tags;
    }

    /**
     * Set the tags used for the scan.
     * @param tags tags used for scan
     */
    public void setTags(final String[] tags) {
        this.tags = tags;
    }

    /**
     * Get SHA1 of the scanned file.
     * @return SHA1 of scanned file.
     */
    public String getFileSha1() {
        return fileSha1;
    }

    /**
     * Set the SHA1 of the scanned file.
     * @param fileSha1 SHA1 of scanned file.
     */
    public void setFileSha1(final String fileSha1) {
        this.fileSha1 = fileSha1;
    }

    /**
     * Get the SHA256 of the scanned file.
     * @return SHA256 of scanned file.
     */
    public String getFileSha256() {
        return fileSha256;
    }

    /**
     * Set the SHA256 of the scanned file.
     * @param fileSha256 SHA256 of scanned file.
     */
    public void setFileSha256(final String fileSha256) {
        this.fileSha256 = fileSha256;
    }

    /**
     * Get the app name.
     * @return app name.
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Set the app name.
     * @param appName app name to set.
     */
    public void setAppName(final String appName) {
        this.appName = appName;
    }
}
