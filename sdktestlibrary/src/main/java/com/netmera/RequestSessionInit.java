package com.netmera;

import com.google.gson.annotations.SerializedName;

class RequestSessionInit extends RequestBase {
    @SerializedName("cfgV")
    private int appConfigVersion;
    @SerializedName("tz")
    private int timeZone;
    @SerializedName("ts")
    private long timeStamp;
    @SerializedName("info")
    private AppDeviceInfo appDeviceInfo;

    RequestSessionInit(int appConfigVersion, int timeZone, long timeStamp, AppDeviceInfo appDeviceInfo) {
        super(3);
        this.appConfigVersion = appConfigVersion;
        this.timeZone = timeZone;
        this.timeStamp = timeStamp;
        this.appDeviceInfo = appDeviceInfo;
    }

    protected String path() {
        return "/session/init";
    }

    public Class<? extends ResponseBase> getResponseClass() {
        return ResponseSessionInit.class;
    }
}
