package dev.fedepucci.students.repository;

import dev.fedepucci.students.domain.Gender;
import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;
import dev.fedepucci.students.service.StudentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcStudentRepositoryTest {
    private JdbcStudentRepository repository;
    private StudentService service;

    @BeforeEach
    void setUp() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        repository = new JdbcStudentRepository(url, "sa", "");
        service = new StudentService(repository);
    }

    @Test
    void createsReadsAndUpdatesStudent() {
        Student original = student(40_000_001L, "Ana Pérez", "Ingeniería Informática");
        service.create(original);

        assertEquals(original, repository.findByDni(original.dni()).orElseThrow());

        Student updated = new Student(original.dni(), "Ana Pérez", original.gender(),
                original.birthDate(), original.enrollmentDate(), 14, new BigDecimal("9.10"),
                original.status(), original.degree());
        service.update(updated);

        assertEquals(14, repository.findByDni(original.dni()).orElseThrow().passedSubjects());
    }

    @Test
    void preventsDuplicateDniThroughService() {
        Student student = student(40_000_001L, "Ana Pérez", "Ingeniería Informática");
        service.create(student);

        assertThrows(IllegalArgumentException.class,
                () -> service.create(student(40_000_001L, "Otra Persona", "Medicina")));
    }

    @Test
    void searchesAndFiltersByStatus() {
        Student ana = student(40_000_001L, "Ana Pérez", "Ingeniería Informática");
        Student bruno = student(40_000_002L, "Bruno Díaz", "Medicina");
        service.create(ana);
        service.create(bruno);
        service.changeStatus(bruno, StudentStatus.INACTIVE);

        List<Student> byDegree = service.search("informática", null);
        assertEquals(List.of(ana), byDegree);

        List<Student> inactive = service.search("", StudentStatus.INACTIVE);
        assertEquals(1, inactive.size());
        assertEquals(bruno.dni(), inactive.getFirst().dni());
    }

    @Test
    void supportsPermanentDeletionAtRepositoryLevel() {
        Student student = student(40_000_001L, "Ana Pérez", "Ingeniería Informática");
        service.create(student);

        assertTrue(repository.deletePermanently(student.dni()));
        assertFalse(repository.findByDni(student.dni()).isPresent());
    }

    private static Student student(long dni, String name, String degree) {
        return new Student(
                dni,
                name,
                Gender.NON_BINARY,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2019, 3, 1),
                10,
                new BigDecimal("8.50"),
                StudentStatus.ACTIVE,
                degree);
    }
}
