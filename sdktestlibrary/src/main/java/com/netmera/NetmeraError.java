package com.netmera;

public final class NetmeraError extends Exception {
    public static final int ERROR_CODE_CANCEL = -2;
    public static final int ERROR_CODE_NO_CONNECTION = -1;
    public static final int ERROR_GENERAL = 0;
    public static final int ERROR_SERVER = 3;
    public static final int ERROR_SERIALIZATION_FAILED = 9998;
    public static final int ERROR_API_KEY_DOES_NOT_EXISTS = 9997;
    public static final int ERROR_INBOX_DOES_NOT_HAVE_NEXT_PAGE = 9996;
    public static final int ERROR_INVALID_PARAMETERS = 9995;
    public static final int ERROR_INVALID_RESPONSE = 9994;
    private int errorCode;
    private int statusCode;
    private byte[] data;

    private NetmeraError(String detailMessage) {
        super(detailMessage);
    }

    private NetmeraError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    private NetmeraError(Throwable throwable) {
        super(throwable);
    }

    public String toString() {
        return "NetmeraError{errorCode=" + this.errorCode + ", statusCode=" + this.statusCode + ", data=" + (this.data != null ? new String(this.data) : "") + ", cause=" + (this.getCause() != null ? this.getCause().toString() : "") + ", message=" + this.getMessage() + '}';
    }

    public static NetmeraError noApiKeyInstance() {
        NetmeraError error = new NetmeraError("There is no API Key that is set. You should give your API key to SDK via Netmera.setApiKey method.");
        error.errorCode = 9997;
        return error;
    }

    public static NetmeraError responseSerializationFailedInstance(Throwable throwable) {
        NetmeraError error = new NetmeraError("Error during converting response to class.", throwable);
        error.errorCode = 9998;
        return error;
    }

    public static NetmeraError invalidResponseInstance() {
        NetmeraError error = new NetmeraError("A non-jsonObject response received from server.");
        error.errorCode = 9994;
        return error;
    }

    public static NetmeraError requestSerializationFailedInstance(Throwable throwable) {
        NetmeraError error = new NetmeraError("Error during converting request to jsonObject.", throwable);
        error.errorCode = 9998;
        return error;
    }

    public static NetmeraError inboxDoesNotHaveNextPageInstance() {
        NetmeraError error = new NetmeraError("Inbox does not have more pages. You can check this via hasNextPage property.");
        error.errorCode = 9996;
        return error;
    }

    public static NetmeraError invalidParametersInstance() {
        NetmeraError error = new NetmeraError("Given parameters are invalid.");
        error.errorCode = 9995;
        return error;
    }

    public static NetmeraError noConnectionInstance() {
        NetmeraError error = new NetmeraError("No network connection.");
        error.errorCode = -1;
        return error;
    }

    public static NetmeraError generalInstance() {
        return generalInstance("An error occurred.");
    }

    public static NetmeraError generalInstance(String errorMessage) {
        NetmeraError error = new NetmeraError(errorMessage);
        error.errorCode = 0;
        return error;
    }

    public static NetmeraError serverErrorInstance(String errorMessage, int statusCode, byte[] data) {
        NetmeraError error = new NetmeraError(errorMessage);
        error.errorCode = 3;
        error.statusCode = statusCode;
        error.data = data;
        return error;
    }

    public static NetmeraError cancelInstance() {
        NetmeraError error = new NetmeraError("Request has been cancelled.");
        error.errorCode = -2;
        return error;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public byte[] getData() {
        return this.data;
    }
}
