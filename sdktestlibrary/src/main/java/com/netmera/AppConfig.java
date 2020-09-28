package com.netmera;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class AppConfig extends BaseModel {
    @SerializedName("v")
    private int version = 0;
    @SerializedName("htmlTemps")
    private Map<String, String> htmlTemplates = Collections.emptyMap();
    @SerializedName("locHist")
    private boolean locationHistoryEnabled = false;
    @SerializedName("sei")
    private int sessionExpirationInterval = 180;
    @SerializedName("cei")
    private int cacheExpirationInterval = 604800;
    @SerializedName("url")
    private String baseUrl;
    @SerializedName("sai")
    private boolean sendAddId = false;
    @SerializedName("ppi")
    private List<String> privateProfileInfoList;
    @SerializedName("scd")
    private boolean skipChannelDelete;
    @SerializedName("bte")
    private boolean batteryLevelTrackEnabled;

    AppConfig() {
    }

    int getVersion() {
        return this.version;
    }

    Map<String, String> getHtmlTemplates() {
        return this.htmlTemplates;
    }

    boolean isLocationHistoryEnabled() {
        return this.locationHistoryEnabled;
    }

    int getSessionExpirationInterval() {
        return this.sessionExpirationInterval;
    }

    int getCacheExpirationInterval() {
        return this.cacheExpirationInterval;
    }

    String getBaseUrl() {
        return this.baseUrl;
    }

    boolean isSendAddId() {
        return this.sendAddId;
    }

    List<String> getPrivateProfileInfoList() {
        return this.privateProfileInfoList;
    }

    boolean shouldSkipChannelDelete() {
        return this.skipChannelDelete;
    }

    boolean shouldTrackBatteryLevel() {
        return this.batteryLevelTrackEnabled;
    }
}