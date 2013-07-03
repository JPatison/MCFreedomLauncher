package net.minecraft.launcher.ui.sidebar.login;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.GameLauncher;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.GameProfile;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.updater.VersionManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class NotLoggedInForm extends BaseLogInForm
{
  private final JTextField usernameField = new JTextField(20);
  private final JPasswordField passwordField = new JPasswordField(20);
  private final JButton playButton = new JButton("Play");
  private final JButton registerButton = new JButton("Register");
  private final JCheckBox rememberMeCheckbox = new JCheckBox("Log me in automatically");

  public NotLoggedInForm(LoginContainerForm container) {
    super(container, "Log In");
    createInterface();
  }

  protected void populateGrid(GridBagConstraints constraints) {
    constraints.fill = 2;

    JLabel usernameLabel = new JLabel("Username:", 2);
    usernameLabel.setLabelFor(this.usernameField);
    add(usernameLabel, constraints, 0, 0, 0, 1);
    add(this.usernameField, constraints, 1, 0, 1, 0);

    JLabel passwordLabel = new JLabel("Password:", 2);
    passwordLabel.setLabelFor(this.passwordField);
    add(passwordLabel, constraints, 0, 1, 0, 1);
    add(this.passwordField, constraints, 1, 1, 1, 0);

    add(this.rememberMeCheckbox, constraints, 0, 2, 0, 2);

    this.playButton.addActionListener(this);
    this.usernameField.addActionListener(this);
    this.passwordField.addActionListener(this);
    this.registerButton.addActionListener(this);
    this.registerButton.setDefaultCapable(false);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new GridLayout(1, 2));
    buttonPanel.add(this.registerButton);
    buttonPanel.add(this.playButton);

    this.playButton.setFont(new Font(this.playButton.getFont().getName(), 1, this.playButton.getFont().getSize()));

    add(buttonPanel, constraints, 0, 3, 0, 0);
  }

  public JCheckBox getRememberMeCheckbox() {
    return this.rememberMeCheckbox;
  }

  public JTextField getUsernameField() {
    return this.usernameField;
  }

  public JPasswordField getPasswordField() {
    return this.passwordField;
  }

  public JButton getPlayButton() {
    return this.playButton;
  }

  public JButton getRegisterButton() {
    return this.registerButton;
  }

  public void checkLoginState()
  {
    boolean canLogIn = true;
    Profile profile = getLauncher().getProfileManager().getSelectedProfile();

    if (getLauncher().getGameLauncher().isWorking()) canLogIn = false;
    if (getLauncher().getVersionManager().getVersions(profile.getVersionFilter()).size() <= 0) canLogIn = false;

    this.playButton.setEnabled(canLogIn);
  }

  public void actionPerformed(ActionEvent e)
  {
    AuthenticationService authentication = getLauncher().getProfileManager().getSelectedProfile().getAuthentication();

    if ((e.getSource() == this.playButton) || (e.getSource() == this.usernameField) || (e.getSource() == this.passwordField)) {
      if ((authentication.isLoggedIn()) && ((ArrayUtils.isEmpty(authentication.getAvailableProfiles())) || (authentication.getSelectedProfile() != null)))
        getLauncher().getGameLauncher().playGame();
      else
        tryLogIn(true, true);
    }
    else
      try {
        OperatingSystem.openLink(new URI("https://account.mojang.com/register"));
      }
      catch (URISyntaxException localURISyntaxException)
      {
      }
  }

  public void onProfilesRefreshed(ProfileManager manager) {
    Profile profile = manager.getSelectedProfile();
    AuthenticationService authentication = profile.getAuthentication();

    this.rememberMeCheckbox.setSelected(profile.getAuthentication().shouldRememberMe());

    if ((authentication.isLoggedIn()) && (authentication.canPlayOnline())) {
      checkLoginState();
    } else if (!StringUtils.isBlank(authentication.getUsername())) {
      getUsernameField().setText(authentication.getUsername());
      String password = authentication.guessPasswordFromSillyOldFormat(new File(getLauncher().getWorkingDirectory(), "lastlogin"));

      if (!StringUtils.isBlank(password)) {
        getLauncher().println("Going to log in with legacy stored username & password...");

        getPasswordField().setText(password);
      }

      authentication.setPassword(String.valueOf(this.passwordField.getPassword()));

      if (authentication.canLogIn())
        tryLogIn(false, false);
    }
    else {
      getUsernameField().setText("");
      getPasswordField().setText("");
    }
  }

  public void tryLogIn(final boolean launchOnSuccess, final boolean verbose) {
    final Profile profile = getLauncher().getProfileManager().getSelectedProfile();
    final AuthenticationService authentication = profile.getAuthentication();

    getLoginContainer().checkLoginState();
    authentication.setRememberMe(this.rememberMeCheckbox.isSelected());

    getLauncher().getVersionManager().getExecutorService().submit(new Runnable()
    {
      public void run() {
        String username = NotLoggedInForm.this.usernameField.getText();
        try
        {
          Launcher.getInstance().println("Trying to log in...");

          authentication.setUsername(username);
          authentication.setPassword(String.valueOf(NotLoggedInForm.this.getPasswordField().getPassword()));
          authentication.logIn();

          if (!NotLoggedInForm.this.getLauncher().getProfileManager().getSelectedProfile().equals(profile)) {
            NotLoggedInForm.this.getLauncher().println("Profile changed during authentication, ignoring response.");
            NotLoggedInForm.this.getLoginContainer().checkLoginState();
            return;
          }

          NotLoggedInForm.this.getLauncher().println("Logged in successfully");
          NotLoggedInForm.this.saveAuthenticationDetails();

          if (launchOnSuccess)
            NotLoggedInForm.this.getLauncher().getGameLauncher().playGame();
          else
            NotLoggedInForm.this.getLoginContainer().checkLoginState();
        }
        catch (Throwable ex) {
          if (!NotLoggedInForm.this.getLauncher().getProfileManager().getSelectedProfile().equals(profile)) {
            NotLoggedInForm.this.getLauncher().println("Profile changed during authentication, ignoring response (which was an error anyway).");
            NotLoggedInForm.this.getLoginContainer().checkLoginState();
            return;
          }

          if (authentication.isLoggedIn()) {
            NotLoggedInForm.this.getLauncher().println("Couldn't go online", ex);

            if (authentication.getSelectedProfile() != null)
              Launcher.getInstance().println("Going to play offline as '" + authentication.getSelectedProfile().getName() + "'...");
            else {
              Launcher.getInstance().println("Going to play offline demo...");
            }

            NotLoggedInForm.this.getLoginContainer().checkLoginState();
          } else {
            NotLoggedInForm.this.loginFailed(ex.getMessage(), verbose, authentication.getUsername().contains("@"));
          }

          NotLoggedInForm.this.saveAuthenticationDetails();
        }
      }
    });
  }

  private void loginFailed(final String error, final boolean verbose, final boolean mojangAccount) {
    Launcher.getInstance().println("Could not log in: " + error);

    SwingUtilities.invokeLater(new Runnable()
    {
      public void run() {
        if (verbose)
        {
          String url = mojangAccount ? "https://account.mojang.com/resetpassword/request" : "https://minecraft.net/resetpassword";
          String errorMessage = "";
          String[] buttons;
          if (StringUtils.containsIgnoreCase(error, "migrated"))
          {
            errorMessage = errorMessage + "Your account has been migrated to a Mojang account.";
            errorMessage = errorMessage + "\nTo log in, please use your email address and not your minecraft name.";
            String[] buttons = { "Need help?", "Okay" };
            url = "http://help.mojang.com/customer/portal/articles/1205055-minecraft-launcher-error---migrated-account";
          } else {
            errorMessage = errorMessage + "Sorry, but your username or password is incorrect!";
            errorMessage = errorMessage + "\nPlease try again, and check your Caps Lock key is not turned on.";
            errorMessage = errorMessage + "\nIf you're having trouble, try the 'Forgot Password' button or visit help.mojang.com";
            buttons = new String[] { "Forgot Password", "Okay" };
          }

          int result = JOptionPane.showOptionDialog(NotLoggedInForm.this.getLauncher().getFrame(), errorMessage, "Could not log in", 0, 0, null, buttons, buttons[0]);

          if (result == 0) {
            try {
              OperatingSystem.openLink(new URI(url));
            } catch (URISyntaxException e) {
              NotLoggedInForm.this.getLauncher().println("Couldn't open help link. Please visit " + url + " manually.", e);
            }
          }
        }

        NotLoggedInForm.this.getLoginContainer().checkLoginState();
      }
    });
  }
}