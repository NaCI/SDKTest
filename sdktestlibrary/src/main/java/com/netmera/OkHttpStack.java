package com.netmera;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.HttpStack;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Protocol;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

class OkHttpStack implements HttpStack {
    private final OkHttpClient client = new OkHttpClient();

    public HttpResponse performRequest(Request<?> request, Map<String, String> additionalHeaders) throws IOException, AuthFailureError {
        Builder clientBuilder = new Builder();
        int timeoutMs = request.getTimeoutMs();
        clientBuilder.connectTimeout((long) timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.readTimeout((long) timeoutMs, TimeUnit.MILLISECONDS);
        clientBuilder.writeTimeout((long) timeoutMs, TimeUnit.MILLISECONDS);
        // Certificate Pinner
        /*clientBuilder.certificatePinner(new CertificatePinner.Builder()
                .add("example.com", "asdasdsa")
                .build()
        );*/
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder();
        builder.url(request.getUrl());
        Map<String, String> headers = request.getHeaders();
        Iterator var7 = headers.keySet().iterator();

        String name;
        while (var7.hasNext()) {
            name = (String) var7.next();
            builder.addHeader(name, (String) headers.get(name));
        }

        var7 = additionalHeaders.keySet().iterator();

        while (var7.hasNext()) {
            name = (String) var7.next();
            builder.addHeader(name, (String) additionalHeaders.get(name));
        }

        setConnectionParametersForRequest(builder, request);
        Call okHttpCall = clientBuilder.build().newCall(builder.build());
        Response okHttpResponse = okHttpCall.execute();
        StatusLine responseStatus = new BasicStatusLine(parseProtocol(okHttpResponse.protocol()), okHttpResponse.code(), okHttpResponse.message());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromOkHttpResponse(okHttpResponse));
        Headers responseHeaders = okHttpResponse.headers();
        int i = 0;

        for (int len = responseHeaders.size(); i < len; ++i) {
            String nameHeader = responseHeaders.name(i);
            String value = responseHeaders.value(i);
            if (nameHeader != null) {
                response.addHeader(new BasicHeader(nameHeader, value));
            }
        }

        return response;
    }

    private static HttpEntity entityFromOkHttpResponse(Response response) throws IOException {
        BasicHttpEntity entity = new BasicHttpEntity();
        ResponseBody body = response.body();
        entity.setContent(body.byteStream());
        entity.setContentLength(body.contentLength());
        entity.setContentEncoding(response.header("Content-Encoding"));
        if (body.contentType() != null) {
            entity.setContentType(body.contentType().type());
        }

        return entity;
    }

    private static void setConnectionParametersForRequest(okhttp3.Request.Builder builder, Request<?> request) throws IOException, AuthFailureError {
        switch (request.getMethod()) {
            case -1:
                byte[] postBody = request.getPostBody();
                if (postBody != null) {
                    builder.post(RequestBody.create(MediaType.parse(request.getPostBodyContentType()), postBody));
                }
                break;
            case 0:
                builder.get();
                break;
            case 1:
                builder.post(createRequestBody(request));
                break;
            case 2:
                builder.put(createRequestBody(request));
                break;
            case 3:
                builder.delete();
                break;
            case 4:
                builder.head();
                break;
            case 5:
                builder.method("OPTIONS", (RequestBody) null);
                break;
            case 6:
                builder.method("TRACE", (RequestBody) null);
                break;
            case 7:
                builder.patch(createRequestBody(request));
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }

    }

    private static ProtocolVersion parseProtocol(Protocol protocol) {
        switch (protocol) {
            case HTTP_1_0:
                return new ProtocolVersion("HTTP", 1, 0);
            case HTTP_1_1:
                return new ProtocolVersion("HTTP", 1, 1);
            case SPDY_3:
                return new ProtocolVersion("SPDY", 3, 1);
            case HTTP_2:
                return new ProtocolVersion("HTTP", 2, 0);
            default:
                throw new IllegalAccessError("Unknown protocol");
        }
    }

    private static RequestBody createRequestBody(Request request) throws AuthFailureError {
        byte[] body = request.getBody();
        return body == null ? null : RequestBody.create(MediaType.parse(request.getBodyContentType()), body);
    }
}
