package net.minecraft.launcher.ui.tabs;

import net.minecraft.launcher.Launcher;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

public class ConsoleTab extends JScrollPane {
    private static final Font MONOSPACED = new Font("Monospaced", 0, 12);

    private final JTextPane console = new JTextPane();
    private final Launcher launcher;

    public ConsoleTab(Launcher launcher) {
        this.launcher = launcher;

        this.console.setFont(MONOSPACED);
        this.console.setEditable(false);
        this.console.setMargin(null);

        setViewportView(this.console);
    }

    public Launcher getLauncher() {
        return this.launcher;
    }

    public void print(final String line) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ConsoleTab.this.print(line);
                }
            });
            return;
        }

        Document document = this.console.getDocument();
        JScrollBar scrollBar = getVerticalScrollBar();
        boolean shouldScroll = false;

        if (getViewport().getView() == this.console) {
            shouldScroll = scrollBar.getValue() + scrollBar.getSize().getHeight() + MONOSPACED.getSize() * 4 > scrollBar.getMaximum();
        }
        try {
            document.insertString(document.getLength(), line, null);
        } catch (BadLocationException localBadLocationException) {
        }
        if (shouldScroll)
            scrollBar.setValue(2147483647);
    }
}


