package com.netmera;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

public abstract class BaseModel {
    public BaseModel() {
    }

    protected void beforeWriteToNetwork(Gson gson, JsonElement serialize) {
    }

    protected void beforeWriteToStorage(Gson gson, JsonElement serialize) {
    }

    protected void afterRead(Gson gson, JsonElement deserialize) {
    }
}
