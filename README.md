# Checklist_Backend

This API backend gives out saved checklist data with data from my train and weather API to my Tablet, so that it can display them there in the morning for me.

This repository belongs with its counterpart [Checklist_Frontend](https://github.com/Laurens-Hertzer/Checklist_Frontend) to the Morning Checklist System.

Tomorrow's forecast for Dietlikon is available at `GET /api/weather`. The previous forecast path, `GET /api/weather/forecast`, remains available as an alias. The response includes morning (09:00), midday (14:00), evening (19:00), and night (23:00), with temperature, a German weather description and WMO code, precipitation probability, and wind speed. The location and coordinates can be changed with `WEATHER_LOCATION`, `WEATHER_LATITUDE`, and `WEATHER_LONGITUDE`. Forecast data is provided by [Open-Meteo](https://open-meteo.com/); attribution is required when displaying or redistributing it.

## API documentation

When the backend is running, open Swagger UI at `http://localhost:8080/swagger-ui/index.html`. The OpenAPI specification is available at `http://localhost:8080/v3/api-docs`.

## Technology

This backend is written in Java with Springboot.

A PostgreSQL Database is used for longterm data storage for this program. The Docker Compose for the Database is also in this repository.

## Tests

WIP

## Deployment

WIP