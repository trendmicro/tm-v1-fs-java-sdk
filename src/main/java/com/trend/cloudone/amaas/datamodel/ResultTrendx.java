package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for TrendX scan result.
 */
public class ResultTrendx {
    private long elapseTime;
    private TrendxVersion version;
    private int malwareCount;
    private Malware[] malware;
    private Err[] error;
    private boolean grid;
    private int fileType;
    private String fileTypeName;
    private int fileSubType;
    private String fileSubTypeName;

    /**
     * Get the elapse time for a scan.
     * @return elapse time in milliseconds
     */
    public long getElapseTime() {
        return elapseTime;
    }

    /**
     * Set the elapse time for a scan.
     * @param elapseTime elapse time in milliseconds
     */
    public void setElapseTime(final long elapseTime) {
        this.elapseTime = elapseTime;
    }

    /**
     * Get TrendX version object.
     * @return TrendX version object
     */
    public TrendxVersion getVersion() {
        return version;
    }

    /**
     * Set TrendX version object.
     * @param version TrendX version object
     */
    public void setVersion(final TrendxVersion version) {
        this.version = version;
    }

    /**
     * Get count for detected malware.
     * @return count for detected malware
     */
    public int getMalwareCount() {
        return malwareCount;
    }

    /**
     * Set count for detected malware.
     * @param malwareCount count for detected malware
     */
    public void setMalwareCount(final int malwareCount) {
        this.malwareCount = malwareCount;
    }

    /**
     * Get list of malware detected.
     * @return list of detected malware objects
     */
    public Malware[] getMalware() {
        return malware;
    }

    /**
     * Set list of malware detected.
     * @param malware list of detected malware objects
     */
    public void setMalware(final Malware[] malware) {
        this.malware = malware;
    }

    /**
     * Get list of scan errors.
     * @return list of scan errors
     */
    public Err[] getError() {
        return error;
    }

    /**
     * Set list of scan errors.
     * @param error list of scan errors
     */
    public void setError(final Err[] error) {
        this.error = error;
    }

    /**
     * Is TrendX grid enable?
     * @return whether TrendX grid enabled
     */
    public boolean isGrid() {
        return grid;
    }

    /**
     * Set whether TrendX grid is enabled.
     * @param grid whether TrendX grid enabled
     */
    public void setGrid(final boolean grid) {
        this.grid = grid;
    }

    /**
     * Get file type of the scanned file.
     * @return file type of the scanned file
     */
    public int getFileType() {
        return fileType;
    }

    /**
     * Set file type of the scanned file.
     * @param fileType file type of the scanned file
     */
    public void setFileType(final int fileType) {
        this.fileType = fileType;
    }

    /**
     * Get name of the file type.
     * @return name of the file type
     */
    public String getFileTypeName() {
        return fileTypeName;
    }

    /**
     * Set name of the file type.
     * @param fileTypeName name of the file type
     */
    public void setFileTypeName(final String fileTypeName) {
        this.fileTypeName = fileTypeName;
    }

    /**
     * Get sub type of the scanned file.
     * @return sub type of the scanned file
     */
    public int getFileSubType() {
        return fileSubType;
    }

    /**
     * Set sub type of the scanned file.
     * @param fileSubType sub type of the scanned file
     */
    public void setFileSubType(final int fileSubType) {
        this.fileSubType = fileSubType;
    }

    /**
     * Get name of the file sub type.
     * @return name of the file sub type
     */
    public String getFileSubTypeName() {
        return fileSubTypeName;
    }

    /**
     * Set name of the file sub type.
     * @param fileSubTypeName name of the file sub type
     */
    public void setFileSubTypeName(final String fileSubTypeName) {
        this.fileSubTypeName = fileSubTypeName;
    }
}
