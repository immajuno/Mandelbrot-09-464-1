package ru.gr0946x.ui;

/**
 * Слушатель завершения сдвига (pan) изображения правой кнопкой мыши.
 * Вызывается один раз при отпускании правой кнопки, если был сдвиг.
 */
@FunctionalInterface
public interface PanListener {
    void onPanFinished();
}
