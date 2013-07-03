package net.minecraft.launcher.ui.sidebar.login;

import java.awt.event.ActionListener;
import java.io.IOException;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.events.RefreshedVersionsListener;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.sidebar.SidebarGridForm;
import net.minecraft.launcher.updater.VersionManager;

public abstract class BaseLogInForm extends SidebarGridForm
  implements ActionListener, RefreshedProfilesListener, RefreshedVersionsListener
{
  private final LoginContainerForm container;
  private final Launcher launcher;

  public BaseLogInForm(LoginContainerForm container, String name)
  {
    super(name);
    this.container = container;
    this.launcher = container.getLauncher();
    this.launcher.getVersionManager().addRefreshedVersionsListener(this);
    this.launcher.getProfileManager().addRefreshedProfilesListener(this);
  }

  public abstract void checkLoginState();

  public void onVersionsRefreshed(VersionManager manager)
  {
    checkLoginState();
  }

  public boolean shouldReceiveEventsInUIThread()
  {
    return true;
  }

  public LoginContainerForm getLoginContainer() {
    return this.container;
  }

  public Launcher getLauncher() {
    return this.launcher;
  }

  protected void saveAuthenticationDetails() {
    try {
      getLauncher().getProfileManager().saveProfiles();
    } catch (IOException e) {
      getLauncher().println("Couldn't save authentication details to profile", e);
    }
  }
}