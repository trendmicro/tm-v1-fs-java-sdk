package com.trend.cloudone.amaas;

/**
 * Enum type defines all the AMaaS error code and message templates.
 */
public enum AMaasErrorCode {
    /**
     * Scan file cannot be found.
     */
    MSG_ID_ERR_FILE_NOT_FOUND("MSG_ID_ERR_FILE_NOT_FOUND", "Failed to open file. No such file or directory %s."),
    
    /**
     * Have no permission to open the scan file.
     */
    MSG_ID_ERR_FILE_NO_PERMISSION("MSG_ID_ERR_FILE_NO_PERMISSION", "Failed to open file. Permission denied to open %s."),
    
    /**
     * Incorrect region is given.
     */
    MSG_ID_ERR_INVALID_REGION("MSG_ID_ERR_INVALID_REGION", "%s is not a supported region."),
    
    /**
    * Cloudone credetial is not given.
    */
    MSG_ID_ERR_MISSING_AUTH("MSG_ID_ERR_MISSING_AUTH","Must provide an API key to use the client."),
    
    /**
     * An GRPC error was reported.
     */
    MSG_ID_GRPC_ERROR("MSG_ID_GRPC_ERROR", "Received gRPC status code: %d, msg: %s."),
    
    /**
     * Authentication failed using the given Cloudone credential.
     */
    MSG_ID_ERR_KEY_AUTH_FAILED("MSG_ID_ERR_KEY_AUTH_FAILED", "Authorization key cannot be authenticated."),
    
    /**
     * Java SDK client received an unexpected interrupt.
     */
    MSG_ID_ERR_UNEXPECTED_INTERRUPT("MSG_ID_ERR_UNEXPECTED_INTERRUPT", "Unexpected interrupt encountered.");

    private final String errorCode;
    private final String message;

    /**
     * The constructor for an AMass error type.
     * @param errorCode error code to be used
     * @param message template to be used.
     */
    AMaasErrorCode(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * The method returns the code of the error enum.
     * @return return the error code.
     */
    public String getErrorCode() {
        return this.errorCode;
    }

    /**
     * This method returns a message for the error enum with no parameters.
     * @return return the message associated with the error enum.
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * This method returns a message prepared using the template with the given parameters.
     * @param params objects to be used to format the template.
     * @return message prepared from the templates using the parameters.
     */
    public String getMessage(Object... params) {
        return String.format(this.message, params);
    }
}
