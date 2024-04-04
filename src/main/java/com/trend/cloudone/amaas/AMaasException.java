
package com.trend.cloudone.amaas;

/**
 * AMaasException wraps all checked standard Java exceptions and gRpc exceptions.
 *
 */
public final class AMaasException extends Exception {
    private AMaasErrorCode erroCode;

    /**
     * Constructor for AMaasException using an error code and optional parameters.
     * @param erroCode error code to be reported.
     * @param params optional parameters used to format the template associated with the error code.
     */
    public AMaasException(final AMaasErrorCode erroCode, final Object... params) {
        super(erroCode.getMessage(params));
        this.erroCode = erroCode;
    }

    /**
     * Constructor for AMassException using an error code, optional parameters and the caught throwable object.
     * @param erroCode error code to be reported.
     * @param cause the caught thowable object.
     * @param params optional parameters used to format the template associated with the error code.
     */
    public AMaasException(final AMaasErrorCode erroCode, final Throwable cause, final Object... params) {
        super(erroCode.getMessage(params), cause);
        this.erroCode = erroCode;
    }

    /**
     * Method to retrieve the associated AMaasErrorCode for the exception.
     * @return AMaasErrorCode object of the exception.
     */
    public AMaasErrorCode getErrorCode() {
        return this.erroCode;
    }
}
