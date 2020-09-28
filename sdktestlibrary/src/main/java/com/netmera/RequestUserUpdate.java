package com.netmera;

import com.google.gson.annotations.SerializedName;
import com.netmera.internal.Optional;

class RequestUserUpdate extends RequestBase {
    @SerializedName("prfl")
    private NetmeraUser netmeraUser;
    @SerializedName("email")
    private Optional<String> email;
    @SerializedName("msisdn")
    private Optional<String> msisdn;

    RequestUserUpdate(NetmeraUser netmeraUser) {
        this.netmeraUser = netmeraUser;
        this.email = netmeraUser.getEmail();
        this.msisdn = netmeraUser.getMsisdn();
    }

    protected String path() {
        return "/user/update";
    }
}