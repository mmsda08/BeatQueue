package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/** A panel with a filled, rounded background (used for cards). */
@SuppressWarnings({"serial", "this-escape"})
public class RoundedPanel extends JPanel {
    private final Color fill;

    public RoundedPanel(LayoutManager layout, Color fill) {
        super(layout);
        this.fill = fill;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS + 6, Theme.RADIUS + 6);
        g2.dispose();
        super.paintComponent(g);
    }
}
