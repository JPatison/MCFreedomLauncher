package net.minecraft.launcher.ui.popups.login;

import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.LauncherConstants;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.authentication.AuthenticationDatabase;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.authentication.SPAuthenticationService;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LogInForm extends JPanel
        implements ActionListener {
    private final LogInPopup popup;
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox userDropdown = new JComboBox();
    private final JPanel userDropdownPanel = new JPanel();
    private final AuthenticationService authentication = Launcher.isSPMode() ? new SPAuthenticationService() : new YggdrasilAuthenticationService();

    public LogInForm(LogInPopup popup) {
        this.popup = popup;

        this.usernameField.addActionListener(this);
        this.passwordField.addActionListener(this);

        createInterface();
    }

    protected void createInterface() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.weightx = 1.0D;

        add(Box.createGlue());

        JLabel usernameLabel = new JLabel("Email Address or Username:");
        Font labelFont = usernameLabel.getFont().deriveFont(1);
        Font smalltextFont = usernameLabel.getFont().deriveFont(labelFont.getSize() - 2.0F);

        usernameLabel.setFont(labelFont);
        add(usernameLabel, constraints);
        add(this.usernameField, constraints);

        add(Box.createVerticalStrut(10), constraints);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        add(passwordLabel, constraints);
        add(this.passwordField, constraints);

        JLabel forgotPasswordLabel = new JLabel("(Forgot Password?)");
        forgotPasswordLabel.setFont(smalltextFont);
        forgotPasswordLabel.setHorizontalAlignment(4);
        forgotPasswordLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if ((StringUtils.isBlank(LogInForm.this.usernameField.getText())) || (LogInForm.this.usernameField.getText().contains("@")))
                    OperatingSystem.openLink(LauncherConstants.URL_FORGOT_PASSWORD_MOJANG);
                else
                    OperatingSystem.openLink(LauncherConstants.URL_FORGOT_PASSWORD_MINECRAFT);
            }
        });
        add(forgotPasswordLabel, constraints);

        createUserDropdownPanel(labelFont);
        add(this.userDropdownPanel, constraints);

        add(Box.createVerticalStrut(10), constraints);
    }

    protected void createUserDropdownPanel(Font labelFont) {
        this.userDropdownPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = 2;
        constraints.gridx = 0;
        constraints.gridy = -1;
        constraints.weightx = 1.0D;

        this.userDropdownPanel.add(Box.createVerticalStrut(8), constraints);

        JLabel userDropdownLabel = new JLabel("Character Name:");
        userDropdownLabel.setFont(labelFont);
        this.userDropdownPanel.add(userDropdownLabel, constraints);
        this.userDropdownPanel.add(this.userDropdown, constraints);

        this.userDropdownPanel.setVisible(false);
    }

    public void actionPerformed(ActionEvent e) {
        if ((e.getSource() == this.usernameField) || (e.getSource() == this.passwordField))
            tryLogIn();
    }

    public void tryLogIn() {
        if ((this.authentication.isLoggedIn()) && (this.authentication.getSelectedProfile() == null) && (ArrayUtils.isNotEmpty(this.authentication.getAvailableProfiles()))) {
            this.popup.setCanLogIn(false);

            GameProfile selectedProfile = null;
            for (GameProfile profile : this.authentication.getAvailableProfiles()) {
                if (profile.getName().equals(this.userDropdown.getSelectedItem())) {
                    selectedProfile = profile;
                    break;
                }
            }
            if (selectedProfile == null) selectedProfile = this.authentication.getAvailableProfiles()[0];

            final GameProfile finalSelectedProfile = selectedProfile;
            this.popup.getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        LogInForm.this.authentication.selectGameProfile(finalSelectedProfile);
                        LogInForm.this.popup.getLauncher().getProfileManager().getAuthDatabase().register(LogInForm.this.authentication.getSelectedProfile().getId(), LogInForm.this.authentication);
                        LogInForm.this.popup.setLoggedIn(LogInForm.this.authentication.getSelectedProfile().getId());
                    } catch (InvalidCredentialsException ex) {
                        LogInForm.this.popup.getLauncher().println(ex);
                        LogInForm.this.popup.getErrorForm().displayError(new String[]{"Sorry, but we couldn't log you in right now.", "Please try again later."});
                        LogInForm.this.popup.setCanLogIn(true);
                    } catch (AuthenticationException ex) {
                        LogInForm.this.popup.getLauncher().println(ex);
                        LogInForm.this.popup.getErrorForm().displayError(new String[]{"Sorry, but we couldn't connect to our servers.", "Please make sure that you are online and that Minecraft is not blocked."});
                        LogInForm.this.popup.setCanLogIn(true);
                    }
                }
            });
        } else {
            this.popup.setCanLogIn(false);
            this.authentication.logOut();
            this.authentication.setUsername(this.usernameField.getText());
            this.authentication.setPassword(String.valueOf(this.passwordField.getPassword()));
            final int passwordLength = this.passwordField.getPassword().length;

            this.passwordField.setText("");

            this.popup.getLauncher().getVersionManager().getExecutorService().execute(new Runnable() {
                public void run() {
                    try {
                        LogInForm.this.authentication.logIn();
                        AuthenticationDatabase authDatabase = LogInForm.this.popup.getLauncher().getProfileManager().getAuthDatabase();

                        if (LogInForm.this.authentication.getSelectedProfile() == null) {
                            if (ArrayUtils.isNotEmpty(LogInForm.this.authentication.getAvailableProfiles())) {
                                for (GameProfile profile : LogInForm.this.authentication.getAvailableProfiles()) {
                                    LogInForm.this.userDropdown.addItem(profile.getName());
                                }

                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        LogInForm.this.usernameField.setEditable(false);
                                        LogInForm.this.passwordField.setEditable(false);
                                        LogInForm.this.userDropdownPanel.setVisible(true);
                                        LogInForm.this.popup.repack();
                                        LogInForm.this.popup.setCanLogIn(true);
                                        LogInForm.this.passwordField.setText(StringUtils.repeat('*', LogInForm.this.passwordField.getPassword().length));
                                    }
                                });
                            } else {
                                String uuid = "demo-" + LogInForm.this.authentication.getUsername();
                                authDatabase.register(uuid, LogInForm.this.authentication);
                                LogInForm.this.popup.setLoggedIn(uuid);
                            }
                        } else {
                            authDatabase.register(LogInForm.this.authentication.getSelectedProfile().getId(), LogInForm.this.authentication);
                            LogInForm.this.popup.setLoggedIn(LogInForm.this.authentication.getSelectedProfile().getId());
                        }
                    } catch (InvalidCredentialsException ex) {
                        LogInForm.this.popup.getLauncher().println(ex);
                        LogInForm.this.popup.getErrorForm().displayError(new String[]{"Sorry, but your username or password is incorrect!", "Please try again. If you need help, try the 'Forgot Password' link."});
                        LogInForm.this.popup.setCanLogIn(true);
                    } catch (AuthenticationException ex) {
                        LogInForm.this.popup.getLauncher().println(ex);
                        LogInForm.this.popup.getErrorForm().displayError(new String[]{"Sorry, but we couldn't connect to our servers.", "Please make sure that you are online and that Minecraft is not blocked."});
                        LogInForm.this.popup.setCanLogIn(true);
                    }
                }
            });
        }
    }
}