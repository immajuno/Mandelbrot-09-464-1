public class Converter {

    // Текущая область комплексной плоскости
    private double reMin, reMax, imMin, imMax;

    // Размер экрана (пикселей)
    private int screenWidth, screenHeight;

    public CoordinateTransformer(double reMin, double reMax,
                                  double imMin, double imMax,
                                  int screenWidth, int screenHeight) {
        this.reMin = reMin;
        this.reMax = reMax;
        this.imMin = imMin;
        this.imMax = imMax;
        this.screenWidth  = screenWidth;
        this.screenHeight = screenHeight;
    }

    // ── Пиксель → комплексная плоскость ──────────────────────────────────────

    /** Преобразует пиксельную X-координату в вещественную часть комплексного числа. */
    public double toRealPart(int px) {
        return reMin + px * (reMax - reMin) / screenWidth;
    }

    /** Преобразует пиксельную Y-координату в мнимую часть комплексного числа. */
    public double toImagPart(int py) {
        // Ось Y экрана направлена вниз, поэтому вычитаем от imMax
        return imMax - py * (imMax - imMin) / screenHeight;
    }

    // ── Комплексная плоскость → пиксель ──────────────────────────────────────

    /** Преобразует вещественную часть в пиксельную X-координату. */
    public int toScreenX(double re) {
        return (int) Math.round((re - reMin) / (reMax - reMin) * screenWidth);
    }

    /** Преобразует мнимую часть в пиксельную Y-координату. */
    public int toScreenY(double im) {
        return (int) Math.round((imMax - im) / (imMax - imMin) * screenHeight);
    }

    // ── Обновление параметров ─────────────────────────────────────────────────

    /** Обновляет область комплексной плоскости (при zoom/pan). */
    public void setComplexBounds(double reMin, double reMax,
                                  double imMin, double imMax) {
        this.reMin = reMin;
        this.reMax = reMax;
        this.imMin = imMin;
        this.imMax = imMax;
    }

    /** Обновляет размеры экрана (при изменении размеров окна). */
    public void setScreenSize(int width, int height) {
        this.screenWidth  = width;
        this.screenHeight = height;
    }

    // ── Геттеры ───────────────────────────────────────────────────────────────

    public double getReMin()  { return reMin;  }
    public double getReMax()  { return reMax;  }
    public double getImMin()  { return imMin;  }
    public double getImMax()  { return imMax;  }
    public int getScreenWidth()  { return screenWidth;  }
    public int getScreenHeight() { return screenHeight; }

    @Override
    public String toString() {
        return String.format("Re[%.6f, %.6f]  Im[%.6f, %.6f]  Screen[%dx%d]",
                reMin, reMax, imMin, imMax, screenWidth, screenHeight);
    }
}
