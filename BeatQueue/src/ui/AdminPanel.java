package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/** Admin screen: add songs to the library (duplicates are caught with binary search) and see recent actions. */
@SuppressWarnings({"serial", "this-escape"})
public class AdminPanel extends JPanel implements Refreshable {
    private final BeatQueueController c;

    private final DefaultListModel<Object> model = new DefaultListModel<>();
    private final JList<Object> list = new JList<>(model);

    public AdminPanel(BeatQueueController c, Consumer<String> status) {
        this.c = c;
        setLayout(new GridLayout(1, 2, 20, 0));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(8, 24, 16, 24));

        // left: add song form
        RoundedPanel form = new RoundedPanel(new BorderLayout(), Theme.SURFACE);
        form.setBorder(new EmptyBorder(20, 20, 20, 20));
        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        JTextField title = new JTextField();
        JTextField artist = new JTextField();
        Theme.styleField(title);
        Theme.styleField(artist);
        title.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        artist.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        RoundedButton add = new RoundedButton("Add Song", Theme.ACCENT, Theme.ACCENT_HOVER);

        inner.add(Theme.label("Add a song to the library", Theme.HEADING, Theme.TEXT));
        inner.add(Box.createVerticalStrut(4));
        inner.add(Theme.label("Duplicates (same title and artist) are rejected.", Theme.SMALL, Theme.MUTED));
        inner.add(Box.createVerticalStrut(18));
        inner.add(Theme.label("Title", Theme.BODY, Theme.MUTED));
        inner.add(Box.createVerticalStrut(4));
        inner.add(title);
        inner.add(Box.createVerticalStrut(12));
        inner.add(Theme.label("Artist", Theme.BODY, Theme.MUTED));
        inner.add(Box.createVerticalStrut(4));
        inner.add(artist);
        inner.add(Box.createVerticalStrut(18));
        inner.add(add);
        form.add(inner, BorderLayout.NORTH);
        add(form);

        // right: recent actions
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("Recent Actions", Theme.HEADING, Theme.TEXT), BorderLayout.NORTH);
        titles.add(Theme.label("Newest first. Press Undo (top right) to reverse the newest one.",
                Theme.SMALL, Theme.MUTED), BorderLayout.SOUTH);
        right.add(titles, BorderLayout.NORTH);
        list.setCellRenderer(Theme.renderer());
        list.setFixedCellHeight(38);
        right.add(Theme.scroll(list), BorderLayout.CENTER);
        add(right);

        add.addActionListener(e -> {
            String err = c.addSong(title.getText(), artist.getText());
            if (err != null) {
                status.accept(err);
            } else {
                status.accept("Added \"" + title.getText().trim() + "\" to the library.");
                title.setText("");
                artist.setText("");
            }
        });
    }

    @Override
    public void refresh() {
        List<String> actions = c.getRecentActions(30);
        model.clear();
        for (String a : actions) model.addElement(a);
    }
}
