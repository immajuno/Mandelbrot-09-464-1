package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;
<<<<<<< HEAD

import java.awt.*;

public class FractalPainter implements Painter{
=======
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FractalPainter implements Painter {
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)

    private final Fractal fractal;
    private final Converter conv;
    private final ColorFunction colorFunction;
<<<<<<< HEAD
=======

    public Converter getConverter() {
        return conv;
    }

>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
    }

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf){
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
    }

<<<<<<< HEAD
    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                var x = conv.xScr2Crt(i);
                var y = conv.yScr2Crt(j);
                var res = fractal.inSetProbability(x, y);
                g.setColor(colorFunction.getColor(res));
                g.drawLine(i, j, i + 1, j);
            }
        }
    }
}
=======
    /**
     * Рендерит текущее состояние фрактала в BufferedImage заданного размера.
     * Используется при сохранении в файл (п. 5).
     */
    public BufferedImage renderToImage(int w, int h) {
        if (w <= 0 || h <= 0) return null;

        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        int rowsPerTask = Math.max(1, h / threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            List<Callable<RenderedStrip>> tasks = new ArrayList<>();
            for (int yStart = 0; yStart < h; yStart += rowsPerTask) {
                int fromY = yStart;
                int toY   = Math.min(h, yStart + rowsPerTask);
                tasks.add(() -> {
                    int stripHeight = toY - fromY;
                    int[] pixels = new int[w * stripHeight];
                    for (int y = fromY; y < toY; y++) {
                        for (int x = 0; x < w; x++) {
                            double crtX = conv.xScr2Crt(x);
                            double crtY = conv.yScr2Crt(y);
                            float result = fractal.inSetProbability(crtX, crtY);
                            pixels[(y - fromY) * w + x] = colorFunction.getColor(result).getRGB();
                        }
                    }
                    return new RenderedStrip(fromY, stripHeight, pixels);
                });
            }
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            List<Future<RenderedStrip>> results = executor.invokeAll(tasks);
            for (Future<RenderedStrip> result : results) {
                RenderedStrip strip = result.get();
                image.setRGB(0, strip.y, w, strip.height, strip.pixels, 0, w);
            }
            return image;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            throw new RuntimeException("Fractal rendering failed", e.getCause());
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        int threadCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        int rowsPerTask = Math.max(1, h / threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        try {
            List<Callable<RenderedStrip>> tasks = new ArrayList<>();
            for (int yStart = 0; yStart < h; yStart += rowsPerTask) {
                int fromY = yStart;
                int toY = Math.min(h, yStart + rowsPerTask);

                tasks.add(() -> {
                    int stripHeight = toY - fromY;
                    int[] pixels = new int[w * stripHeight];
                    for (int y = fromY; y < toY; y++) {
                        for (int x = 0; x < w; x++) {
                            double crtX = conv.xScr2Crt(x);
                            double crtY = conv.yScr2Crt(y);
                            float result = fractal.inSetProbability(crtX, crtY);
                            pixels[(y - fromY) * w + x] = colorFunction.getColor(result).getRGB();
                        }
                    }
                    return new RenderedStrip(fromY, stripHeight, pixels);
                });
            }

            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            List<Future<RenderedStrip>> results = executor.invokeAll(tasks);
            for (Future<RenderedStrip> result : results) {
                RenderedStrip strip = result.get();
                image.setRGB(0, strip.y, w, strip.height, strip.pixels, 0, w);
            }
            g.drawImage(image, 0, 0, null);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new RuntimeException("Fractal rendering failed", e);
        } finally {
            executor.shutdown();
        }
    }

    private static class RenderedStrip {
        private final int y;
        private final int height;
        private final int[] pixels;

        private RenderedStrip(int y, int height, int[] pixels) {
            this.y = y;
            this.height = height;
            this.pixels = pixels;
        }
    }
}
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
