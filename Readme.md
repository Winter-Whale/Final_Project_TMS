**Parking Booking API**

A service for parking spot booking.
Implemented in Java 17 using Spring Boot 3, Spring Security, JWT, Spring Data JPA (Hibernate), and PostgreSQL.
API documentation is available via Swagger/OpenAPI.
The project is covered by unit tests and includes Actuator for monitoring

**🚀 Key Features**
*User registration with roles: OWNER and RENTER.

*Authentication with JWT token issuance.

*Parking spot management: create, view, update, delete (available to owners and administrators).

*Booking spots for a specific time interval with automatic cost calculation.

*Availability checks and prevention of booking overlaps.

*Automatic spot release after the booking time expires.

*Role‑based access control (ADMIN, OWNER, RENTER).

*Built‑in monitoring via Actuator (health, info, metrics).

*Logging to file (logs/spring-boot-log.log).

**🧑‍💻 Roles and Permissions**

*ADMIN - Full access to all endpoints (manage users, spots, bookings).

*OWNER - Create/edit/delete their own parking spots, view information about their spots and bookings.

*RENTER - Create bookings, view available spots, retrieve own information.

**🛠 Technologies**

*Java 17

*Spring Boot 3.5.13

*Spring Security

*JWT (jjwt 0.13.0)

*Spring Data JPA (Hibernate)

*PostgreSQL (main DB), H2 (for tests)

*Lombok

*Swagger / OpenAPI 3 (springdoc-openapi 2.8.9)

*Spring Boot Actuator

*Maven

*JUnit / Spring Boot Test

**📦 Project Structure (brief)**

    src/main/java/com/example/parking/
    ├── config              – configurations (SpringSecurity, OpenApiConfig)
    ├── controller          – REST controllers
    ├── service             – business logic
    ├── repository          – JPA repositories
    ├── entity              – database entities
    ├── dto                 – DTOs for requests/responses
    ├── filter              – JwtFilter
    ├── mapper              – mappers for entity ↔ DTO conversion
    ├── exception           – custom exceptions and global exception handler
    └── util                – helper classes (CurrentUserService)

**🔐 Authentication and JWT**

*Obtaining a token

*POST /security/generate

    Request body:
    json
    {
    "username": "user1",
    "password": "password123"
    }
    
    Response:
    json
    {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }

Using the token

All protected endpoints require the token in the header:

    Authorization: Bearer <your-jwt-token>

**📚 API Endpoints**

    *Security and Registration (/security)
    Method		Path			Role			Description
    POST	/security/registration/owner	–			Register a new owner
    POST	/security/registration/renter	–			Register a new renter
    POST	/security/generate		–			Authenticate and obtain a JWT
    GET	/security/{id}			ADMIN			Get security data by ID
    PUT	/security/{id}/update		OWNER, RENTER, ADMIN	Update login/password (requires current password)

    *Users (/users)
    Method		Path			Role			Description
    GET	/users				ADMIN			Get all users
    GET	/users/{id}			ADMIN			Get user by ID
    GET	/users/info/myself		OWNER, RENTER		Get own information
    POST	/users				ADMIN			Create a user
    PUT	/users				ADMIN, OWNER, RENTER	Update user data (self or any – ADMIN)
    DELETE	/users/{id}			ADMIN			Delete a user

    *Parking Spots (/parking)
    Method		Path			Role			Description
    GET	/parking			– (public)		Get list of all spots (with their statuses)
    GET	/parking/spot/{id}		OWNER, ADMIN		Get spot information by ID
    GET	/parking/user/{id}		OWNER, ADMIN		Get all spots of a user by ID
    POST	/parking			OWNER, ADMIN		Create a new spot
    PUT	/parking/{id}			OWNER, ADMIN		Update a spot
    DELETE	/parking/{id}			OWNER, ADMIN		Delete a spot
    
    *Bookings (/bookings)
    Method		Path			Role			Description
    POST	/bookings			RENTER, OWNER		Create a booking (by renter)

Note: Both RENTER and OWNER can create a booking, but the business logic assumes that the renter makes the booking.

**🗄 Data Model (main entities)**

    *User – user (full name, phone, email, age, creation/update timestamps).
    *Security – credentials (username, password, role), linked to User (One-to-One).
    *ParkingSpot – parking spot (address, description, price per hour, status FREE/BUSY), linked to User (OWNER) (Many-to-One).
    *Booking – booking (start/end time, total price), linked to ParkingSpot and User (RENTER) (Many-to-One).

**⚙️ Running the Project**

    Requirements:
        JDK 17+
        PostgreSQL (or another DB, but PostgreSQL is recommended)
        Maven
        Environment variables set (see below)

Environment Variables

The application.properties file uses variables:

    ${DATABASE_USERNAME} – database username
    ${DATABASE_PASSWORD} – database password

Set them before running (e.g., in IDE or system):

    export DATABASE_USERNAME=postgres
    export DATABASE_PASSWORD=your_password

Alternatively, hardcode them directly in application.properties if you prefer not to use variables.

**Database Configuration**

application.properties already contains the settings:

    properties
    server.port=8081
    spring.datasource.url=jdbc:postgresql://localhost:5432/final_project_tms
    spring.datasource.username=${DATABASE_USERNAME}
    spring.datasource.password=${DATABASE_PASSWORD}
    spring.datasource.driver-class-name=org.postgresql.Driver
    spring.jpa.hibernate.ddl-auto=update   # can be changed to validate/create

**JWT Configuration**

In application.properties:

    properties
    jwt.secret=MyVerySecureSecretKeyForJWTTokenGenerationMustBeLongEnough256bits
    jwt.expiration=60 (minutes)

**Build and Run**

    mvn clean package
    java -jar target/Final_Project_TMS.jar

After startup, the application will be available at:

    http://localhost:8081

**📖 Swagger Documentation**
After starting, open in your browser:

    http://localhost:8081/swagger-ui/index.html

There you can view all endpoints, try them out, and also authorize (click the Authorize button – enter your token).

**📊 Monitoring (Actuator)**
To check the application state, the following endpoints are available:

    http://localhost:8081/actuator/health – application health
    http://localhost:8081/actuator/info – application information
    http://localhost:8081/actuator/metrics – metrics (e.g., metrics/http.server.requests)

These endpoints are accessible without authentication (can be restricted in production).

**📝 Logging**

Logging is configured to write to logs/spring-boot-log.log (INFO level).

Logs are also output to the console (can be adjusted in settings).

**🧪 Testing**

The project is covered by unit tests (using JUnit, Mockito, Spring Boot Test).

An in‑memory H2 database is used for tests (dependency com.h2database:h2).

    Run tests:
    -mvn test

**🔒 Security (Important!)**

In the current version, NoOpPasswordEncoder is used – passwords are stored and compared in plain text.

For production, it is strongly recommended to replace it with BCryptPasswordEncoder to ensure secure password storage

**✏️ Example Requests**
    Creating a spot (OWNER)
    POST /parking (requires owner token)
    
    json
    {
    "address": "ул. Ленина, 10",
    "description": "Подземный паркинг, место 5",
    "pricePerHour": 150.00,
    "status": "FREE",
    "ownerId": 1
    }
    Booking (RENTER)
    POST /bookings
    
    json
    {
    "spotId": 1,
    "startTime": "2026-07-15 10:00",
    "endTime": "2026-07-15 12:00"
    }
📌 Notes

All timestamps are accepted and returned in the format yyyy-MM-dd HH:mm (local time).

Automatic spot release is performed on each request for the spot list (GET /parking). However, in a real project it is better to use a scheduler (@Scheduled).

**📄  Main Dependencies**

Spring Boot Starter Web, Data JPA, Security, Validation, Test, Actuator

PostgreSQL Driver

Lombok

JJWT (API, Impl, Jackson)

SpringDoc OpenAPI UI

H2 Database (test scope)

See pom.xml for the full list.

**👨‍💻 Author**

The project was developed for educational purposes.

For questions and suggestions: vitalij.melnikov.2012@gmail.com

