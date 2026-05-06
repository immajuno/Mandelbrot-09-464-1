package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Окно настройки ключевых кадров для анимированной экскурсии по фракталу.
 */
public class AnimationSettingsFrame extends JFrame {

    private final Converter conv;
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final JTextField totalTimeField;
    private final JButton addButton;
    private final JButton removeButton;
    private final JButton previewButton;
    private final JButton saveVideoButton;

    public AnimationSettingsFrame(Converter conv) {
        this.conv = conv;

        setTitle("Экскурсия по фракталу — настройка анимации");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // ── Таблица ключевых кадров ─────────────────────────────────────
        String[] columns = {"№", "X min", "X max", "Y min", "Y max", "Время (сек)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= 1; // только данные редактируемы, № — нет
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScroll = new JScrollPane(table);

        // ── Кнопки управления кадрами ───────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        addButton = new JButton("+ Добавить текущий вид");
        addButton.addActionListener(e -> addCurrentView());

        removeButton = new JButton("− Удалить выбранный");
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

        // ── Кнопки действий ────────────────────────────────────────────
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        previewButton = new JButton("Предпросмотр");
        previewButton.addActionListener(e -> startPreview());

        saveVideoButton = new JButton("Сохранить видео...");
        saveVideoButton.addActionListener(e -> saveVideo());

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

        // Добавляем стартовый кадр — текущий вид
        addCurrentView();
    }

    // ── Методы работы с таблицей ───────────────────────────────────────

    private void addCurrentView() {
        double totalTime = parseTotalTime();
        double lastTime = keyFrames.isEmpty() ? 0 : keyFrames.get(keyFrames.size() - 1).timeSeconds;
        double newTime = keyFrames.isEmpty() ? 0 : lastTime + totalTime / 5; // шаг по умолчанию

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

        // Меняем в списке
        KeyFrame kf = keyFrames.remove(row);
        keyFrames.add(newRow, kf);

        // Перестраиваем таблицу
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
        if (keyFrames.isEmpty()) return;

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

    // ── Синхронизация таблицы → список (перед использованием) ──────────

    /**
     * Считывает данные из таблицы обратно в список keyFrames.
     * Вызывать перед рендерингом.
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

    // ── Заглушки для кнопок (реализуем позже) ──────────────────────────

    private void startPreview() {
        JOptionPane.showMessageDialog(this,
                "Предпросмотр будет реализован после создания рендерера.",
                "Инфо", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveVideo() {
        JOptionPane.showMessageDialog(this,
                "Сохранение видео будет реализовано после создания рендерера.",
                "Инфо", JOptionPane.INFORMATION_MESSAGE);
    }
}