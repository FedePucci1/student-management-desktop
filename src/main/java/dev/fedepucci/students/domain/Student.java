package dev.fedepucci.students.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

public record Student(
        long dni,
        String fullName,
        Gender gender,
        LocalDate birthDate,
        LocalDate enrollmentDate,
        int passedSubjects,
        BigDecimal average,
        StudentStatus status,
        String degree) {

    public Student {
        if (dni < 1_000_000L || dni > 99_999_999L) {
            throw new IllegalArgumentException("El DNI debe tener entre 7 y 8 dígitos.");
        }
        fullName = requireText(fullName, "El nombre y apellido son obligatorios.");
        degree = requireText(degree, "La carrera es obligatoria.");
        Objects.requireNonNull(gender, "El género es obligatorio.");
        Objects.requireNonNull(birthDate, "La fecha de nacimiento es obligatoria.");
        Objects.requireNonNull(enrollmentDate, "La fecha de ingreso es obligatoria.");
        Objects.requireNonNull(status, "El estado es obligatorio.");

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
        }
        if (enrollmentDate.isBefore(birthDate)) {
            throw new IllegalArgumentException("La fecha de ingreso no puede ser anterior al nacimiento.");
        }
        if (passedSubjects < 0) {
            throw new IllegalArgumentException("Las materias aprobadas no pueden ser negativas.");
        }
        if (average == null || average.compareTo(BigDecimal.ZERO) < 0
                || average.compareTo(BigDecimal.TEN) > 0) {
            throw new IllegalArgumentException("El promedio debe estar entre 0 y 10.");
        }
        average = average.setScale(2, RoundingMode.HALF_UP);
    }

    public Student withStatus(StudentStatus newStatus) {
        return new Student(dni, fullName, gender, birthDate, enrollmentDate,
                passedSubjects, average, newStatus, degree);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim().replaceAll("\\s+", " ");
    }
}
