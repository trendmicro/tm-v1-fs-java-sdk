package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for scan errors.
 */
public class Err {
    private int code;
    private String message;

    /**
     * Get code of an scan error.
     * @return scan error code
     */
    public int getCode() {
        return code;
    }

    /**
     * Set code of an scan error.
     * @param code scan error code
     */
    public void setCode(final int code) {
        this.code = code;
    }

    /**
     * Get message of an scan error.
     * @return scan error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set message of an scan error.
     * @param message scan error message
     */
    public void setMessage(final String message) {
        this.message = message;
    }
}
