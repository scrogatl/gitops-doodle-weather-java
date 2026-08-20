package com.doodle.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record WeatherForecast(
        LocalDate date,
        int temperatureC,
        String summary
) {
    @JsonProperty("temperatureF")
    public int temperatureF() {
        return 32 + (int) (temperatureC / 0.5556);
    }
}
