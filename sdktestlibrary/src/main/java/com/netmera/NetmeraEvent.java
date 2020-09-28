package com.netmera;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public abstract class NetmeraEvent extends BaseModel {
    private static final String CODE = "code";
    @SerializedName("ts")
    private long creationTimeStamp;
    @SerializedName("ids")
    private Identifiers identifiers;
    @SerializedName("revenue")
    private Double revenue;
    @SerializedName("fv")
    private String geoLocation;

    public NetmeraEvent() {
    }

    protected abstract String eventCode();

    public void beforeWriteToNetwork(Gson gson, JsonElement serialize) {
        super.beforeWriteToNetwork(gson, serialize);
        JsonObject jsonObject = serialize.getAsJsonObject();
        jsonObject.addProperty("code", this.eventCode());
        /*if (Netmera.getNetmeraComponent().stateManager().getAppConfig() != null && Netmera.getNetmeraComponent().stateManager().getAppConfig().getPrivateEventInfoList() != null) {
            Iterator var4 = Netmera.getNetmeraComponent().stateManager().getAppConfig().getPrivateEventInfoList().iterator();

            while(var4.hasNext()) {
                NetmeraPrivateEvent nPE = (NetmeraPrivateEvent)var4.next();
                if (this.eventCode().equals(nPE.getEventCode()) && jsonObject.has(nPE.getAttributeCode())) {
                    jsonObject.remove(nPE.getAttributeCode());
                }
            }

        }*/
    }

    protected void beforeWriteToStorage(Gson gson, JsonElement serialize) {
        super.beforeWriteToStorage(gson, serialize);
        JsonObject jsonObject = serialize.getAsJsonObject();
        jsonObject.addProperty("code", this.eventCode());
        /*if (Netmera.getNetmeraComponent().stateManager().getAppConfig() != null && Netmera.getNetmeraComponent().stateManager().getAppConfig().getPrivateEventInfoList() != null) {
            Iterator var4 = Netmera.getNetmeraComponent().stateManager().getAppConfig().getPrivateEventInfoList().iterator();

            while(var4.hasNext()) {
                NetmeraPrivateEvent nPE = (NetmeraPrivateEvent)var4.next();
                if (this.eventCode().equals(nPE.getEventCode()) && jsonObject.has(nPE.getAttributeCode())) {
                    jsonObject.remove(nPE.getAttributeCode());
                }
            }

        }*/
    }

    void setIdentifiers(Identifiers identifiers) {
        this.identifiers = identifiers;
    }

    Identifiers getIdentifiers() {
        return this.identifiers;
    }

    void setCreationTimeStamp(long creationTimeStamp) {
        this.creationTimeStamp = creationTimeStamp;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    void setGeoLocation(String lastGeoLocation) {
        this.geoLocation = lastGeoLocation;
    }
}