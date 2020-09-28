package com.netmera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.netmera.internal.Optional;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

class StateManager {
    private static final String KEY_IDENTIFIERS = "b";
    private static final String KEY_APP_CONFIG = "c";
    private static final String KEY_APP_DEVICE_INFO = "e";
    private static final String KEY_PUSH_SENDER_ID = "f";
    private static final String KEY_PUSH_TOKEN = "g";
    private static final String KEY_POPUP = "h";
    private static final String KEY_HAS_CONTROLLER_GEOFENCE = "i";
    private static final String KEY_NOTIFICATION_STATE_SYSTEM = "k";
    private static final String KEY_AD_ID = "l";
    private static final String KEY_DID = "m";
    private static final String KEY_NOTIFICATION_STATE_APP = "n";
    private static final String KEY_LAST_SHOWN_PUSH_ID = "o";
    private static final String KEY_IN_APP_MESSAGE = "p";
    private static final String KEY_APP_TRACKED = "r";
    private static final String KEY_ACTIVE_NOTIFICATIONS = "s";
    private static final String KEY_API_KEY = "t";
    private static final String KEY_ALARM_REQUEST_CODE = "u";
    private final Context context;
    private final Storage storage;
    private final Gson gson;
    private Identifiers identifiers;
    private AppConfig appConfig;
    private boolean appOnForeground;
    private boolean allowUIPresentation;
    private boolean optOutUserData;
    private boolean hideProgressbar;
    private String lastGeoLocation;
    private String applicationVersion;
    private String operatorName;
    private ContentResolver contentResolver;
    private Activity currentActivity;
    private Long timeForeground;
    private Long timeBackground;
    private Long pushIdSetTime;

    @SuppressLint("WrongConstant")
    StateManager(Context context, Storage storage, Gson gson) {
        this.context = context;
        this.storage = storage;
        this.gson = gson;

        try {
            this.applicationVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException var6) {
            this.applicationVersion = null;
        }

        try {
            this.operatorName = ((TelephonyManager) context.getSystemService("phone")).getNetworkOperatorName();
        } catch (Exception var5) {
            this.operatorName = null;
        }

        this.contentResolver = context.getContentResolver();
    }

    Identifiers getIdentifiers() {
        Identifiers identifiers = new Identifiers(this.createAndGetIdentifiers());
        if (!this.appOnForeground) {
            int sessionExpirationInterval = this.getAppConfig().getSessionExpirationInterval() * 1000;
            long currentTimeMillis = System.currentTimeMillis();
            if (this.timeBackground != null && currentTimeMillis - this.timeBackground >= (long) sessionExpirationInterval) {
                identifiers.setSessionId((String) null);
                if (this.pushIdSetTime != null && currentTimeMillis - this.pushIdSetTime >= (long) sessionExpirationInterval) {
                    identifiers.setPushId((String) null);
                    identifiers.setPushInstanceId((String) null);
                    this.pushIdSetTime = null;
                }
            }
        }

        return identifiers;
    }

    private Identifiers createAndGetIdentifiers() {
        if (this.identifiers == null) {
            String currentData = (String) this.storage.get("b");

            try {
                if (!TextUtils.isEmpty(currentData)) {
                    this.identifiers = (Identifiers) this.gson.fromJson(currentData, Identifiers.class);
                }
            } catch (Error var3) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
            }

            if (this.identifiers == null) {
                this.identifiers = new Identifiers();
                this.identifiers.setInstallationId(IdentifierUtil.generateIdentifier());
                this.identifiers.setUserId(IdentifierUtil.generateIdentifier());
            }

            String deviceId = Secure.getString(this.contentResolver, "android_id");
            if (TextUtils.isEmpty(deviceId) || TextUtils.equals(deviceId, "9774d56d682e549c")) {
                deviceId = (String) this.storage.get("m", (Object) null);
                if (TextUtils.isEmpty(deviceId)) {
                    deviceId = IdentifierUtil.generateIdentifier();
                    this.storage.put("m", deviceId);
                }
            }

            this.identifiers.setDeviceId(deviceId);
            this.identifiers.setSessionId((String) null);
            this.identifiers.setPushId((String) null);
            this.identifiers.setPushInstanceId((String) null);
            this.saveIdentifiers();
        }

        return this.identifiers;
    }

    AppDeviceInfo createAppDeviceInfo() {
        AppDeviceInfo currentInfo = AppDeviceInfo.newInstance(this.context, this.applicationVersion, this.operatorName);
        String storedData = (String) this.storage.get("e");
        AppDeviceInfo storedInfo = null;

        try {
            if (!TextUtils.isEmpty(storedData)) {
                storedInfo = (AppDeviceInfo) this.gson.fromJson(storedData, AppDeviceInfo.class);
            }
        } catch (Error var5) {
            Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
        }

        if (storedInfo == null) {
            this.saveAppDeviceInfo(currentInfo);
            return currentInfo;
        } else {
            if (TextUtils.equals(storedInfo.getOsVersion(), currentInfo.getOsVersion())) {
                currentInfo.setOsVersion((String) null);
            } else {
                storedInfo.setOsVersion(currentInfo.getOsVersion());
            }

            if (TextUtils.equals(storedInfo.getAppVersion(), currentInfo.getAppVersion())) {
                currentInfo.setAppVersion((String) null);
            } else {
                storedInfo.setAppVersion(currentInfo.getAppVersion());
            }

            if (TextUtils.equals(storedInfo.getOperatorName(), currentInfo.getOperatorName())) {
                currentInfo.setOperatorName((String) null);
            } else {
                storedInfo.setOperatorName(currentInfo.getOperatorName());
            }

            if (TextUtils.equals(storedInfo.getLocale(), currentInfo.getLocale())) {
                currentInfo.setLocale((String) null);
            } else {
                storedInfo.setLocale(currentInfo.getLocale());
            }

            if (TextUtils.equals(storedInfo.getManufacturer(), currentInfo.getManufacturer())) {
                currentInfo.setManufacturer((String) null);
            } else {
                storedInfo.setManufacturer(currentInfo.getManufacturer());
            }

            if (TextUtils.equals(storedInfo.getDeviceModel(), currentInfo.getDeviceModel())) {
                currentInfo.setDeviceModel((String) null);
            } else {
                storedInfo.setDeviceModel(currentInfo.getDeviceModel());
            }

            if (storedInfo.getPlayServicesVersion() != null && storedInfo.getPlayServicesVersion().equals(currentInfo.getPlayServicesVersion())) {
                currentInfo.setPlayServicesVersion((Integer) null);
            } else {
                storedInfo.setPlayServicesVersion(currentInfo.getPlayServicesVersion());
            }

            this.saveAppDeviceInfo(storedInfo);
            return currentInfo;
        }
    }

    boolean removeAppDeviceInfo() {
        return this.storage.remove("e");
    }

    Map<String, Boolean> getUpdatedTrackedAppList(ResponseSessionInit response) {
        /*if (response != null && response.getAppConfig() != null && response.getAppConfig().getAppTrackedList() != null) {
            this.putAppTrackedList(response.getAppConfig().getAppTrackedList());
        }*/

        Map<String, Boolean> updatedAppTrackedMap = new LinkedHashMap();
        /*List<AppTracked> storeAppTrackedList = this.getAppTrackedList();
        Iterator var4 = storeAppTrackedList.iterator();

        while(var4.hasNext()) {
            AppTracked appTracked = (AppTracked)var4.next();
            boolean state = NetmeraUtils.isApplicationInstalled(this.context, appTracked.getValue());
            if (appTracked.getIsInstalled() == null) {
                appTracked.setInstalled(state);
                updatedAppTrackedMap.put(appTracked.getId(), state);
            } else if (appTracked.getIsInstalled() != state) {
                appTracked.setInstalled(state);
                updatedAppTrackedMap.put(appTracked.getId(), state);
            }
        }

        this.putAppTrackedList(storeAppTrackedList);*/
        return updatedAppTrackedMap;
    }

    void updateExternalId(Optional<String> externalId) {
        this.createAndGetIdentifiers().setExternalId(externalId);
        this.saveIdentifiers();
    }

    Optional<String> getExternalId() {
        return this.createAndGetIdentifiers().getExternalId();
    }

    void updateUserId(String userId) {
        this.createAndGetIdentifiers().setUserId(userId);
        this.saveIdentifiers();
    }

    void updatePushIdAndPushInstanceId(String pushId, String pushInstanceId) {
        Identifiers identifiers = this.createAndGetIdentifiers();
        identifiers.setPushId(pushId);
        identifiers.setPushInstanceId(pushInstanceId);
        this.pushIdSetTime = System.currentTimeMillis();
    }

    String getPushInstanceId() {
        return this.createAndGetIdentifiers().getPushInstanceId();
    }

    boolean isAppOnForeground() {
        return this.appOnForeground;
    }

    boolean setAppForegroundAndCheckIfSessionUpdateRequired() {
        this.appOnForeground = true;
        this.timeForeground = System.currentTimeMillis();
        int sessionExpirationInterval = this.getAppConfig().getSessionExpirationInterval() * 1000;
        boolean shouldUpdateSession = this.timeBackground == null || this.timeForeground - this.timeBackground >= (long) sessionExpirationInterval;
        if (shouldUpdateSession) {
            this.createAndGetIdentifiers();
            this.identifiers.setSessionId(IdentifierUtil.generateIdentifier());
            if (this.pushIdSetTime != null && this.timeForeground - this.pushIdSetTime >= (long) sessionExpirationInterval) {
                this.identifiers.setPushId((String) null);
                this.identifiers.setPushInstanceId((String) null);
                this.pushIdSetTime = null;
            }
        }

        return shouldUpdateSession;
    }

    boolean shouldHideProgressbarForWebView() {
        return this.hideProgressbar;
    }

    void setShouldHideProgressbarForWebView(boolean visible) {
        this.hideProgressbar = !visible;
    }

    @Nullable
    Double setAppBackgroundAndGetTimeInAppValue() {
        this.appOnForeground = false;
        this.timeBackground = System.currentTimeMillis();
        return this.timeForeground == null ? null : (double) (this.timeBackground - this.timeForeground) / 1000.0D;
    }

    boolean isAllowUIPresentation() {
        return this.allowUIPresentation;
    }

    void setAllowUIPresentation(boolean allowUIPresentation) {
        this.allowUIPresentation = allowUIPresentation;
    }

    boolean isTurnedOffSendingEventAndUserRequest() {
        return this.optOutUserData;
    }

    void turnOffSendingEventAndUserUpdate(boolean turnOff) {
        this.optOutUserData = turnOff;
    }

    Activity getCurrentActivity() {
        return this.currentActivity;
    }

    void setCurrentActivity(Activity activity) {
        this.currentActivity = activity;
    }

    int getAppConfigVersion() {
        return this.getAppConfig().getVersion();
    }

    void putAppConfig(AppConfig appConfig) {
        this.storage.put("c", this.gson.toJson(appConfig));
        this.appConfig = appConfig;
    }

    AppConfig getAppConfig() {
        if (this.appConfig == null) {
            String currentConfig = (String) this.storage.get("c");
            if (TextUtils.isEmpty(currentConfig)) {
                this.appConfig = new AppConfig();
            } else {
                try {
                    this.appConfig = (AppConfig) this.gson.fromJson(currentConfig, AppConfig.class);
                } catch (Exception var3) {
                    this.appConfig = new AppConfig();
                } catch (Error var4) {
                    this.appConfig = new AppConfig();
                    Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
                }
            }
        }

        return this.appConfig;
    }

    void putPushSenderId(String senderId) {
        this.storage.put("f", senderId);
    }

    String getPushSenderId() {
        return (String) this.storage.get("f");
    }

    void putPushToken(String token) {
        this.storage.put("g", token);
    }

    String getPushToken() {
        return (String) this.storage.get("g");
    }

    void putAdId(String adId) {
        this.storage.put("l", adId);
    }

    String getAdId() {
        return (String) this.storage.get("l");
    }

    void removePopup() {
        this.storage.remove("h");
    }

    boolean hasPopup() {
        return this.storage.contains("h");
    }

    /*void putInAppMessage(InAppMessage inAppMessage) {
        this.storage.put("p", this.gson.toJson(inAppMessage));
    }

    @Nullable
    InAppMessage getInAppMessage() {
        InAppMessage inAppMessage = null;
        String inAppJsonString = (String)this.storage.get("p");
        if (!TextUtils.isEmpty(inAppJsonString)) {
            try {
                inAppMessage = (InAppMessage)this.gson.fromJson(inAppJsonString, InAppMessage.class);
                Long expirationTimeInSeconds = inAppMessage.getExpirationTime();
                if (expirationTimeInSeconds != null && System.currentTimeMillis() > expirationTimeInSeconds) {
                    this.removeInAppMessage();
                    inAppMessage = null;
                }
            } catch (Exception var4) {
            } catch (Error var5) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
            }
        }

        return inAppMessage;
    }*/

    void removeInAppMessage() {
        this.storage.remove("p");
    }

    String getLastGeoLocation() {
        return this.lastGeoLocation;
    }

    void setLastGeoLocation(String lastGeoLocation) {
        this.lastGeoLocation = lastGeoLocation;
    }

    void putHasControllerGeofence(boolean hasControllerGeofence) {
        if (hasControllerGeofence) {
            this.storage.put("i", true);
        } else {
            this.storage.remove("i");
        }

    }

    boolean getHasControllerGeofence() {
        return this.storage.contains("i");
    }

    void putNotificationState(int source, boolean enabled) {
        if (source == 0) {
            this.storage.put("k", enabled);
        } else if (source == 1) {
            this.storage.put("n", enabled);
        }

    }

    boolean getNotificationState(int source) {
        if (source == 0) {
            return Boolean.valueOf((String) this.storage.get("k", "true"));
        } else {
            return source == 1 ? Boolean.valueOf((String) this.storage.get("n", "true")) : true;
        }
    }

    void putLastShownPushId(String pushId) {
        this.storage.put("o", pushId);
    }

    @Nullable
    String getLastShownPushId() {
        return (String) this.storage.get("o", (Object) null);
    }

    private void saveAppDeviceInfo(AppDeviceInfo appDeviceInfo) {
        this.storage.put("e", this.gson.toJson(appDeviceInfo));
    }

    private void saveIdentifiers() {
        this.storage.put("b", this.gson.toJson(this.identifiers));
    }

    /*private void putAppTrackedList(List<AppTracked> appTrackedList) {
        if (appTrackedList != null || !appTrackedList.isEmpty()) {
            Type listType = (new TypeToken<ArrayList<AppTracked>>() {
            }).getType();
            this.storage.put("r", this.gson.toJson(appTrackedList, listType));
        }
    }

    private List<AppTracked> getAppTrackedList() {
        String json = (String)this.storage.get("r");
        if (TextUtils.isEmpty(json)) {
            return new LinkedList();
        } else {
            Type listType = (new TypeToken<ArrayList<AppTracked>>() {
            }).getType();

            try {
                return (List)this.gson.fromJson(json, listType);
            } catch (Error var4) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
                return new ArrayList();
            }
        }
    }*/

    void putActiveNotification(int notificationId) {
        Type type = (new TypeToken<Set<Integer>>() {
        }).getType();
        String json = (String) this.storage.get("s");
        Object activeList;
        if (TextUtils.isEmpty(json)) {
            activeList = new LinkedHashSet();
        } else {
            try {
                activeList = (Set) this.gson.fromJson(json, type);
            } catch (Error var6) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
                activeList = new LinkedHashSet();
            }
        }

        ((Set) activeList).add(notificationId);
        this.storage.put("s", this.gson.toJson(activeList, type));
    }

    boolean isNotificationActive(int notificationId) {
        String json = (String) this.storage.get("s");
        if (TextUtils.isEmpty(json)) {
            return false;
        } else {
            Type type = (new TypeToken<Set<Integer>>() {
            }).getType();

            try {
                Set<Integer> activeList = (Set) this.gson.fromJson(json, type);
                return activeList.contains(notificationId);
            } catch (Error var5) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
                return false;
            }
        }
    }

    void removeActiveNotification(int notificationId) {
        String json = (String) this.storage.get("s");
        if (!TextUtils.isEmpty(json)) {
            Type type = (new TypeToken<Set<Integer>>() {
            }).getType();

            try {
                Set<Integer> activeList = (Set) this.gson.fromJson(json, type);
                activeList.remove(notificationId);
                this.storage.put("s", this.gson.toJson(activeList, type));
            } catch (Error var5) {
                Netmera.logger().d("GSON.fromJson() error occured!! Reason :: Android OS.", new Object[0]);
            }

        }
    }

    void putApiKey(String apiKey) {
        this.storage.put("t", apiKey);
    }

    String getApiKey() {
        return (String) this.storage.get("t");
    }

    void putAlarmRequestCode(int requestId) {
        this.storage.put("u", requestId);
    }

    String getAlarmRequestCode() {
        return (String) this.storage.get("u");
    }
}