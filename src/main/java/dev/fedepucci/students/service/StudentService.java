package dev.fedepucci.students.service;

import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;
import dev.fedepucci.students.repository.RepositoryException;
import dev.fedepucci.students.repository.StudentRepository;

import java.util.List;

public final class StudentService {
    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    public List<Student> search(String query, StudentStatus status) {
        return repository.search(query, status);
    }

    public Student create(Student student) {
        if (repository.findByDni(student.dni()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un alumno con ese DNI.");
        }
        return repository.save(student);
    }

    public Student update(Student student) {
        if (repository.findByDni(student.dni()).isEmpty()) {
            throw new RepositoryException("El alumno ya no existe.");
        }
        return repository.save(student);
    }

    public Student changeStatus(Student student, StudentStatus status) {
        return update(student.withStatus(status));
    }
}
