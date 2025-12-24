package com.nhom.weatherdesktop.util;

import javafx.application.Platform;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Utility class to run async tasks with consistent error handling
 * Eliminates duplicate async patterns throughout the codebase
 */
public class AsyncTaskRunner {
    
    /**
     * Run a background task asynchronously with success and error handlers
     * 
     * @param <T> Type of result from background task
     * @param backgroundTask Task to run in background thread (should not touch UI)
     * @param onSuccess Handler for successful result (runs on JavaFX thread)
     * @param onError Handler for exceptions (runs on JavaFX thread)
     */
    public static <T> void runAsync(
        Supplier<T> backgroundTask,
        Consumer<T> onSuccess,
        Consumer<Exception> onError
    ) {
        new Thread(() -> {
            try {
                T result = backgroundTask.get();
                Platform.runLater(() -> {
                    try {
                        onSuccess.accept(result);
                    } catch (Exception e) {
                        onError.accept(e);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> onError.accept(e));
            }
        }).start();
    }
    
    /**
     * Run a background task asynchronously with only success handler
     * Errors will be thrown as RuntimeException
     * 
     * @param <T> Type of result from background task
     * @param backgroundTask Task to run in background thread
     * @param onSuccess Handler for successful result (runs on JavaFX thread)
     */
    public static <T> void runAsync(
        Supplier<T> backgroundTask,
        Consumer<T> onSuccess
    ) {
        runAsync(
            backgroundTask,
            onSuccess,
            e -> {
                throw new RuntimeException("Async task failed", e);
            }
        );
    }
    
    /**
     * Run a background task that returns void
     * 
     * @param backgroundTask Task to run in background thread
     * @param onSuccess Handler to run after successful completion (runs on JavaFX thread)
     * @param onError Handler for exceptions (runs on JavaFX thread)
     */
    public static void runAsyncVoid(
        Runnable backgroundTask,
        Runnable onSuccess,
        Consumer<Exception> onError
    ) {
        runAsync(
            () -> {
                backgroundTask.run();
                return null;
            },
            result -> onSuccess.run(),
            onError
        );
    }
}
