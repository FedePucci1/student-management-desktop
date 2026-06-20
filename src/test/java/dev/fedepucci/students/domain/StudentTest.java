package dev.fedepucci.students.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StudentTest {
    @Test
    void normalizesTextAndAverage() {
        Student student = student("  Ana   Pérez  ", new BigDecimal("8.256"));

        assertEquals("Ana Pérez", student.fullName());
        assertEquals(new BigDecimal("8.26"), student.average());
    }

    @Test
    void rejectsInvalidAverage() {
        assertThrows(IllegalArgumentException.class,
                () -> student("Ana Pérez", new BigDecimal("10.01")));
    }

    @Test
    void rejectsEnrollmentBeforeBirth() {
        assertThrows(IllegalArgumentException.class, () -> new Student(
                40_000_001L,
                "Ana Pérez",
                Gender.FEMALE,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(1999, 1, 1),
                10,
                new BigDecimal("8.50"),
                StudentStatus.ACTIVE,
                "Ingeniería Informática"));
    }

    private static Student student(String name, BigDecimal average) {
        return new Student(
                40_000_001L,
                name,
                Gender.FEMALE,
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2019, 3, 1),
                10,
                average,
                StudentStatus.ACTIVE,
                "Ingeniería Informática");
    }
}
