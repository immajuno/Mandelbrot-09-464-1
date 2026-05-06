package interfaces;

@FunctionalInterface
public interface FractalFunction {
    int calculate(double cReal, double cImag, int maxIter);
}