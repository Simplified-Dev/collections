package dev.sbs.api.collection.concurrent.unmodifiable;

import dev.sbs.api.collection.concurrent.ConcurrentCollection;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An unmodifiable concurrent list that allows for simultaneous fast reading and iteration utilizing {@link AtomicReference}.
 * <p>
 * The AtomicReference changes the methods that modify the list by replacing the
 * entire list on each modification. This allows for maintaining the original
 * speed of {@link ArrayList#contains(Object)} and makes it cross-thread-safe.
 *
 * @param <E> type of elements
 */
public class ConcurrentUnmodifiableCollection<E> extends ConcurrentCollection<E> {

	/**
	 * Create a new unmodifiable concurrent list.
	 */
	public ConcurrentUnmodifiableCollection() {
		super((Collection<? extends E>) null);
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given array.
	 */
	@SafeVarargs
	public ConcurrentUnmodifiableCollection(@NotNull E... array) {
		this(Arrays.asList(array));
	}

	/**
	 * Create a new unmodifiable concurrent list and fill it with the given collection.
	 */
	public ConcurrentUnmodifiableCollection(@NotNull Collection<? extends E> collection) {
		super(new ArrayList<>(collection));
	}

	@Override
	public boolean add(@NotNull E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(@NotNull Collection<? extends E> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("all")
	public boolean remove(Object element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(@NotNull Collection<?> collection) {
		throw new UnsupportedOperationException();
	}

}
