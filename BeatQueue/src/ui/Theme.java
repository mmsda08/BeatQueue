package ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import model.Request;
import model.Song;

/** All colors, fonts and small style helpers in one place. Change the look here. */
public final class Theme {
    private Theme() {}

    public static final Color BG = new Color(0x0F0F17);
    public static final Color SURFACE = new Color(0x1A1A26);
    public static final Color SURFACE_ALT = new Color(0x212132);
    public static final Color ACCENT = new Color(0x8B5CF6);
    public static final Color ACCENT_HOVER = new Color(0xA078FA);
    public static final Color ACCENT_DARK = new Color(0x4C2F96);
    public static final Color BUTTON = new Color(0x2C2C42);
    public static final Color BUTTON_HOVER = new Color(0x3A3A58);
    public static final Color TEXT = new Color(0xF2F2F7);
    public static final Color MUTED = new Color(0x9A9AB0);
    public static final Color GOOD = new Color(0x34D399);

    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADING = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BODY_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);

    public static final int RADIUS = 16;

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static void styleField(JTextField f) {
        f.setFont(BODY);
        f.setBackground(SURFACE_ALT);
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BUTTON_HOVER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
    }

    public static void styleCombo(JComboBox<?> c) {
        c.setFont(BODY);
        c.setBackground(SURFACE_ALT);
        c.setForeground(TEXT);
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static JScrollPane scroll(JList<?> list) {
        list.setBackground(SURFACE);
        list.setForeground(TEXT);
        list.setSelectionBackground(ACCENT_DARK);
        list.setSelectionForeground(TEXT);
        JScrollPane sp = new JScrollPane(list);
        sp.setBorder(BorderFactory.createLineBorder(SURFACE_ALT, 1, true));
        sp.getViewport().setBackground(SURFACE);
        return sp;
    }

    /** Text shown for the objects we put into lists. */
    public static String format(Object v) {
        if (v instanceof Song) {
            Song s = (Song) v;
            return s.getTitle() + "   \u00B7   " + s.getArtist() + "      \u2665 " + s.getVotes();
        }
        if (v instanceof Request) {
            Request r = (Request) v;
            return "#" + r.getRequestId() + "   " + r.getSong().getTitle() + "   \u00B7   "
                    + r.getSong().getArtist() + "      " + r.getVotes() + (r.getVotes() == 1 ? " vote" : " votes");
        }
        return String.valueOf(v);
    }

    public static ListCellRenderer<Object> renderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, format(value), index, isSelected, false);
                l.setBorder(new EmptyBorder(9, 14, 9, 14));
                l.setFont(BODY);
                l.setForeground(TEXT);
                l.setOpaque(true);
                l.setBackground(isSelected ? ACCENT_DARK : (index % 2 == 0 ? SURFACE : SURFACE_ALT));
                return l;
            }
        };
    }
}
