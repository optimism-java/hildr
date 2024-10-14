package io.optimism.utilities;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LruCacheProvider {

    public static final long DEFAULT_CACHE_SIZE = 300L;

    public static <K, V> Cache<K, V> create(long size) {
        return CacheBuilder.newBuilder().maximumSize(size).build();
    }

    public static <K, V> Cache<K, V> create() {
        return CacheBuilder.newBuilder().maximumSize(DEFAULT_CACHE_SIZE).build();
    }
}
