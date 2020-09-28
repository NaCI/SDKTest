package com.netmera;

interface NetworkAdapter {
    boolean isProcessingRequests();

    void startProcessingRequests();

    void stopProcessingRequests();

    void sendRequest(RequestSpec var1, NetworkCallback var2);

    void cancelAllRequests();
}
