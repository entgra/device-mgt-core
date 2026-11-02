# Entgra Device Management Core

<a href='https://opensource.org/licenses/Apache-2.0'><img src='https://img.shields.io/badge/License-Apache%202.0-blue.svg'></a><br/>

<a href='#'><img src="https://builder.entgra.net/buildStatus/icon?job=device-mgt-core"></a>

This repository provides the core services, APIs, UI-facing infrastructure and
deployment features used to assemble Entgra device-management products. Product
distributions, platform plugins and complete UI and device-agent workflows also
depend on other repositories.

## Repository structure

- `components/` contains domain services, shared contracts, persistence,
  integrations, API webapps and UI request handling.
- `features/` packages bundles, webapps, database scripts, configuration and
  templates for installation into the product runtime.
- The [root POM](pom.xml) defines the Maven reactor and dependency versions.

## Development guidance

Builds and tests use JDK 11. Follow [AGENTS.md](AGENTS.md) for repository guidance
and verification commands. Component specifications live under
`components/<component>/Specs/` where available:

- [Application Management](components/application-mgt/specs/README.md)
- [Device Management](components/device-mgt/Specs/README.md)
- [Realtime Notification Management](components/realtime-notification-mgt/Specs/README.md)

Architecture constraints are described in the
[OSGi component constitution](docs/constitutions/OSGI_COMPONENT_CONSTITUTION.md)
and [CXF webapp constitution](docs/constitutions/CXF_WEBAPP_CONSTITUTION.md).
