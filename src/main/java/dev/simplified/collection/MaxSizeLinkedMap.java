package dev.sbs.api.collection;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maximum size linked HashMap for cached data.
 *
 * @param <K> Key
 * @param <V> Value
 */
public class MaxSizeLinkedMap<K, V> extends LinkedHashMap<K, V> {

    private final int maxSize;

    public MaxSizeLinkedMap() {
        this(-1);
    }

    public MaxSizeLinkedMap(int maxSize) {
        this.maxSize = maxSize;
    }

    public MaxSizeLinkedMap(@NotNull Map<? extends K, ? extends V> map) {
        this(map, -1);
    }

    public MaxSizeLinkedMap(@NotNull Map<? extends K, ? extends V> map, int maxSize) {
        super(map);
        this.maxSize = maxSize;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.maxSize != -1 && this.size() > this.maxSize;
    }

}
