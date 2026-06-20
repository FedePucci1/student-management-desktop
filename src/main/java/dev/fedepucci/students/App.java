package dev.fedepucci.students;

import dev.fedepucci.students.repository.JdbcStudentRepository;
import dev.fedepucci.students.service.StudentService;
import dev.fedepucci.students.ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        if (args.length > 0 && "--check".equals(args[0])) {
            runHealthCheck();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                JdbcStudentRepository repository = JdbcStudentRepository.fromEnvironment();
                new MainFrame(new StudentService(repository)).setVisible(true);
            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null,
                        "No se pudo iniciar la aplicación:\n" + exception.getMessage(),
                        "Error de inicio", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static void runHealthCheck() {
        try (JdbcStudentRepository repository = JdbcStudentRepository.fromEnvironment()) {
            int count = repository.search("", null).size();
            System.out.println("OK - base inicializada; alumnos registrados: " + count);
        }
    }
}
