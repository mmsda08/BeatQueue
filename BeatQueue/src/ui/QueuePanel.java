package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import model.Request;

/** DJ screen: requests in arrival order, voting, and "play next" (most votes wins). */
@SuppressWarnings({"serial", "this-escape"})
public class QueuePanel extends JPanel implements Refreshable {
    private final BeatQueueController c;

    private final DefaultListModel<Object> model = new DefaultListModel<>();
    private final JList<Object> list = new JList<>(model);
    private final JLabel nextTitle = Theme.label("Nothing requested yet", Theme.HEADING, Theme.TEXT);
    private final JLabel nextInfo = Theme.label(" ", Theme.SMALL, Theme.MUTED);

    public QueuePanel(BeatQueueController c, Consumer<String> status) {
        this.c = c;
        setLayout(new BorderLayout(0, 12));
        setBackground(Theme.BG);
        setBorder(new EmptyBorder(8, 24, 16, 24));

        // top: "up next" card
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 4), Theme.ACCENT_DARK);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        card.add(Theme.label("UP NEXT (most votes, earliest request wins ties)", Theme.SMALL, Theme.MUTED),
                BorderLayout.NORTH);
        card.add(nextTitle, BorderLayout.CENTER);
        card.add(nextInfo, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(card, BorderLayout.NORTH);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("Request Queue", Theme.HEADING, Theme.TEXT), BorderLayout.NORTH);
        titles.add(Theme.label("Listed first-come, first-served. Vote to move a song up the play order.",
                Theme.SMALL, Theme.MUTED), BorderLayout.SOUTH);
        top.add(titles, BorderLayout.SOUTH);
        add(top, BorderLayout.NORTH);

        list.setCellRenderer(Theme.renderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(42);
        add(Theme.scroll(list), BorderLayout.CENTER);

        RoundedButton vote = new RoundedButton("Vote +1");
        RoundedButton play = new RoundedButton("Play Next", Theme.ACCENT, Theme.ACCENT_HOVER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(vote);
        buttons.add(play);
        add(buttons, BorderLayout.SOUTH);

        vote.addActionListener(e -> {
            Object o = list.getSelectedValue();
            if (!(o instanceof Request)) {
                status.accept("Select a request first, then press Vote +1.");
                return;
            }
            Request r = (Request) o;
            String err = c.vote(r.getRequestId());
            status.accept(err != null ? err : "Voted for \"" + r.getSong().getTitle() + "\".");
        });
        play.addActionListener(e -> {
            Request next = c.getNextRequest();
            String err = c.playNext();
            status.accept(err != null ? err
                    : "Now added to the playlist: \"" + next.getSong().getTitle() + "\".");
        });
    }

    @Override
    public void refresh() {
        Object sel = list.getSelectedValue();
        int selId = (sel instanceof Request) ? ((Request) sel).getRequestId() : -1;

        List<Request> pending = c.getPendingRequests();
        model.clear();
        for (Request r : pending) model.addElement(r);
        for (int i = 0; i < model.size(); i++) {
            if (((Request) model.get(i)).getRequestId() == selId) {
                list.setSelectedIndex(i);
                break;
            }
        }

        Request next = c.getNextRequest();
        if (next == null) {
            nextTitle.setText("Nothing requested yet");
            nextInfo.setText("Request a song from the Library tab.");
        } else {
            nextTitle.setText(next.getSong().getTitle());
            nextInfo.setText(next.getSong().getArtist() + "   \u00B7   " + next.getVotes()
                    + (next.getVotes() == 1 ? " vote" : " votes"));
        }
    }
}
