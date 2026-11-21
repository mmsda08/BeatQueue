import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import ui.MainFrame;

/** Run this file to start BeatQueue. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // keep the default look if this fails
            }
            UIManager.put("ScrollBar.thumb", new Color(0x3A3A58));
            UIManager.put("ScrollBar.track", new Color(0x1A1A26));
            new MainFrame().setVisible(true);
        });
    }
}
