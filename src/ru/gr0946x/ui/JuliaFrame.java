package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JuliaFrame extends JFrame {
    private final double[] cValues = new double[2]; // [0] = realC, [1] = imagC
    private final JuliaPanel juliaPanel;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 300;

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    // -----------------------------------------------------------
    // Статический метод для вызова из основного окна Мандельброта.
    // Другие участники проекта кликают по точке и вызывают этот метод,
    // передавая координаты как параметр C для Жюлиа.
    // -----------------------------------------------------------
    public static void openJuliaWindow(double realC, double imagC) {
        SwingUtilities.invokeLater(() -> new JuliaFrame(realC, imagC));
    }

    public JuliaFrame(double realC, double imagC) {
        cValues[0] = realC;
        cValues[1] = imagC;

        updateTitle();
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        juliaPanel = new JuliaPanel();
        add(juliaPanel);

        // Масштабирование колёсиком мыши
        juliaPanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) zoom *= 1.2;
            else zoom /= 1.2;
            juliaPanel.repaint();
        });

        // Перемещение правой кнопкой и выбор точки C левой кнопкой
        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = e.getPoint();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Получаем координаты клика и преобразуем их в комплексное число
                    double newRealC = (e.getX() - getWidth() / 2.0) * zoom / getWidth() * 3.0 + offsetX;
                    double newImagC = (e.getY() - getHeight() / 2.0) * zoom / getHeight() * 2.0 + offsetY;

                    // Обновляем параметры C через массив
                    cValues[0] = newRealC;
                    cValues[1] = newImagC;

                    // Сбрасываем зум и смещение
                    zoom = 1.0;
                    offsetX = 0;
                    offsetY = 0;

                    // Обновляем заголовок окна
                    updateTitle();

                    // Перерисовываем
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

        setVisible(true);
    }

    private void updateTitle() {
        setTitle("Julia Set: C = " + String.format("%.4f", cValues[0]) + " + " + String.format("%.4f", cValues[1]) + "i");
    }

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

                    int color = getColor(iter, MAX_ITER);
                    image.setRGB(x, y, color);
                }
            }
            g.drawImage(image, 0, 0, null);

            // Отображаем текущие координаты C на панели
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            String cText = String.format("C = %.4f + %.4fi (Click to select)", cValues[0], cValues[1]);
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(cText);
            g.drawString(cText, 10, 20);
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