package com.netmera;

import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION;
import com.google.gson.annotations.SerializedName;
import java.util.Locale;

class AppDeviceInfo extends BaseModel {
    @SerializedName("osV")
    private String osVersion;
    @SerializedName("appV")
    private String appVersion;
    @SerializedName("oper")
    private String operatorName;
    @SerializedName("lang")
    private String locale;
    @SerializedName("mnf")
    private String manufacturer;
    @SerializedName("mdl")
    private String deviceModel;
    @SerializedName("psV")
    private Integer playServicesVersion;

    static AppDeviceInfo newInstance(Context context, String applicationVersion, String operatorName) {
        AppDeviceInfo appDeviceInfo = new AppDeviceInfo();
        appDeviceInfo.setOsVersion(VERSION.RELEASE);
        appDeviceInfo.setAppVersion(applicationVersion);
        appDeviceInfo.setOperatorName(operatorName);
        appDeviceInfo.setLocale(Locale.getDefault().toString());
        appDeviceInfo.setManufacturer(Build.MANUFACTURER);
        appDeviceInfo.setDeviceModel(Build.MODEL);
        appDeviceInfo.setPlayServicesVersion(40001300);
        return appDeviceInfo;
    }

    AppDeviceInfo() {
    }

    String getOsVersion() {
        return this.osVersion;
    }

    String getAppVersion() {
        return this.appVersion;
    }

    String getOperatorName() {
        return this.operatorName;
    }

    String getLocale() {
        return this.locale;
    }

    String getManufacturer() {
        return this.manufacturer;
    }

    String getDeviceModel() {
        return this.deviceModel;
    }

    Integer getPlayServicesVersion() {
        return this.playServicesVersion;
    }

    void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    void setLocale(String locale) {
        this.locale = locale;
    }

    void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    void setPlayServicesVersion(Integer playServicesVersion) {
        this.playServicesVersion = playServicesVersion;
    }
}
