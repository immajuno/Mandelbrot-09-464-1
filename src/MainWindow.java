package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;

import static java.lang.Math.*;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final FractalPainter painter;
    private final Fractal mandelbrot;
    private final Converter conv;
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(conv);

        painter.setFractalFunction((cReal, cImag, maxIter) -> {
            double zReal = 0;
            double zImag = 0;
            int iter = 0;
            while (iter < maxIter) {
                double nextZReal = zReal * zReal - zImag * zImag + cReal;
                double nextZImag = 2 * zReal * zImag + cImag;
                zReal = nextZReal;
                zImag = nextZImag;
                if (zReal * zReal + zImag * zImag > 4.0) break;
                iter++;
            }
            return iter;
        });

        painter.setColorScheme((iterations, maxIter) -> {
            if (iterations == maxIter) {
                return new int[]{0, 0, 0}; // Gara
            } else {
                float value = (float) iterations / maxIter;
                int r = (int)(Math.abs(Math.sin(5 * value)) * 255);
                int g = (int)(Math.abs(Math.cos(8 * value) * Math.sin(3 * value)) * 255);
                int b = (int)(Math.abs((Math.sin(7 * value) + Math.cos(15 * value)) / 2f) * 255);
                return new int[]{r, g, b};
            }
        });
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);

        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
                    double real = conv.xScr2Crt(e.getX());
                    double imag = conv.yScr2Crt(e.getY());
                    new JuliaFrame(real, imag);
                }
            }
        });

        mainPanel.addSelectListener((r)->{
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
        //    conv.setXShape(xMin, xMax);
        //    conv.setYShape(yMin, yMax);
            mainPanel.repaint();
        });
        setContent();
    }

    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }
}
