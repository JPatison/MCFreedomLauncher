package net.minecraft.launcher.ui.bottombar;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.popups.profile.ProfileEditorPopup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;

public class ProfileSelectionPanel extends JPanel
        implements ActionListener, ItemListener, RefreshedProfilesListener {
    private final JComboBox profileList = new JComboBox();
    private final JButton newProfileButton = new JButton("New Profile");
    private final JButton editProfileButton = new JButton("Edit Profile");
    private final Launcher launcher;
    private boolean skipSelectionUpdate;

    public ProfileSelectionPanel(Launcher launcher) {
        this.launcher = launcher;

        this.profileList.setRenderer(new ProfileListRenderer());
        this.profileList.addItemListener(this);
        this.profileList.addItem("Loading profiles...");

        this.newProfileButton.addActionListener(this);
        this.editProfileButton.addActionListener(this);

        createInterface();

        launcher.getProfileManager().addRefreshedProfilesListener(this);
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.weightx = 0.0D;

        constraints.gridy = 0;

        add(new JLabel("Profile: "), constraints);
        constraints.gridx = 1;
        add(this.profileList, constraints);
        constraints.gridx = 0;

        constraints.gridy += 1;

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        buttonPanel.add(this.newProfileButton);
        buttonPanel.add(this.editProfileButton);

        constraints.gridwidth = 2;
        add(buttonPanel, constraints);
        constraints.gridwidth = 1;

        constraints.gridy += 1;
    }

    public void onProfilesRefreshed(ProfileManager manager) {
        populateProfiles();
    }

    public boolean shouldReceiveEventsInUIThread() {
        return true;
    }

    public void populateProfiles() {
        String previous = this.launcher.getProfileManager().getSelectedProfile().getName();
        Profile selected = null;
        Collection profiles = this.launcher.getProfileManager().getProfiles().values();
        this.profileList.removeAllItems();

        this.skipSelectionUpdate = true;

        for (Profile profile : (Collection<Profile>) profiles) {
            if (previous.equals(profile.getName())) {
                selected = profile;
            }

            this.profileList.addItem(profile);
        }

        if (selected == null) {
            if (profiles.isEmpty()) {
                selected = this.launcher.getProfileManager().getSelectedProfile();
                this.profileList.addItem(selected);
            }

            selected = (Profile) profiles.iterator().next();
        }

        this.profileList.setSelectedItem(selected);
        this.skipSelectionUpdate = false;
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() != 1) return;

        if ((!this.skipSelectionUpdate) && ((e.getItem() instanceof Profile))) {
            Profile profile = (Profile) e.getItem();
            this.launcher.getProfileManager().setSelectedProfile(profile.getName());
            this.launcher.ensureLoggedIn();
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.newProfileButton) {
            Profile profile = new Profile(this.launcher.getProfileManager().getSelectedProfile());
            profile.setName("Copy of " + profile.getName());

            while (this.launcher.getProfileManager().getProfiles().containsKey(profile.getName())) {
                profile.setName(profile.getName() + "_");
            }

            ProfileEditorPopup.showEditProfileDialog(getLauncher(), profile);
            this.launcher.getProfileManager().setSelectedProfile(profile.getName());
        } else if (e.getSource() == this.editProfileButton) {
            Profile profile = this.launcher.getProfileManager().getSelectedProfile();
            ProfileEditorPopup.showEditProfileDialog(getLauncher(), profile);
        }
    }

    public Launcher getLauncher() {
        return this.launcher;
    }

    private static class ProfileListRenderer extends BasicComboBoxRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if ((value instanceof Profile)) {
                value = ((Profile) value).getName();
            }

            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            return this;
        }
    }
}