package ru.gr0946x.ui.animation;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Окно предпросмотра анимации — показывает кадры с кнопками навигации.
 */
public class PreviewFrame extends JFrame {

    private final List<File> frameFiles;
    private int currentFrame = 0;
    private final JLabel imageLabel;
    private final JLabel frameLabel;
    private javax.swing.Timer playTimer;
    private boolean playing = false;

    public PreviewFrame(List<File> frameFiles) {
        this.frameFiles = frameFiles;

        setTitle("Предпросмотр анимации");
        setSize(820, 680);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        JScrollPane scrollPane = new JScrollPane(imageLabel);

        // Панель управления
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton prevButton = new JButton("◀ Назад");
        JButton playButton = new JButton("▶ Воспроизвести");
        JButton nextButton = new JButton("Вперёд ▶");
        frameLabel = new JLabel("Кадр: 0 / " + frameFiles.size());

        prevButton.addActionListener(e -> showFrame(currentFrame - 1));
        nextButton.addActionListener(e -> showFrame(currentFrame + 1));
        playButton.addActionListener(e -> togglePlay(playButton));

        controlPanel.add(prevButton);
        controlPanel.add(playButton);
        controlPanel.add(nextButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(frameLabel);

        add(scrollPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        showFrame(0);
    }

    private void showFrame(int index) {
        if (index < 0 || index >= frameFiles.size()) return;
        currentFrame = index;

        try {
            BufferedImage img = ImageIO.read(frameFiles.get(index));
            imageLabel.setIcon(new ImageIcon(img));
            imageLabel.setText("");
        } catch (Exception e) {
            imageLabel.setIcon(null);
            imageLabel.setText("Ошибка загрузки кадра");
        }

        frameLabel.setText("Кадр: " + (currentFrame + 1) + " / " + frameFiles.size());
    }

    private void togglePlay(JButton playButton) {
        if (playing) {
            playTimer.stop();
            playing = false;
            playButton.setText("▶ Воспроизвести");
        } else {
            playTimer = new javax.swing.Timer(1000 / 30, e -> {
                if (currentFrame < frameFiles.size() - 1) {
                    showFrame(currentFrame + 1);
                } else {
                    showFrame(0);
                    playTimer.stop();
                    playing = false;
                    playButton.setText("▶ Воспроизвести");
                }
            });
            playTimer.start();
            playing = true;
            playButton.setText("⏸ Пауза");
        }
    }
}