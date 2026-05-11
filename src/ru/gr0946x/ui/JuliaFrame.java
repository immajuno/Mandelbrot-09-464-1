package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

// 1. Меняем JFrame на JDialog
public class JuliaFrame extends JDialog {
    private final double[] cValues = new double[2];
    private final JuliaPanel juliaPanel;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 300;

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    // 2. Добавляем Frame owner в аргументы для управления модальностью
    public static void openJuliaWindow(Frame owner, double realC, double imagC) {
        SwingUtilities.invokeLater(() -> {
            JuliaFrame dialog = new JuliaFrame(owner, realC, imagC);
            dialog.setVisible(true);
        });
    }

    public JuliaFrame(Frame owner, double realC, double imagC) {
        // 3. Вызываем конструктор суперкласса: owner, заголовок и modal = true
        super(owner, "Julia Set", true);

        cValues[0] = realC;
        cValues[1] = imagC;

        updateTitle();
        setSize(WIDTH, HEIGHT);
        // Для JDialog используем DISPOSE_ON_CLOSE
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);

        juliaPanel = new JuliaPanel();
        add(juliaPanel);

        // Обработчики мыши (остаются без изменений)
        juliaPanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) zoom *= 1.2;
            else zoom /= 1.2;
            juliaPanel.repaint();
        });

        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = e.getPoint();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    double newRealC = (e.getX() - getWidth() / 2.0) * zoom / getWidth() * 3.0 + offsetX;
                    double newImagC = (e.getY() - getHeight() / 2.0) * zoom / getHeight() * 2.0 + offsetY;
                    cValues[0] = newRealC;
                    cValues[1] = newImagC;
                    zoom = 1.0;
                    offsetX = 0;
                    offsetY = 0;
                    updateTitle();
                    juliaPanel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    offsetX += dx * zoom / WIDTH * 3.0;
                    offsetY += dy * zoom / HEIGHT * 2.0;
                    juliaPanel.repaint();
                }
            }
        };

        juliaPanel.addMouseListener(ma);
        juliaPanel.addMouseMotionListener(ma);
        juliaPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        pack();
    }

    private void updateTitle() {
        setTitle("Julia Set: C = " + String.format("%.4f", cValues[0]) + " + " + String.format("%.4f", cValues[1]) + "i");
    }

    // Внутренний класс JuliaPanel и метод getColor остаются без изменений...
    private class JuliaPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    double zr = (x - getWidth() / 2.0) * zoom / getWidth() * 3.0 + offsetX;
                    double zi = (y - getHeight() / 2.0) * zoom / getHeight() * 2.0 + offsetY;
                    int iter = 0;
                    while (zr * zr + zi * zi < 4.0 && iter < MAX_ITER) {
                        double temp = zr * zr - zi * zi + cValues[0];
                        zi = 2.0 * zr * zi + cValues[1];
                        zr = temp;
                        iter++;
                    }
                    image.setRGB(x, y, getColor(iter, MAX_ITER));
                }
            }
            g.drawImage(image, 0, 0, null);
            g.setColor(Color.WHITE);
            g.drawString(String.format("C = %.4f + %.4fi", cValues[0], cValues[1]), 10, 20);
        }
    }

    private int getColor(int iter, int maxIter) {
        if (iter == maxIter) return Color.BLACK.getRGB();
        double t = (double) iter / maxIter;
        int r = (int) (Math.sin(t * Math.PI * 3.0) * 127 + 128);
        int g = (int) (Math.sin(t * Math.PI * 5.0) * 127 + 128);
        int b = (int) (Math.sin(t * Math.PI * 7.0) * 127 + 128);
        return new Color(r, g, b).getRGB();
    }
}