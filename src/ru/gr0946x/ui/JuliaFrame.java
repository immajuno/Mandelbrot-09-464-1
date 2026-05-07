package ru.gr0946x.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.*;

public class JuliaFrame extends JFrame {
    private final double realC;
    private final double imagC;
    private final JuliaPanel juliaPanel;

    private static final int WIDTH  = 600;
    private static final int HEIGHT = 600;

    private double zoom    = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;

    public JuliaFrame(double realC, double imagC) {
        this.realC = realC;
        this.imagC = imagC;

        setTitle("Julia Set: C = " + String.format("%.4f", realC)
                + " + " + String.format("%.4f", imagC) + "i");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        juliaPanel = new JuliaPanel();
        add(juliaPanel);

        // Масштабирование колёсиком мыши
        juliaPanel.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0) zoom *= 1.2;
            else zoom /= 1.2;
            juliaPanel.scheduleRender();
        });

        // Перемещение правой кнопкой мыши (п. 1)
        MouseAdapter ma = new MouseAdapter() {
            private Point lastPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) lastPoint = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && lastPoint != null) {
                    int dx = e.getX() - lastPoint.x;
                    int dy = e.getY() - lastPoint.y;
                    lastPoint = e.getPoint();
                    offsetX -= dx * zoom / juliaPanel.getWidth()  * 3.0;
                    offsetY -= dy * zoom / juliaPanel.getHeight() * 2.0;
                    juliaPanel.scheduleRender();
                }
            }
        };
        juliaPanel.addMouseListener(ma);
        juliaPanel.addMouseMotionListener(ma);

        setVisible(true);
    }

    // ── Внутренняя панель с асинхронным рендерингом ────────────────────────────

    private class JuliaPanel extends JPanel {

        /** Последнее готовое изображение, рисуется в paintComponent без вычислений. */
        private volatile BufferedImage renderedImage = null;
        /** Флаг: рендеринг уже запущен, чтобы не плодить лишние потоки. */
        private volatile boolean rendering = false;

        JuliaPanel() {
            setBackground(Color.BLACK);
            // Запускаем первый рендер после того как панель стала видимой
            addHierarchyListener(e -> {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                    scheduleRender();
                }
            });
            // Перерендеривать при изменении размера
            addComponentListener(new ComponentAdapter() {
                @Override public void componentResized(ComponentEvent e) { scheduleRender(); }
            });
        }

        /**
         * Запускает рендеринг в фоновом потоке.
         * Если рендеринг уже идёт — новый запрос будет проигнорирован;
         * следующий scheduleRender() вызывается автоматически после завершения,
         * если состояние успело измениться.
         */
        void scheduleRender() {
            if (rendering) return;
            rendering = true;

            // Снимаем параметры на EDT перед уходом в фон
            final double snapZoom    = zoom;
            final double snapOffsetX = offsetX;
            final double snapOffsetY = offsetY;
            final int    w           = Math.max(1, getWidth());
            final int    h           = Math.max(1, getHeight());

            new Thread(() -> {
                BufferedImage img = renderJulia(w, h, snapZoom, snapOffsetX, snapOffsetY);
                SwingUtilities.invokeLater(() -> {
                    renderedImage = img;
                    rendering = false;
                    repaint();
                    // Если параметры изменились пока рендерили — запустить ещё раз
                    if (snapZoom != zoom || snapOffsetX != offsetX || snapOffsetY != offsetY) {
                        scheduleRender();
                    }
                });
            }, "julia-render").start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (renderedImage != null) {
                // Просто рисуем уже готовое изображение — EDT не блокируется
                g.drawImage(renderedImage, 0, 0, getWidth(), getHeight(), null);
            } else {
                // Пока идёт первый рендер — показываем заглушку
                g.setColor(Color.DARK_GRAY);
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                g.drawString("Рендеринг...", getWidth() / 2 - 40, getHeight() / 2);
            }
        }
    }

    // ── Вычисление множества Жюлиа в нескольких потоках ───────────────────────

    private BufferedImage renderJulia(int w, int h,
                                      double snapZoom,
                                      double snapOffsetX,
                                      double snapOffsetY) {
        int maxIter = (int) (300 + 150 * Math.abs(Math.log10(Math.max(snapZoom, 1e-10))));
        maxIter = Math.max(100, Math.min(maxIter, 2000));

        int threads     = Math.max(1, Runtime.getRuntime().availableProcessors());
        int rowsPerTask = Math.max(1, h / threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        record Strip(int fromY, int[] pixels) {}
        java.util.List<Future<Strip>> futures = new java.util.ArrayList<>();

        final int finalMaxIter = maxIter;
        for (int yStart = 0; yStart < h; yStart += rowsPerTask) {
            final int fromY = yStart;
            final int toY   = Math.min(h, yStart + rowsPerTask);
            futures.add(pool.submit(() -> {
                int[] pixels = new int[(toY - fromY) * w];
                for (int y = fromY; y < toY; y++) {
                    for (int x = 0; x < w; x++) {
                        double zr = (x - w / 2.0) * snapZoom / w  * 3.0 + snapOffsetX;
                        double zi = (y - h / 2.0) * snapZoom / h  * 2.0 + snapOffsetY;
                        int iter = 0;
                        while (zr * zr + zi * zi < 4.0 && iter < finalMaxIter) {
                            double tmp = zr * zr - zi * zi + realC;
                            zi = 2.0 * zr * zi + imagC;
                            zr = tmp;
                            iter++;
                        }
                        pixels[(y - fromY) * w + x] = juliaColor(iter, finalMaxIter);
                    }
                }
                return new Strip(fromY, pixels);
            }));
        }

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        try {
            for (var f : futures) {
                Strip s = f.get();
                image.setRGB(0, s.fromY(), w, s.pixels().length / w, s.pixels(), 0, w);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            // Возвращаем частично готовое изображение
        } finally {
            pool.shutdownNow();
        }
        return image;
    }

    private int juliaColor(int iter, int maxIter) {
        if (iter == maxIter) return Color.BLACK.getRGB();
        double t = (double) iter / maxIter;
        int r = (int) (Math.sin(t * Math.PI * 3.0) * 127 + 128);
        int gv = (int) (Math.sin(t * Math.PI * 5.0) * 127 + 128);
        int b  = (int) (Math.sin(t * Math.PI * 7.0) * 127 + 128);
        return new Color(r, gv, b).getRGB();
    }
}
