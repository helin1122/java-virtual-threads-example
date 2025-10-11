# High Throughput Comparison Demo

This sample stresses an I/O-bound workload by firing 1,000 simulated API calls that each block for ~200 ms. It compares the total time and requests/second when using a conventional fixed platform-thread pool versus virtual threads.

Run it with `./gradlew runHighThroughputComparison`


# Semaphore-Limited Concurrency Demo

This sample demonstrates limiting the concurrency of virtual threads using a `Semaphore`. It submits 5,000 tasks but caps concurrency at 1,000 concurrent virtual threads, showing how to control resource usage while still benefiting from virtual thread efficiency.

Run it with `./gradlew runSemaphoreLimitedConcurrency`



# Fanout Pattern Demo

This sample demonstrates the fanout pattern where each request spawns multiple concurrent sub-tasks. It processes 100 requests, with each request fanning out to 4 simulated services (user profile, orders, recommendations, notifications) for a total of 400 concurrent operations. It compares performance between platform thread pools and virtual threads, showing how virtual threads excel at high-concurrency fanout scenarios without the resource constraints of platform thread pools.

Run it with `./gradlew runFanoutPattern`
