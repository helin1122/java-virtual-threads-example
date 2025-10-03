package com.example.virtualthreads;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class VirtualThreadsApplication {

    private static final Logger log = LoggerFactory.getLogger(VirtualThreadsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(VirtualThreadsApplication.class, args);
    }

    @Bean
    CommandLineRunner virtualThreadDemo() {
        return args -> {
            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                IntStream.range(0, 3)
                        .forEach(index -> executor.submit(() -> {
                            log.info("Start task {} on {}", index, Thread.currentThread());
                            Thread.sleep(Duration.ofMillis(200));
                            log.info("Finish task {} on {}", index, Thread.currentThread());
                            return index;
                        }));
            }
        };
    }
}
