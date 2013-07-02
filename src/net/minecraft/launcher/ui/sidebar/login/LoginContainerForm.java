package net.minecraft.launcher.ui.sidebar.login;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;

import javax.swing.*;
import java.awt.*;

public class LoginContainerForm extends JPanel {
    private static final String CARD_LOGGED_IN = "Logged In";
    private static final String CARD_NOT_LOGGED_IN = "Not Logged In";
    private final Launcher launcher;
    private final LoggedInForm loggedInForm;
    private final NotLoggedInForm notLoggedInForm;
    private final CardLayout layout = new CardLayout();

    public LoginContainerForm(Launcher launcher) {
        super(true);
        this.launcher = launcher;
        this.loggedInForm = new LoggedInForm(this);
        this.notLoggedInForm = new NotLoggedInForm(this);
        setMaximumSize(new Dimension(2147483647, 300));

        setLayout(this.layout);
        add(this.loggedInForm, "Logged In");
        add(this.notLoggedInForm, "Not Logged In");

        this.layout.show(this, "Not Logged In");
    }

    public void checkLoginState() {
        AuthenticationService authentication = this.launcher.getProfileManager().getSelectedProfile().getAuthentication();

        this.notLoggedInForm.checkLoginState();
        this.loggedInForm.checkLoginState();

        if (authentication.isLoggedIn())
            this.layout.show(this, "Logged In");
        else
            this.layout.show(this, "Not Logged In");
    }

    public NotLoggedInForm getNotLoggedInForm() {
        return this.notLoggedInForm;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}


