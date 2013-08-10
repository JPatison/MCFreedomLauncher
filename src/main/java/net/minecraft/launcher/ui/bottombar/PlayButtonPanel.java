package net.minecraft.launcher.ui.bottombar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.locale.LocaleHelper;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.updater.VersionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

public class PlayButtonPanel extends JPanel
        implements RefreshedProfilesListener, RefreshedVersionsListener {
    private final ResourceBundle resourceBundle = LocaleHelper.getMessages();
    private final Launcher launcher;
    private final JButton playButton = new JButton(resourceBundle.getString("play"));

    public PlayButtonPanel(Launcher launcher) {
        this.launcher = launcher;

        launcher.getProfileManager().addRefreshedProfilesListener(this);
        checkState();
        createInterface();

        this.playButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PlayButtonPanel.this.getLauncher().getVersionManager().getExecutorService().submit(new Runnable() {
                    public void run() {
                        PlayButtonPanel.this.getLauncher().getGameLauncher().playGame();
                    }
                });
            }
        });
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 1;
        constraints.weightx = 1.0D;
        constraints.weighty = 1.0D;

        constraints.gridy = 0;
        constraints.gridx = 0;
        add(this.playButton, constraints);

        this.playButton.setFont(this.playButton.getFont().deriveFont(1, this.playButton.getFont().getSize() + 2));
    }

    public void onProfilesRefreshed(ProfileManager manager) {
        checkState();
    }

    public void checkState() {
        Profile profile = this.launcher.getProfileManager().getProfiles().isEmpty() ? null : this.launcher.getProfileManager().getSelectedProfile();
        AuthenticationService auth = profile == null ? null : this.launcher.getProfileManager().getAuthDatabase().getByUUID(profile.getPlayerUUID());

        if ((auth == null) || (!auth.isLoggedIn()) || (this.launcher.getVersionManager().getVersions(profile.getVersionFilter()).isEmpty())) {
            this.playButton.setEnabled(false);
            this.playButton.setText(resourceBundle.getString("play"));
        } else if (auth.getSelectedProfile() == null) {
            this.playButton.setEnabled(true);
            this.playButton.setText(resourceBundle.getString("play.demo"));
        } else if (auth.canPlayOnline()) {
            this.playButton.setEnabled(true);
            this.playButton.setText(resourceBundle.getString("play"));
        } else {
            this.playButton.setEnabled(true);
            this.playButton.setText(resourceBundle.getString("play.offline"));
        }

        if (this.launcher.getGameLauncher().isWorking())
            this.playButton.setEnabled(false);
    }

    public void onVersionsRefreshed(VersionManager manager) {
        checkState();
    }

    public boolean shouldReceiveEventsInUIThread() {
        return true;
    }

    public Launcher getLauncher() {
        return this.launcher;
    }
}