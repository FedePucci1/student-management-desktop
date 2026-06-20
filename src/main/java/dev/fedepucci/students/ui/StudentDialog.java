package dev.fedepucci.students.ui;

import dev.fedepucci.students.domain.Gender;
import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

final class StudentDialog extends JDialog {
    private final JTextField dniField = new JTextField(18);
    private final JTextField nameField = new JTextField(28);
    private final JComboBox<Gender> genderCombo = new JComboBox<>(Gender.values());
    private final JSpinner birthDateSpinner = dateSpinner(LocalDate.now().minusYears(18));
    private final JSpinner enrollmentDateSpinner = dateSpinner(LocalDate.now());
    private final JSpinner passedSubjectsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 200, 1));
    private final JSpinner averageSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.1));
    private final JTextField degreeField = new JTextField(28);

    private final Student original;
    private Student result;

    StudentDialog(Frame owner, Student student) {
        super(owner, student == null ? "Nuevo alumno" : "Editar alumno", true);
        this.original = student;
        buildUi();
        if (student != null) {
            fill(student);
        }
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    Optional<Student> showDialog() {
        setVisible(true);
        return Optional.ofNullable(result);
    }

    private void buildUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(18, 20, 12, 20));
        int row = 0;
        addRow(form, row++, "DNI", dniField);
        addRow(form, row++, "Nombre y apellido", nameField);
        addRow(form, row++, "Género", genderCombo);
        addRow(form, row++, "Fecha de nacimiento", birthDateSpinner);
        addRow(form, row++, "Fecha de ingreso", enrollmentDateSpinner);
        addRow(form, row++, "Materias aprobadas", passedSubjectsSpinner);
        addRow(form, row++, "Promedio", averageSpinner);
        addRow(form, row, "Carrera", degreeField);

        JButton cancelButton = new JButton("Cancelar");
        cancelButton.addActionListener(event -> dispose());
        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(event -> save());
        getRootPane().setDefaultButton(saveButton);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.add(cancelButton);
        actions.add(saveButton);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    private void save() {
        try {
            long dni = Long.parseLong(dniField.getText().trim());
            StudentStatus status = original == null ? StudentStatus.ACTIVE : original.status();
            result = new Student(
                    dni,
                    nameField.getText(),
                    (Gender) genderCombo.getSelectedItem(),
                    toLocalDate((Date) birthDateSpinner.getValue()),
                    toLocalDate((Date) enrollmentDateSpinner.getValue()),
                    (Integer) passedSubjectsSpinner.getValue(),
                    BigDecimal.valueOf(((Number) averageSpinner.getValue()).doubleValue()),
                    status,
                    degreeField.getText());
            dispose();
        } catch (NumberFormatException exception) {
            showValidationError("El DNI debe contener sólo números.");
        } catch (IllegalArgumentException exception) {
            showValidationError(exception.getMessage());
        }
    }

    private void fill(Student student) {
        dniField.setText(Long.toString(student.dni()));
        dniField.setEditable(false);
        nameField.setText(student.fullName());
        genderCombo.setSelectedItem(student.gender());
        birthDateSpinner.setValue(toDate(student.birthDate()));
        enrollmentDateSpinner.setValue(toDate(student.enrollmentDate()));
        passedSubjectsSpinner.setValue(student.passedSubjects());
        averageSpinner.setValue(student.average().doubleValue());
        degreeField.setText(student.degree());
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message, "Datos inválidos", JOptionPane.WARNING_MESSAGE);
    }

    private static void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(5, 4, 5, 12);
        panel.add(new JLabel(label + ":"), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(5, 4, 5, 4);
        panel.add(component, fieldConstraints);
    }

    private static JSpinner dateSpinner(LocalDate initialDate) {
        JSpinner spinner = new JSpinner(new SpinnerDateModel(toDate(initialDate), null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "dd/MM/yyyy"));
        return spinner;
    }

    private static Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
