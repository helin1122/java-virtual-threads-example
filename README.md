# High Throughput Comparison Demo

This sample stresses an I/O-bound workload by firing 1,000 simulated API calls that each block for ~200 ms. It compares the total time and requests/second when using a conventional fixed platform-thread pool versus virtual threads.

Run it with: 
```bash
./gradlew classes
java -cp build/classes/java/main com.example.virtualthreads.throughput.HighThroughputComparison
```


# Use Semaphore to limit virtual threads concurrency

Run it with `./gradlew runSemaphoreLimitedConcurrency`

This sample demonstrates capping a burst of virtual threads by wrapping submissions with a `Semaphore` that allows 1,000 tasks to run concurrently. The companion adoption guidance in the [Java 21 Virtual Threads adoption guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html#GUID-80A127EA-0843-41E1-A0D4-9449A872F9D6) covers rollout considerations, including sizing constraints and migration tips when layering virtual threads on top of existing workloads.



# Fanout pattern demo
Run it with `./gradlew runFanoutPattern`
