// 8: Открытие сохраненного файла .frac
public void loadFractal() {
    // 1. Создаем стандартный диалог открытия файлов
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Открыть фрактал");
    chooser.setAcceptAllFileFilterUsed(false); // Отключаем фильтр "Все файлы"

    // 2. Устанавливаем фильтр ТОЛЬКО для .frac файлов (как в задании)
    chooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fractal Data (*.frac)", "frac"));

    // 3. Показываем диалог пользователю
    int result = chooser.showOpenDialog(this);

    if (result == JFileChooser.APPROVE_OPTION) { // Если пользователь нажал "Открыть"
        File file = chooser.getSelectedFile();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            // 4. Восстанавливаем состояние фрактала из файла
            FractalState state = (FractalState) ois.readObject(); // 8

            // Сохраняем текущее состояние в историю (для возможности отмены)
            historyManager.saveState(new FractalState(converter.getXMin(), converter.getXMax(), converter.getYMin(), converter.getYMax())); // 9

            // 5. Применяем сохраненные границы к конвертеру
            converter.setBounds(state.xMin, state.xMax, state.yMin, state.yMax); // 8

            // 6. Перерисовываем фрактал с новыми границами
            generateFractalAsync(); // 4

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + e.getMessage());
        }
    }
}