package ru.gr0946x.ui;

<<<<<<< HEAD
import ru.gr0946x.ui.painting.Painter;

=======
import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

<<<<<<< HEAD
public class SelectablePanel extends PaintPanel{
    private SelectedRect rect = null;
    private Graphics g;

    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();
    public void addSelectListener(SelectListener listener){
        selectHandlers.add(listener);
    }

    public void removeSelectListener(SelectListener listener){
        selectHandlers.remove(listener);
    }
=======
public class SelectablePanel extends PaintPanel {

    private SelectedRect rect = null;
    private Graphics g;

    private boolean panning       = false;
    private boolean panMoved      = false;   // был ли реальный сдвиг в этом жесте
    private Point   panStartScreen = null;

    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();
    private final ArrayList<PanListener>    panHandlers    = new ArrayList<>();

    public void addSelectListener(SelectListener listener) { selectHandlers.add(listener); }
    public void removeSelectListener(SelectListener listener) { selectHandlers.remove(listener); }

    public void addPanListener(PanListener listener) { panHandlers.add(listener); }
    public void removePanListener(PanListener listener) { panHandlers.remove(listener); }
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)

    public SelectablePanel(Painter painter) {
        super(painter);
        g = getGraphics();
<<<<<<< HEAD
=======

>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
<<<<<<< HEAD
                rect = new SelectedRect(e.getX(), e.getY());
                paintSelectedRect();
=======
                if (e.getButton() == MouseEvent.BUTTON1) {
                    rect = new SelectedRect(e.getX(), e.getY());
                    paintSelectedRect();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    panning        = true;
                    panMoved       = false;
                    panStartScreen = e.getPoint();
                }
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
<<<<<<< HEAD
                paintSelectedRect();
                for (var handler : selectHandlers) {
                    handler.onSelect(new Rectangle(
                            rect.getUpperLeft().x,
                            rect.getUpperLeft().y,
                            rect.getWidth(),
                            rect.getHeight()
                            )
                    );
                }
                rect = null;

=======
                if (e.getButton() == MouseEvent.BUTTON1 && rect != null) {
                    paintSelectedRect();
                    for (var handler : selectHandlers) {
                        handler.onSelect(new Rectangle(
                                rect.getUpperLeft().x,
                                rect.getUpperLeft().y,
                                rect.getWidth(),
                                rect.getHeight()
                        ));
                    }
                    rect = null;
                } else if (e.getButton() == MouseEvent.BUTTON3 && panning) {
                    panning = false;
                    panStartScreen = null;
                    // Уведомляем слушателей только если сдвиг действительно был
                    if (panMoved) {
                        panMoved = false;
                        for (var handler : panHandlers) {
                            handler.onPanFinished();
                        }
                    }
                }
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
<<<<<<< HEAD
                paintSelectedRect();
                if (rect != null){
                    rect.setLastPoint(e.getX(), e.getY());
                }
                paintSelectedRect();
=======
                if (rect != null) {
                    paintSelectedRect();
                    rect.setLastPoint(e.getX(), e.getY());
                    paintSelectedRect();
                } else if (panning && panStartScreen != null) {
                    int dx = e.getX() - panStartScreen.x;
                    int dy = e.getY() - panStartScreen.y;
                    if (dx == 0 && dy == 0) return;

                    Converter conv = ((FractalPainter) SelectablePanel.this.painter).getConverter();

                    double xRange = conv.getXMax() - conv.getXMin();
                    double yRange = conv.getYMax() - conv.getYMin();
                    int width  = getWidth();
                    int height = getHeight();

                    double deltaXWorld = -(dx * xRange / width);
                    double deltaYWorld =  (dy * yRange / height);

                    conv.setXShape(conv.getXMin() + deltaXWorld, conv.getXMax() + deltaXWorld);
                    conv.setYShape(conv.getYMin() + deltaYWorld, conv.getYMax() + deltaYWorld);

                    panMoved = true;
                    repaint();
                    panStartScreen = e.getPoint();
                }
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                g = getGraphics();
            }
        });
    }

<<<<<<< HEAD
    private void paintSelectedRect(){
        if (g != null){
=======
    private void paintSelectedRect() {
        if (g != null && rect != null) {
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
            g.setXORMode(Color.WHITE);
            g.setColor(Color.BLACK);
            g.drawRect(
                    rect.getUpperLeft().x,
                    rect.getUpperLeft().y,
                    rect.getWidth(),
                    rect.getHeight()
            );
            g.setPaintMode();
        }
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 3dbaa6c (Внесены итоговые правки, исправлены ошибки в коде, исправлены неработающие функции лабы)
