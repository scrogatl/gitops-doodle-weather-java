package com.doodle.weather.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class BackgroundWeatherService {

    private static final Logger log = LoggerFactory.getLogger(BackgroundWeatherService.class);
    private final Random random = new Random();

    @Scheduled(fixedRate = 4000)
    public void pollAtmosphericSensors() {
        String threadName = Thread.currentThread().getName();
        double mockPressure = 1013.25 + (random.nextDouble() * 20 - 10);
        log.debug("[Thread: {}] Background atmospheric sensor poll: {} hPa", threadName, String.format("%.2f", mockPressure));
    }

    @Scheduled(fixedRate = 7000)
    public void syncSatelliteTelemetry() {
        String threadName = Thread.currentThread().getName();
        int mockCloudCover = random.nextInt(100);
        log.debug("[Thread: {}] Satellite telemetry cloud cover sync: {}%", threadName, mockCloudCover);
    }
}
