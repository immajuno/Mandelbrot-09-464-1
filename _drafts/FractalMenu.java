import javax.swing.*;
import java.awt.*;

public class MandelbrotFrame extends JFrame {
    private FractalPanel fractalPanel;
    private Converter converter;

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveItem = new JMenuItem("Сохранить");
        JMenuItem loadItem = new JMenuItem("Открыть");
        JMenuItem undoItem = new JMenuItem("Отменить");
        JMenuItem juliaItem = new JMenuItem("Открыть множество Жюлиа");
        JMenuItem exitItem = new JMenuItem("Выход");

        saveItem.addActionListener(e -> fractalPanel.saveFractal());
        loadItem.addActionListener(e -> fractalPanel.loadFractal());
        undoItem.addActionListener(e -> fractalPanel.undo());
        juliaItem.addActionListener(e -> fractalPanel.openJuliaWindow());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.add(undoItem);
        fileMenu.addSeparator();
        fileMenu.add(juliaItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MandelbrotFrame::new); // 0
    }
}