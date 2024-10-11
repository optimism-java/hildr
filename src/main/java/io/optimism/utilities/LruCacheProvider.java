package io.optimism.utilities;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class LruCacheProvider {

    static <K, V> Cache<K, V> create() {
        return CacheBuilder.newBuilder().maximumSize(1000L).build();
    }
}
