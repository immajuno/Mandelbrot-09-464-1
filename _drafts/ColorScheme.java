package interfaces;

@FunctionalInterface
public interface ColorScheme {
    int[] getColor(int iterations, int maxIter);
}