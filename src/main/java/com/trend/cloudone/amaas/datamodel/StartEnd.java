package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for start time and end time of a scan.
 */
public class StartEnd {
    private String start;
    private String end;

    /**
     * Get start time in ISO 8601 format.
     * @return start time in ISO 8601 format
     */
    public String getStart() {
        return start;
    }

    /**
     * Set start time in ISO 8601 format.
     * @param start start time in ISO 8601 format
     */
    public void setStart(final String start) {
        this.start = start;
    }

    /**
     * Get end time in ISO 8601 format.
     * @return end time in ISO 8601 format
     */
    public String getEnd() {
        return end;
    }

    /**
     * Set end time in ISO 8601 format.
     * @param end end time in ISO 8601 format
     */
    public void setEnd(final String end) {
        this.end = end;
    }
}
