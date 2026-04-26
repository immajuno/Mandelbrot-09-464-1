package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JuliaFrame extends JFrame {
    private final double realC;
    private final double imagC;
    private JuliaPanel juliaPanel;

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 300;

    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    public JuliaFrame(double realC, double imagC) {
        this.realC = realC;
        this.imagC = imagC;

        setTitle("Julia Set: C = " + String.format("%.4f", realC) + " + " + String.format("%.4f", imagC) + "i");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        juliaPanel = new JuliaPanel();
        add(juliaPanel);

        // Масштабирование колёсиком мыши
        juliaPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) zoom *= 1.2;
                else zoom /= 1.2;
                juliaPanel.repaint();
            }
        });

        // Перемещение правой кнопкой
        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint;
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = e.getPoint();
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
                        double temp = zr * zr - zi * zi + realC;
                        zi = 2.0 * zr * zi + imagC;
                        zr = temp;
                        iter++;
                    }

                    int color = getColor(iter, MAX_ITER);
                    image.setRGB(x, y, color);
                }
            }
            g.drawImage(image, 0, 0, null);
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