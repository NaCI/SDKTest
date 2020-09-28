package com.netmera;

import com.android.volley.NoConnectionError;
import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;

class VolleyListener implements Listener<String>, ErrorListener {
    private NetworkCallback callback;

    VolleyListener(NetworkCallback callback) {
        this.callback = callback;
    }

    public void onResponse(String responseBody) {
        this.callback.onResponse(responseBody, (NetmeraError)null);
    }

    public void onErrorResponse(VolleyError volleyError) {
        try {
            NetmeraError netmeraError;
            if (volleyError == null) {
                netmeraError = NetmeraError.generalInstance();
            } else if (volleyError instanceof VolleyCancelError) {
                netmeraError = NetmeraError.cancelInstance();
            } else if (volleyError instanceof NoConnectionError) {
                netmeraError = NetmeraError.noConnectionInstance();
            } else if (volleyError instanceof VolleyParseError) {
                netmeraError = NetmeraError.invalidResponseInstance();
            } else if (volleyError.networkResponse != null) {
                netmeraError = NetmeraError.serverErrorInstance(volleyError.getMessage(), volleyError.networkResponse.statusCode, volleyError.networkResponse.data);
            } else {
                netmeraError = NetmeraError.generalInstance(volleyError.getMessage());
            }

            this.callback.onResponse((String)null, netmeraError);
        } catch (Exception var3) {
            Netmera.logger().e(var3, "Network error was catched successfully", new Object[0]);
        }

    }
}