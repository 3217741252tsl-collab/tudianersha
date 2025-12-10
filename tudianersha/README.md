# Tudianersha System

This is a Java-based system implemented using Spring Boot and MySQL database.

## Project Structure

```
tudianersha/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tudianersha/
│   │   │       ├── Application.java
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       ├── entity/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── data.sql
│   │       └── schema.sql
├── pom.xml
└── README.md
```

## Technologies Used

- Java 11
- Spring Boot 2.7.0
- MySQL 8.0
- Maven
- JPA/Hibernate
- MyBatis

## Setup Instructions

1. **Database Setup**:
   - Install MySQL 8.0
   - Create a database named `tudianersha`
   - Update the database credentials in `application.yml` if needed

2. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

3. **API Endpoints**:
   - GET `/api/users` - Get all users
   - GET `/api/users/{id}` - Get user by ID
   - POST `/api/users` - Create a new user
   - PUT `/api/users/{id}` - Update user by ID
   - DELETE `/api/users/{id}` - Delete user by ID

## Configuration

The application can be configured through the `application.yml` file:
- Server port (default: 8080)
- Database connection settings
- Logging levels

## Development Environment

- IntelliJ IDEA is recommended for development
- Make sure to install Lombok plugin if using IntelliJ IDEA