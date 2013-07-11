package net.minecraft.launcher;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.LegacyAuthenticationService;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.LauncherPanel;
import net.minecraft.launcher.ui.tabs.ConsoleTab;
import net.minecraft.launcher.ui.tabs.LauncherTabPanel;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.download.DownloadJob;

public class Launcher
{
  private static Launcher instance;
  private static final List<String> delayedSysout = new ArrayList();
  private final VersionManager versionManager;
  private final JFrame frame;
  private final LauncherPanel launcherPanel;
  private final GameLauncher gameLauncher;
  private final File workingDirectory;
  private final Proxy proxy;
  private final PasswordAuthentication proxyAuth;
  private final String[] additionalArgs;
  private final Integer bootstrapVersion;
  private final ProfileManager profileManager;
  private UUID clientToken = UUID.randomUUID();

  public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args) {
    this(frame, workingDirectory, proxy, proxyAuth, args, Integer.valueOf(0));
  }

  public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args, Integer bootstrapVersion) {
    this.bootstrapVersion = bootstrapVersion;
    instance = this;
    setLookAndFeel();

    this.proxy = proxy;
    this.proxyAuth = proxyAuth;
    this.additionalArgs = args;
    this.workingDirectory = workingDirectory;
    this.frame = frame;
    this.gameLauncher = new GameLauncher(this);
    this.profileManager = new ProfileManager(this);
    this.versionManager = new VersionManager(new LocalVersionList(workingDirectory), new RemoteVersionList(proxy));
    this.launcherPanel = new LauncherPanel(this);

    initializeFrame();

    for (String line : delayedSysout) {
      this.launcherPanel.getTabPanel().getConsole().print(line + "\n");
    }

    if (bootstrapVersion.intValue() < 4) {
      showOutdatedNotice();
      return;
    }

    downloadResources();
    refreshProfiles();
    refreshVersions();

    println("Launcher 1.0.9 (through bootstrap " + bootstrapVersion + ") started on " + OperatingSystem.getCurrentPlatform().getName() + "...");
    println("Current time is " + DateFormat.getDateTimeInstance(2, 2, Locale.US).format(new Date()));

    if (!OperatingSystem.getCurrentPlatform().isSupported()) {
      println("This operating system is unknown or unsupported, we cannot guarantee that the game will launch.");
    }
    println("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
    println("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
    println("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
    println("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
    println("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
    println("System.getProperty('sun.arch.data.model') == '" + System.getProperty("sun.arch.data.model") + "'");
  }

  private void showOutdatedNotice() {
    String error = "Sorry, but your launcher is outdated! Please redownload it at https://mojang.com/2013/06/minecraft-1-6-pre-release/";

    this.frame.getContentPane().removeAll();

    int result = JOptionPane.showOptionDialog(this.frame, error, "Outdated launcher", 0, 0, null, LauncherConstants.BOOTSTRAP_OUT_OF_DATE_BUTTONS, LauncherConstants.BOOTSTRAP_OUT_OF_DATE_BUTTONS[0]);

    if (result == 0) {
      try {
        OperatingSystem.openLink(new URI("https://mojang.com/2013/06/minecraft-1-6-pre-release/"));
      } catch (URISyntaxException e) {
        println("Couldn't open bootstrap download link. Please visit https://mojang.com/2013/06/minecraft-1-6-pre-release/ manually.", e);
      }
    }
    this.frame.dispatchEvent(new WindowEvent(this.frame, 201));
  }

  private static void setLookAndFeel() {
    JFrame frame = new JFrame();
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Throwable ignored) {
      try {
        getInstance().println("Your java failed to provide normal look and feel, trying the old fallback now");
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Throwable t) {
        getInstance().println("Unexpected exception setting look and feel");
        t.printStackTrace();
      }
    }
    JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder("test"));
    frame.add(panel);
    try
    {
      frame.pack();
    } catch (Throwable t) {
      getInstance().println("Custom (broken) theme detected, falling back onto x-platform theme");
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Throwable ex) {
        getInstance().println("Unexpected exception setting look and feel", ex);
      }
    }

    frame.dispose();
  }

  private void downloadResources() {
    final DownloadJob job = new DownloadJob("Resources", true, this.gameLauncher);
    this.gameLauncher.addJob(job);
    this.versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          Launcher.this.versionManager.downloadResources(job);
          job.startDownloading(Launcher.this.versionManager.getExecutorService());
        } catch (IOException e) {
          Launcher.getInstance().println("Unexpected exception queueing resource downloads", e);
        }
      }
    });
  }

  public void refreshVersions() {
    this.versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          Launcher.this.versionManager.refreshVersions();
        } catch (Throwable e) {
          Launcher.getInstance().println("Unexpected exception refreshing version list", e);
        }
      }
    });
  }

  public void refreshProfiles() {
    this.versionManager.getExecutorService().submit(new Runnable()
    {
      public void run() {
        try {
          if (!Launcher.this.profileManager.loadProfiles()) {
            String[] storedDetails = LegacyAuthenticationService.getStoredDetails(new File(Launcher.this.getWorkingDirectory(), "lastlogin"));

            if (storedDetails != null) {
              Profile profile = Launcher.this.profileManager.getSelectedProfile();

              profile.getAuthentication().setUsername(storedDetails[0]);
              profile.getAuthentication().setPassword(storedDetails[1]);

              Launcher.this.profileManager.saveProfiles();
              Launcher.this.profileManager.fireRefreshEvent();

              Launcher.this.println("Initialized default profile with old lastlogin details");
            } else {
              Launcher.this.println("Created default profile with no authentication details");
            }
          } else {
            Launcher.this.println("Loaded " + Launcher.this.profileManager.getProfiles().size() + " profile(s); selected '" + Launcher.this.profileManager.getSelectedProfile().getName() + "'");
          }
        } catch (Throwable e) {
          Launcher.getInstance().println("Unexpected exception refreshing profile list", e);
        }
      }
    });
  }

  protected void initializeFrame() {
    this.frame.getContentPane().removeAll();
    this.frame.setTitle("Minecraft Launcher 1.0.9");
    this.frame.setPreferredSize(new Dimension(925, 525));
    this.frame.setDefaultCloseOperation(2);

    this.frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e) {
        Launcher.this.frame.setVisible(false);
        Launcher.this.frame.dispose();
        Launcher.this.versionManager.getExecutorService().shutdown();
      }
    });
    try
    {
      InputStream in = Launcher.class.getResourceAsStream("/favicon.png");
      if (in != null)
        this.frame.setIconImage(ImageIO.read(in));
    }
    catch (IOException localIOException)
    {
    }
    this.frame.add(this.launcherPanel);

    this.frame.pack();
    this.frame.setVisible(true);
  }

  public VersionManager getVersionManager() {
    return this.versionManager;
  }

  public JFrame getFrame() {
    return this.frame;
  }

  public LauncherPanel getLauncherPanel() {
    return this.launcherPanel;
  }

  public GameLauncher getGameLauncher() {
    return this.gameLauncher;
  }

  public File getWorkingDirectory() {
    return this.workingDirectory;
  }

  public Proxy getProxy() {
    return this.proxy;
  }

  public PasswordAuthentication getProxyAuth() {
    return this.proxyAuth;
  }

  public String[] getAdditionalArgs() {
    return this.additionalArgs;
  }

  public void println(String line) {
    System.out.println(line);

    if (this.launcherPanel == null)
      delayedSysout.add(line);
    else
      this.launcherPanel.getTabPanel().getConsole().print(line + "\n");
  }

  public void println(String line, Throwable throwable)
  {
    println(line);
    println(throwable);
  }

  public void println(Throwable throwable) {
    StringWriter writer = null;
    PrintWriter printWriter = null;
    String result = throwable.toString();
    try
    {
      writer = new StringWriter();
      printWriter = new PrintWriter(writer);
      throwable.printStackTrace(printWriter);
      result = writer.toString();
    } finally {
      try {
        if (writer != null) writer.close();
        if (printWriter != null) printWriter.close(); 
      }
      catch (IOException localIOException1) {  }

    }
    println(result);
  }

  public int getBootstrapVersion() {
    return this.bootstrapVersion.intValue();
  }

  public static Launcher getInstance() {
    return instance;
  }

  public ProfileManager getProfileManager() {
    return this.profileManager;
  }

  public UUID getClientToken() {
    return this.clientToken;
  }

  public void setClientToken(UUID clientToken) {
    this.clientToken = clientToken;
  }
}