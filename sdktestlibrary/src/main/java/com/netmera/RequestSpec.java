package com.netmera;

import java.util.HashMap;
import java.util.Map;

class RequestSpec {
    private String baseUrl;
    private String path;
    private int httpMethod;
    private Map<String, String> headers;
    private String body;
    private int timeout;
    private int priority;

    private RequestSpec() {
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    String getBaseUrl() {
        return this.baseUrl;
    }

    String getPath() {
        return this.path;
    }

    int getHttpMethod() {
        return this.httpMethod;
    }

    Map<String, String> getHeaders() {
        return this.headers;
    }

    String getBody() {
        return this.body;
    }

    int getTimeout() {
        return this.timeout;
    }

    public int getPriority() {
        return this.priority;
    }

    static class Builder {
        private String path;
        private int httpMethod;
        private Map<String, String> headers;
        private String body;
        private int timeout;
        private int priority;

        public Builder(String path, int httpMethod) {
            this.path = path;
            this.httpMethod = httpMethod;
        }

        public RequestSpec.Builder setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public RequestSpec.Builder setBody(String body) {
            this.body = body;
            return this;
        }

        public RequestSpec.Builder setTimeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public RequestSpec.Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public RequestSpec build() {
            RequestSpec request = new RequestSpec();
            request.path = this.path;
            request.httpMethod = this.httpMethod;
            request.headers = new HashMap();
            if (this.headers != null) {
                request.headers.putAll(this.headers);
            }

            request.body = this.body;
            request.timeout = this.timeout;
            request.priority = this.priority;
            return request;
        }
    }
}