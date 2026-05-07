package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.Painter;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class PaintPanel extends JPanel {

    protected Painter painter;

    public PaintPanel(Painter painter){
        this.painter = painter;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                painter.setWidth(getWidth());
                painter.setHeight(getHeight());
                repaint();
            }
        });
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        painter.paint(g);
    }
}

