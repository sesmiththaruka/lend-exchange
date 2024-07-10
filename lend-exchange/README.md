# Lend-Exchange - Backend

## Overview

The backend of the Lend Exchange project manages all server-side tasks, including user authentication, item handling, and API endpoints. This section outlines the backend architecture, the technologies implemented, and setup guidelines.
## Technologies Used

- **Spring Boot 3**: Robust framework for Java application development.
- **Spring Security 6**: Implements authentication and authorization features to protect the application.
- **JWT Authentication**: Ensures secure client-server communication.
- **Docker**: Enables containerization for simplified backend deployment.
- **Spring Data JPA**: Streamlines data persistence using Java Persistence API.
- **JSR-303 & Spring Validation**: Facilitates object validation through annotations.
- **OpenAPI & Swagger UI**: Generates API endpoint documentation.

## Setup Instructions

To set up the backend of the Book Social Network project, follow these steps:

1. Clone the repository:

```bash
   git clone https://github.com/sesmiththaruka/lend-exchange.git
```

2. Run the docker-compose file

```bash
  docker-compose up -d
```

3. Navigate to the lend-exchange directory:

```bash
  cd lend-exchange
```

4. Install dependencies (assuming Maven is installed):

```bash
  mvn clean install
```

4. Run the application but first replace the `x.x.x` with the current version from the `pom.xml` file

```bash
  java -jar target/lend-exchange-api-x.x.x.jar
```

5. Access the API documentation using Swagger UI:

Open a web browser and go to `http://localhost:8088/swagger-ui/index.html.


## Contributors
* [Sesnith Tharuka - LinkedIn](https://www.linkedin.com/in/sesmiththaruka/)
* [Sesnith Tharuka - github](https://github.com/sesmiththaruka/)
