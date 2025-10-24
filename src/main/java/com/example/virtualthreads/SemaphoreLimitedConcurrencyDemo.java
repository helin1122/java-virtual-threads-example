package com.example.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/** Demonstrates limiting the concurrency of virtual threads with a Semaphore. */
class SemaphoreLimitedConcurrencyDemo {

  private static final int TOTAL_TASKS = 1000;
  private static final int MAX_CONCURRENCY = 10;
  private static final Duration SIMULATED_WORK = Duration.ofMillis(10);

  private SemaphoreLimitedConcurrencyDemo() {
    // prevents instantiation
  }

  public static void main(String[] args) {
    var semaphore = new Semaphore(MAX_CONCURRENCY);
    var inFlight = new AtomicInteger();
    var peakConcurrency = new AtomicInteger();
    var start = Instant.now();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (int task = 0; task < TOTAL_TASKS; task++) {
        final int taskId = task;
        executor.submit(
            () -> {
              acquirePermit(semaphore);
              var current = inFlight.incrementAndGet();
              updatePeakConcurrency(peakConcurrency, current);
              try {
                var thread = Thread.currentThread();
                System.out.printf(
                    "Task %d running on %s (in-flight=%d)%n", taskId, thread, current);
                simulateWork();
              } finally {
                inFlight.decrementAndGet();
                semaphore.release();
              }
            });
      }
    }

    var elapsed = Duration.between(start, Instant.now());
    System.out.printf(
        "Completed %,d tasks with max %,d concurrent virtual threads in %d ms (peak observed"
            + " %,d).%n",
        TOTAL_TASKS, MAX_CONCURRENCY, elapsed.toMillis(), peakConcurrency.get());
  }

  private static void acquirePermit(Semaphore semaphore) {
    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Interrupted while waiting for semaphore", e);
    }
  }

  private static void simulateWork() {
    try {
      Thread.sleep(SIMULATED_WORK);
    } catch (InterruptedException sleepInterrupted) {
      Thread.currentThread().interrupt();
    }
  }

  private static void updatePeakConcurrency(AtomicInteger peakConcurrency, int current) {
    peakConcurrency.accumulateAndGet(current, Math::max);
  }
}
