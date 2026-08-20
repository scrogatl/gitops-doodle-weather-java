package com.doodle.weather;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
public class WeatherController {

    private static final Logger log = LoggerFactory.getLogger(WeatherController.class);

    private static final String[] SUMMARIES = new String[] {
            "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
    };

    private final Random random = new Random();
    private final Executor forecastExecutor;
    private final Executor radarExecutor;

    @Value("${SHARD:na}")
    private String shard;

    public WeatherController(
            @Qualifier("forecastExecutor") Executor forecastExecutor,
            @Qualifier("radarExecutor") Executor radarExecutor
    ) {
        this.forecastExecutor = forecastExecutor;
        this.radarExecutor = radarExecutor;
    }

    @GetMapping("/weatherforecast")
    public List<WeatherForecast> getWeatherForecast() throws Exception {
        log.info("Request received on thread: {}", Thread.currentThread().getName());

        // Asynchronously compute weather components across dedicated worker threads
        CompletableFuture<Integer> tempFuture = CompletableFuture.supplyAsync(() -> {
            log.info("Computing temperature on worker thread: {}", Thread.currentThread().getName());
            try {
                Thread.sleep(20 + random.nextInt(30)); // simulate brief computation
            } catch (InterruptedException ignored) {}
            return random.nextInt(75) - 20; // range [-20, 55)
        }, forecastExecutor);

        CompletableFuture<String> summaryFuture = CompletableFuture.supplyAsync(() -> {
            log.info("Fetching summary condition on worker thread: {}", Thread.currentThread().getName());
            return SUMMARIES[random.nextInt(SUMMARIES.length)];
        }, forecastExecutor);

        CompletableFuture<Void> radarFuture = CompletableFuture.runAsync(() -> {
            log.info("Syncing radar data on radar thread: {}", Thread.currentThread().getName());
            try {
                Thread.sleep(15 + random.nextInt(25));
            } catch (InterruptedException ignored) {}
        }, radarExecutor);

        // Wait for all worker threads to complete
        CompletableFuture.allOf(tempFuture, summaryFuture, radarFuture).join();

        int tempC = tempFuture.get();
        String summary = summaryFuture.get();
        LocalDate date = LocalDate.now().plusDays(1);

        WeatherForecast forecast = new WeatherForecast(date, tempC, summary);
        return List.of(forecast);
    }

    @GetMapping("/threads")
    public Map<String, Object> getThreads() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);

        List<Map<String, Object>> threadsList = new ArrayList<>();
        for (ThreadInfo info : threadInfos) {
            Map<String, Object> threadData = new LinkedHashMap<>();
            threadData.put("id", info.getThreadId());
            threadData.put("name", info.getThreadName());
            threadData.put("state", info.getThreadState().toString());
            threadData.put("isSuspended", info.isSuspended());
            threadData.put("inNative", info.isInNative());
            threadsList.add(threadData);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("threadCount", threadMXBean.getThreadCount());
        response.put("peakThreadCount", threadMXBean.getPeakThreadCount());
        response.put("daemonThreadCount", threadMXBean.getDaemonThreadCount());
        response.put("threads", threadsList);
        return response;
    }

    @GetMapping("/")
    public String index() {
        return "Weather Java Service (" + shard + ") - Multi-threaded";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
