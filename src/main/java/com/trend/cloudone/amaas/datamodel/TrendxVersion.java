package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for TrendX engine and pattern versions.
 */
public class TrendxVersion {
    private String engine;
    private int tmblack;
    private int trendx;

    /**
     * Get the TrendX engine version.
     * @return TrendX engine version
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Set the TrendX engine version.
     * @param engine TrendX engine version
     */
    public void setEngine(final String engine) {
        this.engine = engine;
    }

    /**
     * Get Tmblack pattern version.
     * @return Tmblack pattern version
     */
    public int getTmblack() {
        return tmblack;
    }

    /**
     * Set Tmblack pattern version.
     * @param tmblack Tmblack pattern version
     */
    public void setTmblack(final int tmblack) {
        this.tmblack = tmblack;
    }

    /**
     * Get TrendX model version.
     * @return TrendX model version.
     */
    public int getTrendx() {
        return trendx;
    }

    /**
     * Set TrendX model version.
     * @param trendx TrendX model version.
     */
    public void setTrendx(final int trendx) {
        this.trendx = trendx;
    }
}
