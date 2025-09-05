package dev.sbs.api.collection.search;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@FunctionalInterface
public interface SearchFunction<T, R> extends Function<T, R> {

    /**
     * Alows you to traverse down through method references.
     *
     * @param from  The first function in the chain
     * @param to    The next function in the chain
     * @param <T1>  The first class to traverse
     * @param <T2>  The next class to traverse
     * @param <T3>  The return type
     * @return The method reference between {@link T1} and {@link T3}
     */
    static <T1, T2, T3> @NotNull SearchFunction<T1, T3> combine(@NotNull SearchFunction<T1, T2> from, @NotNull SearchFunction<T2, T3> to) {
        return from.andThen(to);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <V> @NotNull SearchFunction<T, V> andThen(@NotNull Function<? super R, ? extends V> after) {
        return (T t) -> after.apply(apply(t));
    }

    enum Match {

        ALL,
        ANY

    }

}
