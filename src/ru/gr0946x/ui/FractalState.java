package ru.gr0946x.ui;
import java.io.Serializable;

public class FractalState implements Serializable {
    public double xMin, xMax, yMin, yMax;
    public int    maxIterations;

    public FractalState(double xMin, double xMax, double yMin, double yMax, int maxIterations) {
        this.xMin          = xMin;
        this.xMax          = xMax;
        this.yMin          = yMin;
        this.yMax          = yMax;
        this.maxIterations = maxIterations;
    }

    // Обратная совместимость: конструктор без итераций (для десериализации старых .frac)
    public FractalState(double xMin, double xMax, double yMin, double yMax) {
        this(xMin, xMax, yMin, yMax, 100);
    }
}
