/*
 * Copyright (C) individee GmbH - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written 9 2020
 */

package com.individee.async;

import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * A service that can execute lengthy tasks.
 */
public class TaskService {

    private final ExecutorService executorService;
    private final Map<String, Future<?>> futures = new HashMap<>();

    /**
     * Instantiates a new Lengthy task service.
     */
    public TaskService() {
        super();
        executorService = Executors.newCachedThreadPool();
    }

    /**
     * Execute.
     *
     * @param key  the key
     * @param task the callable
     */
    public void execute(String key, Callable<?> task) {
        if (futures.containsKey(key)) {
            Future<?> future = futures.get(key);
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        }
        futures.put(key, executorService.submit(task));
    }

    /**
     * Execute.
     *
     * @param task the task
     */
    public void execute(Callable<?> task) {
        executorService.submit(task);
    }

    /**
     * Is done boolean.
     *
     * @param key the key
     * @return the boolean
     */
    public boolean isDone(String key) {
        if (futures.containsKey(key)) {
            Future<?> future = futures.get(key);
            if (future != null) {
                if(future.isCancelled()){
                    LoggerFactory.getLogger(getClass()).info("Future " + key + " got canceled");
                }
                LoggerFactory.getLogger(getClass()).info("Future " + key + " is done: " + future.isDone());
                return (future.isDone() || future.isCancelled());
            } else {
                LoggerFactory.getLogger(getClass()).info("Missing future " + key);
            }
        } else {
            LoggerFactory.getLogger(getClass()).info("Missing future " + key);
        }
        return true;
    }

    /**
     * Gets result.
     *
     * @param <T>   the type parameter
     * @param key   the key
     * @param clazz the clazz
     * @return the result
     */
    public <T> T getResult(String key, Class<T> clazz) {
        if (futures.containsKey(key)) {
            Future<?> future = futures.get(key);
            if (future != null && future.isDone()) {
                try {
                    Object o = future.get();
                    if (o != null && clazz.isAssignableFrom(o.getClass())) {
                        return (T) o;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        executorService.shutdownNow();
    }

    /**
     * Cancel.
     *
     * @param key the key
     */
    public void cancel(String key) {
        if (futures.containsKey(key)) {
            Future<?> future = futures.get(key);
            if (future != null && !future.isDone()) {
                future.cancel(true);
            }
        }
    }
}
