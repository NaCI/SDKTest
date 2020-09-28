package com.netmera;

import java.util.List;

interface PersistenceAdapter {
    void fetchObjects(PersistenceAdapter.OnFetchCompletedListener var1);

    void saveObject(StorageObject var1);

    void removeObject(StorageObject var1);

    public interface OnFetchCompletedListener {
        void onFetchCompleted(List<StorageObject> var1);
    }
}
