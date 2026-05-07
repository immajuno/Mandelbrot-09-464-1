package ru.gr0946x.ui.animation;

import java.io.Serializable;

/**
 * Модель ключевого кадра для анимации.
 * Хранит область комплексной плоскости и время в секундах от начала анимации.
 */
public class KeyFrame implements Serializable {

    public double xMin, xMax, yMin, yMax;
    public double timeSeconds; // время от начала анимации, когда должен показаться этот кадр

    public KeyFrame(double xMin, double xMax, double yMin, double yMax, double timeSeconds) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        this.timeSeconds = timeSeconds;
    }

    @Override
    public String toString() {
        return String.format("[%.4f с] X[%.4f, %.4f] Y[%.4f, %.4f]",
                timeSeconds, xMin, xMax, yMin, yMax);
    }
}