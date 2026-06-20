package dev.fedepucci.students.domain;

public enum Gender {
    FEMALE("F", "Femenino"),
    MALE("M", "Masculino"),
    NON_BINARY("X", "No binario / Otro");

    private final String code;
    private final String label;

    Gender(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String code() {
        return code;
    }

    public static Gender fromCode(String code) {
        for (Gender value : values()) {
            if (value.code.equalsIgnoreCase(code)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Código de género inválido: " + code);
    }

    @Override
    public String toString() {
        return label;
    }
}
