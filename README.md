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
- Create .env files and add your information:
```bash
SERVER_PORT=
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
MONGODB_URI=
MONGODB_DATABASE=
REDIS_URL=
ADAFRUIT_USERNAME=
ADAFRUIT_KEY=
JWT_SIGNER_KEY=
NOTIFICATION_EMAIL=
```


### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application
#### Using Docker Compose

```bash
docker-compose up --build
```
#### Using Maven

```bash
mvn spring-boot:run
```
### 5. Access the application
- Open your web browser and navigate to `http://localhost:8080`
- You can also access the Adafruit IO dashboard at `https://io.adafruit.com/`
- For the REST API, use tools like Postman or curl to interact with the endpoints.
- To visualize the data and control the devices, you can clone and run the Frontend repository separately. The forked repository is available at: [https://github.com/mp777888/Ecology_DADN-fork.git](https://github.com/mp777888/Ecology_DADN-fork.git)
