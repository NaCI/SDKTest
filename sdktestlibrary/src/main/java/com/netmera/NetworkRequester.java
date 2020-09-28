package com.netmera;

interface NetworkRequester {
    void sendRequest(RequestBase var1);

    void sendRequest(RequestBase var1, ResponseCallback var2);
}
