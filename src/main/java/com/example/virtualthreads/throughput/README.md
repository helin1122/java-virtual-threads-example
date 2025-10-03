# High Throughput Comparison

This sample stresses an I/O-bound workload by firing 1,000 simulated API calls that each block for ~200 ms. It compares the total time and requests/second when using a conventional fixed platform-thread pool versus virtual threads.

## Running

```bash
./gradlew classes
java -cp build/classes/java/main com.example.virtualthreads.throughput.HighThroughputComparison
```
