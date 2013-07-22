package net.minecraft.launcher.ui.popups.login;

import net.minecraft.launcher.authentication.AuthenticationDatabase;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ExistingUserListForm extends JPanel
        implements ActionListener {
    private final LogInPopup popup;
    private final JComboBox userDropdown = new JComboBox();
    private final AuthenticationDatabase authDatabase;
    private final JButton playButton = new JButton("Play");

    public ExistingUserListForm(LogInPopup popup) {
        this.popup = popup;
        this.authDatabase = popup.getLauncher().getProfileManager().getAuthDatabase();

        fillUsers();
        createInterface();

        this.playButton.addActionListener(this);
    }

    private void fillUsers() {
        for (String user : this.authDatabase.getKnownNames())
            this.userDropdown.addItem(user);
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.gridwidth = 2;
        constraints.weightx = 1.0D;

        add(Box.createGlue());

        String currentUser = this.authDatabase.getKnownNames().size() + " different users";
        String thisOrThese = this.authDatabase.getKnownNames().size() == 1 ? "this account" : "one of these accounts";
        add(new JLabel("You're already logged in as " + currentUser + "."), constraints);
        add(new JLabel("You may use " + thisOrThese + " and skip authentication."), constraints);

        add(Box.createVerticalStrut(5), constraints);

        JLabel usernameLabel = new JLabel("Existing User:");
        Font labelFont = usernameLabel.getFont().deriveFont(1);

        usernameLabel.setFont(labelFont);
        add(usernameLabel, constraints);

        constraints.gridwidth = 1;
        add(this.userDropdown, constraints);

        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.weightx = 0.0D;
        constraints.insets = new Insets(0, 5, 0, 0);
        add(this.playButton, constraints);
        constraints.insets = new Insets(0, 0, 0, 0);
        constraints.weightx = 1.0D;
        constraints.gridx = 0;
        constraints.gridy = -1;

        constraints.gridwidth = 2;

        add(Box.createVerticalStrut(5), constraints);
        add(new JLabel("Alternatively, log in with a new account below:"), constraints);
        add(new JPopupMenu.Separator(), constraints);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == this.playButton) {
            this.popup.setCanLogIn(false);

            this.popup.getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    Object selected = ExistingUserListForm.this.userDropdown.getSelectedItem();
                    String uuid;
                    AuthenticationService auth;
                    if ((selected != null) && ((selected instanceof String))) {
                        auth = ExistingUserListForm.this.authDatabase.getByName((String) selected);
                        if (auth.getSelectedProfile() == null)
                            uuid = "demo-" + auth.getUsername();
                        else
                            uuid = auth.getSelectedProfile().getId();
                    } else {
                        auth = null;
                        uuid = null;
                    }

                    if ((auth != null) && (uuid != null))
                        try {
                            auth.logIn();
                            ExistingUserListForm.this.popup.setLoggedIn(uuid);
                        } catch (AuthenticationException ex) {
                            ExistingUserListForm.this.popup.getErrorForm().displayError(new String[]{"We couldn't log you back in as " + selected + ".", "Please try again using your username & password"});

                            ExistingUserListForm.this.userDropdown.removeItem(selected);

                            if (ExistingUserListForm.this.userDropdown.getItemCount() == 0) {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        ExistingUserListForm.this.popup.remove(ExistingUserListForm.this);
                                    }
                                });
                            }

                            ExistingUserListForm.this.popup.setCanLogIn(true);
                        }
                    else
                        ExistingUserListForm.this.popup.setCanLogIn(true);
                }
            });
        }
    }
}