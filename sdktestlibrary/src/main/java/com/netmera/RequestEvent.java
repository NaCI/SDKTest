package com.netmera;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class RequestEvent extends RequestBase {
    private static final String KLAZZ = "klazz";
    private static final String EVENTS = "events";
    static final int EVENT_LIMIT = 100;
    private transient List<NetmeraEvent> events;
    private transient List<Long> containedIds;

    RequestEvent() {
        this.events = new ArrayList(0);
        this.containedIds = new ArrayList(0);
    }

    RequestEvent(List<? extends NetmeraEvent> events) {
        this.events = new ArrayList(events);
        this.containedIds = new ArrayList(0);
    }

    protected String path() {
        return "/event/fire";
    }

    public List<Long> getContainedIds() {
        return this.containedIds;
    }

    RequestSpec createNetworkRequest(Gson gson) throws JsonIOException {
        Iterator var2 = this.events.iterator();

        while(var2.hasNext()) {
            NetmeraEvent event = (NetmeraEvent)var2.next();
            Identifiers identifiers = event.getIdentifiers();
            if (identifiers != null) {
                identifiers.removePropertiesSameWith(this.getIdentifiers());
            }
        }

        return super.createNetworkRequest(gson);
    }

    public void beforeWriteToNetwork(Gson gson, JsonElement serialize) {
        super.beforeWriteToNetwork(gson, serialize);
        JsonArray jsonArray = new JsonArray();
        Iterator var4 = this.events.iterator();

        while(var4.hasNext()) {
            NetmeraEvent event = (NetmeraEvent)var4.next();
            JsonElement jsonElement = gson.toJsonTree(event);
            jsonArray.add(jsonElement);
        }

        serialize.getAsJsonObject().add("events", jsonArray);
    }

    public void beforeWriteToStorage(Gson gson, JsonElement serialize) {
        super.beforeWriteToStorage(gson, serialize);
        JsonArray jsonArray = new JsonArray();
        Iterator var4 = this.events.iterator();

        while(var4.hasNext()) {
            NetmeraEvent event = (NetmeraEvent)var4.next();
            JsonElement jsonElement = gson.toJsonTree(event);
            jsonElement.getAsJsonObject().addProperty("klazz", event.getClass().getName());
            jsonArray.add(jsonElement);
        }

        serialize.getAsJsonObject().add("events", jsonArray);
    }

    public void afterRead(Gson gson, JsonElement deserialize) {
        super.afterRead(gson, deserialize);
        JsonArray jsonArray = deserialize.getAsJsonObject().getAsJsonArray("events");
        if (jsonArray != null) {
            this.events = new ArrayList(jsonArray.size());
            Iterator var4 = jsonArray.iterator();

            while(var4.hasNext()) {
                JsonElement element = (JsonElement)var4.next();

                try {
                    JsonObject jsonObject = element.getAsJsonObject();
                    String className = jsonObject.get("klazz").getAsString();
                    this.events.add((NetmeraEvent)gson.fromJson(element, Class.forName(className)));
                } catch (Exception var8) {
                }
            }

        }
    }

    List<NetmeraEvent> getEvents() {
        return this.events;
    }

    boolean mergeEvents(RequestEvent requestEventToMerge) {
        if (requestEventToMerge.getStorageId() != -1L) {
            this.containedIds.add(requestEventToMerge.getStorageId());
        }

        int size = Math.min(requestEventToMerge.events.size(), 100 - this.events.size());
        List<NetmeraEvent> eventsToAdd = requestEventToMerge.events.subList(0, size);
        Iterator var4 = eventsToAdd.iterator();

        while(var4.hasNext()) {
            NetmeraEvent event = (NetmeraEvent)var4.next();
            if (event.getIdentifiers() == null) {
                event.setIdentifiers(requestEventToMerge.getIdentifiers());
            } else if (event.getIdentifiers().isEmpty()) {
            }
        }

        this.events.addAll(eventsToAdd);
        eventsToAdd.clear();
        return this.events.size() == 100;
    }
}