package com.example.virtualthreads;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Demonstrates throughput differences between platform and virtual threads when simulating I/O
 * bound API calls.
 */
class BasicUse {
  private static final int TOTAL_REQUESTS = 1_000;
  private static final Duration API_LATENCY = Duration.ofMillis(200);
  private static final Logger log = Logger.getLogger(BasicUse.class.getName());

  private BasicUse() {}

  public static void main(String[] args) throws InterruptedException {
    log.info(
        String.format(
            "Simulating %d API calls with ~%d ms latency.",
            TOTAL_REQUESTS, API_LATENCY.toMillis()));
    runScenario(
        "Platform thread pool",
        () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2));
    runScenario("Virtual thread per task", Executors::newVirtualThreadPerTaskExecutor);
  }

  private static void runScenario(String label, Supplier<ExecutorService> executorSupplier)
      throws InterruptedException {
    Instant start = Instant.now();
    try (ExecutorService executor = executorSupplier.get()) {
      List<Callable<Void>> tasks =
          IntStream.range(0, TOTAL_REQUESTS)
              .mapToObj(
                  index ->
                      (Callable<Void>)
                          () -> {
                            long taskStart = System.nanoTime();
                            simulateApiCall();
                            long durationMs =
                                TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - taskStart);
                            if (index % 200 == 0) {
                              log.log(
                                  Level.FINEST,
                                  String.format(
                                      "[%s] Request %d finished in %d ms on %s",
                                      label, index, durationMs, Thread.currentThread()));
                            }
                            return null;
                          })
              .toList();
      executor.invokeAll(tasks);
    }
    Duration totalTime = Duration.between(start, Instant.now());
    double totalSeconds = totalTime.toMillis() / 1_000d;
    double throughput = TOTAL_REQUESTS / totalSeconds;
    log.info(
        String.format(
            "%s completed in %d ms (%.2f requests/sec)", label, totalTime.toMillis(), throughput));
  }

  private static void simulateApiCall() throws InterruptedException {
    Thread.sleep(API_LATENCY);
  }
}
