package net.minecraft.launcher.authentication;

import java.io.File;
import java.util.Map;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.launcher.events.AuthenticationChangedListener;

public class LoadTestingAuthenticationService
  implements AuthenticationService
{
  private final AuthenticationService primary = new LegacyAuthenticationService();
  private final AuthenticationService secondary = new YggdrasilAuthenticationService();

  public void logIn() throws AuthenticationException
  {
    this.primary.logIn();
    try
    {
      this.secondary.logIn();
    } catch (AuthenticationException e) {
      Launcher.getInstance().println("Couldn't load-test new authentication service (method: logIn)", e);
    }
  }

  public boolean canLogIn()
  {
    return this.primary.canLogIn();
  }

  public void logOut()
  {
    this.primary.logOut();
    this.secondary.logOut();
  }

  public boolean isLoggedIn()
  {
    return this.primary.isLoggedIn();
  }

  public boolean canPlayOnline()
  {
    return this.primary.canPlayOnline();
  }

  public GameProfile[] getAvailableProfiles()
  {
    return this.primary.getAvailableProfiles();
  }

  public GameProfile getSelectedProfile()
  {
    return this.primary.getSelectedProfile();
  }

  public void selectGameProfile(GameProfile profile) throws AuthenticationException
  {
    this.primary.selectGameProfile(profile);
    try
    {
      this.secondary.selectGameProfile(profile);
    } catch (AuthenticationException e) {
      Launcher.getInstance().println("Couldn't load-test new authentication service (method: selectGameProfile)", e);
    }
  }

  public void loadFromStorage(Map<String, String> credentials)
  {
    this.primary.loadFromStorage(credentials);
    this.secondary.loadFromStorage(credentials);
  }

  public Map<String, String> saveForStorage()
  {
    return this.primary.saveForStorage();
  }

  public String getSessionToken()
  {
    return this.primary.getSessionToken();
  }

  public String getUsername()
  {
    return this.primary.getUsername();
  }

  public void setUsername(String username)
  {
    this.primary.setUsername(username);
    this.secondary.setUsername(username);
  }

  public void setPassword(String password)
  {
    this.primary.setPassword(password);
    this.secondary.setPassword(password);
  }

  public void addAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    this.primary.addAuthenticationChangedListener(listener);
  }

  public void removeAuthenticationChangedListener(AuthenticationChangedListener listener)
  {
    this.primary.removeAuthenticationChangedListener(listener);
  }

  public String guessPasswordFromSillyOldFormat(File lastlogin)
  {
    return this.primary.guessPasswordFromSillyOldFormat(lastlogin);
  }

  public void setRememberMe(boolean rememberMe)
  {
    this.primary.setRememberMe(rememberMe);
    this.secondary.setRememberMe(rememberMe);
  }

  public boolean shouldRememberMe()
  {
    return this.primary.shouldRememberMe();
  }
}