package com.netmera;

interface ResponseCallback<T extends ResponseBase> {
    void onResponse(T var1, NetmeraError var2);
}