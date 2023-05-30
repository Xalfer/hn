package hn;

import java.awt.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.stream.*;
import javax.swing.*;

/**
 * Class containing the entry point of the program.
 */
public class Main {
    /**
     * Entry point of the program.
     *
     * @param args arguments specified on the command line, will always be ignored
     */
    public static void main(String[] args) {
        // Turn anti-aliasing on. It's wierd that it has to be done this way, but better than nothing.
        System.setProperty("awt.useSystemAAFontSettings", "on");

        JFrame frame = new JFrame("a hacker news client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 600);
        frame.setPreferredSize(new Dimension(1000, 600));
        frame.setVisible(true);

        JPanel panel = new JPanel();
        JScrollPane scrollPane = new JScrollPane(panel);
        frame.add(scrollPane);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Check if we have internet connection.
        try {
            URI uri = new URI(HNApi.baseUri);
            HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
            HttpClient client = HttpClient.newBuilder().build(); 
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            JLabel l = new JLabel("Cannot connect to HackerNews api. Check your internet connection.");
            return;
        }

        BlockingQueue<Story> stories = HNApi.getTopStoriesAsync();
        while (true) {
            try {
                panel.add(stories.take());
                frame.revalidate();
                frame.repaint();
            } catch (Exception e) {
                System.err.println("Cannot take from queue: " + e.getMessage());
                break;
            }
        }
    }

    /**
     * A constructor for the main class. I guess this is a place to mention that a good 
     * amount of the documentation is going to be redundant since I use descriptive names, 
     * but this is the only way to make javadoc stop complaining.
     */
    public Main() {}
}
