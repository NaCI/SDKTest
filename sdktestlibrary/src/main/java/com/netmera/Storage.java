package com.netmera;

interface Storage {
    boolean put(String var1, Object var2);

    <T> T get(String var1);

    <T> T get(String var1, T var2);

    boolean remove(String var1);

    boolean remove(String... var1);

    boolean contains(String var1);
}
