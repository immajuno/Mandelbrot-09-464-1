package ru.gr0946x.ui.painting;

import interfaces.ColorScheme;
import interfaces.FractalFunction;
import ru.gr0946x.Converter;

import java.awt.*;

public class FractalPainter implements Painter {

    private final Converter conv;

    private FractalFunction fractalFunction;
    private ColorScheme colorScheme;

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

    public FractalPainter(Converter conv) {
        this.conv = conv;

        this.fractalFunction = (cReal, cImag, maxIter) -> {
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
        };

        this.colorScheme = (iterations, maxIter) -> {
            if (iterations == maxIter) {
                return new int[]{0, 0, 0}; // Gara
            } else {
                int blue = (iterations * 255) / maxIter;
                return new int[]{0, 0, blue}; // Gök
            }
        };
    }

    public void setFractalFunction(FractalFunction function) {
        this.fractalFunction = function;
    }

    public void setColorScheme(ColorScheme scheme) {
        this.colorScheme = scheme;
    }

    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();

        int maxIterations = 100 ;

        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {

                var x = conv.xScr2Crt(i);
                var y = conv.yScr2Crt(j);

                int iterations = fractalFunction.calculate(x, y, maxIterations);


                int[] rgb = colorScheme.getColor(iterations, maxIterations);


                g.setColor(new Color(rgb[0], rgb[1], rgb[2]));
                g.drawLine(i, j, i + 1, j);
            }
        }
    }
}