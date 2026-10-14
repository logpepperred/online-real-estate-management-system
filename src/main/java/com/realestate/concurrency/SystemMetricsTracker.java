package com.realestate.concurrency;

import org.springframework.stereotype.Component;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Demonstrates Core Java Multithreading & Synchronization.
 * Implements thread-safe counters, synchronized blocks, and a ThreadPoolExecutor
 * for asynchronous audit task execution.
 */
@Component
public class SystemMetricsTracker {

    // Thread-safe atomic counters
    private final AtomicLong totalPageRequests = new AtomicLong(0);
    private final AtomicLong totalSearchQueries = new AtomicLong(0);
    private final AtomicInteger activeUserSessions = new AtomicInteger(0);

    // Explicit lock object for synchronized critical sections
    private final Object lock = new Object();
    private long lastResetTimestamp = System.currentTimeMillis();

    // Multithreaded Executor Service for background tasks
    private final ExecutorService backgroundTaskExecutor = new ThreadPoolExecutor(
            2,                                      // Core pool size
            4,                                      // Maximum pool size
            60L, TimeUnit.SECONDS,                  // Keep-alive time
            new LinkedBlockingQueue<>(100),         // Work queue
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "MetricsWorkerThread-" + threadNumber.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            }
    );

    public void incrementPageRequests() {
        totalPageRequests.incrementAndGet();
    }

    public void incrementSearchQueries() {
        totalSearchQueries.incrementAndGet();
    }

    public void userLoggedIn() {
        activeUserSessions.incrementAndGet();
    }

    public void userLoggedOut() {
        activeUserSessions.updateAndGet(current -> current > 0 ? current - 1 : 0);
    }

    /**
     * Synchronized method demonstrating mutex synchronization.
     */
    public synchronized void resetMetrics() {
        totalPageRequests.set(0);
        totalSearchQueries.set(0);
        lastResetTimestamp = System.currentTimeMillis();
    }

    /**
     * Demonstrates synchronized block on a dedicated monitor lock.
     */
    public long getLastResetTime() {
        synchronized (lock) {
            return lastResetTimestamp;
        }
    }

    /**
     * Submits an asynchronous task to the thread pool worker.
     */
    public CompletableFuture<String> executeAsyncTask(String taskName, Runnable taskAction) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                taskAction.run();
                return "Task '" + taskName + "' completed by " + Thread.currentThread().getName();
            } catch (Exception e) {
                return "Task failed: " + e.getMessage();
            }
        }, backgroundTaskExecutor);
    }

    public long getTotalPageRequests() {
        return totalPageRequests.get();
    }

    public long getTotalSearchQueries() {
        return totalSearchQueries.get();
    }

    public int getActiveUserSessions() {
        return activeUserSessions.get();
    }
}
