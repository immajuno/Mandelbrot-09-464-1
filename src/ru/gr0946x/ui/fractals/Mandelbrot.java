package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

<<<<<<< HEAD
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal{

    private final int maxIterations = 100;
    private final double R2 = 4;
    public double getR(){
        return sqrt(R2);
    }

=======
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal {

    private int maxIterations = 100;
    private double R2 = 4;

    public double getR() {
        return sqrt(R2);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
    @Override
    public float inSetProbability(double x, double y) {
        var c = new Complex(x, y);
        var z = new Complex();
        int i = 0;
<<<<<<< HEAD
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations){
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float)i / maxIterations;
    }
}
=======
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations) {
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float) i / maxIterations;
    }
}
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
