# High Throughput Comparison Demo

This sample stresses an I/O-bound workload by firing 1,000 simulated API calls that each block for ~200 ms. It compares the total time and requests/second when using a conventional fixed platform-thread pool versus virtual threads.


# Semaphore-Limited Concurrency Demo

This sample demonstrates limiting the concurrency of virtual threads using a `Semaphore`. It submits 5,000 tasks but caps concurrency at 1,000 concurrent virtual threads, showing how to control resource usage while still benefiting from virtual thread efficiency.