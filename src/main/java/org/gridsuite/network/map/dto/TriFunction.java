package org.gridsuite.network.map.dto;

@FunctionalInterface
public interface TriFunction<T, S, U, R> {
    /**
     * Applies this function to the given arguments.
     *
     * @param t the first function argument
     * @param s the second function argument
     * @param u the third function argument
     * @return the function result
     */
    R apply(T t, S s, U u);

}
