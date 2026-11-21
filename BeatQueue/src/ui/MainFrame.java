package ui;

import controller.BeatQueueController;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/** The main window: header with navigation + Undo, the screens, and a status bar. */
@SuppressWarnings({"serial", "this-escape"})
public class MainFrame extends JFrame {
    private final BeatQueueController controller = new BeatQueueController();
    private final List<Refreshable> screens = new ArrayList<>();
    private final List<RoundedButton> navButtons = new ArrayList<>();
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel status = Theme.label("Welcome to BeatQueue. Request a song to get started.", Theme.BODY, Theme.MUTED);

    public MainFrame() {
        super("BeatQueue");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 620));
        setSize(1040, 720);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());

        Consumer<String> setStatus = status::setText;

        addScreen("Library", new LibraryPanel(controller, setStatus));
        addScreen("Queue", new QueuePanel(controller, setStatus));
        addScreen("Playlist", new PlaylistPanel(controller, setStatus));
        addScreen("Top 10", new TopChartPanel(controller));
        addScreen("Admin", new AdminPanel(controller, setStatus));
        content.setBackground(Theme.BG);

        // header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        titles.add(Theme.label("BeatQueue", Theme.TITLE, Theme.TEXT), BorderLayout.NORTH);
        titles.add(Theme.label("Song requests and playlist manager", Theme.SMALL, Theme.MUTED), BorderLayout.SOUTH);
        header.add(titles, BorderLayout.WEST);

        RoundedButton undo = new RoundedButton("Undo");
        undo.addActionListener(e -> status.setText(controller.undo()));
        JPanel undoWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        undoWrap.setOpaque(false);
        undoWrap.add(undo);
        header.add(undoWrap, BorderLayout.EAST);

        JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        nav.setOpaque(false);
        for (RoundedButton b : navButtons) nav.add(b);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(16, 24, 12, 24));
        header.setAlignmentX(LEFT_ALIGNMENT);
        nav.setAlignmentX(LEFT_ALIGNMENT);
        top.add(header);
        top.add(javax.swing.Box.createVerticalStrut(14));
        top.add(nav);

        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.SURFACE);
        bar.setBorder(new EmptyBorder(10, 24, 10, 24));
        bar.add(status, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(content, BorderLayout.CENTER);
        add(bar, BorderLayout.SOUTH);

        controller.addChangeListener(this::refreshAll);
        refreshAll();
        showScreen("Library");
    }

    private void addScreen(String name, JPanel panel) {
        content.add(panel, name);
        screens.add((Refreshable) panel);
        RoundedButton b = new RoundedButton(name);
        b.addActionListener(e -> showScreen(name));
        navButtons.add(b);
    }

    private void showScreen(String name) {
        cards.show(content, name);
        for (RoundedButton b : navButtons) {
            if (b.getText().equals(name)) b.setColors(Theme.ACCENT, Theme.ACCENT_HOVER);
            else b.setColors(Theme.BUTTON, Theme.BUTTON_HOVER);
        }
    }

    private void refreshAll() {
        for (Refreshable r : screens) r.refresh();
    }
}
