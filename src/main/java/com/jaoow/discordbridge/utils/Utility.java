package com.jaoow.discordbridge.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Utility {

    /**
     * Check if object isn't null and run a consumer if valid
     *
     * @param object the object to check
     * @param consumer consumer to be executed
     */
    public static <T> void notNull(T object, Consumer<T> consumer) {
        if (object != null) consumer.accept(object);
    }

    /**
     * Check if object isn't null and run a runnable if valid
     *
     * @param object the object to check
     * @param runnable runnable to be executed
     */
    public static <T> void notNull(T object, Runnable runnable) {
        if (object != null) runnable.run();
    }

    /**
     * Check if object isn't null and get one if invalid
     *
     * @param object the object to check
     * @param supplier supplier to be executed
     */
    public static <T> T notNull(T object, Supplier<T> supplier) {
        if (object == null) {
            return supplier.get();
        }
        return object;
    }

    /**
     * Check if object isn't null and throw exception if invalid
     *
     * @param parameter the object to check
     * @param message the exception message
     */
    public static <T> void notNull(T parameter, String message) {
        if (parameter == null) throw new NullPointerException(message);
    }
}
