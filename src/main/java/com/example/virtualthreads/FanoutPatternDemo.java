package com.example.virtualthreads;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * Compares the fanout pattern using virtual threads vs platform threads.
 *
 * <p>The fanout pattern involves concurrently performing multiple tasks (e.g., making parallel
 * calls to different services) and aggregating the results. Virtual threads make this pattern
 * extremely efficient by allowing one thread per task without resource overhead.
 */
class FanoutPatternDemo {
  private static final Logger log = Logger.getLogger(FanoutPatternDemo.class.getName());
  private static final int NUM_REQUESTS = 100;
  private static final int FANOUT_SIZE = 4;
  private static final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  private FanoutPatternDemo() {}

  public static void main(String[] args) throws InterruptedException {
    log.info("Starting fanout pattern comparison...");
    log.info(
        String.format(
            "Processing %d requests, each fanning out to %d services (%d total concurrent"
                + " operations)",
            NUM_REQUESTS, FANOUT_SIZE, NUM_REQUESTS * FANOUT_SIZE));

    // Compare platform threads vs virtual threads
    // Platform thread pool is limited, creating a bottleneck for fanout operations
    runScenario(
        "Platform Thread Pool",
        () -> Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2),
        () -> Executors.newFixedThreadPool(200)); // Limited pool for fanout

    // Virtual threads can scale to handle massive concurrency without pool limits
    runScenario(
        "Virtual Threads",
        Executors::newVirtualThreadPerTaskExecutor,
        Executors::newVirtualThreadPerTaskExecutor);

    log.info("Fanout pattern comparison completed.");
  }

  /** Runs a scenario with the specified executor types. */
  private static void runScenario(
      String label,
      Supplier<ExecutorService> requestExecutorSupplier,
      Supplier<ExecutorService> fanoutExecutorSupplier)
      throws InterruptedException {
    Instant start = Instant.now();

    var requestExecutor = requestExecutorSupplier.get();
    var fanoutExecutor = fanoutExecutorSupplier.get();

    try {
      var tasks =
          IntStream.range(0, NUM_REQUESTS)
              .mapToObj(
                  i ->
                      (Callable<Void>)
                          () -> {
                            handleRequest("User" + i, label, fanoutExecutor);
                            return null;
                          })
              .toList();

      requestExecutor.invokeAll(tasks);
    } finally {
      requestExecutor.shutdown();
      fanoutExecutor.shutdown();
      requestExecutor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS);
      fanoutExecutor.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS);
    }

    Duration totalTime = Duration.between(start, Instant.now());
    double throughput = NUM_REQUESTS / (totalTime.toMillis() / 1000.0);
    log.info(
        String.format(
            "--- [%s] Total time: %d ms (%.2f requests/sec, %d total operations)",
            label, totalTime.toMillis(), throughput, NUM_REQUESTS * FANOUT_SIZE));
  }

  /**
   * Handles a request by fanning out to multiple services concurrently. Uses the shared fanout
   * executor for parallel service calls.
   */
  private static void handleRequest(
      String userId, String scenario, ExecutorService fanoutExecutor) {
    Instant start = Instant.now();

    try {
      // Fan out: Submit multiple concurrent tasks to the fanout executor
      var userFuture = fanoutExecutor.submit(() -> fetchUserProfile(userId));
      var ordersFuture = fanoutExecutor.submit(() -> fetchUserOrders(userId));
      var recommendationsFuture = fanoutExecutor.submit(() -> fetchRecommendations(userId));
      var notificationsFuture = fanoutExecutor.submit(() -> fetchNotifications(userId));

      // Gather results from all concurrent tasks
      String userProfile = userFuture.get();
      String orders = ordersFuture.get();
      String recommendations = recommendationsFuture.get();
      String notifications = notificationsFuture.get();

      // Aggregate results
      String aggregatedResponse =
          aggregateResponse(userProfile, orders, recommendations, notifications);

      Duration elapsed = Duration.between(start, Instant.now());
      if (userId.equals("User0") || userId.equals("User99")) {
        log.info(
            String.format(
                "[%s] Request for %s completed in %d ms on %s",
                scenario, userId, elapsed.toMillis(), Thread.currentThread()));
      }

    } catch (ExecutionException | InterruptedException e) {
      log.severe(String.format("[%s] Request for %s failed: %s", scenario, userId, e.getMessage()));
    }
  }

  /** Simulates fetching user profile from a service. */
  private static String fetchUserProfile(String userId) throws InterruptedException {
    log.info(String.format("Fetching user profile for %s on %s", userId, Thread.currentThread()));
    Thread.sleep(200); // Simulate I/O latency
    return String.format("Profile(%s)", userId);
  }

  /** Simulates fetching user orders from a service. */
  private static String fetchUserOrders(String userId) throws InterruptedException {
    log.info(String.format("Fetching orders for %s on %s", userId, Thread.currentThread()));
    Thread.sleep(300); // Simulate I/O latency
    return String.format("Orders(%s)", userId);
  }

  /** Simulates fetching recommendations from a service. */
  private static String fetchRecommendations(String userId) throws InterruptedException {
    log.info(
        String.format("Fetching recommendations for %s on %s", userId, Thread.currentThread()));
    Thread.sleep(250); // Simulate I/O latency
    return String.format("Recommendations(%s)", userId);
  }

  /** Simulates fetching notifications from a service. */
  private static String fetchNotifications(String userId) throws InterruptedException {
    log.info(String.format("Fetching notifications for %s on %s", userId, Thread.currentThread()));
    Thread.sleep(150); // Simulate I/O latency
    return String.format("Notifications(%s)", userId);
  }

  /** Aggregates responses from multiple services. */
  private static String aggregateResponse(
      String profile, String orders, String recommendations, String notifications) {
    return String.format("{%s, %s, %s, %s}", profile, orders, recommendations, notifications);
  }

  /**
   * Example with real HTTP calls (commented out to avoid external dependencies). Uncomment and
   * modify URLs to test with actual HTTP endpoints.
   */
  @SuppressWarnings("unused")
  private static void handleRequestWithHttpCalls(String userId) {
    Instant start = Instant.now();

    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      var url1 = URI.create("https://api.example.com/users/" + userId);
      var url2 = URI.create("https://api.example.com/orders/" + userId);
      var url3 = URI.create("https://api.example.com/recommendations/" + userId);

      Future<String> future1 = executor.submit(() -> fetchURL(url1));
      Future<String> future2 = executor.submit(() -> fetchURL(url2));
      Future<String> future3 = executor.submit(() -> fetchURL(url3));

      String response = future1.get() + future2.get() + future3.get();

      Duration elapsed = Duration.between(start, Instant.now());
      log.info(String.format("HTTP request completed in %d ms", elapsed.toMillis()));

    } catch (ExecutionException | InterruptedException e) {
      log.severe("HTTP request failed: " + e.getMessage());
    }
  }

  /** Fetches content from a URL using HTTP client. */
  private static String fetchURL(URI uri) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    return response.body();
  }
}
