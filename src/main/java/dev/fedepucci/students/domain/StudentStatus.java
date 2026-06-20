package dev.fedepucci.students.domain;

public enum StudentStatus {
    ACTIVE("Activo"),
    INACTIVE("Inactivo");

    private final String label;

    StudentStatus(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label;
    }
}
