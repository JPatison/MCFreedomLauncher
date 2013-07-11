package net.minecraft.launcher.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.SwingUtilities;
import net.minecraft.launcher.Launcher;
import net.minecraft.launcher.authentication.AuthenticationSerializer;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.LegacyAuthenticationService;
import net.minecraft.launcher.events.RefreshedProfilesListener;
import net.minecraft.launcher.updater.DateTypeAdapter;
import net.minecraft.launcher.updater.FileTypeAdapter;
import net.minecraft.launcher.updater.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.FileUtils;

public class ProfileManager
{
  public static final String DEFAULT_PROFILE_NAME = "(Default)";
  private final Launcher launcher;
  private final Gson gson;
  private final Map<String, Profile> profiles = new HashMap();
  private final File profileFile;
  private final List<RefreshedProfilesListener> refreshedProfilesListeners = Collections.synchronizedList(new ArrayList());
  private String selectedProfile;

  public ProfileManager(Launcher launcher)
  {
    this.launcher = launcher;
    this.profileFile = new File(launcher.getWorkingDirectory(), "launcher_profiles.json");

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
    builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
    builder.registerTypeAdapter(File.class, new FileTypeAdapter());
    builder.registerTypeAdapter(AuthenticationService.class, new AuthenticationSerializer());
    builder.registerTypeAdapter(LegacyAuthenticationService.class, new AuthenticationSerializer());
    builder.enableComplexMapKeySerialization();
    builder.setPrettyPrinting();
    this.gson = builder.create();
  }

  public void saveProfiles() throws IOException {
    RawProfileList rawProfileList = new RawProfileList(null);
    rawProfileList.profiles = this.profiles;
    rawProfileList.selectedProfile = getSelectedProfile().getName();
    rawProfileList.clientToken = this.launcher.getClientToken();

    FileUtils.writeStringToFile(this.profileFile, this.gson.toJson(rawProfileList));
  }

  public boolean loadProfiles() throws IOException {
    this.profiles.clear();
    this.selectedProfile = null;

    if (this.profileFile.isFile()) {
      RawProfileList rawProfileList = (RawProfileList)this.gson.fromJson(FileUtils.readFileToString(this.profileFile), RawProfileList.class);

      this.profiles.putAll(rawProfileList.profiles);
      this.selectedProfile = rawProfileList.selectedProfile;
      this.launcher.setClientToken(rawProfileList.clientToken);

      fireRefreshEvent();
      return true;
    }
    fireRefreshEvent();
    return false;
  }

  public void fireRefreshEvent()
  {
    final List listeners = new ArrayList(this.refreshedProfilesListeners);
    for (Iterator iterator = listeners.iterator(); iterator.hasNext(); ) {
      RefreshedProfilesListener listener = (RefreshedProfilesListener)iterator.next();

      if (!listener.shouldReceiveEventsInUIThread()) {
        listener.onProfilesRefreshed(this);
        iterator.remove();
      }
    }

    if (!listeners.isEmpty())
      SwingUtilities.invokeLater(new Runnable()
      {
        public void run() {
          for (RefreshedProfilesListener listener : listeners)
            listener.onProfilesRefreshed(ProfileManager.this);
        }
      });
  }

  public Profile getSelectedProfile()
  {
    if ((this.selectedProfile == null) || (!this.profiles.containsKey(this.selectedProfile))) {
      if (this.profiles.get("(Default)") != null) {
        this.selectedProfile = "(Default)";
      } else if (this.profiles.size() > 0) {
        this.selectedProfile = ((Profile)this.profiles.values().iterator().next()).getName();
      } else {
        this.selectedProfile = "(Default)";
        this.profiles.put("(Default)", new Profile(this.selectedProfile));
      }
    }

    return (Profile)this.profiles.get(this.selectedProfile);
  }

  public Map<String, Profile> getProfiles() {
    return this.profiles;
  }

  public Launcher getLauncher() {
    return this.launcher;
  }

  public void addRefreshedProfilesListener(RefreshedProfilesListener listener) {
    this.refreshedProfilesListeners.add(listener);
  }

  public void setSelectedProfile(String selectedProfile) {
    boolean update = !this.selectedProfile.equals(selectedProfile);
    this.selectedProfile = selectedProfile;

    if (update)
      fireRefreshEvent();
  }

  private static class RawProfileList
  {
    public Map<String, Profile> profiles = new HashMap();
    public String selectedProfile;
    public UUID clientToken = UUID.randomUUID();
  }
}