package ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

/** Flat button with rounded corners and a hover color. */
@SuppressWarnings({"serial", "this-escape"})
public class RoundedButton extends JButton {
    private Color base;
    private Color hover;
    private boolean over;

    public RoundedButton(String text) {
        this(text, Theme.BUTTON, Theme.BUTTON_HOVER);
    }

    public RoundedButton(String text, Color base, Color hover) {
        super(text);
        this.base = base;
        this.hover = hover;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(false);
        setForeground(Theme.TEXT);
        setFont(Theme.BODY_BOLD);
        setBorder(new EmptyBorder(10, 18, 10, 18));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
            @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
        });
    }

    public void setColors(Color base, Color hover) {
        this.base = base;
        this.hover = hover;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color c = !isEnabled() ? Theme.SURFACE_ALT : (getModel().isPressed() ? base.darker() : (over ? hover : base));
        g2.setColor(c);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS);
        g2.dispose();
        super.paintComponent(g);
    }
}
