package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import model.Song;

/** Guest screen: search (trie autocomplete), sort (merge sort), exact lookup (binary search), request. */
@SuppressWarnings({"serial", "this-escape"})
public class LibraryPanel extends JPanel implements Refreshable {
    private final BeatQueueController c;
    private final Consumer<String> status;

    private final JTextField search = new JTextField();
    private final JComboBox<String> sort = new JComboBox<>(new String[] {
            BeatQueueController.SORT_TITLE, BeatQueueController.SORT_ARTIST, BeatQueueController.SORT_VOTES });
    private final DefaultListModel<Object> model = new DefaultListModel<>();
    private final JList<Object> list = new JList<>(model);
    private final JLabel count = Theme.label(" ", Theme.SMALL, Theme.MUTED);

    public LibraryPanel(BeatQueueController c, Consumer<String> status) {
        this.c = c;
        this.status = status;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(8, 24, 16, 24));

        // top: heading + search + sort
        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("Song Library", Theme.HEADING, Theme.TEXT), BorderLayout.NORTH);
        titles.add(Theme.label("Type the start of any word in a title or artist, for example \"dua\" or \"queen\".",
                Theme.SMALL, Theme.MUTED), BorderLayout.SOUTH);
        top.add(titles, BorderLayout.NORTH);

        Theme.styleField(search);
        Theme.styleCombo(sort);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(Theme.label("Sort by", Theme.BODY, Theme.MUTED));
        right.add(sort);
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(search, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        top.add(row, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        // center: list
        list.setCellRenderer(Theme.renderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(42);
        add(Theme.scroll(list), BorderLayout.CENTER);

        // bottom: buttons
        RoundedButton request = new RoundedButton("Request Song", Theme.ACCENT, Theme.ACCENT_HOVER);
        RoundedButton exact = new RoundedButton("Exact Title Lookup");
        RoundedButton clear = new RoundedButton("Clear Search");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(request);
        buttons.add(exact);
        buttons.add(clear);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(buttons, BorderLayout.WEST);
        bottom.add(count, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        sort.addActionListener(e -> refresh());

        request.addActionListener(e -> {
            Object o = list.getSelectedValue();
            if (!(o instanceof Song)) {
                status.accept("Select a song first, then press Request Song.");
                return;
            }
            Song s = (Song) o;
            String err = c.requestSong(s.getId());
            status.accept(err != null ? err : "Requested \"" + s.getTitle() + "\". It is now in the queue.");
        });
        exact.addActionListener(e -> {
            List<Song> found = c.findExactTitle(search.getText());
            model.clear();
            for (Song s : found) model.addElement(s);
            count.setText(found.size() + " exact match(es)");
            status.accept(found.isEmpty()
                    ? "No song has exactly that title (binary search)."
                    : "Binary search found " + found.size() + " exact match(es).");
        });
        clear.addActionListener(e -> search.setText(""));
    }

    @Override
    public void refresh() {
        Object sel = list.getSelectedValue();
        int selId = (sel instanceof Song) ? ((Song) sel).getId() : -1;

        List<Song> songs = c.search(search.getText(), (String) sort.getSelectedItem());
        model.clear();
        for (Song s : songs) model.addElement(s);
        count.setText(songs.size() + " song(s)");

        for (int i = 0; i < model.size(); i++) {
            if (((Song) model.get(i)).getId() == selId) {
                list.setSelectedIndex(i);
                break;
            }
        }
    }
}
