# Checklist_Backend

This API backend gives out saved checklist data with data from my train and weather API to my Tablet, so that it can display them there in the morning for me.

This repository belongs with its counterpart [Checklist_Frontend](https://github.com/Laurens-Hertzer/Checklist_Frontend) to the Morning Checklist System.

## Documentation

### API Documentation

When the backend is running, open Swagger UI at `http://localhost:8080/swagger-ui/index.html`. The OpenAPI specification is available at `http://localhost:8080/v3/api-docs`.


### Tests Documentation

The Phase 4 test overview is in [`docs/testing.md`](docs/testing.md), and the
testing process and decisions are documented in [`docs/phase4.md`](docs/phase4.md). Since those both are for my school, they're written in german.
Run automated tests with `.\mvnw.cmd test`.

## Postman negative tests

Import `postman/Checklist_Backend.postman_collection.json` for successful requests and `postman/Checklist Backend Bad Tests.postman_collection.json` for negative requests. Both collections use `{{baseUrl}}`, defaulting to `http://localhost:8080`. Run the successful collection in order because later requests use IDs saved by the create requests. External train and weather requests require their respective APIs.

## Technology

This backend is written in Java with Springboot.

A MariaDB Database is used for longterm data storage for this program. The Docker Compose for the Database is also in this repository.

## Deployment

WIP