# Spring Boot REST API in Eclipse — Complete Beginner's Guide

This guide builds a **Student Management REST API** with Spring Boot and Java in Eclipse. It explains the common HTTP methods and how to test them **without Thymeleaf, HTML templates, or a database**.

The application returns JSON directly from a REST controller. We'll use an in-memory Java map to keep the example simple; its data resets whenever the application restarts.

## Contents

1. What we're building
2. HTTP methods explained
3. Create and import the project in Eclipse
4. Understand the project structure
5. Create the `Student` model
6. Create the REST controller
7. Understand GET, POST, PUT, PATCH, and DELETE
8. Check the main Spring Boot application class
9. Run the application
10. Test the API with Chrome and Postman
11. Common errors and fixes
12. Important limitations and next steps

---

# 1. What We're Building

Imagine a college needs an application to manage student records. Our API will support these operations:

| Operation | HTTP method | Example URL |
|---|---|---|
| View all students | `GET` | `/api/students` |
| View one student | `GET` | `/api/students/1` |
| Add a student | `POST` | `/api/students` |
| Replace a student's details | `PUT` | `/api/students/1` |
| Update selected fields | `PATCH` | `/api/students/1` |
| Delete a student | `DELETE` | `/api/students/1` |

The flow is:

```text
Postman or browser
        |
        | HTTP request
        v
StudentController.java
        |
        | reads or changes data
        v
Student.java + in-memory Map
        |
        | Java object / collection
        v
Spring Boot serializes the result as JSON
        |
        v
HTTP response to the client
```

A `@RestController` writes a method's return value directly into the HTTP response body. For Java objects and collections, Spring Boot typically converts the data to JSON using an HTTP message converter. No Thymeleaf view is involved.

## Important terms

- **HTTP request:** A message sent by a client to the server. It includes a method, URL, headers, and sometimes a body.
- **HTTP response:** The server's reply. It contains a status code, headers, and sometimes a body.
- **JSON:** A common text format for exchanging structured data, for example `{"name":"Vipul"}`.
- **REST API:** An HTTP-based interface that exposes resources, such as students, through URLs and standard HTTP methods.
- **Endpoint:** A particular method-and-URL combination handled by the application.

---

# 2. HTTP Methods Explained

HTTP methods tell the server what operation the client wants to perform on a resource.

| Method | Purpose | Student example | Typical use |
|---|---|---|---|
| `GET` | Retrieve a representation of a resource | View students | Read data |
| `POST` | Submit data, often to create a resource | Add a student | Create or trigger an operation |
| `PUT` | Create or replace the target resource's representation | Replace a student's details | Full replacement; often used for updates |
| `PATCH` | Apply a partial modification | Change only a student's name | Partial update |
| `DELETE` | Remove the target resource | Delete a student | Delete data |
| `HEAD` | Same general response as GET but without a response body | Check headers for an endpoint | Check metadata or resource availability |
| `OPTIONS` | Discover communication options supported by a resource | Ask which methods an endpoint supports | Capability discovery, including CORS preflight use cases |
| `TRACE` | Diagnostic method intended to reflect the received request | Inspect a request path through HTTP infrastructure | Rarely used; often disabled for security |
| `CONNECT` | Establish a tunnel to a destination, commonly through a proxy | HTTPS tunnelling through a proxy | Usually handled by proxy infrastructure, not a typical controller |

For most Spring Boot CRUD applications, focus on **GET, POST, PUT, PATCH, and DELETE** first.

## Safe and idempotent — useful concepts

- **Safe method:** Intended not to change the server's application state. `GET`, `HEAD`, and `OPTIONS` are safe by definition.
- **Idempotent method:** Repeating the same request has the same intended effect as making it once. `GET`, `HEAD`, `PUT`, and `DELETE` are defined as idempotent. `POST` is not generally idempotent. `PATCH` depends on how the patch operation is designed.
- **Note:** Idempotent does not mean the server must return the exact same response every time. For example, the first DELETE might return `204`, while a second DELETE could return `404` if the resource is already gone. The intended state change is still idempotent.

## How do we see API output without Thymeleaf?

Use `@RestController` rather than a controller that returns a template name:

```java
@RestController
public class DemoController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from Spring Boot!";
    }
}
```

After starting the application, open:

```text
http://localhost:8080/hello
```

The browser shows the text directly. There is no HTML template required.

If a controller method returns a Java object, Spring Boot can return JSON instead:

```java
@RestController
public class StudentApiController {

    @GetMapping("/api/student")
    public Student getStudent() {
        return new Student(1, "Vipul", "Spring Boot");
    }
}
```

Assuming `Student` has those fields and the usual accessors, the response resembles:

```json
{
  "id": 1,
  "name": "Vipul",
  "course": "Spring Boot"
}
```

A normal browser address bar is useful for testing GET. Use Postman or another API client to send POST, PUT, PATCH, and DELETE requests.

---

# 3. Create the Spring Boot Project in Eclipse

## Step 1 — Generate the project

1. Open [Spring Initializr](https://start.spring.io/).
2. Select **Maven** as the project type.
3. Select **Java** as the language.
4. Set the group to `com.example`.
5. Set the artifact and project name to `student-api`.
6. Choose a Java version supported by the Spring Boot version offered by Initializr and installed on your computer.
7. Add the **Spring Web** dependency.
8. Optionally add **Spring Boot DevTools** for development-time restarts.
9. Click **Generate**, then extract the downloaded ZIP file.

Spring Initializr generates the Maven setup and main application class for you. You do not need to add Thymeleaf.

## Step 2 — Import the project into Eclipse

1. Open Eclipse.
2. Choose **File → Import**.
3. Select **Maven → Existing Maven Projects** and click **Next**.
4. Browse to the extracted project directory.
5. Select the discovered Maven project.
6. Click **Finish**.
7. Wait for Maven to resolve and download dependencies.

If dependencies do not appear correctly, right-click the project and choose **Maven → Update Project…**, select the project, and click **OK**.

## Step 3 — Understand the project structure

Create the following packages and class under `src/main/java`:

```text
student-api
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com.example.studentapi
        │       ├── StudentApiApplication.java
        │       ├── model
        │       │   └── Student.java
        │       └── controller
        │           └── StudentController.java
        └── resources
            └── application.properties
```

In Eclipse, right-click `com.example.studentapi` and select **New → Package**. Create:

- `com.example.studentapi.model`
- `com.example.studentapi.controller`

**Important:** Keep `StudentApiApplication.java` in the base package `com.example.studentapi`. The model and controller packages should be nested underneath it so the default Spring Boot component scan can find the controller.

---

# 4. Create the Student Model

## Step 1 — Create `Student.java`

Right-click the `model` package, choose **New → Class**, and name it `Student`.

Paste this code:

```java
package com.example.studentapi.model;

public class Student {

    private int id;
    private String name;
    private String course;

    public Student() {
    }

    public Student(int id, String name, String course) {
        this.id = id;
        this.name = name;
        this.course = course;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }
}
```

## Step-by-step explanation

### Package declaration

```java
package com.example.studentapi.model;
```

This tells Java that the class belongs to the `model` package.

### Class declaration

```java
public class Student {
```

This defines a Java class named `Student`. It acts as the blueprint for one student object.

### Fields

```java
private int id;
private String name;
private String course;
```

Each student has three properties:

- `id`: an integer that identifies the student in this example.
- `name`: the student's name.
- `course`: the course in which the student is enrolled.

The keyword `private` prevents other classes from accessing these fields directly. Public getter and setter methods provide controlled access.

### No-argument constructor

```java
public Student() {
}
```

This constructor takes no arguments. It is useful for JSON deserialization approaches that instantiate an object first and then populate its properties.

### Parameterized constructor

```java
public Student(int id, String name, String course) {
    this.id = id;
    this.name = name;
    this.course = course;
}
```

This constructor lets the application initialize all the fields at once:

```java
Student student = new Student(1, "Amit", "Java");
```

`this.id` refers to the field on the current object. The `id` on the right-hand side is the constructor parameter. The same applies to `name` and `course`.

### Getters

```java
public int getId() {
    return id;
}
```

A getter returns the current value of a field. `getName()` returns the name, and `getCourse()` returns the course.

### Setters

```java
public void setName(String name) {
    this.name = name;
}
```

A setter changes a field's value. `setId()` and `setCourse()` work similarly.

Spring Boot's JSON conversion uses the Java object's properties to map JSON request data into Java objects and Java objects into JSON responses.

---

# 5. Create the REST Controller

## Step 1 — Create `StudentController.java`

Right-click the `controller` package, choose **New → Class**, and name it `StudentController`.

Paste this complete code:

```java
package com.example.studentapi.controller;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.studentapi.model.Student;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final Map<Integer, Student> students =
            new LinkedHashMap<>();

    private final AtomicInteger nextId = new AtomicInteger(3);

    public StudentController() {
        students.put(1, new Student(1, "Amit", "Java"));
        students.put(2, new Student(2, "Priya", "Python"));
    }

    // GET: Fetch all students
    @GetMapping
    public Collection<Student> getAllStudents() {
        return students.values();
    }

    // GET: Fetch one student
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudent(
            @PathVariable int id) {

        Student student = students.get(id);

        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(student);
    }

    // POST: Create a student
    @PostMapping
    public ResponseEntity<Student> createStudent(
            @RequestBody Student student) {

        int id = nextId.getAndIncrement();
        student.setId(id);
        students.put(id, student);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(student);
    }

    // PUT: Replace student details
    @PutMapping("/{id}")
    public ResponseEntity<Student> replaceStudent(
            @PathVariable int id,
            @RequestBody Student updatedStudent) {

        if (!students.containsKey(id)) {
            return ResponseEntity.notFound().build();
        }

        updatedStudent.setId(id);
        students.put(id, updatedStudent);

        return ResponseEntity.ok(updatedStudent);
    }

    // PATCH: Update selected fields
    @PatchMapping("/{id}")
    public ResponseEntity<Student> patchStudent(
            @PathVariable int id,
            @RequestBody Student changes) {

        Student existing = students.get(id);

        if (existing == null) {
            return ResponseEntity.notFound().build();
        }

        if (changes.getName() != null) {
            existing.setName(changes.getName());
        }

        if (changes.getCourse() != null) {
            existing.setCourse(changes.getCourse());
        }

        return ResponseEntity.ok(existing);
    }

    // DELETE: Remove a student
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable int id) {

        Student removed = students.remove(id);

        if (removed == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
```

---

# 6. Understand the Controller Code

## A. Package and imports

```java
package com.example.studentapi.controller;
```

The class belongs to the `controller` package.

```java
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
```

These are standard Java classes:

| Class | Why it is used |
|---|---|
| `Collection` | Represents the group of student objects returned by GET all. |
| `LinkedHashMap` | Stores students as key-value pairs and retains insertion order. |
| `Map` | Interface for looking up students by ID. |
| `AtomicInteger` | Generates sequential IDs using an atomic increment. |

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
```

These imports are from Spring:

- `HttpStatus` represents status codes such as `201 Created`.
- `ResponseEntity` lets the method set a response status and body.
- `web.bind.annotation.*` imports annotations such as `@GetMapping`, `@PostMapping`, `@RequestBody`, and `@PathVariable`.

```java
import com.example.studentapi.model.Student;
```

This lets the controller use the `Student` class created earlier.

## B. `@RestController`

```java
@RestController
```

This tells Spring that the class handles web requests and that method return values should be written directly to the HTTP response body. Returned Java objects are normally converted to JSON by Spring Boot's configured message converters.

## C. `@RequestMapping("/api/students")`

```java
@RequestMapping("/api/students")
```

This defines the common URL prefix for all endpoints in this controller.

For example, a method with `@GetMapping("/{id}")` is available at `/api/students/{id}`. The HTTP method annotation specifies which type of request is matched.

## D. The in-memory collection

```java
private final Map<Integer, Student> students =
        new LinkedHashMap<>();
```

This creates an in-memory map. The key is a student's integer ID, and the value is the corresponding `Student` object.

At the beginning, the map will contain:

| Key | Student |
|---:|---|
| 1 | Amit, Java |
| 2 | Priya, Python |

`private` means other classes cannot access the field directly. `final` means the variable cannot be pointed to a different map, but entries may still be added or removed.

```java
private final AtomicInteger nextId = new AtomicInteger(3);
```

The initial students use IDs 1 and 2, so new students begin at ID 3. Calling `getAndIncrement()` returns the current number and then increments the counter.

```java
public StudentController() {
    students.put(1, new Student(1, "Amit", "Java"));
    students.put(2, new Student(2, "Priya", "Python"));
}
```

This constructor populates the map with two sample students when the controller is created.

**Learning-example limitation:** `LinkedHashMap` is not thread-safe. This code is designed for local practice, not production use. In a real application, use appropriate service and repository layers and a database.

---

# 7. Understand Each CRUD HTTP Method

## 7.1 GET — Read data

### GET all students

```java
@GetMapping
public Collection<Student> getAllStudents() {
    return students.values();
}
```

Explanation:

1. `@GetMapping` maps GET requests to `/api/students`.
2. `public` means the method is accessible to Spring's request handler.
3. `Collection<Student>` says the method returns a collection of `Student` objects.
4. `getAllStudents()` is the Java method name; the name itself does not determine the URL.
5. `students.values()` returns all the student objects stored in the map.

Spring converts the collection into JSON for the response.

### GET one student

```java
@GetMapping("/{id}")
public ResponseEntity<Student> getStudent(
        @PathVariable int id) {

    Student student = students.get(id);

    if (student == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(student);
}
```

Explanation:

- `@GetMapping("/{id}")` matches a URL ending in an ID, such as `/api/students/1`.
- `@PathVariable int id` takes the ID from the URL and passes it into the Java method.
- `students.get(id)` searches the map for that ID.
- If no student exists, `ResponseEntity.notFound().build()` returns `404 Not Found` with no body.
- If the student exists, `ResponseEntity.ok(student)` returns `200 OK` and the student JSON.

Example request:

```http
GET http://localhost:8080/api/students/1
```

Expected response body:

```json
{
  "id": 1,
  "name": "Amit",
  "course": "Java"
}
```

## 7.2 POST — Create data

```java
@PostMapping
public ResponseEntity<Student> createStudent(
        @RequestBody Student student) {

    int id = nextId.getAndIncrement();
    student.setId(id);
    students.put(id, student);

    return ResponseEntity.status(HttpStatus.CREATED)
            .body(student);
}
```

Explanation:

1. `@PostMapping` handles POST requests to `/api/students`.
2. `ResponseEntity<Student>` allows the method to return a response status and a student object.
3. `@RequestBody` tells Spring to read the request body and convert JSON into a `Student` object.
4. `nextId.getAndIncrement()` obtains the next ID. The first new ID is 3.
5. `student.setId(id)` assigns the server-generated ID.
6. `students.put(id, student)` stores the student in the map.
7. `ResponseEntity.status(HttpStatus.CREATED).body(student)` returns `201 Created` and the created student as JSON.

Example request:

```http
POST http://localhost:8080/api/students
Content-Type: application/json
```

Body:

```json
{
  "name": "Vipul",
  "course": "Spring Boot"
}
```

Expected response: `201 Created`

```json
{
  "id": 3,
  "name": "Vipul",
  "course": "Spring Boot"
}
```

The client does not need to provide the ID in the request body because the server generates it.

## 7.3 PUT — Replace a resource

```java
@PutMapping("/{id}")
public ResponseEntity<Student> replaceStudent(
        @PathVariable int id,
        @RequestBody Student updatedStudent) {

    if (!students.containsKey(id)) {
        return ResponseEntity.notFound().build();
    }

    updatedStudent.setId(id);
    students.put(id, updatedStudent);

    return ResponseEntity.ok(updatedStudent);
}
```

Explanation:

- `@PutMapping("/{id}")` maps PUT requests for an individual student.
- `@PathVariable int id` reads the target ID from the URL.
- `@RequestBody Student updatedStudent` converts the JSON body into a new student object.
- `students.containsKey(id)` checks whether that student exists.
- If it does not exist, the method returns `404 Not Found`.
- `updatedStudent.setId(id)` ensures the path ID stays as the resource ID, even if a different ID was sent in the body.
- `students.put(id, updatedStudent)` replaces the existing entry.
- `ResponseEntity.ok(updatedStudent)` returns `200 OK` and the updated student.

This is a simplified PUT implementation: send all editable fields. If the body omits a field, that field may be `null` in the replacement object. This example returns `404` rather than creating a record for an unknown ID.

Example request:

```http
PUT http://localhost:8080/api/students/1
Content-Type: application/json
```

Body:

```json
{
  "name": "Amit Sharma",
  "course": "Spring Boot"
}
```

Expected response: `200 OK`

```json
{
  "id": 1,
  "name": "Amit Sharma",
  "course": "Spring Boot"
}
```

## 7.4 PATCH — Partially update a resource

```java
@PatchMapping("/{id}")
public ResponseEntity<Student> patchStudent(
        @PathVariable int id,
        @RequestBody Student changes) {

    Student existing = students.get(id);

    if (existing == null) {
        return ResponseEntity.notFound().build();
    }

    if (changes.getName() != null) {
        existing.setName(changes.getName());
    }

    if (changes.getCourse() != null) {
        existing.setCourse(changes.getCourse());
    }

    return ResponseEntity.ok(existing);
}
```

Explanation:

1. `@PatchMapping("/{id}")` maps PATCH requests to a particular student's URL.
2. `@PathVariable` reads the student's ID from the URL.
3. `@RequestBody` converts the submitted JSON into a `Student` object named `changes`.
4. `students.get(id)` looks up the existing student.
5. If the student does not exist, the API returns `404 Not Found`.
6. If `changes.getName()` is not `null`, the name is updated. Otherwise, the current name is left alone.
7. The course is handled the same way.
8. `ResponseEntity.ok(existing)` returns the updated record with `200 OK`.

Example request:

```http
PATCH http://localhost:8080/api/students/1
Content-Type: application/json
```

Body:

```json
{
  "name": "Vipul Kayate"
}
```

Only the name is changed. The course remains the same.

Expected response:

```json
{
  "id": 1,
  "name": "Vipul Kayate",
  "course": "Spring Boot"
}
```

**Limitation of this simple PATCH approach:** For ordinary Java fields, an omitted property and a property explicitly supplied as `null` both become `null`. This example treats `null` as “do not change.” If an API must explicitly clear a field, use a dedicated update DTO or a formal patch format.

## 7.5 DELETE — Remove data

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteStudent(
        @PathVariable int id) {

    Student removed = students.remove(id);

    if (removed == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.noContent().build();
}
```

Explanation:

- `@DeleteMapping("/{id}")` maps DELETE requests for an individual student.
- `ResponseEntity<Void>` indicates that the successful response does not need a body.
- `students.remove(id)` removes the student and returns the removed object, or `null` if it did not exist.
- If `removed == null`, the method returns `404 Not Found`.
- `ResponseEntity.noContent().build()` returns `204 No Content`, indicating successful deletion with an empty body.

Example request:

```http
DELETE http://localhost:8080/api/students/2
```

Expected response: `204 No Content` with no response body.

A later GET request for `/api/students/2` will return `404 Not Found` because the student is no longer in the map.

---

# 8. Check the Main Spring Boot Application Class

Spring Initializr generates a main class similar to this:

```java
package com.example.studentapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudentApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentApiApplication.class, args);
    }
}
```

Explanation:

- `package com.example.studentapi;` places the main class in the base package.
- The import statements provide Spring Boot's startup classes.
- `@SpringBootApplication` marks the application configuration and enables Spring Boot's standard auto-configuration and component scanning.
- `public static void main(String[] args)` is the Java program entry point.
- `SpringApplication.run(...)` starts the Spring application and its embedded web server.
- Because `StudentController` is underneath the base package, component scanning can discover and register it automatically.

You do not need to create an HTML file or a Thymeleaf `templates` folder.

---

# 9. Run the Application in Eclipse

1. Save all files with **Ctrl + S**.
2. In Package Explorer, right-click `StudentApiApplication.java`.
3. Select **Run As → Java Application** or **Run As → Spring Boot App**, depending on your Eclipse installation.
4. Check the Console view for a successful startup message and the embedded server running on port `8080`.

If port `8080` is already in use, stop the application using that port or set another port in `src/main/resources/application.properties`, for example:

```properties
server.port=8081
```

If you use port `8081`, update all test URLs accordingly.

---

# 10. Test the API

## A. Test GET in Chrome

Open these URLs after the application has started.

### Get all students

```text
http://localhost:8080/api/students
```

Expected response body:

```json
[
  {"id":1,"name":"Amit","course":"Java"},
  {"id":2,"name":"Priya","course":"Python"}
]
```

### Get one student

```text
http://localhost:8080/api/students/1
```

Expected response body:

```json
{
  "id": 1,
  "name": "Amit",
  "course": "Java"
}
```

The browser's address bar sends GET requests, making it an easy way to test GET endpoints.

## B. Test POST, PUT, PATCH, and DELETE in Postman

Install or open [Postman](https://www.postman.com/).

For each request:

1. Create a new HTTP request.
2. Select the HTTP method from the dropdown.
3. Enter the URL.
4. For POST, PUT, and PATCH, select **Body → raw → JSON**.
5. Paste the JSON body shown below, when applicable.
6. Click **Send** and examine the status code and response body.

### POST — Add a student

Method: `POST`

URL:

```text
http://localhost:8080/api/students
```

Body:

```json
{
  "name": "Vipul",
  "course": "Spring Boot"
}
```

Expected: `201 Created`, with a response containing ID 3.

### PUT — Replace student 1

Method: `PUT`

URL:

```text
http://localhost:8080/api/students/1
```

Body:

```json
{
  "name": "Amit Sharma",
  "course": "Spring Boot"
}
```

Expected: `200 OK`, with the replaced details.

### PATCH — Update only the name

Method: `PATCH`

URL:

```text
http://localhost:8080/api/students/1
```

Body:

```json
{
  "name": "Vipul Kayate"
}
```

Expected: `200 OK`. The course should stay unchanged.

### DELETE — Delete student 2

Method: `DELETE`

URL:

```text
http://localhost:8080/api/students/2
```

No request body is required.

Expected: `204 No Content` with an empty response body.

Then send:

```text
GET http://localhost:8080/api/students/2
```

Expected: `404 Not Found`.

## C. Recommended test sequence

Run these requests in order in the same running application:

1. GET `/api/students` — see Amit and Priya.
2. POST `/api/students` — create Vipul with ID 3.
3. PUT `/api/students/1` — replace Amit's details.
4. PATCH `/api/students/1` — change the name only.
5. DELETE `/api/students/2` — delete Priya.
6. GET `/api/students` — confirm the list now includes students with IDs 1 and 3.

This order lets you observe how each request changes the in-memory data.

---

# 11. Other HTTP Methods: HEAD, OPTIONS, TRACE, and CONNECT

These methods are part of HTTP, but they're less common in beginner CRUD APIs.

## HEAD

HEAD is similar to GET, except the server does not send a response body. It can be useful when you want to inspect response headers without downloading the full representation.

Spring MVC supports HEAD for GET mappings. You can choose `HEAD` in Postman and send a request to an existing GET endpoint, such as:

```text
http://localhost:8080/api/students
```

Check the status and headers; there should be no response body.

## OPTIONS

OPTIONS asks which communication options are supported for a resource. Spring MVC handles OPTIONS requests automatically for mapped URLs in normal configurations and can return an `Allow` header listing supported HTTP methods.

In Postman, select `OPTIONS` and request:

```text
http://localhost:8080/api/students
```

Inspect the response headers. Exact results can depend on the application and Spring configuration.

## TRACE

TRACE is intended for diagnostic loop-back use. It is uncommon in application APIs and is often disabled because reflecting requests may expose sensitive information or introduce security concerns. You generally do not implement it in this Student API.

## CONNECT

CONNECT is mainly used to establish a tunnel through a proxy, often for HTTPS traffic. It is usually handled by proxy and networking infrastructure rather than a typical Spring MVC controller.

## QUERY and extensions

Some HTTP extensions or newer specifications may define additional methods, and framework support varies. They are not needed for this beginner CRUD application. Always check the specification and framework version before relying on an uncommon method.

---

# 12. Common Errors and How to Fix Them

| Error / symptom | Likely cause | What to try |
|---|---|---|
| `404 Not Found` | Wrong URL, method, or ID | Check the URL carefully and confirm the ID exists. |
| `405 Method Not Allowed` | The URL exists, but that HTTP method is not mapped | Select the matching method in Postman. For example, use POST to create, not GET. |
| `400 Bad Request` | Malformed JSON or values that cannot be converted | Check commas, quotation marks, property names, and data types. |
| `415 Unsupported Media Type` | Request body is sent with an unsupported content type | In Postman, choose **Body → raw → JSON**. |
| `package org.springframework... does not exist` | Maven dependencies are missing or unresolved | Right-click project → **Maven → Update Project…**. |
| Controller is not detected | Controller package is outside the application's scan path | Place it underneath `com.example.studentapi` or configure component scanning. |
| Port already in use | Another app is using port 8080 | Stop the other app or configure `server.port`. |
| Browser displays an error instead of JSON | Application failed to start or URL is incorrect | Read the Eclipse Console and verify the URL and port. |
| Changes are not reflected | The app was not restarted or classes were not rebuilt | Save files, check Eclipse's **Project → Build Automatically**, and configure Spring Boot DevTools if desired. |

## Quick check when an endpoint does not work

1. Is the Spring Boot application still running?
2. Is the port correct?
3. Is the URL exactly correct?
4. Did you select the right HTTP method?
5. If sending JSON, is the request body valid and the content type `application/json`?
6. Does the Eclipse Console show an exception?

---

# 13. Important Limitations of This Example

This app is intentionally simple for learning. Keep these limitations in mind:

1. **Data is temporary.** The `Map` is cleared when the application restarts. A database is needed for persistent data.
2. **No service layer.** The controller directly contains data-handling logic. A larger application should generally have a controller, service, and repository layer.
3. **No database or validation.** The sample doesn't validate empty names, invalid courses, or duplicate business rules.
4. **Not a thread-safe data store.** `LinkedHashMap` is not designed for concurrent writes. `AtomicInteger` makes the ID increment atomic, but it does not make the map thread-safe.
5. **Simplified PATCH behavior.** The current implementation interprets `null` as “leave the field unchanged.” It cannot explicitly clear a field.
6. **Simple PUT behavior.** The submitted editable fields replace the object. Send all relevant fields in a PUT request.

A more realistic architecture would look like this:

```text
HTTP Client
    |
    v
StudentController  (HTTP request and response)
    |
    v
StudentService     (business logic)
    |
    v
StudentRepository (database access)
    |
    v
Database
```

---

# 14. Quick Revision — Important Annotations and Classes

| Annotation / class | Purpose |
|---|---|
| `@SpringBootApplication` | Main Spring Boot configuration and application entry setup |
| `@RestController` | Controller whose method responses are written to the response body |
| `@RequestMapping` | Defines a shared or general request mapping |
| `@GetMapping` | Maps GET requests |
| `@PostMapping` | Maps POST requests |
| `@PutMapping` | Maps PUT requests |
| `@PatchMapping` | Maps PATCH requests |
| `@DeleteMapping` | Maps DELETE requests |
| `@PathVariable` | Reads a value from the URL path |
| `@RequestBody` | Converts request-body data into a Java object |
| `ResponseEntity` | Controls response status, headers, and body |
| `HttpStatus.CREATED` | Represents HTTP `201 Created` |
| `ResponseEntity.ok(...)` | Returns `200 OK` with a body |
| `ResponseEntity.notFound().build()` | Returns `404 Not Found` |
| `ResponseEntity.noContent().build()` | Returns `204 No Content` |

## Remember these three points

1. **GET** reads data; **POST** creates; **PUT** replaces; **PATCH** partially updates; **DELETE** removes.
2. `@RestController` lets Spring Boot return JSON or text directly, without Thymeleaf.
3. Chrome is easy for GET requests. Use Postman to practise requests with different methods and JSON bodies.

## Useful links

- [Spring Initializr](https://start.spring.io/) — generate a Spring Boot project.
- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/index.html) — Spring Boot setup and features.
- [Spring Framework Web MVC Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc.html) — request mapping and Spring MVC behavior.
- [Postman](https://www.postman.com/) — test HTTP requests.

---

**End of guide.**
