package dev.fedepucci.students.repository;

import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends AutoCloseable {
    Student save(Student student);

    Optional<Student> findByDni(long dni);

    List<Student> search(String query, StudentStatus status);

    boolean deletePermanently(long dni);

    @Override
    default void close() {
    }
}
