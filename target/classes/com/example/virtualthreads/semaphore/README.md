# Semaphore-Limited Virtual Threads

This sample demonstrates capping a burst of virtual threads by wrapping submissions with a `Semaphore` that allows 1,000 tasks to run concurrently. The companion adoption guidance in the [Java 21 Virtual Threads adoption guide](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html#GUID-80A127EA-0843-41E1-A0D4-9449A872F9D6) covers rollout considerations, including sizing constraints and migration tips when layering virtual threads on top of existing workloads.

No special JVM flags are required for this example; run it with `./gradlew runSemaphoreLimitedConcurrency` or invoke the `main` class directly from your IDE.
