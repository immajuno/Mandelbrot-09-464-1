import ru.gr0946x.ui.MainWindow;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Запускаем UI в потоке событий Swing (EDT) — обязательное требование Swing
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
