package dev.fedepucci.students.ui;

import dev.fedepucci.students.domain.Student;
import dev.fedepucci.students.domain.StudentStatus;
import dev.fedepucci.students.service.StudentService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public final class MainFrame extends JFrame {
    private static final Color PRIMARY = new Color(37, 99, 235);

    private final StudentService service;
    private final StudentTableModel tableModel = new StudentTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField searchField = new JTextField(24);
    private final JComboBox<StatusFilter> statusFilter = new JComboBox<>(StatusFilter.values());
    private final JLabel summaryLabel = new JLabel(" ");
    private final JButton editButton = new JButton("Editar");
    private final JButton statusButton = new JButton("Dar de baja");

    public MainFrame(StudentService service) {
        super("Gestión de alumnos");
        this.service = service;
        buildUi();
        bindEvents();
        refresh();
    }

    private void buildUi() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1120, 680);
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("Gestión de alumnos");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("Administración académica · altas, edición y seguimiento");
        subtitle.setForeground(new Color(219, 234, 254));
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        header.add(titles, BorderLayout.WEST);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 12));
        filters.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
        filters.add(new JLabel("Buscar:"));
        searchField.setToolTipText("Nombre, DNI o carrera");
        filters.add(searchField);
        filters.add(new JLabel("Estado:"));
        filters.add(statusFilter);

        JButton newButton = new JButton("Nuevo alumno");
        newButton.setBackground(PRIMARY);
        newButton.setForeground(Color.WHITE);
        newButton.addActionListener(event -> createStudent());
        filters.add(newButton);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(30);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);
        table.getColumnModel().getColumn(2).setPreferredWidth(220);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        editButton.setEnabled(false);
        statusButton.setEnabled(false);
        editButton.addActionListener(event -> editSelected());
        statusButton.addActionListener(event -> toggleStatus());
        actions.add(summaryLabel);
        actions.add(editButton);
        actions.add(statusButton);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(0, 18, 10, 18));
        center.add(filters, BorderLayout.NORTH);
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        JLabel footer = new JLabel("Los datos se guardan automáticamente en ./data/students.mv.db", SwingConstants.CENTER);
        footer.setBorder(BorderFactory.createEmptyBorder(7, 10, 10, 10));
        footer.setForeground(Color.DARK_GRAY);

        add(header, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void bindEvents() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                refresh();
            }
        });
        statusFilter.addActionListener(event -> refresh());
        table.getSelectionModel().addListSelectionListener(event -> updateActions());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    editSelected();
                }
            }
        });
    }

    private void refresh() {
        try {
            StatusFilter filter = (StatusFilter) statusFilter.getSelectedItem();
            List<Student> students = service.search(searchField.getText(), filter == null ? null : filter.status);
            tableModel.setStudents(students);
            summaryLabel.setText(students.size() + (students.size() == 1 ? " alumno" : " alumnos"));
            updateActions();
        } catch (RuntimeException exception) {
            showError(exception);
        }
    }

    private void createStudent() {
        new StudentDialog(this, null).showDialog().ifPresent(student -> {
            try {
                service.create(student);
                refresh();
                selectStudent(student.dni());
            } catch (RuntimeException exception) {
                showError(exception);
            }
        });
    }

    private void editSelected() {
        Student selected = selectedStudent();
        if (selected == null) {
            return;
        }
        new StudentDialog(this, selected).showDialog().ifPresent(student -> {
            try {
                service.update(student);
                refresh();
                selectStudent(student.dni());
            } catch (RuntimeException exception) {
                showError(exception);
            }
        });
    }

    private void toggleStatus() {
        Student selected = selectedStudent();
        if (selected == null) {
            return;
        }
        StudentStatus newStatus = selected.status() == StudentStatus.ACTIVE
                ? StudentStatus.INACTIVE : StudentStatus.ACTIVE;
        String action = newStatus == StudentStatus.INACTIVE ? "dar de baja" : "reactivar";
        int option = JOptionPane.showConfirmDialog(this,
                "¿Querés " + action + " a " + selected.fullName() + "?",
                "Confirmar cambio de estado", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            try {
                service.changeStatus(selected, newStatus);
                refresh();
            } catch (RuntimeException exception) {
                showError(exception);
            }
        }
    }

    private Student selectedStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            return null;
        }
        return tableModel.getStudent(table.convertRowIndexToModel(viewRow));
    }

    private void updateActions() {
        Student selected = selectedStudent();
        editButton.setEnabled(selected != null);
        statusButton.setEnabled(selected != null);
        if (selected != null) {
            statusButton.setText(selected.status() == StudentStatus.ACTIVE ? "Dar de baja" : "Reactivar");
        }
    }

    private void selectStudent(long dni) {
        for (int modelRow = 0; modelRow < tableModel.getRowCount(); modelRow++) {
            if (tableModel.getStudent(modelRow).dni() == dni) {
                int viewRow = table.convertRowIndexToView(modelRow);
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void showError(RuntimeException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(), "No se pudo completar la operación",
                JOptionPane.ERROR_MESSAGE);
    }

    private enum StatusFilter {
        ALL("Todos", null),
        ACTIVE("Activos", StudentStatus.ACTIVE),
        INACTIVE("Inactivos", StudentStatus.INACTIVE);

        private final String label;
        private final StudentStatus status;

        StatusFilter(String label, StudentStatus status) {
            this.label = label;
            this.status = status;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
