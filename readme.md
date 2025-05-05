# Spring Boot with Docker Compose - Database Connection Guide

This guide explains how a Spring Boot application connects to a MySQL database using Docker Compose, focusing on the `SPRING_DATASOURCE_URL` environment variable and the networking magic of Docker Compose.

## Table of Contents
- [Project Overview](#project-overview)
- [Docker Compose Setup](#docker-compose-setup)
- [Understanding `SPRING_DATASOURCE_URL`](#understanding-spring_datasource_url)
- [How Spring Boot Connects to MySQL](#how-spring-boot-connects-to-mysql)
- [Key Notes](#key-notes)

## Project Overview
This project demonstrates a Spring Boot application containerized with Docker and connected to a MySQL database using Docker Compose. The setup includes:
- A Spring Boot application (`spring_app`) running in a Docker container.
- A MySQL database (`mysql_db`) running in a separate container.
- Docker Compose to orchestrate both services and enable seamless communication.

The Spring Boot application connects to MySQL using environment variables defined in the `docker-compose.yml` file, specifically `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`.

## Docker Compose Setup
The `docker-compose.yml` file defines two services: `mysql_db` (MySQL) and `spring_app` (Spring Boot). Below is a simplified version of the configuration:

```yaml
version: '3.8'
services:
  mysql_db:
    image: mysql:8.0.27
    container_name: mysql_container
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: learn
      MYSQL_USER: springstudent
      MYSQL_PASSWORD: springstudent
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    restart: unless-stopped

  spring_app:
    build: .
    container_name: spring_container
    ports:
      - "8080:8081"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql_db:3306/learn?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: springstudent
      SPRING_DATASOURCE_PASSWORD: springstudent
    depends_on:
      - mysql_db

volumes:
  mysql_data:
```

### Key Points:
- `mysql_db` is the service name for the MySQL container, and it creates a database named `learn` with a user `springstudent`.
- `spring_app` is built from a `Dockerfile` and depends on `mysql_db`, ensuring MySQL starts first.
- The `SPRING_DATASOURCE_*` environment variables configure the database connection for Spring Boot.

## Understanding `SPRING_DATASOURCE_URL`

The `SPRING_DATASOURCE_URL` environment variable tells Spring Boot how to connect to the MySQL database. Let's break down its value:

```yaml
SPRING_DATASOURCE_URL: jdbc:mysql://mysql_db:3306/learn?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
```

### Components of the URL:
1. **`jdbc:mysql://`**
   - This is the protocol prefix for JDBC (Java Database Connectivity) when connecting to a MySQL database.
   - It tells Spring Boot to use the MySQL JDBC driver to communicate with the database.

2. **`mysql_db`**
   - This is **not** an IP address or `localhost`. It is the **service name** of the MySQL container defined in `docker-compose.yml`.
   - Docker Compose creates an internal DNS (Domain Name System) that automatically resolves `mysql_db` to the IP address of the MySQL container.
   - **Why not `localhost`?** Each container has its own isolated network stack. `localhost` inside the `spring_app` container refers to itself, not the `mysql_db` container. Using the service name (`mysql_db`) ensures proper communication.

3. **`:3306`**
   - This is the port where MySQL is running inside the `mysql_db` container.
   - MySQL uses `3306` by default, and the `docker-compose.yml` exposes this port (`"3306:3306"`).

4. **`/learn`**
   - This specifies the database name (`learn`) that Spring Boot will connect to.
   - The `mysql_db` service is configured to create this database automatically (via `MYSQL_DATABASE: learn`).

5. **`?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true`**
   - These are additional connection parameters:
     - `createDatabaseIfNotExist=true`: Instructs MySQL to create the `learn` database if it doesn't already exist (requires proper user permissions).
     - `useSSL=false`: Disables SSL for the connection, which is common in local or testing environments to simplify setup.
     - `allowPublicKeyRetrieval=true`: Allows the JDBC driver to retrieve the MySQL server's public key, which is sometimes required for certain authentication methods.

### Other Environment Variables:
- **`SPRING_DATASOURCE_USERNAME: springstudent`**
  - The MySQL username for authentication, matching the `MYSQL_USER` defined in the `mysql_db` service.
- **`SPRING_DATASOURCE_PASSWORD: springstudent`**
  - The password for the `springstudent` user, matching the `MYSQL_PASSWORD`.

## How Spring Boot Connects to MySQL

1. **Docker Compose Startup**:
   - Docker Compose reads `docker-compose.yml` and starts the `mysql_db` container first (since `spring_app` depends on it).
   - The `mysql_db` container initializes MySQL, creates the `learn` database, and sets up the `springstudent` user.
   - Then, the `spring_app` container is built (using the `Dockerfile`) and started.

2. **Spring Boot Configuration**:
   - Spring Boot reads the environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`) from the `docker-compose.yml`.
   - These values are typically injected into the application's configuration, such as `application.properties` or `application.yml`. For example:

     ```properties
     spring.datasource.url=${SPRING_DATASOURCE_URL}
     spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
     spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
     ```

   - Spring Boot uses the JDBC driver to establish a connection to `mysql_db:3306`, authenticating with `springstudent` and accessing the `learn` database.

3. **Docker Networking**:
   - Docker Compose creates a default network for the services, allowing containers to communicate using their service names (`mysql_db` resolves to the MySQL container's IP).
   - The `depends_on: [mysql_db]` ensures that the Spring Boot application doesn't start until MySQL is ready.

4. **Schema Initialization**:
   - If you're using Spring Data JPA with Hibernate, Spring Boot can automatically create or update the database schema based on your entity classes (if configured with `spring.jpa.hibernate.ddl-auto=create` or `update`).

## Key Notes
- **Avoid Hardcoding Database Details**: By using environment variables (e.g., `${SPRING_DATASOURCE_URL}`), you make the application portable and easier to configure across environments (local, staging, production).
- **Debugging Connection Issues**:
  - Check logs for both containers:
    ```bash
    docker-compose logs spring_app
    docker-compose logs mysql_db
    ```
  - Ensure `mysql_db` is fully started before `spring_app` attempts to connect (Docker Compose handles this via `depends_on`, but delays can occur).
- **Networking Tip**: Always use the service name (`mysql_db`) instead of `localhost` or an IP address in the `SPRING_DATASOURCE_URL` when running in Docker Compose.
- **Volumes for Persistence**: The `mysql_data` volume ensures that MySQL data persists even if the container is stopped or removed.

## Running the Project
1. Build the Spring Boot JAR:
   ```bash
   ./mvnw clean package -DskipTests
   ```
2. Start the containers:
   ```bash
   docker-compose up --build
   ```
3. Access the Spring Boot application at `http://localhost:8080`.
4. Stop the containers:
   ```bash
   docker-compose down
   ```
