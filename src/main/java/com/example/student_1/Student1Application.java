package com.example.student_1;

import com.example.student_1.entities.Student;
import com.example.student_1.repositories.StudentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Student1Application {

    public static void main(String[] args) {
        SpringApplication.run(Student1Application.class, args);
    }
    @Bean
    CommandLineRunner start(StudentRepository etudiantRepository) {
        return args -> {

            Student c = Student.builder().email("chaimaafahmi@gmail.com").lastName("fahmi").firstName("chaimaa").cne("F143029580").build();
            etudiantRepository.save(c);
            List<Student> listes = etudiantRepository.findAll();
            for (Student cp : listes) {
                System.out.println("--------------------");
                System.out.println(cp.getEmail());
                System.out.println(cp.getLastName());
                System.out.println(cp.getFirstName());
                System.out.println(cp.getCne());
            }
        };
    }
}
