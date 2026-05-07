package ru.gr0946x;

public class Converter {
    private double xMin, xMax, yMin, yMax;
    private int width, height;

    public Converter(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.width = 800;
        this.height = 600;
        keepProportions();
    }

    public double xScr2Crt(int x) { return xMin + x * (xMax - xMin) / width; }
    public double yScr2Crt(int y) { return yMax - y * (yMax - yMin) / height; }

    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public void setXShape(double xMin, double xMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        keepProportions();
    }

    public void setYShape(double yMin, double yMax) {
        this.yMin = yMin;
        this.yMax = yMax;
        keepProportions();
    }

    public void setWidth(int width) {
        this.width = width;
        keepProportions();
    }

    public void setHeight(int height) {
        this.height = height;
        keepProportions();
    }

    private void keepProportions() {
        if (width <= 0 || height <= 0) return;

        double widthRe = xMax - xMin;
        double requiredHeight = widthRe * height / width;
        double centerIm = (yMin + yMax) / 2;

        yMin = centerIm - requiredHeight / 2;
        yMax = centerIm + requiredHeight / 2;
    }
}
