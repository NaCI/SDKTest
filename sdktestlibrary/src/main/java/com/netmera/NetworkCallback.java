package com.netmera;

import androidx.annotation.Nullable;

interface NetworkCallback {
    void onResponse(@Nullable String var1, @Nullable NetmeraError var2);
}
