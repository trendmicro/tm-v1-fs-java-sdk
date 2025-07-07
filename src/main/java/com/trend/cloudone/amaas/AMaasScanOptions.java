package com.trend.cloudone.amaas;

/**
 * Scan options class to encapsulate scan configuration parameters.
 * This class provides a convenient way to group scan-related boolean flags together.
 */
public final class AMaasScanOptions {
    private final boolean pml;
    private final boolean feedback;
    private final boolean verbose;
    private final boolean activeContent;
    private final String[] tagList;

    /**
     * Private constructor - can only be called by the builder.
     *
     * @param pml flag to indicate whether to use predictive machine learning detection.
     * @param feedback flag to indicate whether to use Trend Micro Smart Protection Network's Smart Feedback.
     * @param verbose flag to enable log verbose mode
     * @param activeContent flag to enable active content scanning.
     * @param tagList List of tags to be used for the scan.
     */
    private AMaasScanOptions(final boolean pml, final boolean feedback, final boolean verbose, final boolean activeContent, final String[] tagList) {
        this.pml = pml;
        this.feedback = feedback;
        this.verbose = verbose;
        this.activeContent = activeContent;
        this.tagList = tagList != null ? tagList.clone() : null;
    }

    /**
     * Create a builder for constructing ScanOptions with fluent API.
     *
     * @return new ScanOptionsBuilder instance
     */
    public static ScanOptionsBuilder builder() {
        return new ScanOptionsBuilder();
    }

    /**
     * Get the predictive machine learning detection flag.
     *
     * @return true if PML detection is enabled, false otherwise.
     */
    public boolean isPml() {
        return pml;
    }

    /**
     * Get the Smart Feedback flag.
     *
     * @return true if Smart Feedback is enabled, false otherwise.
     */
    public boolean isFeedback() {
        return feedback;
    }

    /**
     * Get the verbose mode flag.
     *
     * @return true if verbose mode is enabled, false otherwise.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Get the active content scanning flag.
     *
     * @return true if active content scanning is enabled, false otherwise.
     */
    public boolean isActiveContent() {
        return activeContent;
    }

    /**
     * Get the list of tags.
     *
     * @return array of tags, or null if no tags are set
     */
    public String[] getTagList() {
        return tagList;
    }

    /**
     * Builder class for creating ScanOptions instances with fluent API.
     */
    public static final class ScanOptionsBuilder {
        private boolean pml = false;
        private boolean feedback = false;
        private boolean verbose = false;
        private boolean activeContent = false;
        private String[] tagList = null;

        private ScanOptionsBuilder() {
        }

        /**
         * Enable or disable predictive machine learning detection.
         *
         * @param pml true to enable PML, false to disable
         * @return this builder instance for method chaining
         */
        public ScanOptionsBuilder pml(final boolean pml) {
            this.pml = pml;
            return this;
        }

        /**
         * Enable or disable Smart Feedback.
         *
         * @param feedback true to enable Smart Feedback, false to disable
         * @return this builder instance for method chaining
         */
        public ScanOptionsBuilder feedback(final boolean feedback) {
            this.feedback = feedback;
            return this;
        }

        /**
         * Enable or disable verbose mode.
         *
         * @param verbose true to enable verbose mode, false to disable
         * @return this builder instance for method chaining
         */
        public ScanOptionsBuilder verbose(final boolean verbose) {
            this.verbose = verbose;
            return this;
        }

        /**
         * Enable or disable active content scanning.
         *
         * @param activeContent true to enable active content scanning, false to disable
         * @return this builder instance for method chaining
         */
        public ScanOptionsBuilder activeContent(final boolean activeContent) {
            this.activeContent = activeContent;
            return this;
        }

        /**
         * Set the list of tags for the scan.
         *
         * @param tagList array of tags to be used for the scan
         * @return this builder instance for method chaining
         */
        public ScanOptionsBuilder tagList(final String[] tagList) {
            this.tagList = tagList != null ? tagList.clone() : null;
            return this;
        }

        /**
         * Build the ScanOptions instance.
         *
         * @return new ScanOptions instance with configured values
         */
        public AMaasScanOptions build() {
            return new AMaasScanOptions(pml, feedback, verbose, activeContent, tagList);
        }
    }
}
