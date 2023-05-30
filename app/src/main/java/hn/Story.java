package hn;

import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import javax.swing.*;

/**
 * A component displaying information about a story.
 */
public class Story extends JPanel {
    private final static Font titleFont = new Font("Serif", Font.BOLD, 20);
    private final static Font subtitleFont = new Font("Serif", Font.PLAIN, 15);

    /**
     * The label displaying the title of the story. It is styled with {@link titleFont}.
     */
    private JLabel titleLabel;
    /**
     * The label displaying the points of the story. It is styled with {@link subtitleFont}.
     * It also contains a separator at the end to distinguish it from {@link authorLabel}.
     */
    private JLabel pointLabel;
    /**
     * The label displaying the name of the author. It is styled with {@link subtitleFont}.
     */
    private JLabel authorLabel;

    /**
     * Creates a story from the supplied item. If a specific field of the item is not present 
     * then it will use a default value.
     *
     * @param item the item this comment is created from
     */
    public Story(Item item) {
        URI uri = item.url.orElse(null);
        String title = item.title.orElse("No title specified.");
        int score = item.score.orElse(0);
        String author = item.by.orElse("no author specified");

        this.enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        MouseListener mouseListener = new uriOpeningMouseListener(uri);
        this.addMouseListener(mouseListener);

        SpringLayout layout = new SpringLayout();
        this.setLayout(layout);

        this.titleLabel = new JLabel(title);
        this.titleLabel.addMouseListener(mouseListener);
        this.titleLabel.setFont(this.titleFont);
        this.titleLabel.setToolTipText(uri != null ? uri.toString() : "No uri specified.");
        this.add(this.titleLabel);

        this.pointLabel = new JLabel(score + " points | ");
        this.pointLabel.setFont(this.subtitleFont);
        this.add(this.pointLabel);
        
        this.authorLabel = new JLabel("by " + author);
        this.authorLabel.setFont(this.subtitleFont);
        this.add(this.authorLabel);

        layout.putConstraint(SpringLayout.WEST, this.titleLabel, 10, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, this.titleLabel, 0, SpringLayout.NORTH, this);
        layout.putConstraint(SpringLayout.WEST, this.pointLabel, 20, SpringLayout.WEST, this);
        layout.putConstraint(SpringLayout.NORTH, this.pointLabel, 0, SpringLayout.SOUTH, this.titleLabel);
        layout.putConstraint(SpringLayout.WEST, this.authorLabel, 0, SpringLayout.EAST, this.pointLabel);
        layout.putConstraint(SpringLayout.NORTH, this.authorLabel, 0, SpringLayout.SOUTH, this.titleLabel);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension titleSize = this.titleLabel.getPreferredSize();
        Dimension authorSize = this.authorLabel.getPreferredSize();
        Dimension pointSize = this.pointLabel.getPreferredSize();
        return new Dimension(
            Integer.max(titleSize.width, authorSize.width + pointSize.width),
            titleSize.height + authorSize.height
        );
    }

    private class uriOpeningMouseListener implements MouseListener {
        private final URI uri;

        public uriOpeningMouseListener(URI uri) {
            this.uri = uri;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() != MouseEvent.BUTTON1 || e.getID() != MouseEvent.MOUSE_CLICKED) {
                return;
            }
            if (uri == null) {
                return;
            }
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(this.uri);
                } else {
                    // If no desktop environment in availible, try to use xdg-open. 
                    // Note that this is only availible on linux and may not work because xdg-open
                    // does not need to be present on a system.
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec("xdg-open " + uri);
                }
            } catch (Exception ex) {
                System.err.println("Cannot open link in default desktop browser.");
            }
        }
        @Override
        public void mouseEntered(MouseEvent e) {}
        @Override
        public void mouseExited(MouseEvent e) {}
        @Override
        public void mousePressed(MouseEvent e) {}
        @Override
        public void mouseReleased(MouseEvent e) {}
    }
}
