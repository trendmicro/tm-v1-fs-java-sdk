package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for scan result.
 */
public class ScanResult {
    private ResultATSE atse;
    private ResultTrendx trendx;

    /**
     * Get ATSE scan result.
     * @return ATSE scan result
     */
    public ResultATSE getAtse() {
        return atse;
    }

    /**
     * Set ATSE scan result.
     * @param atse ATSE scan result
     */
    public void setAtse(final ResultATSE atse) {
        this.atse = atse;
    }

    /**
     * Get TrendX scan result.
     * @return TrendX scan result
     */
    public ResultTrendx getTrendx() {
        return trendx;
    }

    /**
     * Set TrendX scan result.
     * @param trendx TrendX scan result
     */
    public void setTrendx(final ResultTrendx trendx) {
        this.trendx = trendx;
    }
}
