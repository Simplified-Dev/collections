package dev.sbs.api.collection.concurrent.linked;

import dev.sbs.api.collection.concurrent.Concurrent;
import dev.sbs.api.collection.concurrent.ConcurrentList;
import dev.sbs.api.collection.concurrent.ConcurrentSet;
import dev.sbs.api.collection.concurrent.atomic.AtomicMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A thread-safe map backed by a {@link MaxSizeLinkedMap} with concurrent read and write access
 * via {@link java.util.concurrent.locks.ReadWriteLock}. Maintains insertion order and supports
 * an optional maximum size with eldest-entry eviction.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
public class ConcurrentLinkedMap<K, V> extends AtomicMap<K, V, MaxSizeLinkedMap<K, V>> {

	/**
	 * Create a new concurrent map.
	 */
	public ConcurrentLinkedMap() {
		super(new MaxSizeLinkedMap<>(), (HashMap<K, V>) null);
	}

	/**
	 * Create a new concurrent map.
	 *
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), (HashMap<K, V>) null);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map) {
		super(new MaxSizeLinkedMap<>(), map);
	}

	/**
	 * Create a new concurrent map and fill it with the given map.
	 *
	 * @param map Map to fill the new map with.
	 * @param maxSize The maximum number of entries allowed in the map.
	 */
	public ConcurrentLinkedMap(@Nullable Map<? extends K, ? extends V> map, int maxSize) {
		super(new MaxSizeLinkedMap<>(maxSize), map);
	}

	/**
	 * Creates a new empty {@code ConcurrentSet} for holding map entries, used internally by entry set operations.
	 *
	 * @return a new empty {@link ConcurrentSet} of entries
	 */
	@Override
	protected final @NotNull ConcurrentSet<Entry<K, V>> createEmptyEntrySet() {
		return Concurrent.newSet();
	}

	/**
	 * Creates a new empty {@code ConcurrentSet} for holding map keys, used internally by key set operations.
	 *
	 * @return a new empty {@link ConcurrentSet} of keys
	 */
	@Override
	protected final @NotNull ConcurrentSet<K> createEmptyKeySet() {
		return Concurrent.newSet();
	}

	/**
	 * Creates a new empty {@code ConcurrentList} for holding map values, used internally by values operations.
	 *
	 * @return a new empty {@link ConcurrentList} of values
	 */
	@Override
	protected final @NotNull ConcurrentList<V> createEmptyValueList() {
		return Concurrent.newList();
	}

}
