package net.minecraft.launcher.ui.bottombar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.VersionSyncInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class PlayerInfoPanel extends JPanel
        implements RefreshedProfilesListener, RefreshedVersionsListener {
    private final Launcher launcher;
    private final JLabel welcomeText = new JLabel("", 0);
    private final JLabel versionText = new JLabel("", 0);
    private final JButton logOutButton = new JButton("Log Out");

    public PlayerInfoPanel(final Launcher launcher) {
        this.launcher = launcher;

        launcher.getProfileManager().addRefreshedProfilesListener(this);
        checkState();
        createInterface();

        this.logOutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launcher.getProfileManager().getSelectedProfile().setPlayerUUID(null);
                launcher.getProfileManager().trimAuthDatabase();
                launcher.showLoginPrompt();
            }
        });
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;

        constraints.gridy = 0;

        constraints.weightx = 1.0D;
        constraints.gridwidth = 2;
        add(this.welcomeText, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 0.0D;

        constraints.gridy += 1;

        constraints.weightx = 1.0D;
        constraints.gridwidth = 2;
        add(this.versionText, constraints);
        constraints.gridwidth = 1;
        constraints.weightx = 0.0D;

        constraints.gridy += 1;

        constraints.weightx = 0.5D;
        constraints.fill = 0;
        add(this.logOutButton, constraints);
        constraints.weightx = 0.0D;

        constraints.gridy += 1;
    }

    public void onProfilesRefreshed(ProfileManager manager) {
        checkState();
    }

    public void checkState() {
        Profile profile = this.launcher.getProfileManager().getProfiles().isEmpty() ? null : this.launcher.getProfileManager().getSelectedProfile();
        AuthenticationService auth = profile == null ? null : this.launcher.getProfileManager().getAuthDatabase().getByUUID(profile.getPlayerUUID());
        List versions = profile == null ? null : this.launcher.getVersionManager().getVersions(profile.getVersionFilter());
        VersionSyncInfo version = (profile == null) || (versions.isEmpty()) ? null : (VersionSyncInfo) versions.get(0);

        if ((profile != null) && (profile.getLastVersionId() != null)) {
            version = this.launcher.getVersionManager().getVersionSyncInfo(profile.getLastVersionId());
        }

        if ((auth == null) || (!auth.isLoggedIn())) {
            this.welcomeText.setText("Welcome, guest! Please log in.");
            this.logOutButton.setEnabled(false);
        } else if (auth.getSelectedProfile() == null) {
            this.welcomeText.setText("<html>Welcome, player!</html>");
            this.logOutButton.setEnabled(true);
        } else {
            this.welcomeText.setText("<html>Welcome, <b>" + auth.getSelectedProfile().getName() + "</b></html>");
            this.logOutButton.setEnabled(true);
        }

        if (version == null)
            this.versionText.setText("Loading versions...");
        else if (version.isUpToDate())
            this.versionText.setText("Ready to play Minecraft " + version.getLatestVersion().getId());
        else if (version.isInstalled())
            this.versionText.setText("Ready to update & play Minecraft " + version.getLatestVersion().getId());
        else if (version.isOnRemote())
            this.versionText.setText("Ready to download & play Minecraft " + version.getLatestVersion().getId());
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