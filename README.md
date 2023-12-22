<div align="center">
 
<h1>Blockchain Connector</h1>
<span>by </span><a href="https://in2.es">in2.es</a>
<p><p>

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=alert_status)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)

[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=bugs)](https://sonarcloud.io/summary/new_code?id=in2workspace_blockchain-connector)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=security_rating)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=in2workspace_blockchain-connector)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=ncloc)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=coverage)](https://sonarcloud.io/summary/new_code?id=in2workspace_blockchain-connector)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=in2workspace_blockchain-connector)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=in2workspace_blockchain-connector)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=in2workspace_blockchain-connector&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=in2workspace_blockchain-connector)

</div>

## Introduction
The Blockchain Connector serves as a crucial component within the Access Node architecture, facilitating interaction between off-chain storage and on-chain storage. This RESTful API exposes a set of endpoints designed for seamless communication between the Context Broker and Blockchain.

#### Technologies
The Blockchain Connector is constructed using the following technologies:

- Java 17: The core programming language for building the connector.
- Spring Boot 3.x: A framework that simplifies the development of Java-based applications, providing a robust and efficient platform.
- Gradle: A build automation tool that manages the project's dependencies and facilitates the build process.

## Functionalities
The key functionalities of the Blockchain Connector include:

- **Interaction with Context Broker:** The connector provides endpoints for fetching and updating data from the Context Broker, ensuring synchronization between off-chain and on-chain data.

- **Blockchain Operations:** Users can perform various blockchain operations, such as submitting transactions, retrieving transaction details, and monitoring blockchain events.
- **Subscribe to New Entities:** Enables subscription to new entities stored in the Context Broker.
- **Create Blockchain Events:** Generates Blockchain Events with detailed data from the Context Broker Entity as attribute values.
- **Publish Blockchain Events:** Sends Blockchain Events to a configured Blockchain Node.
- **Query Blockchain Events:** Allows querying Blockchain Events to the Blockchain Node using Event Metadata as filters.
- **Retrieve Entity Data:** Retrieves Entity Data using the 'data location' attribute of the Blockchain Event.
- **Publish Retrieved Entities:** Publishes retrieved Entities to the configured Context Broker.

## Installation
### Prerequisites
To install the blockchain connector, the following prerequisites are required:
- Docker or Docker Desktop

### Image installation / Component setup

#### Introduction

For the installation of the component and its remaining dependencies, we will create a docker-compose file that allows us to configure containers from the images needed to install our entire environment. This setup ensures the proper functioning of the Blockchain Connector along with its dependencies, which will also be installed.


#### Component Overview

In the docker-compose.yml file, the definition of services is structured collectively under the services section, following the specified header in the document:

```yaml
version: "3.8"
# services: 
# Following components will be defined here
```

## `mkt1-blockchain-connector`
- **Description:** The primary component responsible for connecting the Marketplace to the blockchain network.
- **Image:** `in2kizuna/blockchain-connector:v1.0.0-SNAPSHOT`
- **Environment Variables:**
  - `DATABASE_URL`: R2DBC URL for the transaction database.
  - `FLYWAY_URL`: Flyway migration URL for the transaction database.
  - `ORGANIZATION_ID`: Identifier for the organization.
- **Ports:** `8081:8080`
- **Volumes:** Binds `./marketplace1-config.yml` to `/src/main/resources/external-config.yml`.
- **Links:** Connected to `mkt1-broker-adapter`, `mkt1-dlt-adapter`, `mkt1-postgres`.
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-blockchain-connector:
  container_name: mkt1-blockchain-connector
  image: in2kizuna/blockchain-connector:v1.0.0-SNAPSHOT
  environment:
    - "DATABASE_URL=r2dbc:postgresql://mkt1-postgres:5432/mkt1db"
    - "FLYWAY_URL=jdbc:postgresql://mkt1-postgres:5432/mkt1db"
    - "ORGANIZATION_ID=VATES-00869735"
  ports:
    - "8081:8080"
  volumes:
    - ./marketplace1-config.yml:/src/main/resources/external-config.yml
  links:
    - mkt1-broker-adapter
    - mkt1-dlt-adapter
    - mkt1-postgres
  networks:
    - local_network
```

## `mkt1-dlt-adapter`
- **Description:** Adapter component facilitating simplified operations with the DLT (Distributed Ledger Technology), in order to create a Blockchain node.
- **Image:** `aleniet/dlt-adapter:1.1`
- **Environment Variables:**
  - `PRIVATE_KEY`: Private key for DLT interactions.
- **Ports:** `8082:8080`
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-dlt-adapter:
  container_name: mkt1-dlt-adapter
  image: aleniet/dlt-adapter:1.1
  environment:
    - "PRIVATE_KEY=0xe2afef2c880b138d741995ba56936e389b0b5dd2943e21e4363cc70d81c89346"
  ports:
    - "8082:8080"
  networks:
    - local_network
```

## `mkt1-broker-adapter`
- **Description:** Adapter component providing simplified interactions with the Context Broker.
- **Image:** `in2kizuna/broker-adapter:v1.0.0-SNAPSHOT`
- **Ports:** `8083:8080`
- **Volumes:** Binds `./marketplace1-config.yml` to `/src/main/resources/external-config.yml`.
- **Links:** Connected to `mkt1-context-broker`.
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-broker-adapter:
  container_name: mkt1-broker-adapter
  image: in2kizuna/broker-adapter:v1.0.0-SNAPSHOT
  ports:
    - "8083:8080"
  volumes:
    - ./marketplace1-config.yml:/src/main/resources/external-config.yml
  links:
    - mkt1-context-broker
  networks:
    - local_network
```

## `mkt1-context-broker`
- **Description:** Manages context information, facilitating communication between components.
- **Image:** `fiware/orion-ld:latest`
- **Command:** Configured with MongoDB host and port.
- **Ports:** `1027:1026`
- **Links:** Connected to `mkt1-mongo`.
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-context-broker:
  container_name: mkt1-context-broker
  image: fiware/orion-ld:latest
  restart: always
  command: "-dbhost mkt1-mongo -port 1026"
  ports:
    - "1027:1026"
  links:
    - mkt1-mongo
  networks:
    - local_network
```

## `mkt1-mongo`
- **Description:** NoSQL database for storing context information.
- **Image:** `mongo:4.4`
- **Command:** Disables journaling for improved performance.
- **Volumes:** Mounts volumes for data and configuration.
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-mongo:
  container_name: mkt1-mongo
  image: mongo:4.4
  command: "--nojournal"
  volumes:
    - mkt1_data:/data/db
    - mkt1_db_config:/data/configdb
  networks:
    - local_network
```

## `mkt1-postgres`
- **Description:** Relational database for recording and monitoring transactions, both on-chain and off-chain.
- **Image:** `postgres:11`
- **Ports:** `5433:5432`
- **Environment Variables:**
  - `POSTGRES_PASSWORD`: Password for PostgreSQL.
  - `POSTGRES_USER`: PostgreSQL user.
  - `POSTGRES_DB`: PostgreSQL database.
- **Hostname:** `mkt1-postgres`
- **Networks:** Connected to `local_network`.

**Docker Compose Configuration:**
```yaml
mkt1-postgres:
  container_name: mkt1-postgres
  image: postgres:11
  ports:
    - "5433:5432"
  environment:
    - POSTGRES_PASSWORD=postgres
    - POSTGRES_USER=postgres
    - POSTGRES_DB=mkt1db
  hostname: mkt1-postgres
  networks:
    - local_network
```





## Configuration
In addition to the `docker-compose.yml` file, it is necessary to create a configuration file that includes, at a minimum, the following fields for proper functionality. The example configuration provided aligns with the previously defined components in the Docker Compose setup.

**Example `marketplace1-config.yml`:**

```yaml
# DLT Adapter Configuration
dlt-adapter:
  domain: http://mkt1-dlt-adapter:8080
```
This section configures the DLT (Distributed Ledger Technology) Adapter, specifying the domain where the adapter can be accessed.

```yaml
# Blockchain Configuration
blockchain:
  rpcAddress: https://red-t.alastria.io/v0/9461d9f4292b41230527d57ee90652a6
  userEthereumAddress: "0xb794f5ea0ba39494ce839613fffba74279579268"
  subscription:
    active: false
    notificationEndpoint: http://mkt1-blockchain-connector:8080/notifications/dlt
    eventTypes: >
      ProductOffering
```
This section provides configuration details for the Blockchain component. It includes the RPC (Remote Procedure Call) address for blockchain interactions, the Ethereum address associated with the user, and subscription settings for receiving blockchain events. The subscription is currently inactive (`active: false`), but it specifies the notification endpoint and event types if activated.

```yaml
# Broker Adapter Configuration
broker-adapter:
  domain: http://mkt1-broker-adapter:8080
  openapi:
    url: http://localhost:8080
```
Here, the Broker Adapter is configured with its domain and an OpenAPI URL. The domain specifies where the adapter is accessible, and the OpenAPI URL indicates the location of the OpenAPI configuration.

```yaml
# Context Broker Configuration
broker:
  externalDomain: https://<example.com>/orion-ld
  internalDomain: http://mkt1-context-broker:1026
```
This section configures the Context Broker with both external and internal domains. The external domain is where the Context Broker is accessible from outside, and the internal domain is its address within the environment.

```yaml
# NGSI Subscription Configuration
ngsi-subscription:
  notificationEndpoint: http://mkt1-blockchain-connector:8080/notifications/broker
  entityTypes: >
    ProductOffering
```
Lastly, the NGSI Subscription Configuration specifies the notification endpoint for NGSI (Next Generation Service Interface) subscriptions and the entity types for which subscriptions are activated. In this case, the example includes a subscription for the "ProductOffering" entity type.

## Usage

After implementing both the `docker-compose.yml` and configuration files, the next step is to compose the Docker services with the command:

`docker-compose up -d`

Subsequently, within Docker, wait for all components to initialize properly. Once everything has initialized successfully (in case of any issues, double-check the endpoint addresses in the configuration file), you can test its functionality by making POST, PATCH or DELETE entities to the Context Broker of the type to which it has subscribed.

Users can utilize tools like POSTMAN for entity management, ensuring that the entity type matches the type established for subscription beforehand.

If you wish to create a new instance serving to receive entities and publish them to the Context Broker within an offChain process, simply change the subscriptions to the DLT adapter to (`active: true`).

Additionally, you can create multiple instances of the Blockchain Connector with variations in subscription types or DLT subscription activation. Repeat the same installation process mentioned above for each instance.




## Contribution

## License


## Project/Component Status
This project is in version 1.0.0 of the MVP (Minimum Viable Product) for the Blockchain Connector at 12/04/2023.

## Contact

For any inquiries or further information, feel free to reach out to us:

- **Email:** [info@in2.es](mailto:info@in2.es)
- **Name:** IN2, Ingeniería de la Información
- **Website:** [https://in2.es](https://in2.es)

## Acknowledgments


## Creation Date and Last Update
This project was created on July 07, 2023, and last updated on December 4, 2023.
