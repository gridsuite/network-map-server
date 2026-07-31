# Network Map Server

[![Actions Status](https://github.com/gridsuite/network-map-server/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/gridsuite/network-map-server/actions)
[![Coverage Status](https://sonarcloud.io/api/project_badges/measure?project=org.gridsuite%3Anetwork-map-server&metric=coverage)](https://sonarcloud.io/component_measures?id=org.gridsuite%3Anetwork-map-server&metric=coverage)
[![MPL-2.0 License](https://img.shields.io/badge/license-MPL_2.0-blue.svg)](https://www.mozilla.org/en-US/MPL/2.0/)

## Description

The **network-map-server** is a microservice of the [GridSuite](https://github.com/gridsuite) platform. It acts as a **read-only projection layer between the PowSyBl IIDM model and the UI** — it stores nothing and runs no heavy computation.

Its core value is the `infoType` system: the same equipment is exposed at different levels of detail (`MAP`, `TOOLTIP`, `TAB`, `FORM`, `OPERATING_STATUS`) so that each UI context only receives the data it needs. It also abstracts away IIDM internals (NODE_BREAKER vs BUS_BREAKER topology, extensions, terminal model), centralises the network store preloading strategy, and exposes JSON schemas so the frontend can dynamically discover data structures.

It provides the following capabilities:

- **Query network elements** (substations, voltage levels, lines, transformers, generators, loads, HVDC lines, shunt compensators, static var compensators, batteries, tie lines, buses, busbar sections, converter stations) with optional filtering by substation IDs and nominal voltages.
- **Retrieve element IDs**, with optional filtering by substation scope and nominal voltage.
- **Query voltage-level topology**: buses or busbar sections, feeder bays, switches, and connected equipments.
- **Retrieve network-level metadata**: countries and nominal voltages present in the network.

---

## Technical Stack

- Spring Boot (Web, Actuator)
- PowSyBl network store client (`powsybl-network-store-client`)
- Jackson JSON Schema (`jackson-module-jsonSchema`)
- API documentation: OpenAPI / Swagger (`springdoc`)
- Micrometer / Prometheus

---

## Development Scripts

Build Docker image

```shell
mvn install -DskipTests -Dpowsybl.docker.install
```

---

## Interactions with Other Microservices

```
┌──────────────────────────┐
│   network-map-server     │──► network-store-server  (read network topology and element data)
└──────────────────────────┘
```

---

## Network Store Preloading Strategy

The `PreloadingStrategy` is a parameter of the `powsybl-network-store-client` API. It controls how many HTTP requests the client makes to the network-store-server at load time. The service selects the appropriate strategy based on the request context to balance performance and memory usage:

| Strategy | When used |
|----------|-----------|
| `NONE` (lazy) | Single element query (e.g. map popup), or substation IDs provided |
| `COLLECTION` | Network-wide query without substation filter (e.g. speadsheets) |
| `ALL_COLLECTIONS_NEEDED_FOR_BUS_VIEW` | Bulk query (`/all`) with many substations (e.g. network impacts notifications) or when bus view components are requested |


---
