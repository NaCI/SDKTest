package com.netmera;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Request.Priority;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class VolleyRequest extends JsonRequest<String> {
    private RequestSpec requestSpec;

    VolleyRequest(RequestSpec requestSpec, VolleyListener listener) {
        super(requestSpec.getHttpMethod(), requestSpec.getBaseUrl() + requestSpec.getPath(), requestSpec.getBody(), listener, listener);
        this.setRetryPolicy(new DefaultRetryPolicy(requestSpec.getTimeout(), 1, 0.0F));
        this.requestSpec = requestSpec;
    }

    public Map<String, String> getHeaders() throws AuthFailureError {
        return this.requestSpec.getHeaders();
    }

    public Priority getPriority() {
        switch(this.requestSpec.getPriority()) {
            case 0:
                return Priority.LOW;
            case 1:
                return Priority.NORMAL;
            case 2:
                return Priority.HIGH;
            case 3:
                return Priority.IMMEDIATE;
            default:
                return Priority.NORMAL;
        }
    }

    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String responseBody = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
            return Response.success(responseBody, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException var3) {
            return Response.error(new VolleyParseError(response));
        }
    }
}