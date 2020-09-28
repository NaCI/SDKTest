package com.netmera;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class NetworkManager implements NetworkRequester {
    private static final String HEADER_API_KEY = "X-netmera-api-key";
    private static final String HEADER_OS = "X-netmera-os";
    private static final String HEADER_SDK_V = "X-netmera-sdkV";
    private static final String HEADER_PROVIDER = "X-netmera-provider";
    private static final String VALUE_HEADER_OS = "ANDROID";
    private static final long TIME_INTERVAL = 30000L;
    private static final String BASE_URL = "https://sdkapi.netmera.com";
    private static final String VALUE_HUAWEI_PROVIDER = "huawei";
    private final NetworkAdapter networkAdapter;
    private final PersistenceAdapter persistenceAdapter;
    private final StateManager stateManager;
    private final Gson gson;
    private NetworkManager.OnNetworkResponseListener listener;
    private String apiKey;
    private String baseUrl = "https://sdkapi.netmera.com";
    private ScheduledExecutorService scheduler;
    private Runnable runnable = new Runnable() {
        public void run() {
            NetworkManager.this.processRequestsOnStorage();
        }
    };
    private boolean canSendRequest = true;
    private List<RequestBase> continuingRequests;
    private ContentValues headerValueSet;
    private Context context;

    NetworkManager(Context context, PersistenceAdapter persistenceAdapter, NetworkAdapter networkAdapter, StateManager stateManager, Gson gson, NetworkManager.OnNetworkResponseListener listener) {
        this.persistenceAdapter = persistenceAdapter;
        this.networkAdapter = networkAdapter;
        this.stateManager = stateManager;
        this.gson = gson;
        this.listener = listener;
        this.context = context;
        this.continuingRequests = new ArrayList();
    }

    void startScheduler(long timeInterval) {
        if (this.scheduler == null) {
            this.scheduler = Executors.newSingleThreadScheduledExecutor();
            this.scheduler.scheduleWithFixedDelay(this.runnable, 0L, timeInterval, TimeUnit.MILLISECONDS);
            Netmera.logger().d("Schedule bulk request processing timer with period of %d seconds.", new Object[]{30L});
        }

    }

    void startScheduler() {
        this.startScheduler(30000L);
    }

    void stopScheduler() {
        if (this.scheduler != null) {
            this.scheduler.shutdownNow();
            this.scheduler = null;
            Netmera.logger().d("Stop bulk request processing timer.", new Object[0]);
        }

    }

    void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    void setListener(NetworkManager.OnNetworkResponseListener listener) {
        this.listener = listener;
    }

    void setHeaderValueSet(ContentValues contentValues) {
        this.headerValueSet = contentValues;
    }

    public void sendRequest(RequestBase request) {
        this.sendRequest(request, (ResponseCallback)null);
    }

    public void sendRequest(RequestBase request, ResponseCallback callback) {
        if (request.getIdentifiers() == null && this.canSendRequest) {
            request.setIdentifiers(this.stateManager.getIdentifiers());
        }

        if (callback != null) {
            if (TextUtils.isEmpty(this.apiKey)) {
                callback.onResponse((ResponseBase)null, NetmeraError.noApiKeyInstance());
                return;
            }

            this.startRequest(request, callback);
        } else {
            this.persistenceAdapter.saveObject(request);
            if (this.canSendRequest && !TextUtils.isEmpty(this.apiKey)) {
                this.startRequest(request, (ResponseCallback)null);
            }
        }

    }

    @VisibleForTesting
    void startRequest(final RequestBase request, final ResponseCallback callback) {
        if (request.getIdentifiers() == null) {
            request.setIdentifiers(this.stateManager.getIdentifiers());
        }

        if (callback == null) {
            this.continuingRequests.add(request);
        }

        /*if (request instanceof RequestSessionInit || request instanceof RequestUserUpdate || request instanceof RequestUserIdentify || request instanceof RequestAppConfig) {
            this.canSendRequest = false;
        }*/

        RequestSpec requestSpec;
        try {
            requestSpec = request.createNetworkRequest(this.gson);
        } catch (JsonIOException var8) {
            this.continuingRequests.remove(request);
            this.deliverResponse(request, (ResponseBase)null, NetmeraError.requestSerializationFailedInstance(var8), callback);
            return;
        }

        if (TextUtils.isEmpty(requestSpec.getBaseUrl())) {
            requestSpec.setBaseUrl(this.baseUrl);
        }

        Map<String, String> headers = new HashMap(3);
        headers.put("X-netmera-api-key", this.apiKey);
        headers.put("X-netmera-os", "ANDROID");
        headers.put("X-netmera-sdkV", "3.7.1");

        try {
            if (!NetmeraProviderChecker.isGoogleApiAvailable(this.context)) {
                headers.put("X-netmera-provider", "huawei");
            }
        } catch (Exception var7) {
        }

        if (this.headerValueSet != null && this.headerValueSet.keySet().size() > 0) {
            Iterator var5 = this.headerValueSet.keySet().iterator();

            while(var5.hasNext()) {
                String key = (String)var5.next();
                headers.put(key, this.headerValueSet.getAsString(key));
            }
        }

        requestSpec.getHeaders().putAll(headers);
        this.networkAdapter.sendRequest(requestSpec, new NetworkCallback() {
            public void onResponse(@Nullable String responseBody, @Nullable NetmeraError error) {
                NetworkManager.this.continuingRequests.remove(request);
                NetworkManager.this.handleNetworkResponse(request, responseBody, error, callback);
            }
        });
        Netmera.logger().d("Start request for \"" + request.path() + "\" with body \n" + requestSpec.getBody(), new Object[0]);
    }

    private void handleNetworkResponse(RequestBase request, String responseBody, NetmeraError error, ResponseCallback callback) {
        if (error != null) {
            this.deliverResponse(request, (ResponseBase)null, error, callback);
            this.handleError(request, error);
            Netmera.logger().d("Error for \"" + request.path() + "\" as\n" + error.toString(), new Object[0]);
        } else {
            if (callback == null) {
                this.persistenceAdapter.removeObject(request);
            }

            if (TextUtils.isEmpty(responseBody)) {
                responseBody = "{}";
            }

            ResponseBase response = null;

            try {
                response = (ResponseBase)this.gson.fromJson(responseBody, request.getResponseClass());
            } catch (Exception var7) {
                error = NetmeraError.responseSerializationFailedInstance(var7);
            }

            this.deliverResponse(request, response, error, callback);
            Netmera.logger().d("Response received for \"" + request.path() + "\" with body\n" + responseBody, new Object[0]);
        }
    }

    private void deliverResponse(RequestBase request, ResponseBase response, NetmeraError error, ResponseCallback callback) {
        if (callback != null) {
            callback.onResponse(response, error);
        } else if (this.listener != null) {
            this.listener.onNetworkResponse(request, response, error);
        }

        /*if (request instanceof RequestSessionInit || request instanceof RequestUserUpdate || request instanceof RequestUserIdentify || request instanceof RequestAppConfig) {
            this.canSendRequest = true;
        }*/

    }

    private void handleError(RequestBase request, NetmeraError error) {
        if (error.getErrorCode() != -2) {
            if (error.getErrorCode() == -1 || error.getStatusCode() >= 500 && error.getStatusCode() < 600) {
                if (this.canSendRequest) {
                    this.canSendRequest = false;
                    this.networkAdapter.cancelAllRequests();
                }
            } else {
                this.persistenceAdapter.removeObject(request);
            }

        }
    }

    private void processRequestsOnStorage() {
        if (this.networkAdapter.isProcessingRequests() && !TextUtils.isEmpty(this.apiKey)) {
            this.networkAdapter.stopProcessingRequests();
            this.canSendRequest = true;
            this.persistenceAdapter.fetchObjects(new PersistenceAdapter.OnFetchCompletedListener() {
                public void onFetchCompleted(List<StorageObject> list) {
                    NetworkManager.this.prepareAndSendRequests(list);
                    NetworkManager.this.networkAdapter.startProcessingRequests();
                }
            });
        }
    }

    private void prepareAndSendRequests(List<StorageObject> list) {
        if (list != null && list.size() > 0) {
            Netmera.logger().d("Fetched %d request objects.", new Object[]{list.size()});
            List<RequestBase> copyOfContinuingRequests = new ArrayList(this.continuingRequests);
            RequestEvent requestEventToMergeOnto = new RequestEvent();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
                StorageObject object = (StorageObject)var4.next();
                RequestBase request = (RequestBase)object;
                if (!copyOfContinuingRequests.contains(request)) {
                    if (request instanceof RequestEvent) {
                        if (requestEventToMergeOnto.mergeEvents((RequestEvent)request)) {
                            requestEventToMergeOnto.setPriority(3);
                            this.sendRequest(requestEventToMergeOnto);
                            requestEventToMergeOnto = new RequestEvent(((RequestEvent)request).getEvents());
                        }
                    } else {
                        if (!requestEventToMergeOnto.getEvents().isEmpty()) {
                            requestEventToMergeOnto.setPriority(3);
                            this.sendRequest(requestEventToMergeOnto);
                            requestEventToMergeOnto = new RequestEvent();
                        }

                        request.setPriority(3);
                        this.sendRequest(request);
                    }
                }
            }

            if (!requestEventToMergeOnto.getEvents().isEmpty()) {
                requestEventToMergeOnto.setPriority(3);
                this.sendRequest(requestEventToMergeOnto);
            }

        }
    }

    interface OnNetworkResponseListener {
        void onNetworkResponse(RequestBase var1, ResponseBase var2, @Nullable NetmeraError var3);
    }
}