package com.netmera;

interface EventSender {
    <T extends NetmeraEvent> void sendRequestEvent(T var1);
}
