package com.netmera;

import java.util.List;

interface StorageObject {
    long INVALID_ID = -1L;

    List<Long> getContainedIds();

    long getStorageId();

    void setStorageId(long var1);
}
