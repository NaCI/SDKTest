package com.netmera;

import android.text.TextUtils;
import androidx.annotation.VisibleForTesting;
import com.google.gson.annotations.SerializedName;
import com.netmera.internal.Optional;

class Identifiers {
    @SerializedName("iid")
    private String installationId;
    @SerializedName("sid")
    private String sessionId;
    @SerializedName("did")
    private String deviceId;
    @SerializedName("uid")
    private String userId;
    @SerializedName("xid")
    private Optional<String> externalId;
    @SerializedName("pid")
    private Optional<String> pushId;
    @SerializedName("piid")
    private Optional<String> pushInstanceId;

    Identifiers() {
    }

    Identifiers(Identifiers identifiers) {
        this.installationId = identifiers.installationId;
        this.sessionId = identifiers.sessionId;
        this.deviceId = identifiers.deviceId;
        this.userId = identifiers.userId;
        this.externalId = identifiers.externalId;
        this.pushId = identifiers.pushId;
        this.pushInstanceId = identifiers.pushInstanceId;
    }

    boolean isEmpty() {
        return this.installationId == null && this.deviceId == null && this.userId == null;
    }

    void removePropertiesSameWith(Identifiers identifiers) {
        if (identifiers != null) {
            if (TextUtils.equals(this.installationId, identifiers.installationId)) {
                this.installationId = null;
            }

            if (TextUtils.equals(this.sessionId, identifiers.sessionId)) {
                this.sessionId = null;
            }

            if (TextUtils.equals(this.deviceId, identifiers.deviceId)) {
                this.deviceId = null;
            }

            if (TextUtils.equals(this.userId, identifiers.userId)) {
                this.userId = null;
            }

            if (this.externalId != null && identifiers.externalId != null && (this.externalId.isPresent() && identifiers.externalId.isPresent() && TextUtils.equals((CharSequence)this.externalId.get(), (CharSequence)identifiers.externalId.get()) || !this.externalId.isPresent() && !identifiers.externalId.isPresent())) {
                this.externalId = null;
            }

            if (this.pushId != null && identifiers.pushId != null && (this.pushId.isPresent() && identifiers.pushId.isPresent() && TextUtils.equals((CharSequence)this.pushId.get(), (CharSequence)identifiers.pushId.get()) || !this.pushId.isPresent() && !identifiers.pushId.isPresent())) {
                this.pushId = null;
            }

        }
    }

    void setInstallationId(String installationId) {
        this.installationId = installationId;
    }

    void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    void setUserId(String userId) {
        this.userId = userId;
    }

    void setExternalId(Optional<String> externalId) {
        this.externalId = externalId;
    }

    void setPushId(String pushId) {
        this.pushId = pushId != null ? Optional.of(pushId) : null;
    }

    void setPushInstanceId(String pushInstanceId) {
        this.pushInstanceId = pushInstanceId != null ? Optional.of(pushInstanceId) : null;
    }

    String getSessionId() {
        return this.sessionId;
    }

    Optional<String> getExternalId() {
        return this.externalId;
    }

    @VisibleForTesting
    String getUserId() {
        return this.userId;
    }

    @VisibleForTesting
    String getPushId() {
        return this.pushId != null && this.pushId.isPresent() ? (String)this.pushId.get() : null;
    }

    String getPushInstanceId() {
        return this.pushInstanceId != null && this.pushInstanceId.isPresent() ? (String)this.pushInstanceId.get() : null;
    }
}