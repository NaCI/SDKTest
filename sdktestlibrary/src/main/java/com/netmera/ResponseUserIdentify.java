package com.netmera;

import com.google.gson.annotations.SerializedName;

class ResponseUserIdentify extends ResponseBase {
    @SerializedName("uid")
    private String userId;

    ResponseUserIdentify() {
    }

    public String getUserId() {
        return this.userId;
    }
}