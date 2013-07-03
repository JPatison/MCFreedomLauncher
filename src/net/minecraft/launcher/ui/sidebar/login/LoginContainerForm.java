package net.minecraft.launcher.ui.sidebar.login;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.locale.LocaleHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ResourceBundle;

public class LoginContainerForm extends JPanel {
    private static ResourceBundle messages= LocaleHelper.getMessages();
    private static final String CARD_LOGGED_IN = messages.getString("logged.in");
    private static final String CARD_NOT_LOGGED_IN = messages.getString("not.logged.in");
    private final Launcher launcher;
    private final LoggedInForm loggedInForm;
    private final NotLoggedInForm notLoggedInForm;
    private final CardLayout layout = new CardLayout();

    public LoginContainerForm(Launcher launcher) {
        super(true);
        this.launcher = launcher;
        this.loggedInForm = new LoggedInForm(this);
        this.notLoggedInForm = new NotLoggedInForm(this);
        //setMaximumSize(new Dimension(2147483647, 300));

        setLayout(this.layout);
        add(this.loggedInForm, CARD_LOGGED_IN);
        add(this.notLoggedInForm, CARD_NOT_LOGGED_IN);

        this.layout.show(this, CARD_NOT_LOGGED_IN);
    }

    public void checkLoginState() {
        AuthenticationService authentication = this.launcher.getProfileManager().getSelectedProfile().getAuthentication();

        this.notLoggedInForm.checkLoginState();
        this.loggedInForm.checkLoginState();

        if (authentication.isLoggedIn())
            this.layout.show(this, messages.getString("logged.in"));
        else
            this.layout.show(this, messages.getString("not.logged.in"));
    }

    public NotLoggedInForm getNotLoggedInForm() {
        return this.notLoggedInForm;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}


