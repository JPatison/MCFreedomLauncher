package net.minecraft.launcher.ui.popups.login;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AuthErrorForm extends JPanel {
    private final LogInPopup popup;
    private final JLabel errorLabel = new JLabel();

    public AuthErrorForm(LogInPopup popup) {
        this.popup = popup;

        createInterface();
        clear();
    }

    protected void createInterface() {
        setBorder(new EmptyBorder(0, 0, 15, 0));
        this.errorLabel.setFont(this.errorLabel.getFont().deriveFont(1));
        add(this.errorLabel);
    }

    public void clear() {
        setVisible(false);
    }

    public void setVisible(boolean value) {
        super.setVisible(value);
        this.popup.repack();
    }

    public void displayError(final String[] lines) {
        if (SwingUtilities.isEventDispatchThread()) {
            String error = "";
            for (String line : lines) {
                error = error + "<p>" + line + "</p>";
            }
            this.errorLabel.setText("<html><div style='text-align: center;'>" + error + " </div></html>");
            setVisible(true);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    AuthErrorForm.this.displayError(lines);
                }
            });
        }
    }
}