package com.netmera;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.annotations.SerializedName;
import com.netmera.RequestSpec.Builder;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

abstract class RequestBase extends BaseModel implements StorageObject {
    private static final int DEFAULT_TIMEOUT = 20000;
    private static final String API_VERSION = "/sdk/3.0";
    private transient long storageId;
    private transient int priority;
    @SerializedName("ids")
    private Identifiers identifiers;
    static final int PRIORITY_LOW = 0;
    static final int PRIORITY_NORMAL = 1;
    static final int PRIORITY_HIGH = 2;
    static final int PRIORITY_IMMEDIATE = 3;
    static final int METHOD_GET = 0;
    static final int METHOD_POST = 1;

    RequestBase() {
        this(1);
    }

    RequestBase(int priority) {
        this.storageId = -1L;
        this.priority = priority;
    }

    protected abstract String path();

    public long getStorageId() {
        return this.storageId;
    }

    public void setStorageId(long storageId) {
        this.storageId = storageId;
    }

    public List<Long> getContainedIds() {
        return new ArrayList(0);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            return this.storageId == ((RequestBase)o).storageId;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (int)(this.storageId ^ this.storageId >>> 32);
    }

    Class<? extends ResponseBase> getResponseClass() {
        return ResponseBase.class;
    }

    int getHttpMethod() {
        return 1;
    }

    void setPriority(int priority) {
        this.priority = priority;
    }

    void setIdentifiers(Identifiers identifiers) {
        this.identifiers = identifiers;
    }

    Identifiers getIdentifiers() {
        return this.identifiers;
    }

    RequestSpec createNetworkRequest(Gson gson) throws JsonIOException {
        return (new Builder("/sdk/3.0" + this.path(), this.getHttpMethod())).setPriority(this.priority).setBody(gson.toJson(this)).setTimeout(20000).build();
    }

    public String toString() {
        return "BaseRequestModel{storageId=" + this.storageId + '}';
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface HttpMethod {
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {
    }
}