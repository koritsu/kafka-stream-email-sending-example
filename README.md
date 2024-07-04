Here's a README for your Kafka Streams-based email sending application, based on the provided zip file:

---

# Email Sending Application

This project is a Kafka Streams-based application that processes real-time email requests and enriches them with user information. It demonstrates the use of Kafka Streams to join a KStream of email requests with a GlobalKTable of user information.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Kafka Streams Components](#kafka-streams-components)
- [Setup and Installation](#setup-and-installation)
- [Running the Application](#running-the-application)
- [Example Data](#example-data)
- [Contributing](#contributing)
- [License](#license)

## Introduction

The Email Sending Application demonstrates the use of Kafka Streams to process and join real-time data streams. It reads email request messages from a Kafka topic, enriches these messages with user information from another Kafka topic, and processes the enriched data.

## Features

- Processes email requests in real-time.
- Enriches email requests with user information.
- Demonstrates the use of Kafka Streams KStream and GlobalKTable for data processing.
- Handles real-time data joins.

## Kafka Streams Components

### KStream

KStream is a Kafka topic's continuous stream of records. This application uses KStream to process the email requests.

### GlobalKTable

GlobalKTable is similar to KTable but stores all partitions' data locally. This is useful for enriching streams with data that needs to be globally accessible.

## Setup and Installation

### Prerequisites

- Java 11
- Kafka and Zookeeper
- Docker (for Kafka and Zookeeper setup)

### Installation Steps

1. **Clone the repository**:
   ```sh
   git clone https://github.com/your-username/email-sending-application.git
   cd email-sending-application
   ```

2. **Set up Kafka and Zookeeper using Docker**:
   ```sh
   docker-compose up -d
   ```

3. **Build the application using Gradle**:
   ```sh
   ./gradlew clean build
   ```

## Running the Application

1. **Start the Kafka Streams application**:
   ```sh
   ./gradlew run
   ```

2. **Produce sample data to Kafka topics**:
   Use the provided `OrderProducer` and `UserProducer` classes to send sample data to the `OrderStream` and `UserTableTopic` topics.

## Add Kafka Topic
1. **Run shell on kafka in docker**:
   ```sh
   docker-compose exec kafka /bin/sh
   ```
2. **Create topics**:
   ```sh
   kafka-topics.sh --create --topic EmailRequestsTopic --zookeeper zookeeper:2181 --partitions 1 --replication-factor 1
   kafka-topics.sh --create --topic UserTableTopic --zookeeper zookeeper:2181 --partitions 1 --replication-factor 1

   ```
