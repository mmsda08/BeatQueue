package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import model.Song;

/** DJ screen: the playlist (doubly linked list) with next, previous, insert, delete and shuffle. */
@SuppressWarnings({"serial", "this-escape"})
public class PlaylistPanel extends JPanel implements Refreshable {
    private final BeatQueueController c;
    private final Consumer<String> status;

    private final DefaultListModel<Song> model = new DefaultListModel<>();
    private final JList<Song> list = new JList<>(model);
    private final JLabel nowPlaying = Theme.label("Nothing playing", Theme.HEADING, Theme.TEXT);
    private final JLabel nowInfo = Theme.label(" ", Theme.SMALL, Theme.MUTED);
    private int currentIndex = -1;

    public PlaylistPanel(BeatQueueController c, Consumer<String> status) {
        this.c = c;
        this.status = status;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(8, 24, 16, 24));

        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 4), Theme.ACCENT_DARK);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.add(Theme.label("NOW PLAYING", Theme.SMALL, Theme.MUTED), BorderLayout.NORTH);
        card.add(nowPlaying, BorderLayout.CENTER);
        card.add(nowInfo, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        top.add(Theme.label("Playlist", Theme.HEADING, Theme.TEXT), BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(42);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel lab = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, false);
                boolean cur = index == currentIndex;
                lab.setText((cur ? "\u25B6   " : "      ") + (index + 1) + ".   " + value);
                lab.setBorder(new EmptyBorder(9, 14, 9, 14));
                lab.setFont(cur ? Theme.BODY_BOLD : Theme.BODY);
                lab.setForeground(cur ? Theme.GOOD : Theme.TEXT);
                lab.setOpaque(true);
                lab.setBackground(isSelected ? Theme.ACCENT_DARK : (index % 2 == 0 ? Theme.SURFACE : Theme.SURFACE_ALT));
                return lab;
            }
        });
        add(Theme.scroll(list), BorderLayout.CENTER);

        RoundedButton prev = new RoundedButton("\u25C0 Previous");
        RoundedButton next = new RoundedButton("Next \u25B6", Theme.ACCENT, Theme.ACCENT_HOVER);
        RoundedButton playSel = new RoundedButton("Play Selected");
        RoundedButton insert = new RoundedButton("Insert Song...");
        RoundedButton delete = new RoundedButton("Delete Selected");
        RoundedButton shuffle = new RoundedButton("Shuffle");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(prev);
        buttons.add(next);
        buttons.add(playSel);
        buttons.add(insert);
        buttons.add(delete);
        buttons.add(shuffle);
        add(buttons, BorderLayout.SOUTH);

        prev.addActionListener(e -> report(c.playlistPrevious(), "Went back to the previous song."));
        next.addActionListener(e -> report(c.playlistNext(), "Moved to the next song."));
        playSel.addActionListener(e -> report(c.playlistPlayAt(list.getSelectedIndex()), "Now playing the selected song."));
        delete.addActionListener(e -> report(c.deleteFromPlaylist(list.getSelectedIndex()), "Deleted from the playlist."));
        shuffle.addActionListener(e -> report(c.shufflePlaylist(), "Playlist shuffled (Fisher-Yates)."));
        insert.addActionListener(e -> insertDialog());
    }

    private void report(String err, String ok) {
        status.accept(err != null ? err : ok);
    }

    private void insertDialog() {
        List<Song> songs = c.getLibrary(BeatQueueController.SORT_TITLE);
        JComboBox<Song> combo = new JComboBox<>(songs.toArray(new Song[0]));
        int size = model.size();
        JSpinner pos = new JSpinner(new SpinnerNumberModel(size + 1, 1, size + 1, 1));
        JPanel p = new JPanel(new GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Song:"));
        p.add(combo);
        p.add(new JLabel("Position (1 = first, " + (size + 1) + " = last):"));
        p.add(pos);
        int ok = JOptionPane.showConfirmDialog(this, p, "Insert into playlist",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;
        Song chosen = (Song) combo.getSelectedItem();
        int position = ((Integer) pos.getValue()) - 1;
        report(c.insertIntoPlaylist(position, chosen), "Inserted \"" + chosen.getTitle() + "\" into the playlist.");
    }

    @Override
    public void refresh() {
        int selected = list.getSelectedIndex();
        List<Song> songs = c.getPlaylist();
        currentIndex = c.getPlaylistCurrentIndex();
        model.clear();
        for (Song s : songs) model.addElement(s);
        if (selected >= 0 && selected < model.size()) list.setSelectedIndex(selected);

        if (currentIndex < 0) {
            nowPlaying.setText("Nothing playing");
            nowInfo.setText("Use Play Next in the Queue tab to add songs.");
        } else {
            Song s = songs.get(currentIndex);
            nowPlaying.setText(s.getTitle());
            nowInfo.setText(s.getArtist() + "   \u00B7   song " + (currentIndex + 1) + " of " + songs.size());
        }
        list.repaint();
    }
}
