package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.animation.AnimationSettingsFrame;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;

import static javax.swing.SwingUtilities.invokeLater;
import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    private final Deque<FractalState> undoStack = new ArrayDeque<>();

    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(mandelbrot, conv, (value)->{
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    double real = conv.xScr2Crt(e.getX());
                    double imag = conv.yScr2Crt(e.getY());
                    new JuliaFrame(real, imag);
                }
            }
        });

        // Zoom по выделению — сохраняем состояние ДО изменения
        mainPanel.addSelectListener((r) -> {
            pushUndo();

            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);

            double currentWidth = xMax - xMin;
            int newIterations = (int) (100 + 100 * Math.abs(Math.log10(currentWidth / 3.0)));
            newIterations = Math.min(newIterations, 2000);
            ((Mandelbrot) mandelbrot).setMaxIterations(newIterations);

            mainPanel.repaint();
        });

        // Pan (правая кнопка) — сохраняем состояние ОДИН РАЗ при завершении жеста
        mainPanel.addPanListener(() -> pushUndo());

        setContent();
        createMenu();
    }

    /** Сохраняет текущее состояние в стек отмены (не более 100 шагов). */
    private void pushUndo() {
        undoStack.push(new FractalState(
                conv.getXMin(), conv.getXMax(),
                conv.getYMin(), conv.getYMax(),
                ((Mandelbrot) mandelbrot).getMaxIterations()
        ));
        if (undoStack.size() > 100) undoStack.pollLast();
    }

    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // ── Файл ──────────────────────────────────────────────────────────────
        JMenu fileMenu = new JMenu("Файл");

        JMenuItem saveItem = new JMenuItem("Сохранить фрактал...");
        saveItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveFractal());

        JMenuItem loadItem = new JMenuItem("Открыть фрактал...");
        loadItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        loadItem.addActionListener(e -> loadFractal());

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Выход")).addActionListener(e -> System.exit(0));

        // ── Правка ────────────────────────────────────────────────────────────
        JMenu editMenu = new JMenu("Правка");

        JMenuItem undoItem = new JMenuItem("Отмена");
        undoItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> undo());
        editMenu.add(undoItem);

        // ── Вид ───────────────────────────────────────────────────────────────
        JMenu viewMenu = new JMenu("Вид");

        JMenuItem juliaItem = new JMenuItem("Множество Жюлиа...");
        juliaItem.addActionListener(e -> {
            double cx = (conv.getXMin() + conv.getXMax()) / 2.0;
            double cy = (conv.getYMin() + conv.getYMax()) / 2.0;
            new JuliaFrame(cx, cy);
        });

        JMenuItem tourItem = new JMenuItem("Экскурсия по фракталу...");
        tourItem.addActionListener(e -> new AnimationSettingsFrame(conv).setVisible(true));

        viewMenu.add(juliaItem);
        viewMenu.add(tourItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        setJMenuBar(menuBar);
    }

    // ── Сохранение (п. 5) ─────────────────────────────────────────────────────

    private void saveFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить фрактал");
        chooser.setAcceptAllFileFilterUsed(false);

        var filterFrac = new javax.swing.filechooser.FileNameExtensionFilter(
                "Данные фрактала (*.frac)", "frac");
        var filterPng  = new javax.swing.filechooser.FileNameExtensionFilter(
                "PNG-изображение (*.png)", "png");
        var filterJpg  = new javax.swing.filechooser.FileNameExtensionFilter(
                "JPG-изображение (*.jpg)", "jpg");

        chooser.addChoosableFileFilter(filterFrac);
        chooser.addChoosableFileFilter(filterPng);
        chooser.addChoosableFileFilter(filterJpg);
        chooser.setFileFilter(filterFrac);

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        var selected = chooser.getFileFilter();

        try {
            if (selected == filterFrac) {
                // ── .frac — сериализованный FractalState ─────────────────────
                if (!file.getName().toLowerCase().endsWith(".frac"))
                    file = new java.io.File(file.getAbsolutePath() + ".frac");
                try (var oos = new java.io.ObjectOutputStream(new java.io.FileOutputStream(file))) {
                    oos.writeObject(new FractalState(
                            conv.getXMin(), conv.getXMax(),
                            conv.getYMin(), conv.getYMax(),
                            ((Mandelbrot) mandelbrot).getMaxIterations()));
                }
                JOptionPane.showMessageDialog(this, "Фрактал сохранён:\n" + file.getAbsolutePath());

            } else {
                // ── PNG / JPG — рендерим изображение с подписью ──────────────
                boolean isPng = (selected == filterPng);
                String ext = isPng ? ".png" : ".jpg";
                String fmt = isPng ? "png"  : "jpg";
                if (!file.getName().toLowerCase().endsWith(ext))
                    file = new java.io.File(file.getAbsolutePath() + ext);

                // Рендерим в фоне, чтобы UI не замёрз
                final java.io.File finalFile = file;
                final String finalFmt = fmt;
                setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.WAIT_CURSOR));
                new Thread(() -> {
                    try {
                        FractalPainter fp = (FractalPainter) painter;
                        java.awt.image.BufferedImage img =
                                fp.renderToImage(mainPanel.getWidth(), mainPanel.getHeight());
                        java.awt.image.BufferedImage signed = addSignature(img);
                        javax.imageio.ImageIO.write(signed, finalFmt, finalFile);
                        SwingUtilities.invokeLater(() -> {
                            setCursor(java.awt.Cursor.getDefaultCursor());
                            JOptionPane.showMessageDialog(this,
                                    "Изображение сохранено:\n" + finalFile.getAbsolutePath());
                        });
                    } catch (Exception ex) {
                        SwingUtilities.invokeLater(() -> {
                            setCursor(java.awt.Cursor.getDefaultCursor());
                            JOptionPane.showMessageDialog(this,
                                    "Ошибка сохранения: " + ex.getMessage(),
                                    "Ошибка", JOptionPane.ERROR_MESSAGE);
                        });
                    }
                }).start();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Ошибка сохранения: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Добавляет подпись с координатами на изображение (требование п. 5б/в). */
    private java.awt.image.BufferedImage addSignature(java.awt.image.BufferedImage src) {
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(
                src.getWidth(), src.getHeight(), src.getType());
        java.awt.Graphics2D g2 = out.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        String text = String.format("Re[%.8f; %.8f]  Im[%.8f; %.8f]",
                conv.getXMin(), conv.getXMax(), conv.getYMin(), conv.getYMax());
        // Тень для читаемости на тёмном фоне
        g2.setColor(Color.BLACK);
        g2.drawString(text, 9, src.getHeight() - 9);
        g2.setColor(Color.WHITE);
        g2.drawString(text, 8, src.getHeight() - 10);
        g2.dispose();
        return out;
    }

    // ── Открытие (п. 6) ───────────────────────────────────────────────────────

    private void loadFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть фрактал");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Данные фрактала (*.frac)", "frac"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try (var ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream(chooser.getSelectedFile()))) {
            FractalState state = (FractalState) ois.readObject();
            pushUndo();   // сохраняем текущее состояние перед загрузкой
            conv.setXShape(state.xMin, state.xMax);
            conv.setYShape(state.yMin, state.yMax);
            ((Mandelbrot) mandelbrot).setMaxIterations(state.maxIterations);
            mainPanel.repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось открыть файл: " + ex.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            FractalState state = undoStack.pop();
            conv.setXShape(state.xMin, state.xMax);
            conv.setYShape(state.yMin, state.yMax);
            ((Mandelbrot) mandelbrot).setMaxIterations(state.maxIterations);
            mainPanel.repaint();
        }
    }

    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }
}
