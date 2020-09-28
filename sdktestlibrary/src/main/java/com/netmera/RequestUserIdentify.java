package com.netmera;

class RequestUserIdentify extends RequestUserUpdate {
    RequestUserIdentify(NetmeraUser netmeraUser) {
        super(netmeraUser);
    }

    protected String path() {
        return "/user/identify";
    }

    public Class<? extends ResponseBase> getResponseClass() {
        return ResponseUserIdentify.class;
    }
}
