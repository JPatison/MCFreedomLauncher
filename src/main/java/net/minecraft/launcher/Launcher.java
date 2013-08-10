
package net.minecraft.launcher;

import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.authentication.OldAuthentication;
import net.minecraft.launcher.authentication.SPAuthenticationService;
import net.minecraft.launcher.authentication.exceptions.AuthenticationException;
import net.minecraft.launcher.authentication.exceptions.InvalidCredentialsException;
import net.minecraft.launcher.authentication.yggdrasil.YggdrasilAuthenticationService;
import net.minecraft.launcher.locale.LocaleHelper;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.profile.ProfileManager;
import net.minecraft.launcher.ui.LauncherPanel;
import net.minecraft.launcher.ui.popups.login.LogInPopup;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.RemoteVersionList;
import net.minecraft.launcher.updater.VersionManager;
import net.minecraft.launcher.updater.download.DownloadJob;
import org.hopto.energy.InstallDirSettings;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.*;
import java.util.List;

public class Launcher {
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
    private final OldAuthentication authentication;
    private UUID clientToken = UUID.randomUUID();
    private Locale locale;
    private static boolean SPMode = true;

    public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args) {
        this(frame, workingDirectory, proxy, proxyAuth, args, Integer.valueOf(0));
    }

    public Launcher(JFrame frame, File workingDirectory, Proxy proxy, PasswordAuthentication proxyAuth, String[] args, Integer bootstrapVersion) {
        // this.locale=new Locale("en","US");
        //LocaleHelper.setCurrentLocale(this.locale);

        this.bootstrapVersion = bootstrapVersion;
        instance = this;
        setLookAndFeel();

        this.proxy = proxy;
        this.proxyAuth = proxyAuth;
        this.additionalArgs = args;
        this.workingDirectory = InstallDirSettings.loadAtStartup(frame, workingDirectory);
        this.frame = frame;
        this.gameLauncher = new GameLauncher(this);
        this.profileManager = new ProfileManager(this);
        this.versionManager = new VersionManager(new LocalVersionList(this.workingDirectory), new RemoteVersionList(proxy));
        this.launcherPanel = new LauncherPanel(this);
        this.authentication = new OldAuthentication(this, proxy);
        //  this.locale=this.profileManager.getSelectedProfile().getLocale();
        this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);


        initializeFrame();

        for (String line : delayedSysout) {
            this.launcherPanel.getTabPanel().getConsole().print(line + "\n");
        }

        downloadResources();
        //refreshProfiles();
        //refreshVersions();
        refreshVersionsAndProfiles();

        println("Launcher "+LauncherConstants.VERSION_NAME+" (through bootstrap " + bootstrapVersion + ") started on " + OperatingSystem.getCurrentPlatform().getName() + "...");
        println("Current time is " + DateFormat.getDateTimeInstance(2, 2, Locale.US).format(new Date()));
        println("Current Locale is " + LocaleHelper.getCurrentLocale());
        if (!OperatingSystem.getCurrentPlatform().isSupported()) {
            println("This operating system is unknown or unsupported, we cannot guarantee that the game will launch.");
        }
        println("System.getProperty('os.name') == '" + System.getProperty("os.name") + "'");
        println("System.getProperty('os.version') == '" + System.getProperty("os.version") + "'");
        println("System.getProperty('os.arch') == '" + System.getProperty("os.arch") + "'");
        println("System.getProperty('java.version') == '" + System.getProperty("java.version") + "'");
        println("System.getProperty('java.vendor') == '" + System.getProperty("java.vendor") + "'");
    }

    public static void setLookAndFeel() {
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
        try {
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

    public static Launcher getInstance() {
        return instance;
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

    private void downloadResources() {
        final DownloadJob job = new DownloadJob("Resources", true, this.gameLauncher);
        this.gameLauncher.addJob(job);
        this.versionManager.getExecutorService().submit(new Runnable() {
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

    public void refreshVersionsAndProfiles() {
        this.versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    Launcher.this.versionManager.refreshVersions();
                } catch (Throwable e) {
                    Launcher.getInstance().println("Unexpected exception refreshing version list", e);
                }
                try {
                    Launcher.this.profileManager.loadProfiles();
                    Launcher.this.println("Loaded " + Launcher.this.profileManager.getProfiles().size() + " profile(s); selected '" + Launcher.this.profileManager.getSelectedProfile().getName() + "'");
                } catch (Throwable e) {
                    Launcher.getInstance().println("Unexpected exception refreshing profile list", e);
                }

                Launcher.this.ensureLoggedIn();
            }
        });
    }

   /* public void refreshVersions() {
        this.versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    Launcher.this.versionManager.refreshVersions();
                } catch (Throwable e) {
                    Launcher.getInstance().println("Unexpected exception refreshing version list", e);
                }
            }
        });
    }*/

    /*public void refreshProfiles() {
        this.versionManager.getExecutorService().submit(new Runnable() {
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
    }*/

    /*public void refreshProfiles() {
        this.versionManager.getExecutorService().submit(new Runnable() {
            public void run() {
                try {
                    if (!Launcher.this.profileManager.loadProfiles()) {
                        OldAuthentication.StoredDetails storedDetails=   Launcher.this.authentication.getStoredDetails();
                        
                        if (storedDetails != null) {
                            storedDetails = new OldAuthentication.StoredDetails(storedDetails.getUsername(), null, storedDetails.getDisplayName(), storedDetails.getUUID());
                            Profile profile = Launcher.this.profileManager.getSelectedProfile();
                            
                           

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
    }*/

    public void ensureLoggedIn() {
        Profile selectedProfile = this.profileManager.getSelectedProfile();
        AuthenticationService auth = this.profileManager.getAuthDatabase().getByUUID(selectedProfile.getPlayerUUID());

        if (auth == null)
            showLoginPrompt();
        else if (!auth.isLoggedIn()) {
            if (auth.canLogIn())
                try {
                    auth.logIn();
                    try {
                        this.profileManager.saveProfiles();
                    } catch (IOException e) {
                        println("Couldn't save profiles after refreshing auth!", e);
                    }
                    this.profileManager.fireRefreshEvent();
                } catch (AuthenticationException e) {
                    println(e);
                    showLoginPrompt();
                }
            else
                showLoginPrompt();
        } else if (!auth.canPlayOnline())
            try {
                println("Refreshing auth...");
                auth.logIn();
                try {
                    this.profileManager.saveProfiles();
                } catch (IOException e) {
                    println("Couldn't save profiles after refreshing auth!", e);
                }
                this.profileManager.fireRefreshEvent();
            } catch (InvalidCredentialsException e) {
                println(e);
                showLoginPrompt();
            } catch (AuthenticationException e) {
                println(e);
            }
    }

    public void showLoginPrompt() {
        try {
            this.profileManager.saveProfiles();
        } catch (IOException e) {
            println("Couldn't save profiles before logging in!", e);
        }

        for (Profile profile : this.profileManager.getProfiles().values()) {
            Map credentials = profile.getAuthentication();

            if (credentials != null) {
                AuthenticationService auth = SPMode ? new SPAuthenticationService() : new YggdrasilAuthenticationService();
                auth.loadFromStorage(credentials);

                if (auth.isLoggedIn()) {
                    String uuid = auth.getSelectedProfile() == null ? "demo-" + auth.getUsername() : auth.getSelectedProfile().getId();
                    if (this.profileManager.getAuthDatabase().getByUUID(uuid) == null) {
                        this.profileManager.getAuthDatabase().register(uuid, auth);
                    }
                }

                profile.setAuthentication(null);
            }
        }

        final Profile selectedProfile = this.profileManager.getSelectedProfile();
        LogInPopup.showLoginPrompt(this, new LogInPopup.Callback() {
            public void onLogIn(String uuid) {
                AuthenticationService auth = Launcher.this.profileManager.getAuthDatabase().getByUUID(uuid);
                selectedProfile.setPlayerUUID(uuid);

                if ((selectedProfile.getName().equals("(Default)")) && (auth.getSelectedProfile() != null)) {
                    String playerName = auth.getSelectedProfile().getName();
                    String profileName = auth.getSelectedProfile().getName();
                    int count = 1;

                    while (Launcher.this.profileManager.getProfiles().containsKey(profileName)) {
                        profileName = playerName + " " + ++count;
                    }

                    Profile newProfile = new Profile(selectedProfile);
                    newProfile.setName(profileName);
                    Launcher.this.profileManager.getProfiles().put(profileName, newProfile);
                    Launcher.this.profileManager.getProfiles().remove("(Default)");
                    Launcher.this.profileManager.setSelectedProfile(profileName);
                }
                try {
                    Launcher.this.profileManager.saveProfiles();
                } catch (IOException e) {
                    Launcher.this.println("Couldn't save profiles after logging in!", e);
                }

                if (uuid == null)
                    Launcher.this.closeLauncher();
                else {
                    Launcher.this.profileManager.fireRefreshEvent();
                }

                Launcher.this.launcherPanel.setCard("launcher", null);
            }
        });
    }

    public void closeLauncher() {
        this.frame.dispatchEvent(new WindowEvent(this.frame, 201));
    }

    protected void initializeFrame() {
        this.frame.getContentPane().removeAll();
        this.frame.setTitle("Minecraft Freedom Launcher "+LauncherConstants.VERSION_NAME+" [modified by Energy]( Inspired by Sparamoule's Minecraft Open Launcher)");
        this.frame.setPreferredSize(new Dimension(1100, 525));
        this.frame.setDefaultCloseOperation(2);

        this.frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Launcher.this.frame.setVisible(false);
                Launcher.this.frame.dispose();
                Launcher.this.versionManager.getExecutorService().shutdown();
            }
        });
        try {
            InputStream in = Launcher.class.getResourceAsStream("/favicon.png");
            if (in != null)
                this.frame.setIconImage(ImageIO.read(in));
        } catch (IOException localIOException) {
        }
        this.frame.add(this.launcherPanel);

        this.frame.pack();
        this.frame.setVisible(true);
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
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

    public void println(String line, Throwable throwable) {
        println(line);
        println(throwable);
    }

    public void println(Throwable throwable) {
        StringWriter writer = null;
        PrintWriter printWriter = null;
        String result = throwable.toString();
        try {
            writer = new StringWriter();
            printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);
            result = writer.toString();
        } finally {
            try {
                if (writer != null) writer.close();
                if (printWriter != null) printWriter.close();
            } catch (IOException localIOException1) {
            }
        }
        println(result);
    }

    public int getBootstrapVersion() {
        return this.bootstrapVersion.intValue();
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

    public OldAuthentication getAuthentication() {
        return this.authentication;
    }

    public static boolean isSPMode() {
        return SPMode;
    }

    public static void setSPMode(boolean SPMode) {
        Launcher.SPMode = SPMode;
    }

    public static void setInstance(Launcher instance) {
        Launcher.instance = instance;
    }
}

