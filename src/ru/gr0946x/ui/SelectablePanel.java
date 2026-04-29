package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class SelectablePanel extends PaintPanel {

    private SelectedRect rect = null;
    private Graphics g;

    private boolean panning = false;
    private Point panStartScreen = null;

    private final ArrayList<SelectListener> selectHandlers = new ArrayList<>();

    public void addSelectListener(SelectListener listener){
        selectHandlers.add(listener);
    }

    public void removeSelectListener(SelectListener listener){
        selectHandlers.remove(listener);
    }

    public SelectablePanel(Painter painter) {
        super(painter);
        g = getGraphics();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == MouseEvent.BUTTON1) {
                    rect = new SelectedRect(e.getX(), e.getY());
                    paintSelectedRect();
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    panning = true;
                    panStartScreen = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if (rect != null) {
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
                } else if (panning) {
                    panning = false;
                    panStartScreen = null;
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                if (rect != null) {
                    paintSelectedRect();
                    rect.setLastPoint(e.getX(), e.getY());
                    paintSelectedRect();
                } else if (panning && panStartScreen != null) {
                    int dx = e.getX() - panStartScreen.x;
                    int dy = e.getY() - panStartScreen.y;
                    if (dx == 0 && dy == 0) return;

                    Converter conv = ((FractalPainter) painter).getConverter();

                    double xRange = conv.getXMax() - conv.getXMin();
                    double yRange = conv.getYMax() - conv.getYMin();
                    int width = getWidth();
                    int height = getHeight();

                    double deltaXWorld = -(dx * xRange / width);
                    double deltaYWorld =  (dy * yRange / height);

                    conv.setXShape(conv.getXMin() + deltaXWorld, conv.getXMax() + deltaXWorld);
                    conv.setYShape(conv.getYMin() + deltaYWorld, conv.getYMax() + deltaYWorld);

                    repaint();
                    panStartScreen = e.getPoint();
                }
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

    private void paintSelectedRect(){
        if (g != null && rect != null) {
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
}