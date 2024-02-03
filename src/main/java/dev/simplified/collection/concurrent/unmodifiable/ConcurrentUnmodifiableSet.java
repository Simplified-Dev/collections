package dev.sbs.api.collection.concurrent.unmodifiable;

import dev.sbs.api.collection.concurrent.ConcurrentSet;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An unmodifiable concurrent set that allows for simultaneous fast reading and iteration utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the set by replacing the
 * entire set each modification. This allows for maintaining the original speed
 * of {@link HashSet#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentUnmodifiableSet<E> extends ConcurrentSet<E> {

	/**
	 * Create a new unmodifiable concurrent set.
	 */
	public ConcurrentUnmodifiableSet() {
		super(new HashSet<>());
	}

	/**
	 * Create a new unmodifiable concurrent set and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableSet(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new unmodifiable concurrent set and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableSet(@NotNull Collection<? extends E> collection) {
		super(new HashSet<>(collection));
	}

	@Override
	public final boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean remove(Object item) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public final boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

}
