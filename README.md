# Smart Campus: Sensor & Room Management API

Hi there! Welcome to my **Smart Campus API** project. This is a RESTful web service built with JAX-RS to manage a university's building infrastructure. It handles everything from tracking rooms to monitoring different types of sensors (like CO2, Temperature, and Occupancy) and keeping a historical log of their readings.

I've developed this project from scratch to strictly follow the coursework requirements:
- **No Database**: All data is managed using thread-safe, in-memory Java collections.
- **No Spring Boot**: The entire API is built using native JAX-RS (Java EE 8) and Jersey.
- **Robust Error Handling**: Implemented custom exception mappers to ensure the API never leaks stack traces.

---

## 🛠️ Technology Stack

Based on the module specification, the project is configured with:
- **Java Version**: Java 8 (1.8)
- **Framework**: Java EE 8 (JAX-RS using `javax.ws.rs.*` namespace)
- **Implementation**: Jersey 2.32
- **JSON Provider**: Jackson (via `jersey-media-json-jackson:2.32`)
- **Server**: Apache Tomcat 9.x
- **Build Tool**: Maven (WAR packaging)

---

## 🚀 Build & Launch Instructions

Here is exactly how you can build and run this API on your local machine:

### Prerequisites
- **NetBeans IDE** (or any preferred Java IDE)
- **JDK 8** installed and configured
- **Apache Tomcat 9** set up as your local web server

### Step-by-Step Launch Guide
1. **Clone the Repository**: Download or clone this project and open the `SmartBuildingAPI` folder in NetBeans.
2. **Clean and Build**: Right-click the project name in the *Projects* window and select **Clean and Build**. Maven will automatically download the required Jersey 2.32 dependencies and compile the `.war` file.
3. **Deploy to Tomcat**: Right-click the project and select **Run**. NetBeans will deploy the compiled WAR file to your configured Tomcat 9 server.
4. **Verify it's Live**: Once Tomcat starts, the API will be listening. You can verify it's working by navigating to the base discovery endpoint: `http://localhost:8080/SmartBuildingAPI/api/v1`

---

## 📡 Sample API Interactions (cURL)

Here are five `curl` commands you can run in your terminal to test different parts of the API:

### 1. API Discovery (Root Endpoint)
Retrieves the metadata and available resource paths.
```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1
```

### 2. List All Rooms
Fetches a JSON array of all registered rooms.
```bash
curl -X GET http://localhost:8080/SmartBuildingAPI/api/v1/rooms
```

### 3. Create a New Room
Registers a new room in the system.
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -d '{"name": "Computer Lab 1", "capacity": 30}'
```

### 4. Search for CO2 Sensors
Demonstrates query parameter filtering.
```bash
curl -X GET "http://localhost:8080/SmartBuildingAPI/api/v1/sensors?type=CO2"
```

### 5. Validation Error Test (422 Unprocessable Entity)
Attempts to create a sensor for a room that doesn't exist, which triggers the custom exception mapper.
```bash
curl -X POST http://localhost:8080/SmartBuildingAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -d '{"roomId": "FAKE-ROOM-999", "type": "CO2", "status": "ACTIVE"}'
```

---

## 📝 Conceptual Report

Here are my answers to the coursework questions detailing the architectural decisions behind this API.

### Part 1: Service Architecture & Setup
**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.**
**A:** By default, JAX-RS resource classes are **request-scoped**. This means the server creates a brand new instance of the resource class for every single incoming HTTP request, and then destroys it once the response is sent. Because of this, we cannot store our API's data in regular instance variables inside the resource classes—they would just disappear! To fix this, I had to use `static` collections in a separate `Database` class. However, because multiple users might hit the API at the exact same time (multiple threads), these static collections are vulnerable to race conditions. To prevent data corruption, any shared in-memory data must be managed using thread-safe collections or synchronized operations.

**Q: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**
**A:** HATEOAS makes an API truly "discoverable." Instead of forcing frontend developers to read static documentation and hardcode URLs into their apps, the API itself tells the client what they can do next by providing links in the JSON response. If the backend team decides to change a URL path later, the client applications won't break because they are dynamically following the links provided by the server, rather than relying on hardcoded paths. 

### Part 2: Room Management
**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.**
**A:** If we return **only IDs**, the server sends a tiny payload, which saves network bandwidth and makes the initial request super fast. But, the client will then have to make dozens of follow-up requests to get the actual details for each room (the N+1 problem), which can be slow and intensive on the client side. Returning **full objects** means a heavier initial payload, but it allows the client to render the entire UI immediately in just one single network trip.

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**
**A:** **Yes, it is idempotent.** If a client sends a DELETE request for room "LIB-301", the API deletes it and returns a 200 OK. If the client mistakenly sends that *exact same* DELETE request again, the room is already gone. The API will simply return a 404 Not Found. Even though the HTTP status code changed, the actual state of the server remains exactly the same—the room is deleted. Therefore, the operation is mathematically idempotent.

### Part 3: Sensor Operations & Linking
**Q: We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**
**A:** By using `@Consumes`, we are strictly telling the JAX-RS runtime to only accept JSON. If a client ignores this and sends `text/plain`, the JAX-RS framework will intercept the request before it even reaches my Java method. It will automatically reject it and return an **HTTP 415 Unsupported Media Type** error to the client. This is great because it protects my code from having to manually parse invalid data formats.

**Q: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?**
**A:** URL paths (like `/sensors/{id}`) are meant to identify a specific, unique resource or a nested relationship. Query parameters (`?type=CO2`) are meant to act as modifiers or filters on a larger collection. Using query parameters is much better because it's flexible—you can easily stack them (e.g., `?type=CO2&status=ACTIVE`). If you used paths for filtering, you'd have to create a messy, rigid routing structure for every possible combination of filters, which is hard to maintain.

### Part 4: Deep Nesting with Sub-Resources
**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?**
**A:** As APIs grow, resource classes can easily turn into massive, unreadable "God classes." The Sub-Resource Locator pattern solves this by delegating responsibilities. Instead of putting all the reading-related logic inside `SensorResource`, I just use a locator method that forwards the request to a dedicated `SensorReadingResource`. This keeps the code modular, easier to test, and perfectly separates concerns. 

### Part 5: Advanced Error Handling, Exception Mapping & Logging
**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**
**A:** A 404 error implies that the URL endpoint itself doesn't exist. However, when a client posts a Sensor with a fake `roomId`, the URL (`/sensors`) is perfectly valid, and the JSON syntax is perfectly fine. The issue is a *business logic* error—the data inside the payload refers to an entity that doesn't exist. HTTP 422 (Unprocessable Entity) perfectly describes this: the server understands the request and the JSON, but it logically cannot process the instructions.

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**
**A:** A stack trace is a goldmine for an attacker. It reveals internal file paths, the exact versions of libraries being used (like Jersey 2.32), and the underlying architecture of the app. If an attacker knows we are using a specific, outdated version of a library, they can just search for known vulnerabilities (CVEs) for that version and exploit them. It completely breaks the principle of "Security through obscurity."

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?**
**A:** Manually typing `Logger.info()` into every single GET, POST, and DELETE method violates the DRY (Don't Repeat Yourself) principle. It clutters the business logic and is easy to forget. JAX-RS Filters allow us to implement logging in one single, centralized place. Every single request and response flows through the filter automatically. If I ever need to change the logging format or turn it off, I only have to edit one file instead of fifty.
