package dev.fedepucci.students.repository;

import dev.fedepucci.students.domain.Gender;
import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JdbcStudentRepository implements StudentRepository {
    public static final String DEFAULT_URL = "jdbc:h2:file:./data/students;DB_CLOSE_ON_EXIT=FALSE";

    private final String url;
    private final String user;
    private final String password;

    public JdbcStudentRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        initializeSchema();
    }

    public static JdbcStudentRepository fromEnvironment() {
        String url = valueOrDefault(System.getenv("STUDENT_DB_URL"), DEFAULT_URL);
        String user = valueOrDefault(System.getenv("STUDENT_DB_USER"), "sa");
        String password = valueOrDefault(System.getenv("STUDENT_DB_PASSWORD"), "");
        if (DEFAULT_URL.equals(url)) {
            try {
                Files.createDirectories(Path.of("data"));
            } catch (IOException exception) {
                throw new RepositoryException("No se pudo crear el directorio de datos.", exception);
            }
        }
        return new JdbcStudentRepository(url, user, password);
    }

    @Override
    public Student save(Student student) {
        String update = """
                UPDATE students
                   SET full_name = ?, gender = ?, birth_date = ?, enrollment_date = ?,
                       passed_subjects = ?, average = ?, status = ?, degree = ?, updated_at = CURRENT_TIMESTAMP
                 WHERE dni = ?
                """;
        String insert = """
                INSERT INTO students
                    (dni, full_name, gender, birth_date, enrollment_date, passed_subjects, average, status, degree)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = connection()) {
            connection.setAutoCommit(false);
            try {
                int updated;
                try (PreparedStatement statement = connection.prepareStatement(update)) {
                    bindStudentForUpdate(statement, student);
                    updated = statement.executeUpdate();
                }
                if (updated == 0) {
                    try (PreparedStatement statement = connection.prepareStatement(insert)) {
                        bindStudentForInsert(statement, student);
                        statement.executeUpdate();
                    }
                }
                connection.commit();
                return student;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new RepositoryException("No se pudo guardar el alumno.", exception);
        }
    }

    @Override
    public Optional<Student> findByDni(long dni) {
        String sql = "SELECT * FROM students WHERE dni = ?";
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dni);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RepositoryException("No se pudo consultar el alumno.", exception);
        }
    }

    @Override
    public List<Student> search(String query, StudentStatus status) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();

        if (!normalized.isBlank()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR CAST(dni AS VARCHAR) LIKE ? OR LOWER(degree) LIKE ?)");
            String pattern = "%" + normalized + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            parameters.add(status.name());
        }
        sql.append(" ORDER BY full_name, dni");

        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }
            List<Student> students = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    students.add(map(result));
                }
            }
            return students;
        } catch (SQLException exception) {
            throw new RepositoryException("No se pudo listar los alumnos.", exception);
        }
    }

    @Override
    public boolean deletePermanently(long dni) {
        try (Connection connection = connection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM students WHERE dni = ?")) {
            statement.setLong(1, dni);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RepositoryException("No se pudo eliminar el alumno.", exception);
        }
    }

    private void initializeSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS students (
                    dni BIGINT PRIMARY KEY,
                    full_name VARCHAR(120) NOT NULL,
                    gender VARCHAR(16) NOT NULL,
                    birth_date DATE NOT NULL,
                    enrollment_date DATE NOT NULL,
                    passed_subjects INTEGER NOT NULL CHECK (passed_subjects >= 0),
                    average DECIMAL(4, 2) NOT NULL CHECK (average >= 0 AND average <= 10),
                    status VARCHAR(16) NOT NULL,
                    degree VARCHAR(120) NOT NULL,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection connection = connection(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_students_name ON students(full_name)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_students_status ON students(status)");
        } catch (SQLException exception) {
            throw new RepositoryException("No se pudo inicializar la base de datos.", exception);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static void bindStudentForInsert(PreparedStatement statement, Student student) throws SQLException {
        statement.setLong(1, student.dni());
        statement.setString(2, student.fullName());
        statement.setString(3, student.gender().code());
        statement.setDate(4, Date.valueOf(student.birthDate()));
        statement.setDate(5, Date.valueOf(student.enrollmentDate()));
        statement.setInt(6, student.passedSubjects());
        statement.setBigDecimal(7, student.average());
        statement.setString(8, student.status().name());
        statement.setString(9, student.degree());
    }

    private static void bindStudentForUpdate(PreparedStatement statement, Student student) throws SQLException {
        statement.setString(1, student.fullName());
        statement.setString(2, student.gender().code());
        statement.setDate(3, Date.valueOf(student.birthDate()));
        statement.setDate(4, Date.valueOf(student.enrollmentDate()));
        statement.setInt(5, student.passedSubjects());
        statement.setBigDecimal(6, student.average());
        statement.setString(7, student.status().name());
        statement.setString(8, student.degree());
        statement.setLong(9, student.dni());
    }

    private static Student map(ResultSet result) throws SQLException {
        return new Student(
                result.getLong("dni"),
                result.getString("full_name"),
                Gender.fromCode(result.getString("gender")),
                result.getDate("birth_date").toLocalDate(),
                result.getDate("enrollment_date").toLocalDate(),
                result.getInt("passed_subjects"),
                result.getBigDecimal("average"),
                StudentStatus.valueOf(result.getString("status")),
                result.getString("degree"));
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
