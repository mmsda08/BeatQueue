package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import model.Song;

/** Top 10 most voted songs, drawn as a bar chart. The list comes from the top-K min-heap. */
@SuppressWarnings({"serial", "this-escape"})
public class TopChartPanel extends JPanel implements Refreshable {
    private final BeatQueueController c;
    private List<Song> top = new ArrayList<>();

    private final JPanel chart = new JPanel() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawChart((Graphics2D) g);
        }
    };

    public TopChartPanel(BeatQueueController c) {
        this.c = c;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(8, 24, 16, 24));

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("Top 10 Requested Songs", Theme.HEADING, Theme.TEXT), BorderLayout.NORTH);
        titles.add(Theme.label("Most votes first. Found with a top-K heap, without sorting the whole library.",
                Theme.SMALL, Theme.MUTED), BorderLayout.SOUTH);
        add(titles, BorderLayout.NORTH);

        chart.setBackground(Theme.BG);
        add(chart, BorderLayout.CENTER);
    }

    private void drawChart(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int w = chart.getWidth();

        if (top.isEmpty()) {
            g.setFont(Theme.BODY);
            g.setColor(Theme.MUTED);
            g.drawString("No votes yet. Request and vote for songs to fill the chart.", 8, 36);
            return;
        }

        int maxVotes = top.get(0).getVotes();
        int rowH = 48;
        int rankX = 14, titleX = 56, titleW = 300;
        int barX = titleX + titleW + 16;
        int barMax = Math.max(60, w - barX - 90);

        for (int i = 0; i < top.size(); i++) {
            Song s = top.get(i);
            int y = 8 + i * rowH;

            g.setColor(i % 2 == 0 ? Theme.SURFACE : Theme.SURFACE_ALT);
            g.fillRoundRect(0, y, w, rowH - 6, 14, 14);

            g.setFont(Theme.HEADING);
            g.setColor(i < 3 ? Theme.ACCENT_HOVER : Theme.MUTED);
            g.drawString(String.valueOf(i + 1), rankX, y + 28);

            g.setFont(Theme.BODY_BOLD);
            g.setColor(Theme.TEXT);
            g.drawString(fit(s.getTitle(), g.getFontMetrics(), titleW), titleX, y + 20);
            g.setFont(Theme.SMALL);
            g.setColor(Theme.MUTED);
            g.drawString(fit(s.getArtist(), g.getFontMetrics(), titleW), titleX, y + 34);

            int bw = Math.max(8, (int) ((long) barMax * s.getVotes() / maxVotes));
            g.setColor(new Color(0x2C2C42));
            g.fillRoundRect(barX, y + 14, barMax, 14, 14, 14);
            g.setColor(i == 0 ? Theme.GOOD : Theme.ACCENT);
            g.fillRoundRect(barX, y + 14, bw, 14, 14, 14);

            g.setFont(Theme.BODY_BOLD);
            g.setColor(Theme.TEXT);
            g.drawString("\u2665 " + s.getVotes(), barX + barMax + 12, y + 26);
        }
    }

    private static String fit(String text, FontMetrics fm, int maxWidth) {
        if (fm.stringWidth(text) <= maxWidth) return text;
        String t = text;
        while (t.length() > 1 && fm.stringWidth(t + "...") > maxWidth) t = t.substring(0, t.length() - 1);
        return t + "...";
    }

    @Override
    public void refresh() {
        top = c.getTopSongs();
        chart.repaint();
    }
}
