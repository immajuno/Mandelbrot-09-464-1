package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnimationSettingsFrame extends JFrame {

    private final Converter conv;
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField totalTimeField;
    private final JLabel statusLabel;

    public AnimationSettingsFrame(Converter conv) {
        this.conv = conv;

        setTitle("Экскурсия по фракталу — настройка анимации");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Таблица ключевых кадров ─────────────────────────────────────
        String[] columns = {"№", "X min", "X max", "Y min", "Y max", "Время (сек)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);

        // ── Кнопки управления кадрами ───────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton addButton = new JButton("+ Добавить текущий вид");
        addButton.addActionListener(e -> addCurrentView());

        JButton removeButton = new JButton("− Удалить выбранный");
        removeButton.addActionListener(e -> removeSelected());

        JButton moveUpButton = new JButton("▲ Вверх");
        moveUpButton.addActionListener(e -> moveSelected(-1));

        JButton moveDownButton = new JButton("▼ Вниз");
        moveDownButton.addActionListener(e -> moveSelected(1));

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(moveUpButton);
        buttonPanel.add(moveDownButton);

        // ── Общее время анимации ────────────────────────────────────────
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timePanel.add(new JLabel("Общее время анимации (сек):"));
        totalTimeField = new JTextField("10", 6);
        timePanel.add(totalTimeField);

        JButton autoDistributeButton = new JButton("Распределить равномерно");
        autoDistributeButton.addActionListener(e -> autoDistributeTime());
        timePanel.add(autoDistributeButton);

        // ── Статус ─────────────────────────────────────────────────────
        statusLabel = new JLabel("Готово");
        statusLabel.setForeground(Color.GRAY);

        // ── Кнопки действий ────────────────────────────────────────────
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton previewButton = new JButton("Предпросмотр");
        previewButton.addActionListener(e -> startPreview());

        JButton saveVideoButton = new JButton("Сохранить видео...");
        saveVideoButton.addActionListener(e -> saveVideo());

        actionPanel.add(statusLabel);
        actionPanel.add(previewButton);
        actionPanel.add(saveVideoButton);

        // ── Компоновка ─────────────────────────────────────────────────
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        mainPanel.add(tableScroll, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(timePanel, BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);

        addCurrentView();
    }

    // ── Методы работы с таблицей ───────────────────────────────────────

    private void addCurrentView() {
        double totalTime = parseTotalTime();
        double lastTime = keyFrames.isEmpty() ? 0 : keyFrames.get(keyFrames.size() - 1).timeSeconds;
        double newTime = keyFrames.isEmpty() ? 0 : lastTime + totalTime / 5;

        KeyFrame kf = new KeyFrame(
                conv.getXMin(), conv.getXMax(),
                conv.getYMin(), conv.getYMax(),
                newTime
        );
        keyFrames.add(kf);
        addRow(kf, keyFrames.size());
    }

    private void addRow(KeyFrame kf, int number) {
        tableModel.addRow(new Object[]{
                number,
                String.format("%.8f", kf.xMin),
                String.format("%.8f", kf.xMax),
                String.format("%.8f", kf.yMin),
                String.format("%.8f", kf.yMax),
                String.format("%.2f", kf.timeSeconds)
        });
    }

    private void removeSelected() {
        int row = table.getSelectedRow();
        if (row >= 0 && keyFrames.size() > 1) {
            keyFrames.remove(row);
            tableModel.removeRow(row);
            renumberTable();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Должен остаться хотя бы один кадр.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void moveSelected(int delta) {
        int row = table.getSelectedRow();
        int newRow = row + delta;
        if (row < 0 || newRow < 0 || newRow >= keyFrames.size()) return;

        KeyFrame kf = keyFrames.remove(row);
        keyFrames.add(newRow, kf);

        tableModel.setRowCount(0);
        for (int i = 0; i < keyFrames.size(); i++) {
            addRow(keyFrames.get(i), i + 1);
        }
        table.setRowSelectionInterval(newRow, newRow);
    }

    private void renumberTable() {
        for (int i = 0; i < keyFrames.size(); i++) {
            tableModel.setValueAt(i + 1, i, 0);
        }
    }

    private void autoDistributeTime() {
        double totalTime = parseTotalTime();
        if (keyFrames.size() < 2) return;

        double step = totalTime / (keyFrames.size() - 1);
        for (int i = 0; i < keyFrames.size(); i++) {
            keyFrames.get(i).timeSeconds = i * step;
            tableModel.setValueAt(String.format("%.2f", i * step), i, 5);
        }
    }

    private double parseTotalTime() {
        try {
            return Double.parseDouble(totalTimeField.getText().trim());
        } catch (NumberFormatException e) {
            return 10.0;
        }
    }

    /**
     * Считывает данные из таблицы обратно в список keyFrames.
     */
    public List<KeyFrame> getKeyFrames() {
        for (int i = 0; i < keyFrames.size() && i < tableModel.getRowCount(); i++) {
            try {
                keyFrames.get(i).xMin = Double.parseDouble((String) tableModel.getValueAt(i, 1));
                keyFrames.get(i).xMax = Double.parseDouble((String) tableModel.getValueAt(i, 2));
                keyFrames.get(i).yMin = Double.parseDouble((String) tableModel.getValueAt(i, 3));
                keyFrames.get(i).yMax = Double.parseDouble((String) tableModel.getValueAt(i, 4));
                keyFrames.get(i).timeSeconds = Double.parseDouble((String) tableModel.getValueAt(i, 5));
            } catch (NumberFormatException ignored) {
            }
        }
        return new ArrayList<>(keyFrames);
    }

    // ── Предпросмотр и сохранение видео ────────────────────────────────

    private void startPreview() {
        List<KeyFrame> frames = getKeyFrames();
        if (frames.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте минимум 2 ключевых кадра.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Отключаем кнопки на время рендеринга
        setButtonsEnabled(false);
        statusLabel.setText("Рендеринг предпросмотра...");

        new Thread(() -> {
            try {
                AnimationRenderer renderer = new AnimationRenderer(frames, conv);
                java.util.List<File> frameFiles = renderer.renderAllFrames(null);

                // Показываем кадры в окне предпросмотра
                SwingUtilities.invokeLater(() -> {
                    new PreviewFrame(frameFiles).setVisible(true);
                    statusLabel.setText("Предпросмотр готов (" + frameFiles.size() + " кадров)");
                    setButtonsEnabled(true);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка рендеринга: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Ошибка");
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }

    private void saveVideo() {
        List<KeyFrame> frames = getKeyFrames();
        if (frames.size() < 2) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте минимум 2 ключевых кадра.",
                    "Ошибка", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Диалог сохранения
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить видео");
        chooser.setSelectedFile(new File("fractal_tour.mp4"));
        chooser.addChoosableFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("MP4 Video (*.mp4)", "mp4"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File videoFile = chooser.getSelectedFile();
        if (!videoFile.getName().toLowerCase().endsWith(".mp4")) {
            videoFile = new File(videoFile.getAbsolutePath() + ".mp4");
        }

        setButtonsEnabled(false);
        statusLabel.setText("Рендеринг видео...");

        final File finalVideoFile = videoFile;
        new Thread(() -> {
            try {
                AnimationRenderer renderer = new AnimationRenderer(frames, conv);
                renderer.renderAllFrames(statusLabel);

                File result = renderer.assembleVideo(finalVideoFile);

                SwingUtilities.invokeLater(() -> {
                    if (result != null) {
                        statusLabel.setText("Видео сохранено: " + result.getName());
                        JOptionPane.showMessageDialog(this,
                                "Видео сохранено:\n" + result.getAbsolutePath(),
                                "Готово", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        statusLabel.setText("Кадры сохранены в папку (ffmpeg не найден)");
                        JOptionPane.showMessageDialog(this,
                                "FFmpeg не установлен.\nКадры сохранены в папку animation_frames.\n" +
                                        "Установите ffmpeg или используйте кадры для сборки видео вручную.",
                                "Инфо", JOptionPane.INFORMATION_MESSAGE);
                    }
                    setButtonsEnabled(true);
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка", JOptionPane.ERROR_MESSAGE);
                    statusLabel.setText("Ошибка");
                    setButtonsEnabled(true);
                });
            }
        }).start();
    }

    private void setButtonsEnabled(boolean enabled) {
        // Отключаем/включаем все кнопки, кроме закрытия окна
        for (java.awt.Component comp : getContentPane().getComponents()) {
            setEnabledRecursive(comp, enabled);
        }
    }

    private void setEnabledRecursive(java.awt.Component comp, boolean enabled) {
        if (comp instanceof JPanel || comp instanceof JScrollPane) {
            for (java.awt.Component child : ((java.awt.Container) comp).getComponents()) {
                setEnabledRecursive(child, enabled);
            }
        } else if (comp instanceof JButton || comp instanceof JTable || comp instanceof JTextField) {
            comp.setEnabled(enabled);
        }
    }
}