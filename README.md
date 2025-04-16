# IOT-project-Hcmut

A Spring Boot application for monitoring and controlling IoT devices through Adafruit IO.

## 🌟 Features

- Real-time monitoring of environmental sensors (temperature, humidity, soil moisture, light)
- Automated device control based on configurable thresholds
- Notification system for out-of-range sensor values
- OAuth2 authentication with Google
- REST API for device control and data retrieval

## 🛠 Technologies

- Java 21+
- Spring Boot
- MongoDB Atlas (data storage)
- Redis (caching/session management)
- Adafruit IO (MQTT broker)
- Docker & Docker Compose
- JWT for API authentication

## ✅ Prerequisites

- JDK 21 or higher
- Maven 3.9+
- Docker and Docker Compose (for containerized deployment)
- Adafruit IO account
- MongoDB Atlas account
- Redis instance (local or cloud, e.g., Upstash)

---

## 🚀 Setup Instructions

### 1. Clone the repository

```bash
git clone https://github.com/mp777888/IOT-project-Hcmut.git
cd IOT-project-Hcmut
```

### 2. Configure application properties
- Create configuration files from templates:
```bash
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml
cp src/main/resources/application-prod.yml.template src/main/resources/application-prod.yml
cp docker-compose.yml.template docker-compose.yml
```


### 3. Update the configuration files with your credentials:
