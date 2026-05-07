package ru.gr0946x.ui.animation;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.fractals.ColorFunction;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Рендерит кадры анимации между ключевыми кадрами и собирает их в видеофайл.
 * Видео сохраняется в формате последовательности PNG + скрипт ffmpeg
 * (либо можно подключить библиотеку для прямого сохранения в видеоформат).
 */
public class AnimationRenderer {

    // Параметры выходного видео
    private static final int FRAME_WIDTH = 800;
    private static final int FRAME_HEIGHT = 600;
    private static final int FPS = 30;

    private final List<KeyFrame> keyFrames;
    private final Converter conv;
    private final Mandelbrot mandelbrot;
    private final ColorFunction colorFunction;
    private final File outputDir;

    public AnimationRenderer(List<KeyFrame> keyFrames, Converter conv) {
        this.keyFrames = keyFrames;
        this.conv = conv;
        this.mandelbrot = new Mandelbrot();
        this.colorFunction = (value) -> {
            if (value == 1.0) return Color.BLACK;
            float r = (float) abs(sin(5 * value));
            float g = (float) abs(cos(8 * value) * sin(3 * value));
            float b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        };
        this.outputDir = new File("animation_frames");
    }

    /**
     * Генерирует все кадры и сохраняет их как PNG в папку animation_frames.
     * Возвращает список сохранённых файлов.
     */
    public List<File> renderAllFrames(javax.swing.JLabel progressLabel) throws Exception {
        if (keyFrames.size() < 2) {
            throw new IllegalStateException("Нужно минимум 2 ключевых кадра");
        }

        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Вычисляем общее время анимации
        double totalTime = keyFrames.get(keyFrames.size() - 1).timeSeconds;
        int totalFrames = (int) (totalTime * FPS);

        List<File> frameFiles = new ArrayList<>();

        for (int frameIndex = 0; frameIndex <= totalFrames; frameIndex++) {
            double currentTime = (double) frameIndex / FPS;

            // Находим два ключевых кадра, между которыми находимся
            KeyFrame kf1 = keyFrames.get(0);
            KeyFrame kf2 = keyFrames.get(keyFrames.size() - 1);

            for (int i = 0; i < keyFrames.size() - 1; i++) {
                if (currentTime >= keyFrames.get(i).timeSeconds &&
                        currentTime <= keyFrames.get(i + 1).timeSeconds) {
                    kf1 = keyFrames.get(i);
                    kf2 = keyFrames.get(i + 1);
                    break;
                }
            }

            // Вычисляем t (0..1) между kf1 и kf2
            double segmentDuration = kf2.timeSeconds - kf1.timeSeconds;
            double t;
            if (segmentDuration <= 0) {
                t = 1.0;
            } else {
                t = (currentTime - kf1.timeSeconds) / segmentDuration;
                t = max(0, min(1, t));
            }

            // Плавная интерполяция (ease-in-out)
            double smoothT = smoothstep(t);

            // Интерполируем координаты
            double xMin = lerp(kf1.xMin, kf2.xMin, smoothT);
            double xMax = lerp(kf1.xMax, kf2.xMax, smoothT);
            double yMin = lerp(kf1.yMin, kf2.yMin, smoothT);
            double yMax = lerp(kf1.yMax, kf2.yMax, smoothT);

            // Рендерим кадр
            BufferedImage frame = renderFrame(xMin, xMax, yMin, yMax);

            // Сохраняем как PNG
            String fileName = String.format("frame_%05d.png", frameIndex);
            File outputFile = new File(outputDir, fileName);
            ImageIO.write(frame, "png", outputFile);
            frameFiles.add(outputFile);

            // Обновляем прогресс строго в EDT
            if (progressLabel != null && frameIndex % 5 == 0) {
                final int percent = (int) (100.0 * frameIndex / totalFrames);
                SwingUtilities.invokeLater(() -> {
                    progressLabel.setText("Рендеринг: " + percent + "%");
                });
            }
        }

        return frameFiles;
    }

    /**
     * Рендерит один кадр с заданными границами комплексной плоскости.
     */
    private BufferedImage renderFrame(double xMin, double xMax, double yMin, double yMax) {
        BufferedImage image = new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_RGB);

        // Вычисляем количество итераций в зависимости от приближения
        double currentWidth = xMax - xMin;
        int maxIterations = (int) (100 + 100 * Math.abs(Math.log10(currentWidth / 3.0)));
        maxIterations = Math.min(maxIterations, 2000);
        mandelbrot.setMaxIterations(maxIterations);

        // Временный конвертер для этого кадра
        Converter frameConv = new Converter(xMin, xMax, yMin, yMax);
        frameConv.setWidth(FRAME_WIDTH);
        frameConv.setHeight(FRAME_HEIGHT);

        for (int x = 0; x < FRAME_WIDTH; x++) {
            for (int y = 0; y < FRAME_HEIGHT; y++) {
                double crtX = frameConv.xScr2Crt(x);
                double crtY = frameConv.yScr2Crt(y);
                float value = mandelbrot.inSetProbability(crtX, crtY);
                Color color = colorFunction.getColor(value);
                image.setRGB(x, y, color.getRGB());
            }
        }

        return image;
    }

    /**
     * Собирает PNG-кадры в видеофайл через ffmpeg.
     * Если ffmpeg не установлен, сохраняет кадры в папку.
     */
    public File assembleVideo(File outputVideoFile) throws Exception {
        // Пробуем использовать ffmpeg
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-framerate", String.valueOf(FPS),
                    "-i", outputDir.getAbsolutePath() + File.separator + "frame_%05d.png",
                    "-c:v", "libx264",
                    "-pix_fmt", "yuv420p",
                    outputVideoFile.getAbsolutePath()
            );
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                return outputVideoFile;
            }
        } catch (Exception e) {
            // ffmpeg не установлен — возвращаем null
        }

        return null; // ffmpeg не найден, но кадры сохранены в папке
    }

    /**
     * Очищает временную папку с кадрами.
     */
    public void cleanUp() {
        if (outputDir.exists()) {
            File[] files = outputDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            outputDir.delete();
        }
    }

    // ── Вспомогательные функции интерполяции ───────────────────────────

    /** Линейная интерполяция */
    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /** Smoothstep для плавного старта и остановки */
    private double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }
}