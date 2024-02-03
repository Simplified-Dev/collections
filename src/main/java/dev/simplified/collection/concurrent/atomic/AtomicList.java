package dev.sbs.api.collection.concurrent.atomic;

import dev.sbs.api.collection.concurrent.iterator.ConcurrentIterator;
import dev.sbs.api.collection.search.Sortable;
import dev.sbs.api.collection.sort.SortOrder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public abstract class AtomicList<E, T extends List<E>> extends AtomicCollection<E, T> implements Sortable<E>, List<E> {

	protected AtomicList(@NotNull T type) {
		super(type);
	}

	public void add(int index, @NotNull E element) {
		try {
			super.lock.writeLock().lock();
			super.ref.add(index, element);
		} finally {
			super.lock.writeLock().unlock();
		}
	}

	@Override
	public boolean addAll(int index, @NotNull Collection<? extends E> collection) {
		try {
			super.lock.writeLock().lock();
			return super.ref.addAll(index, collection);
		} finally {
			super.lock.writeLock().unlock();
		}
	}

	@Override
	public final E get(int index) {
		try {
			super.lock.readLock().lock();
			return super.ref.get(index);
		} finally {
			super.lock.readLock().unlock();
		}
	}

	public final Optional<E> getFirst() {
		return Optional.ofNullable(this.size() > 0 ? this.get(0) : null);
	}

	public final Optional<E> getLast() {
		return Optional.ofNullable(this.size() > 0 ? this.get(this.size() - 1) : null);
	}

	public final E getOrDefault(int index, E defaultValue) {
		return index < this.size() ? this.get(index) : defaultValue;
	}

	@Override
	public final int indexOf(Object item) {
		return this.ref.indexOf(item);
	}

	public List<E> inverse() {
		Collections.reverse(this.ref);
		return this;
	}

	@Override
	public final int lastIndexOf(Object item) {
		return this.ref.lastIndexOf(item);
	}

	@Override @NotNull
	public final ListIterator<E> listIterator() {
		return this.listIterator(0);
	}

	@Override @NotNull
	public ListIterator<E> listIterator(int index) {
		return new ConcurrentListIterator(this.ref.toArray(), index);
	}

	@Override
	public final @NotNull Iterator<E> iterator() {
		return this.listIterator();
	}

	@Override
	public E remove(int index) {
		try {
			super.lock.writeLock().lock();
			return super.ref.remove(index);
		} finally {
			super.lock.writeLock().unlock();
		}
	}

	public final E removeFirst() {
		return this.remove(0);
	}

	public final E removeLast() {
		return this.remove(this.size() - 1);
	}

	@Override
	public E set(int index, E element) {
		try {
			super.lock.writeLock().lock();
			return super.ref.set(index, element);
		} finally {
			super.lock.writeLock().unlock();
		}
	}

	@SuppressWarnings("all")
	public AtomicList<E, T> sorted(Function<E, ? extends Comparable>... functions) {
		return this.sorted(SortOrder.DESCENDING, functions);
	}

	@SuppressWarnings("all")
	public AtomicList<E, T> sorted(@NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		return this.sorted(SortOrder.DESCENDING, functions);
	}

	@SuppressWarnings("all")
	public AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, Function<E, ? extends Comparable>... functions) {
		return this.sorted(sortOrder, Arrays.asList(functions));
	}

	@SuppressWarnings("all")
	public AtomicList<E, T> sorted(@NotNull SortOrder sortOrder, @NotNull Iterable<Function<E, ? extends Comparable>> functions) {
		if (functions.iterator().hasNext()) {
			this.sort((s1, s2) -> {
				Iterator<Function<E, ? extends Comparable>> iterator = functions.iterator();
				Comparator<E> comparator = Comparator.comparing(iterator.next());

				while (iterator.hasNext())
					comparator = comparator.thenComparing(iterator.next());

				return sortOrder == SortOrder.ASCENDING ? comparator.compare(s1, s2) : comparator.compare(s2, s1);
			});
		}

		return this;
	}

	public AtomicList<E, T> sorted(Comparator<? super E> comparator) {
		this.sort(comparator);
		return this;
	}

	@Override
	public void sort(Comparator<? super E> comparator) {
		List.super.sort(comparator);
	}

	@Override
	public @NotNull List<E> subList(int fromIndex, int toIndex) {
		return this.ref.subList(fromIndex, toIndex);
	}

	/**
	 * A concurrent list version of {@link ConcurrentIterator}.
	 */
	private final class ConcurrentListIterator extends ConcurrentCollectionIterator implements ListIterator<E> {

		private ConcurrentListIterator(Object[] snapshot, int index) {
			super(snapshot, index);
		}

		@Override
		public boolean hasPrevious() {
			return this.cursor > 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E previous() {
			if (this.hasPrevious())
				return (E) this.snapshot[this.last = --this.cursor];
			else
				throw new NoSuchElementException();
		}

		@Override
		public int nextIndex() {
			return this.cursor;
		}

		@Override
		public int previousIndex() {
			return this.cursor - 1;
		}

		@Override
		public void set(E element) {
			if (this.last < 0)
				throw new IllegalStateException();

			try {
				AtomicList.this.set(this.last, element);
				this.snapshot = AtomicList.this.toArray();
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add(E element) {
			this.snapshot = AtomicList.this.toArray();

			try {
				AtomicList.this.add(this.cursor++, element);
				this.last = -1;
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}

	}

}
