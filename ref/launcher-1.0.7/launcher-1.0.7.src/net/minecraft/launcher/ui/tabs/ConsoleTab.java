package net.minecraft.launcher.ui.tabs;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import net.minecraft.launcher.Launcher;

public class ConsoleTab extends JScrollPane
{
  private static final Font MONOSPACED = new Font("Monospaced", 0, 12);

  private final JTextPane console = new JTextPane();
  private final Launcher launcher;

  public ConsoleTab(Launcher launcher)
  {
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
      SwingUtilities.invokeLater(new Runnable()
      {
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
    try
    {
      document.insertString(document.getLength(), line, null);
    } catch (BadLocationException localBadLocationException) {
    }
    if (shouldScroll)
      scrollBar.setValue(2147483647);
  }
}