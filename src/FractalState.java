import java.io.Serializable;

public class FractalState implements Serializable {
    public double xMin, xMax, yMin, yMax;

    public FractalState(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
}
