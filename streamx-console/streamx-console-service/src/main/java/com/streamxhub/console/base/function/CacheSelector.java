package com.streamxhub.console.base.function;

@FunctionalInterface
public interface CacheSelector<T> {
    T select() throws Exception;
}