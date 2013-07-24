package net.minecraft.launcher.ui.popups;

import javax.swing.*;
import java.awt.*;

public class ErrorMessagePopup
        implements Runnable {
    private final Component component;
    private final String error;

    private ErrorMessagePopup(Component component, String error) {
        this.component = component;
        this.error = error;
    }

    public void run() {
        JOptionPane.showMessageDialog(this.component, this.error);
    }

    public static void show(Component component, String error) {
        SwingUtilities.invokeLater(new ErrorMessagePopup(component, error));
    }
}
