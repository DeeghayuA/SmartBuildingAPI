# Smart Campus: Sensor & Room Management API

Welcome to the **Smart Campus API** project. This is a robust, scalable JAX-RS RESTful web service designed to manage building infrastructure, including rooms, various sensor types (CO2, Temperature, Occupancy), and historical telemetry logs.

This project was developed strictly adhering to coursework constraints:

- **No Database**: Uses localized in-memory Java collections.
- **No Spring Boot**: Built using native JAX-RS (Jakarta REST) via Jersey.
- **Production Quality**: Includes custom exception mapping, HATEOAS discovery, and request/response logging.

---

## 🛠️ Technology Stack

- **Language**: Java 1.8+
- **Framework**: JAX-RS 2.1 (Jersey Implementation)
- **JSON Provider**: Jackson
- **Server**: Apache Tomcat 9+
- **Build Tool**: Maven

---

## 🚀 Build & Launch Instructions

### Prerequisites

- **NetBeans IDE** (Recommended) or IntelliJ.
- **JDK 17** (or 1.8+).
- **Apache Tomcat 9** configured in your IDE.

### Step-by-Step Launch

1.  **Clone/Open Project**: Open the `SmartBuildingAPI` folder in NetBeans.
2.  **Clean and Build**: Right-click the project name in the Projects navigator and select **Clean and Build**. This will resolve Maven dependencies (Jersey, JAXB, Jackson).
3.  **Run Server**: Right-click the project and select **Run**.
4.  **Verification**: Once the browser opens to `http://localhost:8080/SmartBuildingAPI/`, your API is live at the `/api/v1` base path.

---

## 📡 Sample API Interactions (CURL)

### 1. API Discovery (Part 1)

Retrieve administrative metadata and resource maps.

```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1
```

### 2. List All Rooms (Part 2)

```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1/rooms
```

### 3. Create a New Room (Part 2)

```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"name": "Main Lecture Hall", "capacity": 200}'
```

### 4. Create Sensor with Invalid Room (Part 5 - Security Test)

Expect a **422 Unprocessable Entity** error.

```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"roomId": "FAKE-ROOM-999", "type": "CO2", "status": "ACTIVE"}'
```

### 5. Fetch Readings for a Sensor (Part 4)

```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1/sensors/TEMP-001/readings
```

---

## 📝 Conceptual Report (Question Answers)

### Part 1: Service Architecture

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every request? How does this impact synchronization?**
**A:** By default, JAX-RS resources are **request-scoped**. This means the JAX-RS runtime instantiates a completely new object for every incoming HTTP request and discards it after the response is sent. Because our API manages shared in-memory data (Maps/Lists in `Database.java`), this decision forces us to use **static members** or shared singletons for the data store. Consequently, synchronization is critical; multiple threads handling different request instances will access the same static maps simultaneously. Without thread-safe collections (like `ConcurrentHashMap`) or `synchronized` blocks, race conditions could lead to data corruption or loss.

**Q: Why is the provision of ”Hypermedia” (HATEOAS) considered a hallmark of advanced RESTful design?**
**A:** HATEOAS (Hypermedia as the Engine of Application State) allows an API to be self-documenting. By providing links in the response (like our Discovery endpoint), the client can navigate the API dynamically without having hardcoded URLs. This benefits developers because the server can evolve its URL structure without breaking clients, as long as the entry point and link relationships remain consistent.

### Part 2: Room Management

**Q: What are the implications of returning only IDs versus returning the full room objects in a list?**
**A:** Returning only **IDs** significantly reduces network bandwidth and speeds up the initial response, making it ideal for mobile clients or very large datasets. However, it forces the client to make many secondary "N+1" requests to fetch details for each item. Returning **Full Objects** increases the initial payload size but improves performance by reducing round-trips, allowing the client to render the complete UI immediately.

**Q: Is the DELETE operation idempotent in your implementation?**
**A:** **Yes**. The operation is idempotent because sending the same DELETE request multiple times results in the same server state: the resource no longer exists. While the first call returns `204 No Content` and subsequent calls return `404 Not Found`, the side-effect on the system state is identical across all calls.

### Part 3: Sensor Operations

**Q: Explain the technical consequences if a client attempts to send data in a different format, such as text/plain.**
**A:** Because we used the `@Consumes(MediaType.APPLICATION_JSON)` annotation, JAX-RS performs strict Content-Type negotiation. If a client sends `text/plain`, the server will automatically intercept the request before it reaches our logic and return an **HTTP 415 Unsupported Media Type** status. This ensures our models never receive data they cannot parse.

**Q: Contrast QueryParam filtering with Path-based URL filtering.**
**A:** Query parameters (`?type=CO2`) are the RESTful standard for **filtering, sorting, or searching** collections because they represent optional modifiers on a resource. URL paths (`/sensors/type/CO2`) are semantically intended to identify a specific unique resource or a sub-hierarchy. Using query parameters keeps the URI structure clean and allows for multiple filters to be combined (e.g., `?type=CO2&status=ACTIVE`) without creating a complex, rigid path structure.

### Part 4: Deep Nesting

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**
**A:** The Sub-Resource Locator pattern promotes **Separation of Concerns**. Instead of creating one "God Class" that handles rooms, sensors, and readings, we delegate the logic to a specialized `SensorReadingResource`. This keeps the code maintainable, testable, and allows the locator to inject context (like the `sensorId`) into the sub-resource seamlessly.

### Part 5: Error Handling & Logging

**Q: Explain the risks associated with exposing internal Java stack traces to external API consumers.**
**A:** Exposing stack traces is a significant **Information Disclosure** vulnerability. An attacker can gather internal file system paths, the specific names and versions of libraries (e.g., `Jersey 2.34`), and insights into the application's internal calling logic. This information allows them to search for known exploits for those specific library versions or identify logic flaws to bypass security controls.

**Q: Why is it advantageous to use JAX-RS filters for logging instead of manual statements?**
**A:** JAX-RS filters handle **Cross-Cutting Concerns** in a centralized location. Using filters ensures that _every_ request and response is logged automatically without needing to repeat code in every method. This follows the **DRY (Don't Repeat Yourself)** principle and makes the logging behavior much easier to modify, enable, or disable globally across the entire application.
