package com.example.demo;


import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Student;

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
