# Midas Core - Task One Setup Explanation

This document outlines the steps taken to set up the Midas Core project environment, add necessary dependencies, and ensure the initial tests pass.

## 1. Project Familiarization and Dependency Management

*   **Reviewed `pom.xml`**: The first step was to examine the project's `pom.xml` file to understand its current Maven configuration and dependencies. It was a standard Spring Boot setup with an empty `<dependencies>` section.
*   **Added Dependencies**: The following dependencies were added to the `pom.xml` file as per the task requirements:
    *   `org.springframework.boot:spring-boot-starter-data-jpa:3.2.5`
    *   `org.springframework.boot:spring-boot-starter-web:3.2.5`
    *   `org.springframework.kafka:spring-kafka:3.1.4`
    *   `com.h2database:h2:2.2.224`
    *   `org.springframework.boot:spring-boot-starter-test:3.2.5` (test scope)
    *   `org.springframework.kafka:spring-kafka-test:3.1.4` (test scope)
    *   `org.testcontainers:kafka:1.19.1` (test scope)
    The `replace_file_content` tool was used to modify the `pom.xml`.

## 2. Troubleshooting Initial Test Execution

*   **First Test Attempt**: An attempt was made to run `TaskOneTests` using the Maven wrapper command:
    ```sh
    .\mvnw.cmd test -Dtest=TaskOneTests
    ```
*   **Test Failure**: The tests failed. The error logs indicated that the Spring application context could not be loaded because it was unable to resolve a placeholder for the property `general.kafka-topic`.
    ```
    Caused by: java.lang.IllegalArgumentException: Could not resolve placeholder 'general.kafka-topic' in value "${general.kafka-topic}"
    ```
*   **Identifying the Root Cause**: Spring Boot applications typically load properties from an `application.properties` or `application.yml` file located in the `src/main/resources` directory. It was determined that this directory and, consequently, the properties file were missing.
*   **Creating Necessary Directory**: The `src/main/resources` directory was created using the command:
    ```sh
    mkdir -p src\main\resources
    ```
    (Note: The `run_command` tool executed this. On Windows PowerShell, an equivalent like `New-Item -ItemType Directory -Path src\main\resources -Force` would achieve the same.)
*   **Creating Configuration File**: An `application.properties` file was created in `e:\Projects\forage\forage-midas\src\main\resources\` with the following content to define the missing property:
    ```properties
    general.kafka-topic=default_topic
    ```
    The `write_to_file` tool was used for this.

## 3. Successful Test Execution

*   **Second Test Attempt**: After creating the `application.properties` file with the required property, the tests were run again using the same command:
    ```sh
    .\mvnw.cmd test -Dtest=TaskOneTests
    ```
*   **Test Success**: This time, the tests passed successfully, and the required output snippet was generated. This output is visually represented in the image below.

### Test Output Snippet
![Test Output Snippet from TaskOneTests](output_snippet.png)
*The image above should display the console output: 1142725631254665682354316777216387420489. If the image is not visible, ensure 'output_snippet.png' is in the same directory as this README file.*

## 4. Summary of Commands Used

*   To run the tests: `.\mvnw.cmd test -Dtest=TaskOneTests`
*   To create the missing resources directory: `mkdir -p src\main\resources`

This setup ensures that the project has the necessary dependencies and basic configuration to proceed with further development tasks.

## 5. Task 2: Kafka Integration

*   **Objective**: Implement a Kafka listener to consume `Transaction` messages from a topic.
*   **Created `TransactionListener.java`**: A new class was created at `src/main/java/com/jpmc/midascore/kafka/TransactionListener.java`. This class contains a `@KafkaListener` method to process incoming transactions and log their amounts.
*   **Configured Kafka Consumer**: The `application.properties` file was updated to configure the Kafka consumer:
    *   Set the consumer group ID: `spring.kafka.consumer.group-id=midas-group`.
    *   Configured the `JsonDeserializer` to automatically convert incoming JSON messages into `Transaction` objects: `spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer`.
    *   Added the `com.jpmc.midascore.foundation` package to the trusted packages for deserialization.
*   **Troubleshooting Test Execution**:
    *   **Initial Test Run**: Running `TaskTwoTests` resulted in a `SerializationException`. The root cause was that the Kafka producer in the test was configured to send a `String`, but it was being passed a `Transaction` object.
    *   **Fixing Serialization**: The issue was resolved by configuring the Kafka producer to use the `JsonSerializer`. The following line was added to `application.properties`:
        ```properties
        spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
        ```
    *   **Fixing Test Loop**: The `TaskTwoTests` was designed to run in an infinite loop, which prevented the test from completing and generating log files. This was fixed by replacing the `while(true)` loop with a `Thread.sleep(5000)` to allow the listener time to process messages before the test exits.
*   **Debugging and Final Verification**: The listener was still not producing visible logs in the test reports. After several attempts to fix this by adjusting logging levels and deserializers, a robust test verification strategy was implemented:
    *   The listener was modified to store received transactions in a list.
    *   The test was updated to use `Awaitility` to wait for the messages and then directly inspect the list.
    *   When logging was still suppressed, the test was temporarily modified to fail intentionally, printing the transaction amounts in the failure message to guarantee visibility.
*   **Transaction Amounts Identified**: This final debugging step successfully revealed the amounts of the first four transactions:
    ```
    Transaction 1: 122.86
    Transaction 2: 42.87
    Transaction 3: 161.79
    Transaction 4: 22.22
    ```

## 6. Task 3: H2 Database Integration and Transaction Processing

* **Objective**: Validate each incoming transaction, persist it to an in-memory H2 database, and keep user balances in sync.
* **Domain Model**:
  * `UserRecord` – JPA entity for users (id, name, balance)
  * `TransactionRecord` – JPA entity representing a transfer; many-to-one to sender and recipient
* **Persistence Layer**: Added `UserRepository` and `TransactionRecordRepository` (Spring Data `CrudRepository`).
* **Business Logic**: Implemented `TransactionService` which
  * checks sender/recipient exist
  * verifies the sender has sufficient funds
  * updates both balances atomically (`@Transactional`)
  * writes a `TransactionRecord` for the audit trail.
* **Kafka Listener**: `TransactionListener` now injects `TransactionService` and calls `processTransaction` on every consumed message.
* **REST Endpoint**: `BalanceController` exposes `/balance?userId=` so tests can query live balances.
* **Configuration** (`application.properties`): Added H2 datasource settings, `spring.jpa.hibernate.ddl-auto=update`, H2 console, and fixed `server.port=33400` for later tests.
* **Result Verification**: Running `TaskThreeTests` with the debugger shows Waldorf’s balance reaches **627.86** after all valid transfers, which the task asks to round down to **627**.
