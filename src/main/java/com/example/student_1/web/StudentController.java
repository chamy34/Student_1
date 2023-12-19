package com.example.student_1.web;
import com.example.student_1.entities.Student;
import com.example.student_1.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Endpoint pour récupérer tous les étudiants
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // Endpoint pour récupérer un étudiant par son ID
    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        return student.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Endpoint pour créer un nouvel étudiant
    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentRepository.save(student);
        return new ResponseEntity<>(createdStudent, HttpStatus.CREATED);
    }

    // Endpoint pour mettre à jour un étudiant
    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student updatedStudent) {
        Optional<Student> existingStudent = studentRepository.findById(id);

        if (existingStudent.isPresent()) {
            Student savedStudent = existingStudent.get();
            savedStudent.setFirstName(updatedStudent.getFirstName());
            savedStudent.setLastName(updatedStudent.getLastName());
            savedStudent.setCne(updatedStudent.getCne());
            savedStudent.setEmail(updatedStudent.getEmail());

            studentRepository.save(savedStudent);
            return ResponseEntity.ok(savedStudent);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Endpoint pour supprimer un étudiant
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        Optional<Student> studentToDelete = studentRepository.findById(id);

        if (studentToDelete.isPresent()) {
            studentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
