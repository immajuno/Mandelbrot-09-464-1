import converting.Converter;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.concurrent.*;

public class saveFractal extends JPanel {
    private BufferedImage image;
    public Converter converter;

    public Converter getConverter() {
        return converter;
    }

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
                    ImageIO.write(addSignature(image), "png", file);
                } else if (filterDesc.contains("JPG")) {
                    if (!ext.equalsIgnoreCase("jpg") && !ext.equalsIgnoreCase("jpeg")) file = new File(file.getAbsolutePath() + ".jpg");
                    ImageIO.write(addSignature(image), "jpg", file);
                } else if (filterDesc.contains("Fractal")) {
                    if (!ext.equalsIgnoreCase("frac")) file = new File(file.getAbsolutePath() + ".frac");
                    saveFractalData(file);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Ошибка сохранения: " + e.getMessage());
            }
        }
    }

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
            FractalState state = new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax());
            oos.writeObject(state);
        }
    }

    private String getExtension(String filename) {
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return "";
    }
}
