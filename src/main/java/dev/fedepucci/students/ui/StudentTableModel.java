package dev.fedepucci.students.ui;

import dev.fedepucci.students.domain.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

final class StudentTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {
            "DNI", "Nombre y apellido", "Carrera", "Materias", "Promedio", "Estado"
    };

    private List<Student> students = new ArrayList<>();

    public void setStudents(List<Student> students) {
        this.students = new ArrayList<>(students);
        fireTableDataChanged();
    }

    public Student getStudent(int modelRow) {
        return students.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return switch (columnIndex) {
            case 0 -> Long.class;
            case 3 -> Integer.class;
            case 4 -> Double.class;
            default -> String.class;
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Student student = students.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> student.dni();
            case 1 -> student.fullName();
            case 2 -> student.degree();
            case 3 -> student.passedSubjects();
            case 4 -> student.average().doubleValue();
            case 5 -> student.status().toString();
            default -> throw new IndexOutOfBoundsException("Columna inválida: " + columnIndex);
        };
    }
}
