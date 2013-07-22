package net.minecraft.launcher.ui.tabs;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.OperatingSystem;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.net.URL;

/**
 * Created by Energy on 13年7月23日.
 */
public class ReadmeTab extends JScrollPane{
    private final JTextPane readme = new JTextPane();
    private final Launcher launcher;

    public ReadmeTab(Launcher launcher) {
        this.launcher = launcher;

        this.readme.setEditable(false);
        this.readme.setMargin(null);
        this.readme.setBackground(Color.WHITE);
        this.readme.setContentType("text/html");
        this.readme.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Loading page..</h1></center></font></body></html>");
        this.readme.addHyperlinkListener(new HyperlinkListener() {
            public void hyperlinkUpdate(HyperlinkEvent he) {
                if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                    try {
                        OperatingSystem.openLink(he.getURL().toURI());
                    } catch (Exception e) {
                        Launcher.getInstance().println("Unexpected exception opening link " + he.getURL(), e);
                    }
            }
        });
        setViewportView(this.readme);
    }

    public void setPage(final String url) {
        Thread thread = new Thread("Update website tab") {
            public void run() {
                try {
                    ReadmeTab.this.readme.setPage(new URL(url));
                } catch (Exception e) {
                    Launcher.getInstance().println("Unexpected exception loading " + url, e);
                    ReadmeTab.this.readme.setText("<html><body><font color=\"#808080\"><br><br><br><br><br><br><br><center><h1>Failed to get page</h1><br>" + e.toString() + "</center></font></body></html>");
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}
