package com.netmera;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import java.io.File;

class VolleyNetworkAdapter implements NetworkAdapter {
    static final String OKHTTP_CLIENT = "okhttp3.OkHttpClient";
    static final String REQUEST_TAG = "nmRequestTag";
    static final int THREAD_POOL_SIZE = 1;
    private boolean processingRequests = true;
    private RequestQueue requestQueue;

    VolleyNetworkAdapter(Context context) {
        File cacheDir = new File(context.getCacheDir(), "volley");

        BasicNetwork network;
        try {
            Class.forName("okhttp3.OkHttpClient");
            network = new BasicNetwork(new OkHttpStack());
        } catch (ClassNotFoundException var5) {
            network = new BasicNetwork(new HurlStack());
        }

        this.requestQueue = new RequestQueue(new DiskBasedCache(cacheDir), network, 1);
        this.requestQueue.start();
    }

    VolleyNetworkAdapter(RequestQueue requestQueue) {
        this.requestQueue = requestQueue;
        this.requestQueue.start();
    }

    public boolean isProcessingRequests() {
        return this.processingRequests;
    }

    public void startProcessingRequests() {
        if (!this.processingRequests) {
            this.processingRequests = true;
            this.requestQueue.start();
        }

    }

    public void stopProcessingRequests() {
        if (this.processingRequests) {
            this.processingRequests = false;
            this.requestQueue.stop();
        }

    }

    public void sendRequest(RequestSpec requestSpec, NetworkCallback callback) {
        VolleyRequest volleyRequest = new VolleyRequest(requestSpec, new VolleyListener(callback));
        volleyRequest.setTag("nmRequestTag");
        volleyRequest.setShouldCache(false);
        this.requestQueue.add(volleyRequest);
    }

    public void cancelAllRequests() {
        this.requestQueue.cancelAll(new RequestFilter() {
            public boolean apply(Request<?> request) {
                return request.getTag() == "nmRequestTag";
            }
        });
    }
}