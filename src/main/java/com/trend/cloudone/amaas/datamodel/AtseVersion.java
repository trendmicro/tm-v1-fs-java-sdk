package com.trend.cloudone.amaas.datamodel;

/**
 * Data object for ATSE engine and pattern versions.
 */
public class AtseVersion {
    private String engine;
    private int lptvpn;
    private int ssaptn;
    private int tmblack;
    private int tmwhite;
    private int macvpn;

    /**
     * Get the ATSE engine version.
     * @return version of ATSE engine.
     */
    public String getEngine() {
        return engine;
    }

    /**
     * Set the ATSE engine version.
     * @param engine ATSE engine version
     */
    public void setEngine(final String engine) {
        this.engine = engine;
    }

    /**
     * Get LPTVPN pattern version.
     * @return LPTVPN pattern version.
     */
    public int getLptvpn() {
        return lptvpn;
    }

    /**
     * Set LPTVPN pattern version.
     * @param lptvpn LPTVPN pattern version
     */
    public void setLptvpn(final int lptvpn) {
        this.lptvpn = lptvpn;
    }

    /**
     * Get SSA pattern version.
     * @return SSA pattern version
     */
    public int getSsaptn() {
        return ssaptn;
    }

    /**
     * Set SSA pattern version.
     * @param ssaptn SSA pattern version
     */
    public void setSsaptn(final int ssaptn) {
        this.ssaptn = ssaptn;
    }

    /**
     * Get TMblack pattern version.
     * @return TMblack pattern version
     */
    public int getTmblack() {
        return tmblack;
    }

    /**
     * Set TMBlack pattern version.
     * @param tmblack TMBlack pattern version
     */
    public void setTmblack(final int tmblack) {
        this.tmblack = tmblack;
    }

    /**
     * Get TMWhite pattern version.
     * @return TMWhite pattern version
     */
    public int getTmwhite() {
        return tmwhite;
    }

    /**
     * Set TMWhite pattern version.
     * @param tmwhite TMWhite pattern version
     */
    public void setTmwhite(final int tmwhite) {
        this.tmwhite = tmwhite;
    }

    /**
     * Get MACVPN pattern version.
     * @return MACVPN pattern version
     */
    public int getMacvpn() {
        return macvpn;
    }

    /**
     * Set MACVPN pattern version.
     * @param macvpn MACVPN pattern version
     */
    public void setMacvpn(final int macvpn) {
        this.macvpn = macvpn;
    }
}
