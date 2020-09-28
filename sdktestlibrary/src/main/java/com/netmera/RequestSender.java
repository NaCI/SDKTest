package com.netmera;

import android.text.TextUtils;
import com.netmera.internal.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

class RequestSender implements EventSender {
    private final StateManager stateManager;
    private final NetworkRequester networkRequester;

    RequestSender(StateManager stateManager, NetworkRequester networkRequester) {
        this.stateManager = stateManager;
        this.networkRequester = networkRequester;
    }

    public <T extends NetmeraEvent> void sendRequestEvent(T netmeraEvent) {
        if (netmeraEvent == null) {
            Netmera.logger().d("Netmera cannot send given event.\nCause: NetmeraEvent cannot be null.", new Object[0]);
        } else {
            netmeraEvent.setCreationTimeStamp(System.currentTimeMillis());
            if (this.stateManager.getAppConfig().isLocationHistoryEnabled()) {
                String lastGeoLocation = this.stateManager.getLastGeoLocation();
                if (!TextUtils.isEmpty(lastGeoLocation)) {
                    netmeraEvent.setGeoLocation(lastGeoLocation);
                }
            }

            RequestBase request = new RequestEvent(Collections.singletonList(netmeraEvent));
            this.networkRequester.sendRequest(request);
        }
    }

    void sendRequestInitSession() {
        long currentTimeMillis = System.currentTimeMillis();
        int tzSeconds = (new GregorianCalendar()).getTimeZone().getOffset(currentTimeMillis) / 1000;
        RequestSessionInit request = new RequestSessionInit(this.stateManager.getAppConfigVersion(), tzSeconds, currentTimeMillis, this.stateManager.createAppDeviceInfo());
        this.networkRequester.sendRequest(request);
    }

    void sendRequestUpdateUser(NetmeraUser netmeraUser) {
        Optional<String> externalId = netmeraUser.getUserId();
        Optional<String> currentExternalId = this.stateManager.getExternalId();
        if (externalId == null || currentExternalId != null && !externalId.isPresent() && !currentExternalId.isPresent() || currentExternalId != null && externalId.isPresent() && currentExternalId.isPresent() && TextUtils.equals((CharSequence)externalId.get(), (CharSequence)currentExternalId.get())) {
            this.networkRequester.sendRequest(new RequestUserUpdate(netmeraUser));
        } else {
            this.stateManager.updateExternalId(externalId);
            this.networkRequester.sendRequest(new RequestUserIdentify(netmeraUser));
            if (!externalId.isPresent()) {
                this.stateManager.updateExternalId((Optional)null);
            }
        }

    }

    /*void sendRequestPushEnable(int source) {
        RequestBase request = new RequestPushEnable(source);
        this.networkRequester.sendRequest(request);
    }

    void sendRequestPushDisable(int source) {
        RequestBase request = new RequestPushDisable(source);
        this.networkRequester.sendRequest(request);
    }

    void sendRequestPushRegister(String token) {
        RequestBase request = new RequestPushRegister(token);
        this.networkRequester.sendRequest(request);
    }

    void sendRequestAppConfig() {
        RequestBase request = new RequestAppConfig();
        this.networkRequester.sendRequest(request);
    }

    void sendRequestInboxFetch(NetmeraInboxFilter filter, ResponseCallback callback) {
        RequestBase request = new RequestInboxFetch(filter);
        this.networkRequester.sendRequest(request, callback);
    }

    void sendRequestInboxCategoryCountFetch(NetmeraCategoryFilter filter, ResponseCallback callback) {
        RequestBase request = new RequestCategoryFetch(filter);
        this.networkRequester.sendRequest(request, callback);
    }

    void sendRequestInboxSetStatus(List<NetmeraPushObject> netmeraPushObjects, int inboxStatus, ResponseCallback callback) {
        int size = netmeraPushObjects.size();
        List<String> pushInstanceIds = new ArrayList(size);

        for(int i = 0; i < size; ++i) {
            pushInstanceIds.add(((NetmeraPushObject)netmeraPushObjects.get(i)).getPushInstanceId());
        }

        RequestBase request = new RequestInboxSetStatus(pushInstanceIds, inboxStatus);
        this.networkRequester.sendRequest(request, callback);
    }

    void sendRequestTestDeviceAdd(String params, ResponseCallback callback) {
        RequestTestDeviceAdd requestTestDeviceAdd = new RequestTestDeviceAdd(params);
        this.networkRequester.sendRequest(requestTestDeviceAdd, callback);
    }

    void sendRequestSendAdId(String adId) {
        RequestSendAdId request = new RequestSendAdId(adId);
        this.networkRequester.sendRequest(request);
    }

    void sendUpdatedAppTrackedList(Map<String, Boolean> updatedAppTrackedMap) {
        RequestUpdateAppTrackedList request = new RequestUpdateAppTrackedList(updatedAppTrackedMap);
        this.networkRequester.sendRequest(request);
    }*/
}