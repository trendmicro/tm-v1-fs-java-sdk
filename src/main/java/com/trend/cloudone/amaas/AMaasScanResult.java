package com.trend.cloudone.amaas;

/**
 * Data object for converting AMaaS json scan result returned by AMaaS scanning APIs.
 */
public class AMaasScanResult {
    private String version;
    private int scanResult;
    private String scanId;
    private String scanTimestamp;
    private String fileName;
    private MalwareItem[] foundMalwares;

    /**
     * Constructor for an AMaasScanResult object.
     * @param version schema version number of a json scan result
     * @param scanResult scan result. 0 for fail and 1 for success
     * @param scanId identifier to identify a scan session
     * @param scanTimestamp timestamp a scan was performed
     * @param fileName identifier of a file
     * @param foundMalwares an array of found malwares
     */
    public AMaasScanResult(final String version, final int scanResult, final String scanId, final String scanTimestamp, final String fileName,
            final MalwareItem[] foundMalwares) {
        this.version = version;
        this.scanResult = scanResult;
        this.scanId = scanId;
        this.scanTimestamp = scanTimestamp;
        this.fileName = fileName;
        this.foundMalwares = foundMalwares;
    }

    /**
     * Get schema version number of a json scan result.
     * @return schema version number
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get a scan result.
     * @return 0 for fail and 1 for success
     */
    public int getScanResult() {
        return scanResult;
    }

    /**
     * Get the Id to identify a scan.
     * @return scan id
     */
    public String getScanId() {
        return scanId;
    }

    /**
     * Get timestamp when a scan was performed.
     * @return scanned timestamp
     */
    public String getScanTimestamp() {
        return scanTimestamp;
    }

    /**
     * Get the identifier of a file.
     * @return file identifier
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Get an array of found malwares.
     * @return an array of MalwareItem objects.
     */
    public MalwareItem[] getFoundMalwares() {
        return foundMalwares;
    }

    /**
     * Set schema version number of a json scan result.
     * @param version AMaaS scan result json schema version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Set a scan result.
     * @param scanResult scan outcome to set. 0 for fail and 1 for success.
     */
    public void setScanResult(final int scanResult) {
        this.scanResult = scanResult;
    }

    /**
     * Set the Id to identify a scan.
     * @param scanId scanId to set
     */
    public void setScanId(final String scanId) {
        this.scanId = scanId;
    }

    /**
     * Set timestamp a scan was performed.
     * @param scanTimestamp timestamp to set
     */
    public void setScanTimestamp(final String scanTimestamp) {
        this.scanTimestamp = scanTimestamp;
    }

    /**
     * Set the identifier of a file.
     * @param fileName file identifier to set
     */
    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    /**
     * Set an array of found MalwareItem objects.
     * @param foundMalwares array of MalwareItem to set
     */
    public void setFoundMalwares(final MalwareItem[] foundMalwares) {
        this.foundMalwares = foundMalwares;
    }
}
