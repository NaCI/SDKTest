package com.netmera;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

class ResponseAppConfig extends ResponseBase {
    @SerializedName("cfg")
    private AppConfig appConfig;

    ResponseAppConfig() {
    }

    @Nullable
    AppConfig getAppConfig() {
        return this.appConfig;
    }
}