package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class JuliaFrame extends JFrame {
    private final double[] cValues = new double[2]; // [0] = realC, [1] = imagC
    private final JuliaPanel juliaPanel;
    private final JFrame parentFrame; // Ссылка на родительское окно

    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int MAX_ITER = 300; //Максимальное количество итераций для проверки ухода точки в бесконечность

    //Переменные для управления видом: масштаб и смещение по осям
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    public static void openJuliaWindow(double realC, double imagC, JFrame parentFrame) {
        SwingUtilities.invokeLater(() -> new JuliaFrame(realC, imagC, parentFrame));
    }

    //Конструктор класса, принимает действительную и мнимую части числа C и родительское окно
    public JuliaFrame(double realC, double imagC, JFrame parentFrame) {
        cValues[0] = realC;
        cValues[1] = imagC;
        this.parentFrame = parentFrame;

        updateTitle();
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parentFrame);

        // Блокируем родительское окно
        if (parentFrame != null) {
            parentFrame.setEnabled(false);
        }

        // Добавляем обработчик закрытия окна для разблокировки родительского окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                unlockParentFrame();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                unlockParentFrame();
            }
        });

        //Создаем экземпляр внутреннего класса для рисования
        juliaPanel = new JuliaPanel();
        add(juliaPanel);

        // Кнопка для возврата к основному окну
        JPanel buttonPanel = new JPanel();
        JButton backButton = new JButton("Вернуться к множеству Мандельброта");
        backButton.addActionListener(e -> {
            unlockParentFrame();
            dispose(); // Закрываем текущее окно
        });
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        //Масштабирование колёсиком мыши
        juliaPanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) zoom *= 1.2;
            else zoom /= 1.2;
            juliaPanel.repaint();
        });

        //Перемещение правой кнопкой и выбор точки C левой кнопкой
        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint; //храним предыдущую точку при перетаскивании

            //Метод вызова кнопкой мыши
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = e.getPoint(); //точка начала перетаскивания
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    // Получаем координаты клика и преобразуем их в комплексное число
                    double newRealC = (e.getX() - getWidth() / 2.0) * zoom / getWidth() * 3.0 + offsetX;
                    double newImagC = (e.getY() - getHeight() / 2.0) * zoom / getHeight() * 2.0 + offsetY;

                    //Обновляем параметры C через массив
                    cValues[0] = newRealC;
                    cValues[1] = newImagC;

                    //Сбрасываем зум и смещение
                    zoom = 1.0;
                    offsetX = 0;
                    offsetY = 0;

                    //Обновляем заголовок окна
                    updateTitle();

                    //Перерисовываем
                    juliaPanel.repaint();
                }
            }

            //Метод перетаскивания с зажатой кнопкой
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

        //Регистрируем обработчики для событий нажатия и перемещения
        juliaPanel.addMouseListener(ma);
        juliaPanel.addMouseMotionListener(ma);

        // Устанавливаем расположение компонентов
        juliaPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        pack();
        setVisible(true);
    }

    // Метод для разблокировки родительского окна
    private void unlockParentFrame() {
        if (parentFrame != null) {
            parentFrame.setEnabled(true);
            parentFrame.toFront(); // Возвращаем фокус на родительское окно
        }
    }

    //Обновление текста заголовка окна
    private void updateTitle() {
        setTitle("Julia Set: C = " + String.format("%.4f", cValues[0]) + " + " + String.format("%.4f", cValues[1]) + "i");
    }

    //Внутренний класс, представляющий панель для рисования фрактала
    private class JuliaPanel extends JPanel {
        //метод для отрисовки компонента
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);

            //Циклы для прохода по каждому пикселю изображения
            for (int x = 0; x < getWidth(); x++) {
                for (int y = 0; y < getHeight(); y++) {
                    double zr = (x - getWidth() / 2.0) * zoom / getWidth() * 3.0 + offsetX;
                    double zi = (y - getHeight() / 2.0) * zoom / getHeight() * 2.0 + offsetY;

                    int iter = 0;
                    while (zr * zr + zi * zi < 4.0 && iter < MAX_ITER) {
                        //zi следующего шага зависит от старого zr
                        double temp = zr * zr - zi * zi + cValues[0];
                        zi = 2.0 * zr * zi + cValues[1]; //новая мнимая часть
                        zr = temp; //новая действительная часть
                        iter++;
                    }

                    int color = getColor(iter, MAX_ITER); //цвет на основе количества итераций
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

            // Подсказка по управлению
            g.setFont(new Font("Arial", Font.PLAIN, 11));
            g.drawString("Left click: new C point | Right drag: move | Wheel: zoom", 10, 40);
        }
    }

    //метод для вычисления цвета на основе количества итераций
    private int getColor(int iter, int maxIter) {
        if (iter == maxIter) return Color.BLACK.getRGB();

        double t = (double) iter / maxIter;
        int r = (int) (Math.sin(t * Math.PI * 3.0) * 127 + 128);
        int g = (int) (Math.sin(t * Math.PI * 5.0) * 127 + 128);
        int b = (int) (Math.sin(t * Math.PI * 7.0) * 127 + 128);

        return new Color(r, g, b).getRGB();
    }
}