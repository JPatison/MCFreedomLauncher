package net.minecraft.launcher.ui.sidebar.login;

import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.locale.LocaleHelper;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ResourceBundle;

public class LoggedInForm extends BaseLogInForm {
    private ResourceBundle messages= LocaleHelper.getMessages();
    private final JButton playButton = new JButton(messages.getString("play"));
    private final JButton logOutButton = new JButton(messages.getString("log.out"));
    private final JLabel welcomeText = new JLabel(messages.getString("html.oh.no.panic.html"));
    private AuthenticationService previousAuthentication = null;

    public LoggedInForm(LoginContainerForm container) {
        super(container, LocaleHelper.getMessages().getString("play.game"));
        //setMaximumSize(new Dimension(2147483647, 300));
        createInterface();
    }

    protected void populateGrid(GridBagConstraints constraints) {
        constraints.fill = 2;

        add(this.welcomeText, constraints, 0, 0, 0, 0);

        this.playButton.addActionListener(this);
        this.logOutButton.addActionListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));
        buttonPanel.add(this.logOutButton);
        buttonPanel.add(this.playButton);

        this.playButton.setFont(new Font(this.playButton.getFont().getName(), 1, this.playButton.getFont().getSize()));

        add(buttonPanel, constraints, 0, 3, 0, 0);
    }

    public void checkLoginState() {
        boolean canPlay = true;
        boolean canLogOut = true;
        //AuthenticationService authentication = getLauncher().getProfileManager().getSelectedProfile().getAuthentication();
        Profile profile = getLauncher().getProfileManager().getSelectedProfile();
        AuthenticationService authentication = profile.getAuthentication();

        if (getLauncher().getGameLauncher().isWorking()) {
            canPlay = false;
            canLogOut = false;
        }

        if (getLauncher().getVersionManager().getVersions(profile.getVersionFilter()).size() <= 0) {
        //if (getLauncher().getVersionManager().getVersions().size() <= 0) {
            canPlay = false;
        }

        this.welcomeText.setText(messages.getString("html.welcome.guest.html"));

        if (authentication.isLoggedIn()) {
            if (authentication.getSelectedProfile() == null) {
                this.playButton.setText(messages.getString("play.demo"));
            } else {
                this.welcomeText.setText(messages.getString("html.welcome.b") + authentication.getSelectedProfile().getName() + "</b>!</html>");

                if (authentication.canPlayOnline())
                    this.playButton.setText(messages.getString("play"));
                else {
                    this.playButton.setText(messages.getString("play.offline"));
                }
            }
        }

        this.logOutButton.setEnabled(canLogOut);
        this.playButton.setEnabled(canPlay);

        this.previousAuthentication = authentication;
    }

    public void onProfilesRefreshed(ProfileManager manager) {
        Profile profile = manager.getSelectedProfile();

        if (profile.getAuthentication() != this.previousAuthentication)
            getLoginContainer().checkLoginState();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.playButton) {
            getLauncher().getVersionManager().getExecutorService().submit(new Runnable() {
                public void run() {
                    LoggedInForm.this.getLauncher().getGameLauncher().playGame();
                }
            });
        } else if (e.getSource() == this.logOutButton) {
            getLauncher().getProfileManager().getSelectedProfile().getAuthentication().logOut();
            saveAuthenticationDetails();
            getLoginContainer().checkLoginState();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent itemEvent) {

    }
}


