package com.netmera;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.ProcessLifecycleOwner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.netmera.NetmeraInbox.NetmeraInboxFetchCallback;
import com.netmera.NetmeraInboxCategory.NetmeraInboxCategoryCallback;
import com.netmera.NetmeraLifeCycleManager.Listener;
import com.netmera.NetworkManager.OnNetworkResponseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Netmera implements OnNetworkResponseListener, Listener {
    private static NetmeraDaggerComponent netmeraComponent;
    private static NetmeraLogger netmeraLogger;
    private static NetmeraBatteryReceiver netmeraBatteryReceiver;

    static NetmeraLogger logger() {
        if (netmeraLogger == null) {
            netmeraLogger = new NetmeraLogger();
        }

        return netmeraLogger;
    }

    Netmera(Context context) {
        ((Application)context).registerActivityLifecycleCallbacks(new NetmeraLifeCycleManager(this));
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new NetmeraLifeCycleObserver(this));
    }

    public static void init(Context context, String senderId) {
        init(context, senderId, (String)null);
    }

    public static void init(Context context, @NonNull String senderId, String apiKey) {
        if (NetmeraProviderChecker.isGoogleApiAvailable(context)) {
            /*if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context);
            }

            try {
                HmsInstanceId.getInstance(context).deleteAAID();
            } catch (Exception var7) {
            }*/
        }

        if (VERSION.SDK_INT < 14) {
            logger().d("Netmera could not be initialized!\nCause: Supported min sdk version is 14!", new Object[0]);
        } else if (TextUtils.isEmpty(senderId)) {
            logger().d("Netmera could not be initialized!\nCause: GCM Sender Id can not be null or empty!", new Object[0]);
        } else {
            Context appContext = context.getApplicationContext();
            netmeraComponent = DaggerNetmeraDaggerComponent.builder().netmeraDaggerModule(new NetmeraDaggerModule(appContext)).build();
            setApiKey(apiKey);
            String baseUrl = netmeraComponent.stateManager().getAppConfig().getBaseUrl();
            if (!TextUtils.isEmpty(baseUrl)) {
                netmeraComponent.networkManager().setBaseUrl(baseUrl);
            }

            netmeraComponent.stateManager().setSecondaryFirebaseApp(firebaseApp);
            netmeraComponent.pushManager().registerPush(appContext, senderId);
            RequestRemoveLegacyData requestRemoveLegacyData = LegacyManager.shouldHandleLegacyData(appContext);
            if (requestRemoveLegacyData != null && !LegacyManager.checkLegacyRemoved(appContext)) {
                netmeraComponent.networkManager().sendRequest(requestRemoveLegacyData);
            }

            netmeraBatteryReceiver = new NetmeraBatteryReceiver();
        }
    }

    public static void initForBothProviders(Context context, @NonNull String firebaseSenderId, @NonNull String hmsSenderId, String apiKey) {
        initForBothProviders(context, firebaseSenderId, hmsSenderId, apiKey, (FirebaseApp)null);
    }

    public static void initForBothProviders(Context context, @NonNull String firebaseSenderId, @NonNull String hmsSenderId, String apiKey, FirebaseApp firebaseApp) {
        if (VERSION.SDK_INT < 14) {
            logger().d("Netmera could not be initialized!\nCause: Supported min sdk version is 14!", new Object[0]);
        } else if (TextUtils.isEmpty(firebaseSenderId)) {
            logger().d("Netmera could not be initialized!\nCause: Firebase Sender Id can not be null or empty!", new Object[0]);
        } else if (TextUtils.isEmpty(hmsSenderId)) {
            logger().d("Netmera could not be initialized!\nCause: HSM Sender Id can not be null or empty!", new Object[0]);
        } else {
            Context appContext = context.getApplicationContext();
            netmeraComponent = DaggerNetmeraDaggerComponent.builder().netmeraDaggerModule(new NetmeraDaggerModule(appContext)).build();
            boolean isGooglePlayServicesAvailable = NetmeraProviderChecker.isGoogleApiAvailable(appContext);
            if (isGooglePlayServicesAvailable) {
                /*if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context);
                }

                try {
                    HmsInstanceId.getInstance(context).deleteAAID();
                } catch (Exception var9) {
                }*/
            }

            setApiKey(apiKey);
            String baseUrl = netmeraComponent.stateManager().getAppConfig().getBaseUrl();
            if (!TextUtils.isEmpty(baseUrl)) {
                netmeraComponent.networkManager().setBaseUrl(baseUrl);
            }

            netmeraComponent.stateManager().setSecondaryFirebaseApp(firebaseApp);
            netmeraComponent.pushManager().registerPush(appContext, isGooglePlayServicesAvailable ? firebaseSenderId : hmsSenderId);
            RequestRemoveLegacyData requestRemoveLegacyData = LegacyManager.shouldHandleLegacyData(appContext);
            if (requestRemoveLegacyData != null && !LegacyManager.checkLegacyRemoved(appContext)) {
                netmeraComponent.networkManager().sendRequest(requestRemoveLegacyData);
            }

            netmeraBatteryReceiver = new NetmeraBatteryReceiver();
        }
    }

    public static void logging(boolean enabled) {
        NetmeraLogger.logging(enabled);
    }

    public static void setBaseUrl(String url) {
        if (!shouldInitialize()) {
            String baseUrl = netmeraComponent.stateManager().getAppConfig().getBaseUrl();
            if (TextUtils.isEmpty(baseUrl)) {
                netmeraComponent.networkManager().setBaseUrl(url);
            }

        }
    }

    public static void setApiKey(String apiKey) {
        if (!shouldInitialize()) {
            NetworkManager networkManager = netmeraComponent.networkManager();
            if (!TextUtils.isEmpty(apiKey)) {
                String currentApiKey = netmeraComponent.stateManager().getApiKey();
                if (TextUtils.isEmpty(currentApiKey)) {
                    netmeraComponent.stateManager().putApiKey(apiKey);
                } else if (!currentApiKey.equals(apiKey)) {
                    netmeraComponent.stateManager().putApiKey(apiKey);
                    netmeraComponent.behaviorManager().applyBehaviorChanges();
                }

                networkManager.setApiKey(apiKey);
                networkManager.startScheduler();
            } else {
                networkManager.stopScheduler();
            }

        }
    }

    public static void turnOffSendingEventAndUserUpdate(boolean turnOff) {
        if (!shouldInitialize()) {
            if (turnOff) {
                netmeraComponent.requestSender().sendRequestEvent(new EventTurnoff());
            }

            netmeraComponent.stateManager().turnOffSendingEventAndUserUpdate(turnOff);
        }
    }

    public static <T extends NetmeraEvent> void sendEvent(@NonNull T netmeraEvent) {
        if (!shouldInitialize()) {
            if (netmeraComponent.stateManager().isTurnedOffSendingEventAndUserRequest()) {
                logger().d("Sending event was skipped according to OptOutUserData", new Object[0]);
            } else {
                netmeraComponent.requestSender().sendRequestEvent(netmeraEvent);
            }
        }
    }

    public static <T extends NetmeraUser> void updateUser(T netmeraUser) {
        if (!shouldInitialize()) {
            if (netmeraComponent.stateManager().isTurnedOffSendingEventAndUserRequest()) {
                logger().d("Sending Update User was skipped according to OptOutUserData", new Object[0]);
            } else {
                netmeraComponent.requestSender().sendRequestUpdateUser(netmeraUser);
            }
        }
    }

    public static void enablePush() {
        if (!shouldInitialize()) {
            netmeraComponent.pushManager().enablePush(1);
        }
    }

    public static void disablePush() {
        if (!shouldInitialize()) {
            netmeraComponent.pushManager().disablePush(1);
        }
    }

    public static boolean isPushEnabled() {
        return shouldInitialize() ? false : netmeraComponent.pushManager().isPushEnabled();
    }

    public static void enablePopupPresentation() {
        if (!shouldInitialize()) {
            netmeraComponent.stateManager().setAllowUIPresentation(true);
            showPopupIfHasAny();
            showInAppMessageIfHasAny();
        }
    }

    public static void disablePopupPresentation() {
        if (!shouldInitialize()) {
            netmeraComponent.stateManager().setAllowUIPresentation(false);
        }
    }

    public static void toggleWebViewProgressbarVisibility(boolean isVisible) {
        if (!shouldInitialize()) {
            netmeraComponent.stateManager().setShouldHideProgressbarForWebView(isVisible);
        }
    }

    public static boolean isWebViewProgressbarEnabled() {
        if (shouldInitialize()) {
            return true;
        } else {
            return !netmeraComponent.stateManager().shouldHideProgressbarForWebView();
        }
    }

    public static void handleWebContent(@NonNull WebView webView) {
        if (!shouldInitialize()) {
            handleWebContent(webView, (NetmeraWebViewCallback)null);
        }
    }

    public static void handleWebContent(@NonNull WebView webView, NetmeraWebViewCallback netmeraWebViewCallback) {
        if (!shouldInitialize()) {
            netmeraComponent.actionManager().handleWebContent(netmeraComponent.context(), webView, netmeraWebViewCallback);
        }
    }

    public static void handlePushObject(@NonNull Activity activity, @NonNull NetmeraPushObject netmeraPushObject) {
        if (!shouldInitialize()) {
            netmeraComponent.pushManager().handlePushObject(activity, netmeraPushObject);
        }
    }

    public static void fetchInbox(@NonNull NetmeraInboxFilter filter, @NonNull NetmeraInboxFetchCallback callback) {
        if (!shouldInitialize()) {
            NetmeraInbox inbox = new NetmeraInbox(netmeraComponent.requestSender(), filter);
            inbox.fetchFirstPage(callback);
        }
    }

    public static void fetchCategory(@NonNull NetmeraCategoryFilter filter, @NonNull NetmeraInboxCategoryCallback callback) {
        if (!shouldInitialize()) {
            NetmeraInboxCategory category = new NetmeraInboxCategory(netmeraComponent.requestSender(), filter);
            category.fetchFirstPage(callback);
        }
    }

    public static void requestPermissionsForLocation() {
        if (!shouldInitialize()) {
            Context context = netmeraComponent.context();
            if (NetmeraUtils.doesHaveLocationPermission(context)) {
                performLocationOperations();
            } else {
                context.startActivity(NetmeraActivityPermission.newIntent(context));
            }

        }
    }

    public static void setNetmeraMaxActiveRegions(int netmeraMaxActiveRegions) {
        if (!shouldInitialize()) {
            netmeraComponent.locationManager().setActiveGeofenceLimit(netmeraMaxActiveRegions);
        }
    }

    public static void onNetmeraPushMessageReceived(RemoteMessage remoteMessage) {
        if (!shouldInitialize()) {
            if (remoteMessage.getData() == null) {
                logger().d("Received push data is null!", new Object[0]);
            } else {
                Bundle tempBundle = new Bundle();
                Iterator var2 = remoteMessage.getData().keySet().iterator();

                while(var2.hasNext()) {
                    String key = (String)var2.next();
                    tempBundle.putString(key, (String)remoteMessage.getData().get(key));
                }

                getNetmeraComponent().pushManager().handlePushMessage(getNetmeraComponent().context(), remoteMessage.getFrom(), tempBundle);
            }
        }
    }

    public static void onNetmeraPushMessageReceived(com.huawei.hms.push.RemoteMessage remoteMessage) {
        if (!shouldInitialize()) {
            if (remoteMessage.getData() == null) {
                logger().d("Received push data is null!", new Object[0]);
            } else {
                Bundle tempBundle = new Bundle();

                try {
                    JsonObject tempObject = (JsonObject)(new Gson()).fromJson((new JsonParser()).parse(remoteMessage.getData()), JsonObject.class);
                    Iterator var3 = tempObject.keySet().iterator();

                    while(var3.hasNext()) {
                        String key = (String)var3.next();
                        tempBundle.putString(key, tempObject.get(key).toString());
                    }

                    getNetmeraComponent().pushManager().handlePushMessage(getNetmeraComponent().context(), remoteMessage.getFrom(), tempBundle);
                } catch (Exception var5) {
                    logger().e("Invalid push data received!!", new Object[]{var5});
                }

            }
        }
    }

    public static void onNetmeraNewToken(String token) {
        if (!shouldInitialize()) {
            Context context = netmeraComponent.context();
            String senderId = getNetmeraComponent().stateManager().getPushSenderId();
            getNetmeraComponent().pushManager().handlePushToken(context, senderId, token);
        }
    }

    public static boolean isNetmeraRemoteMessage(RemoteMessage remoteMessage) {
        return remoteMessage.getData().containsKey("_nm");
    }

    public static boolean isNetmeraRemoteMessage(com.huawei.hms.push.RemoteMessage remoteMessage) {
        try {
            return ((JsonObject)(new Gson()).fromJson((new JsonParser()).parse(remoteMessage.getData()), JsonObject.class)).keySet().contains("_nm");
        } catch (Exception var2) {
            return false;
        }
    }

    public static void setNetmeraHeaders(ContentValues headerValueSet) {
        if (!shouldInitialize()) {
            netmeraComponent.networkManager().setHeaderValueSet(headerValueSet);
        }
    }

    private static boolean shouldInitialize() {
        if (netmeraComponent == null) {
            logger().d("Netmera.init() has not been called!", new Object[0]);
            return true;
        } else {
            return false;
        }
    }

    static NetmeraDaggerComponent getNetmeraComponent() {
        return netmeraComponent;
    }

    @VisibleForTesting
    static void initForTesting(NetmeraDaggerComponent netmeraComponent) {
        Netmera.netmeraComponent = netmeraComponent;
    }

    static void performLocationOperations() {
        netmeraComponent.locationManager().performOperations(netmeraComponent.context(), true);
    }

    static void addTestDevice(String params, ResponseCallback callback) {
        netmeraComponent.requestSender().sendRequestTestDeviceAdd(params, callback);
    }

    private static void showPopupIfHasAny() {
        Popup popup = netmeraComponent.stateManager().getPopup();
        if (popup != null && !popup.canPopupOnHome()) {
            netmeraComponent.actionManager().performAction(netmeraComponent.context(), popup.getAction());
        }

    }

    private static void showInAppMessageIfHasAny() {
        InAppMessage inAppMessage = netmeraComponent.stateManager().getInAppMessage();
        if (inAppMessage != null && !netmeraComponent.inAppMessageManager().isShown()) {
            netmeraComponent.inAppMessageManager().show(netmeraComponent.context(), inAppMessage);
        }

    }

    private void handleAppConfig(@Nullable AppConfig appConfigNew) {
        if (appConfigNew != null) {
            if (appConfigNew.isSendAddId()) {
                this.getAndSendAdId();
            }

            if (appConfigNew.getCacheExpirationInterval() != 0 && netmeraComponent.stateManager().getAppConfig().getCacheExpirationInterval() != appConfigNew.getCacheExpirationInterval()) {
                netmeraComponent.stateManager().putAlarmRequestCode(NetmeraBehaviorBroadcastReceiver.rescheduleAlarm(netmeraComponent.context(), appConfigNew));
            }

            netmeraComponent.stateManager().putAppConfig(appConfigNew);
            this.handleNotificationChannels(appConfigNew);
            String newUrl = appConfigNew.getBaseUrl();
            if (!TextUtils.isEmpty(newUrl)) {
                netmeraComponent.networkManager().setBaseUrl(newUrl);
            }

            netmeraComponent.locationManager().performOperations(netmeraComponent.context(), true);
        } else {
            netmeraComponent.locationManager().performOperations(netmeraComponent.context(), false);
        }

    }

    private void handleNotificationChannels(AppConfig appConfig) {
        if (VERSION.SDK_INT >= 26 && appConfig.getNotificationChannelList() != null && !appConfig.shouldSkipChannelDelete()) {
            NotificationManager notificationManager = (NotificationManager)netmeraComponent.context().getSystemService("notification");
            List<String> skipIDS = new ArrayList();
            List<NotificationChannel> channelList = notificationManager.getNotificationChannels();

            for(int i = 0; i < channelList.size(); ++i) {
                String channelIdToDelete = ((NotificationChannel)channelList.get(i)).getId();
                boolean keep = false;
                Iterator var8 = appConfig.getNotificationChannelList().iterator();

                while(var8.hasNext()) {
                    NetmeraNotificationChannel ntfchn = (NetmeraNotificationChannel)var8.next();
                    if (ntfchn.getChannelId().equals(channelIdToDelete)) {
                        keep = true;
                        skipIDS.add(ntfchn.getChannelId());
                        break;
                    }
                }

                if (!keep && (channelIdToDelete.contains("default") || channelIdToDelete.contains("sv_") || channelIdToDelete.contains("s_") || channelIdToDelete.contains("vibrate") || channelIdToDelete.contains("sdxsilent") || channelIdToDelete.toLowerCase().contains("nm_"))) {
                    notificationManager.deleteNotificationChannel(channelIdToDelete);
                }
            }

            Iterator var10 = appConfig.getNotificationChannelList().iterator();

            while(var10.hasNext()) {
                NetmeraNotificationChannel ntfchn = (NetmeraNotificationChannel)var10.next();
                if (!skipIDS.contains(ntfchn.getChannelId())) {
                    NotificationChannel channel = new NotificationChannel(ntfchn.getChannelId(), ntfchn.getChannelName(), ntfchn.isHighPriority() ? 4 : 3);
                    channel.setShowBadge(ntfchn.isShowBadge());
                    channel.enableLights(ntfchn.isEnableLights());
                    channel.enableVibration(ntfchn.isSoundVibration());
                    if (!TextUtils.isEmpty(ntfchn.getSound())) {
                        Uri soundUri = NetmeraUtils.getSoundUri(netmeraComponent.context(), ntfchn.getSound());
                        if (soundUri != null) {
                            channel.setSound(soundUri, (new Builder()).setContentType(4).setUsage(5).build());
                        }
                    } else {
                        channel.setSound((Uri)null, (AudioAttributes)null);
                    }

                    notificationManager.createNotificationChannel(channel);
                }
            }

        }
    }

    private void checkNotificationStateAndSendIfRequired() {
        boolean notificationEnabled = NetmeraUtils.isNotificationEnabled(netmeraComponent.context());
        if (notificationEnabled) {
            netmeraComponent.pushManager().enablePush(0);
        } else {
            netmeraComponent.pushManager().disablePush(0);
        }

    }

    private void getAndSendAdId() {
        if (NetmeraProviderChecker.isGoogleApiAvailable(netmeraComponent.context())) {
            (new AsyncTask<Void, Void, Info>() {
                protected Info doInBackground(Void... params) {
                    Info adInfo;
                    try {
                        adInfo = AdvertisingIdClient.getAdvertisingIdInfo(Netmera.netmeraComponent.context());
                    } catch (Exception var4) {
                        adInfo = null;
                    }

                    return adInfo;
                }

                protected void onPostExecute(Info adInfo) {
                    if (adInfo != null) {
                        String adId = null;
                        if (!adInfo.isLimitAdTrackingEnabled()) {
                            adId = adInfo.getId();
                        }

                        if (!TextUtils.equals(Netmera.netmeraComponent.stateManager().getAdId(), adId)) {
                            Netmera.netmeraComponent.stateManager().putAdId(adId);
                            Netmera.netmeraComponent.requestSender().sendRequestSendAdId(adId);
                        }
                    }

                }
            }).execute(new Void[0]);
        } else if (VERSION.SDK_INT >= 19) {
            (new Thread() {
                public void run() {
                    try {
                        com.huawei.hms.ads.identifier.AdvertisingIdClient.Info info = com.huawei.hms.ads.identifier.AdvertisingIdClient.getAdvertisingIdInfo(Netmera.netmeraComponent.context());
                        if (null != info) {
                            String adId = null;
                            if (!info.isLimitAdTrackingEnabled()) {
                                adId = info.getId();
                            }

                            if (!TextUtils.equals(Netmera.netmeraComponent.stateManager().getAdId(), adId)) {
                                Netmera.netmeraComponent.stateManager().putAdId(adId);
                                Netmera.netmeraComponent.requestSender().sendRequestSendAdId(adId);
                            }
                        }
                    } catch (IOException var3) {
                        Netmera.logger().i("HMS getAdvertisingIdInfo Error: " + var3.toString(), new Object[0]);
                    }

                }
            }).start();
        } else {
            logger().i("Cannot retrieve AdId !!", new Object[0]);
        }

    }

    public void onNetworkResponse(RequestBase request, final ResponseBase response, NetmeraError error) {
        if (error == null && response != null) {
            if (request instanceof RequestRemoveLegacyData) {
                LegacyManager.clearLegacyData(netmeraComponent.context());
            } else {
                String userId;
                if (response instanceof ResponseSessionInit) {
                    userId = request.getIdentifiers().getSessionId();
                    String sessionIdOfCurrentSession = netmeraComponent.stateManager().getIdentifiers().getSessionId();
                    if (TextUtils.equals(userId, sessionIdOfCurrentSession)) {
                        this.handleAppConfig(((ResponseSessionInit)response).getAppConfig());
                    }

                    (new AsyncTask<Void, Void, Map<String, Boolean>>() {
                        protected Map<String, Boolean> doInBackground(Void... params) {
                            return Netmera.netmeraComponent.stateManager().getUpdatedTrackedAppList((ResponseSessionInit)response);
                        }

                        protected void onPostExecute(Map<String, Boolean> updatedAppTrackedMap) {
                            if (!updatedAppTrackedMap.isEmpty()) {
                                Netmera.netmeraComponent.requestSender().sendUpdatedAppTrackedList(updatedAppTrackedMap);
                            }

                        }
                    }).execute(new Void[0]);
                } else if (response instanceof ResponseUserIdentify) {
                    userId = ((ResponseUserIdentify)response).getUserId();
                    if (!TextUtils.isEmpty(userId)) {
                        netmeraComponent.stateManager().updateUserId(userId);
                    }
                } else if (response instanceof ResponseAppConfig) {
                    this.handleAppConfig(((ResponseAppConfig)response).getAppConfig());
                }

            }
        } else {
            logger().d("Request error: \n" + (error != null ? error.toString() : null), new Object[0]);
        }
    }

    public void onOpen() {
        logger().d("NetmeraLifeCycle: App opened.", new Object[0]);
        if (netmeraComponent.stateManager().getAppConfig().isSendAddId()) {
            this.getAndSendAdId();
        }

    }

    public void onForeground() {
        logger().d("NetmeraLifeCycle: App came to foreground.", new Object[0]);
        boolean shouldUpdateSession = netmeraComponent.stateManager().setAppForegroundAndCheckIfSessionUpdateRequired();
        if (shouldUpdateSession) {
            netmeraComponent.requestSender().sendRequestInitSession();
        } else {
            netmeraComponent.requestSender().sendRequestEvent(new EventAppOpen());
        }

        if (netmeraComponent.stateManager().isAllowUIPresentation()) {
            showPopupIfHasAny();
            showInAppMessageIfHasAny();
        }

        this.checkNotificationStateAndSendIfRequired();
        if (netmeraComponent.stateManager().getAppConfig().shouldTrackBatteryLevel()) {
            netmeraComponent.context().registerReceiver(netmeraBatteryReceiver, new IntentFilter("android.intent.action.BATTERY_LOW"));
        }

    }

    public void onBackground() {
        logger().d("NetmeraLifeCycle: App went to background.", new Object[0]);
        Double timeInApp = netmeraComponent.stateManager().setAppBackgroundAndGetTimeInAppValue();
        if (timeInApp != null) {
            netmeraComponent.requestSender().sendRequestEvent(new EventTimeInApp(timeInApp));
        }

        try {
            netmeraComponent.context().unregisterReceiver(netmeraBatteryReceiver);
        } catch (Exception var3) {
        }

    }

    public void onClose() {
        logger().d("NetmeraLifeCycle: App closed.", new Object[0]);
    }

    public void onActivityChanged(Activity activity) {
        logger().d("NetmeraLifeCycle: Top of activity changed.", new Object[0]);
        netmeraComponent.stateManager().setCurrentActivity(activity);
    }
}
