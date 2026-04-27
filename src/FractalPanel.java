package fractal;

import converting.Converter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class FractalPanel extends JPanel {
    private BufferedImage image;
    public Converter converter; // 0: Использование внешнего JAR Converter

    private FractalFunction fractalFunction; // 11: Пеяредача функции через лямбду
    private ColorSchemeFunction colorSchemeFunction; // 11: Передача цветовой схемы через лямбду

    private int maxIterations = 500; // 12: Динамическое изменение числа итераций

    private Point dragStart, dragEnd;
    private Rectangle selectionRect;
    private Point panStart;

    private HistoryManager historyManager = new HistoryManager(); // 9: История для отмены

    private final int THREAD_COUNT = Runtime.getRuntime().availableProcessors(); // 4: Многопоточность

    public FractalPanel(Converter converter, FractalFunction fractalFunction, ColorSchemeFunction colorSchemeFunction) {
        this.converter = converter; // 0, 11
        this.fractalFunction = fractalFunction; // 11
        this.colorSchemeFunction = colorSchemeFunction; // 11

        setPreferredSize(new Dimension(converter.getWidth(), converter.getHeight()));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                converter.setSize(getWidth(), getHeight()); // 5: Соблюдение пропорций при изменении размера окна
                generateFractalAsync(); // 4
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragStart = e.getPoint(); // 2: Начало выделения прямоугольника
                    dragEnd = dragStart;
                    selectionRect = new Rectangle();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    panStart = e.getPoint(); // 3: Начало сдвига
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    dragEnd = e.getPoint(); // 2: Обновление выделения
                    updateSelectionRect(); // 2
                    repaint(); // 2
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    Point panEnd = e.getPoint(); // 3
                    int dx = panEnd.x - panStart.x; // 3
                    int dy = panEnd.y - panStart.y; // 3
                    panStart = panEnd; // 3

                    double dxCrt = converter.xScr2Crt(0) - converter.xScr2Crt(dx); // 3
                    double dyCrt = converter.yScr2Crt(0) - converter.yScr2Crt(dy); // 3

                    historyManager.saveState(new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax())); // 9

                    converter.setBounds(
                            converter.getXMin() + dxCrt,
                            converter.getXMax() + dxCrt,
                            converter.getYMin() + dyCrt,
                            converter.getYMax() + dyCrt); // 3

                    generateFractalAsync(); // 4
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && selectionRect != null && selectionRect.width > 5 && selectionRect.height > 5) {
                    zoomToSelection(); // 2: Масштабирование выделенного фрагмента
                    selectionRect = null;
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    openJuliaWindowAt(e.getX(), e.getY()); // 10: Открытие множества Жюлиа по выбранной точке
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);

        SwingUtilities.invokeLater(this::generateFractalAsync); // 4: Асинхронная генерация
    }

    public Converter getConverter() {
        return converter; // 0
    }

    private void updateSelectionRect() {
        int x = Math.min(dragStart.x, dragEnd.x); // 2
        int y = Math.min(dragStart.y, dragEnd.y); // 2
        int w = Math.abs(dragStart.x - dragEnd.x); // 2
        int h = Math.abs(dragStart.y - dragEnd.y); // 2
        selectionRect.setBounds(x, y, w, h); // 2
    }

    private void zoomToSelection() {
        double xMin = converter.xScr2Crt(selectionRect.x); // 2
        double xMax = converter.xScr2Crt(selectionRect.x + selectionRect.width); // 2
        double yMax = converter.yScr2Crt(selectionRect.y); // 2
        double yMin = converter.yScr2Crt(selectionRect.y + selectionRect.height); // 2

        historyManager.saveState(new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax())); // 9

        converter.setBounds(xMin, xMax, yMin, yMax); // 2
        generateFractalAsync(); // 4
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, null); // 1: Отрисовка фрактала в цвете
        } else {
            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        if (selectionRect != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 255, 100));
            g2.fill(selectionRect);
            g2.setColor(Color.BLUE);
            g2.draw(selectionRect);
            g2.dispose();
        }
    }

    public void generateFractalAsync() {
        updateMaxIterations(); // 12: Динамическое изменение числа итераций
        new SwingWorker<BufferedImage, Void>() {
            @Override
            protected BufferedImage doInBackground() throws Exception {
                return generateFractalImageMultiThreaded(); // 4: Многопоточная генерация
            }

            @Override
            protected void done() {
                try {
                    image = get();
                    repaint();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void updateMaxIterations() {
        double scale = converter.getXMax() - converter.getXMin(); // 12
        maxIterations = (int) Math.min(2000, 100 + 1000 / scale); // 12
    }

    private BufferedImage generateFractalImageMultiThreaded() throws InterruptedException, ExecutionException {
        int width = getWidth() > 0 ? getWidth() : converter.getWidth(); // 4
        int height = getHeight() > 0 ? getHeight() : converter.getHeight(); // 4

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); // 1

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT); // 4
        List<Future<?>> futures = new ArrayList<>(); // 4

        int rowsPerThread = height / THREAD_COUNT; // 4

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int startY = i * rowsPerThread;
            final int endY = (i == THREAD_COUNT - 1) ? height : startY + rowsPerThread;

            futures.add(executor.submit(() -> {
                for (int py = startY; py < endY; py++) {
                    for (int px = 0; px < width; px++) {
                        double x = converter.xScr2Crt(px); // 0, 5
                        double y = converter.yScr2Crt(py); // 0, 5
                        int iter = fractalFunction.compute(x, y, maxIterations); // 11
                        Color color = colorSchemeFunction.getColor(iter, maxIterations); // 11
                        img.setRGB(px, py, color.getRGB()); // 1
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            f.get(); // 4
        }

        executor.shutdown(); // 4

        return img; // 1
    }

    // 7: Сохранение фрактала с подписью координат
    public void saveFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить фрактал");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG Image", "png"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JPG Image", "jpg"));
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fractal Data (*.frac)", "frac"));

        int result = chooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String ext = getExtension(file.getName());
            String filterDesc = chooser.getFileFilter().getDescription();

            try {
                if (filterDesc.contains("PNG")) {
                    if (!ext.equalsIgnoreCase("png")) file = new File(file.getAbsolutePath() + ".png");
                    ImageIO.write(addSignature(image), "png", file); // 7: Сохраняем с подписью
                } else if (filterDesc.contains("JPG")) {
                    if (!ext.equalsIgnoreCase("jpg") && !ext.equalsIgnoreCase("jpeg")) file = new File(file.getAbsolutePath() + ".jpg");
                    ImageIO.write(addSignature(image), "jpg", file); // 7: Сохраняем с подписью
                } else if (filterDesc.contains("Fractal")) {
                    if (!ext.equalsIgnoreCase("frac")) file = new File(file.getAbsolutePath() + ".frac");
                    saveFractalData(file); // 7
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + e.getMessage());
            }
        }
    }

    // 7: Добавление подписи координат
    private BufferedImage addSignature(BufferedImage img) {
        BufferedImage signed = new BufferedImage(img.getWidth(), img.getHeight(), img.getType());
        Graphics2D g2 = signed.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String text = String.format("X:[%.5f; %.5f] Y:[%.5f; %.5f]",
                converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax());
        g2.drawString(text, 10, img.getHeight() - 10);
        g2.dispose();
        return signed;
    }

    private void saveFractalData(File file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            FractalState state = new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax()); // 7
            oos.writeObject(state); // 7
        }
    }

    // 8: Открытие сохраненного файла .frac
    public void loadFractal() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть фрактал");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fractal Data (*.frac)", "frac"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                FractalState state = (FractalState) ois.readObject(); // 8
                historyManager.saveState(new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax())); // 9
                converter.setBounds(state.xMin, state.xMax, state.yMin, state.yMax); // 8
                generateFractalAsync(); // 4
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
            }
        }
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return "";
    }

    // 9: Отмена действий пользователя
    public void undo() {
        FractalState prevState = historyManager.undo(); // 9
        if (prevState != null) {
            converter.setBounds(prevState.xMin, prevState.xMax, prevState.yMin, prevState.yMax); // 9
            generateFractalAsync(); // 4
        }
    }

    // 10: Открытие множества Жюлиа по центру
    public void openJuliaWindow() {
        double cRe = (converter.getXMin() + converter.getXMax()) / 2; // 10
        double cIm = (converter.getYMin() + converter.getYMax()) / 2; // 10
        JuliaFrame juliaFrame = new JuliaFrame(cRe, cIm); // 10
        juliaFrame.setVisible(true); // 10
    }

    // 10: Открытие множества Жюлиа по выбранной точке
    public void openJuliaWindowAt(int x, int y) {
        double cRe = converter.xScr2Crt(x); // 10
        double cIm = converter.yScr2Crt(y); // 10
        JuliaFrame juliaFrame = new JuliaFrame(cRe, cIm); // 10
        juliaFrame.setVisible(true); // 10
    }
}
