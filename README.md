# Smart Campus REST API

## Developer Profile
* **Name:** Deeghayu
* **UoW ID:** w2119769
* **IIT ID:** 20240905
* **Module:** 5COSC022W Client-Server Architectures (2025/26)

---

## API Design Overview
This project is a fully functional RESTful API developed for a Smart Campus Sensor and Room Management System. It has been built strictly utilizing the JAX-RS (Jersey) specification without any reliance on external frameworks like Spring Boot, and it operates entirely in-memory without a traditional database for storage.

---

## Architectural Concepts

* **In-Memory Data Storage:** Because JAX-RS resource classes operate on a request-scoped lifecycle (creating and destroying instances on every request), data persistence is handled by a centralized `Database` class. This class uses `static` Java collections to ensure that the state of rooms, sensors, and telemetry readings remains persistent across all concurrent HTTP calls.
* **HATEOAS Compliance:** The root discovery endpoint at `/api/v1/` returns system metadata alongside hyperlinks that point to the major collections. This allows API clients to discover and navigate the system dynamically rather than relying on hardcoded paths.
* **Strict Error Handling:** The API is protected by custom `ExceptionMappers`. These intercept specific business logic violations, such as attempting to delete a room containing active sensors or referencing an invalid Room ID, and map them to their semantically correct HTTP Status codes (409 Conflict, 422 Unprocessable Entity). A global exception mapper acts as a safety net, sanitizing 500 server errors so that raw Java stack traces are never leaked to the client.
* **Observability:** A custom `LoggingFilter` intercepts every inbound request and outbound response, logging the HTTP Method, URI, and final Status Code to the server console using standard `java.util.logging`.

---

## Build and Launch Instructions
This project is built using Maven and is designed to be deployed to an Apache Tomcat Server via Apache NetBeans.

### Step-by-Step Deployment:
1. **Open the Project:** Launch Apache NetBeans IDE. Go to `File > Open Project` and select the `SmartBuildingAPI` directory.
2. **Configure the Server:** Ensure an instance of Apache Tomcat 9 is added to your NetBeans servers under the Services tab.
3. **Clean and Build:** In the Projects explorer, right-click the `SmartBuildingAPI` project and select **Clean and Build**. Maven will automatically download the required Jersey 2.32 dependencies and package the `.war` file.
4. **Run the Application:** Right-click the project again and select **Run**. NetBeans will deploy the application to Tomcat and launch your default browser.
   
> The base URL for the API is `http://localhost:8080/SmartBuildingAPI/api/v1`

---

## API Endpoints

The Smart Campus API implements the following core endpoints:

* `GET /api/v1/` - Discovery endpoint returning API metadata and navigational links.
* `GET /api/v1/rooms` - Retrieves a list of all registered rooms.
* `GET /api/v1/rooms/{id}` - Retrieves detailed metadata for a specific room.
* `POST /api/v1/rooms` - Registers a new physical room.
* `DELETE /api/v1/rooms/{id}` - Deletes a room (blocked with a 409 Conflict if active sensors are linked).
* `GET /api/v1/sensors` - Retrieves a list of all registered sensors.
* `GET /api/v1/sensors?type={type}` - Filters the sensor list by a specific type (e.g., CO2).
* `GET /api/v1/sensors/{id}` - Retrieves data for a specific sensor.
* `POST /api/v1/sensors` - Registers a new sensor to an existing room.
* `GET /api/v1/sensors/{id}/readings` - Retrieves the historical telemetry readings for a specific sensor.
* `POST /api/v1/sensors/{id}/readings` - Appends a new reading to a sensor's history and triggers a side-effect updating its current value.

---

## Business Rules

* **HATEOAS Discovery:** The root endpoint provides dynamic navigation links to guide client interaction.
* **Foreign Key Validation:** Creating a sensor requires an existing `roomId`. Providing an invalid ID throws a custom exception mapped to a 422 Unprocessable Entity.
* **State-Guarded Deletion:** Deleting a room that still has registered sensors is blocked to prevent orphaned data, throwing a custom exception mapped to a 409 Conflict.
* **Side-Effect Synchronization:** When a new reading is appended to a sensor's history, the parent sensor's `currentValue` is automatically updated to maintain data consistency.
* **Leak-Proof Error Handling:** All exceptions return structured JSON error messages, strictly hiding internal server errors and stack traces.

---

## Sample cURL Commands

Once the Tomcat server is running, you can test the API functionality using your terminal:

### 1. Discovery Endpoint
```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1
```

### 2. Create a New Room
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"name\": \"Computer Lab 1\", \"capacity\": 30}"
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1/rooms
```

### 4. Register a Sensor to the Room
*(Replace `<ROOM_ID>` with the ID returned from the room creation step)*
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"type\": \"TEMPERATURE\", \"roomId\": \"<ROOM_ID>\", \"status\": \"ACTIVE\"}"
```

### 5. Filter Sensors by Type
```bash
curl -X GET "http://localhost:8080/SmartBuildingAPI/api/v1/sensors?type=TEMPERATURE"
```

### 6. Append a Sensor Reading
*(Replace `<SENSOR_ID>` with the ID of the sensor you created)*
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/sensors/<SENSOR_ID>/readings \
-H "Content-Type: application/json" \
-d "{\"value\": 24.5}"
```

### 7. Fetch Sensor Readings History
```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1/sensors/<SENSOR_ID>/readings
```

### 8. Validation Error Test (Invalid Room)
*(This request will fail gracefully and return a 422 Unprocessable Entity)*
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"roomId\": \"FAKE-ROOM-999\", \"type\": \"CO2\", \"status\": \"ACTIVE\"}"
```

---

## Status Codes
* **200 OK:** The request succeeded.
* **201 Created:** A new resource was successfully created.
* **204 No Content:** The request succeeded, but no data is returned (used for successful DELETE operations).
* **400 Bad Request:** Missing or invalid JSON syntax.
* **403 Forbidden:** Action blocked due to current state constraints (e.g., sensor offline).
* **404 Not Found:** The requested resource ID does not exist.
* **409 Conflict:** Integrity violation (e.g., attempting to delete an occupied room).
* **422 Unprocessable Entity:** Business logic failure (e.g., invalid foreign key reference).
* **500 Internal Server Error:** An unexpected server error occurred, stack traces remain hidden.

---

## Conceptual Report

### Part 1: Service Architecture & Setup

**Q 1.1 Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

**Answer:** 
By default, JAX-RS uses a "per-request" lifecycle for its resource classes. This means that every time a client sends an HTTP request to the server, the JAX-RS runtime instantiates a completely new object of the resource class (like `RoomResource`), processes the request, and then immediately discards the object. If I had stored the rooms or sensors in a standard instance variable, such as a regular `List` or `Map` inside the resource class, that data would be lost the moment the request finished. The next request would get a completely empty list.

To solve this problem without using an external database, I implemented a centralized `Database` class that uses static collections (e.g., `static Map<String, Room> rooms`). Because these variables are marked as static, they belong to the `Database` class itself rather than any specific instance. This ensures the data persists in memory for the entire lifetime of the application, and all instances of the resource classes interact with the exact same shared data.

While this solves the persistence issue, it introduces a synchronization challenge. Multiple HTTP requests could arrive simultaneously on different threads, leading to race conditions if they try to modify the static `HashMap`s at the exact same time. In a full production environment under heavy load, these standard `HashMap`s would need to be replaced with `ConcurrentHashMap` or wrapped in synchronized blocks to prevent data corruption.

**Q 1.2 Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

**Answer:** 
HATEOAS (Hypermedia As The Engine Of Application State) is what makes a REST API truly discoverable and self-describing. In a standard API, a client developer has to read static external documentation to figure out what URLs exist and how to navigate between them. If the backend team ever changes those URLs, the client applications break because the paths were hardcoded.

With HATEOAS, the API guides the client. My discovery endpoint at the root path (`/api/v1/`) returns a JSON response that includes not just metadata, but a collection of links pointing to the main resource collections like rooms and sensors. A client only needs to know the single entry point. From there, it can read the links provided in the response and navigate the system dynamically.

This approach significantly benefits client developers because it decouples the client from the server's routing structure. If the server updates its internal paths, the client does not need to be rewritten, it simply follows the new links provided in the discovery response. It brings the navigability of the World Wide Web to the API level.

### Part 2: Room Management

**Q 2.1 When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**

**Answer:** 
When deciding what a `GET /rooms` endpoint should return, there is a distinct trade-off between network efficiency and client-side processing. If the endpoint returns an array of only room IDs, the initial response is extremely lightweight and fast. This saves bandwidth, which is great for mobile clients on poor connections. However, the client is now forced to make separate follow-up requests to fetch the details for every single room (the N+1 query problem). This can drastically slow down the application's perceived performance and flood the server with requests.

In my implementation, I chose to return the full room objects, which includes the ID, name, capacity, and the list of linked sensor IDs. While this makes the initial JSON payload heavier, it allows the client to retrieve all necessary information in a single network round-trip. The client can immediately render the full user interface without waiting for secondary requests.

For a campus building system where the number of rooms is likely in the hundreds rather than millions, returning the full objects is the more practical and performant choice. If the dataset were to grow significantly, the ideal solution would be to implement pagination along with a summarized version of the room object.

**Q 2.2 Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

**Answer:** 
An operation is considered idempotent if executing it multiple times has the exact same effect on the server's state as executing it only once. For a DELETE operation, the intention is that the resource is removed and stays removed, regardless of duplicate requests.

In my implementation, the DELETE operation is idempotent from the perspective of server state. If a client sends a DELETE request for a specific room, the system checks if the room exists and has no active sensors, deletes it, and returns a 200 OK success message. If the client mistakenly sends that exact same DELETE request a second time, the system will not find the room and will return a 404 Not Found.

Although the HTTP response code changes from 200 to 404, the actual state of the server remains identical: the room does not exist. Nothing is double-deleted, and no data is corrupted. Returning a 404 on the second attempt is the correct behavior because it honestly informs the client that the resource they are trying to manipulate is no longer there.

### Part 3: Sensor Operations & Linking

**Q 3.1 We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

**Answer:** 
The `@Consumes(MediaType.APPLICATION_JSON)` annotation serves as a strict contract between the server and the client. It explicitly tells the JAX-RS runtime that this specific method is only capable of processing incoming data formatted as JSON.

If a client attempts to send a request with a different format, such as setting the `Content-Type` header to `text/plain` or `application/xml`, the JAX-RS framework intercepts the request before it even reaches my Java method. The framework compares the incoming header against the allowed media types, realizes there is a mismatch, and automatically rejects the request by sending back an HTTP 415 Unsupported Media Type status code.

The technical consequence of this is highly beneficial for the developer. It means my Java code is completely protected from having to handle or parse malformed or unexpected data types. I do not need to write manual checks to verify the content type inside the method body. The framework guarantees that by the time my method is invoked, the payload has already been validated as JSON and mapped to my Java object.

**Q 3.2 You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**

**Answer:** 
Using a query parameter like `?type=CO2` is the standard and most semantically correct way to filter collections in a REST API. A URL path is fundamentally designed to identify a specific, unique resource or a nested hierarchical relationship (for example, `/sensors/TEMP-001` uniquely identifies one sensor).

If we used a path-based design for filtering, such as `/sensors/type/CO2`, we are falsely implying that the filtered list is a distinct, hardcoded resource. This design quickly becomes rigid and unmanageable. If a user wanted to filter by both type and status, the path would become a convoluted string like `/sensors/type/CO2/status/ACTIVE`. The routing logic on the server would become extremely complex to handle every possible combination of filters in different orders.

Query parameters, on the other hand, are designed specifically to act as optional modifiers applied to a base collection. They are flexible and infinitely composable. The base resource remains logically targeted at `/sensors`, and the query parameters simply adjust what the server returns. It is also completely optional; if the client omits the query parameter, the server naturally falls back to returning the entire unfiltered collection without requiring a separate routing path.

### Part 4: Deep Nesting with Sub-Resources

**Q 4.1 Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?**

**Answer:** 
As a REST API grows, controller classes can easily become bloated "God classes" that try to handle too many responsibilities at once. If I had defined every single endpoint related to both sensors and their historical readings inside the `SensorResource` class, it would have become massive, difficult to read, and hard to maintain.

The Sub-Resource Locator pattern solves this complexity by enforcing the principle of separation of concerns. In my `SensorResource` class, I have a method mapped to `/{sensorId}/readings`, but it does not have an HTTP method annotation like `@GET` or `@POST`. Instead, this method acts as a router. It extracts the sensor ID from the URL and instantiates a completely separate class, `SensorReadingResource`, passing the ID into its constructor.

This delegation means that `SensorResource` only contains logic for managing the sensors themselves, while `SensorReadingResource` is entirely dedicated to handling the telemetry history. This makes the codebase highly modular. It is easier to test the reading logic in isolation, and multiple developers can work on different parts of the API simultaneously without causing merge conflicts in a single massive file.

### Part 5: Advanced Error Handling, Exception Mapping & Logging

**Q 5.2 Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

**Answer:** 
HTTP 404 Not Found is used specifically when the URL endpoint itself does not map to anything on the server. If a client attempts to access a non-existent URL, returning a 404 clearly indicates that the path is wrong.

However, when a client sends a POST request to create a sensor, the URL `/sensors` is perfectly valid. The JSON payload is also syntactically correct and properly formatted. The error occurs because the client provided a `roomId` inside that JSON payload that does not exist in our system's memory. This is a business logic violation, a broken foreign key reference.

If the server returned a 404 in this scenario, it would be highly misleading. The client developer would likely assume they typed the URL incorrectly or used the wrong HTTP method. HTTP 422 Unprocessable Entity was explicitly created for this exact situation. It communicates that the server understands the content type of the request entity, and the syntax of the request entity is correct, but it was unable to process the contained instructions because of a semantic error. It points the developer directly to the problem in their data, rather than their URL.

**Q 5.4 From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

**Answer:** 
Exposing a raw Java stack trace to an external API consumer is a severe Information Disclosure vulnerability. A stack trace is effectively a detailed map of the internal workings of the application.

When an attacker reads a stack trace, they gain visibility into the exact package names, class names, and internal logic flows of the backend system. More importantly, stack traces often reveal the exact names and versions of the third-party libraries and frameworks being used, such as revealing that the server is running Jersey version 2.32. An attacker can take this version number and search public CVE databases for known security vulnerabilities associated with it, allowing them to craft a highly targeted exploit.

My implementation prevents this by using a `GlobalExceptionMapper`. This class acts as a safety net that catches any unexpected runtime errors before they reach the client. It logs the full, detailed stack trace to the server's internal console where only the development team can see it for debugging purposes. Meanwhile, it constructs a clean, generic 500 Internal Server Error JSON response to send back to the client, ensuring that no sensitive architectural data is ever leaked over the network.

**Q 5.5 Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**

**Answer:** 
Logging every incoming request and outgoing response is a classic example of a cross-cutting concern, something that needs to happen uniformly across the entire application. If I had manually inserted `Logger.info()` statements into every single GET, POST, PUT, and DELETE method in my resource classes, it would have created a significant maintenance burden.

Manual logging violates the DRY (Don't Repeat Yourself) principle. It clutters the business logic with infrastructure code. Furthermore, as the API expands and new endpoints are added, developers might forget to add the logging statements, leading to silent gaps in the system's observability.

JAX-RS filters provide an elegant, centralized architectural solution. My `LoggingFilter` class implements both `ContainerRequestFilter` and `ContainerResponseFilter` and is registered with the `@Provider` annotation. The JAX-RS framework automatically intercepts every single request before it reaches the resource method, and every response after the method finishes, routing them through this filter. This guarantees complete coverage across the API without writing a single line of logging code inside the actual resource classes. If the logging format ever needs to be updated, it only requires modifying one single file.
