package net.minecraft.launcher;

import net.minecraft.launcher.authentication.AuthenticationService;
import net.minecraft.launcher.process.JavaProcess;
import net.minecraft.launcher.process.JavaProcessLauncher;
import net.minecraft.launcher.process.JavaProcessRunnable;
import net.minecraft.launcher.profile.Profile;
import net.minecraft.launcher.ui.tabs.CrashReportTab;
import net.minecraft.launcher.updater.LocalVersionList;
import net.minecraft.launcher.updater.VersionList;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.updater.download.DownloadJob;
import net.minecraft.launcher.updater.download.DownloadListener;
import net.minecraft.launcher.updater.download.Downloadable;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.ExtractRules;
import net.minecraft.launcher.versions.Library;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.*;
import org.apache.commons.lang3.text.StrSubstitutor;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class GameLauncher
        implements JavaProcessRunnable, DownloadListener {
    private final Object lock = new Object();
    private final Launcher launcher;
    private final List<DownloadJob> jobs = new ArrayList();
    private CompleteVersion version;
    private boolean isWorking;
    private File nativeDir;

    public GameLauncher(Launcher launcher) {
        this.launcher = launcher;
    }

    private void setWorking(boolean working) {
        synchronized (this.lock) {
            if (this.nativeDir != null) {
                Launcher.getInstance().println("Deleting " + this.nativeDir);
                if ((!this.nativeDir.isDirectory()) || (FileUtils.deleteQuietly(this.nativeDir))) {
                    this.nativeDir = null;
                } else {
                    Launcher.getInstance().println("Couldn't delete " + this.nativeDir + " - scheduling for deletion upon exit");
                    try {
                        FileUtils.forceDeleteOnExit(this.nativeDir);
                    } catch (Throwable localThrowable) {
                    }
                }
            }
            this.isWorking = working;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GameLauncher.this.launcher.getLauncherPanel().getSidebar().getLoginForm().checkLoginState();
                }
            });
        }
    }

    public boolean isWorking() {
        return this.isWorking;
    }

    public void playGame() {
        synchronized (this.lock) {
            if (this.isWorking) {
                this.launcher.println("Tried to play game but game is already starting!");
                return;
            }

            setWorking(true);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GameLauncher.this.launcher.getLauncherPanel().getTabPanel().showConsole();
            }
        });
        this.launcher.println("Getting syncinfo for selected version");

        String lastVersionId = this.launcher.getProfileManager().getSelectedProfile().getLastVersionId();
        VersionSyncInfo syncInfo;

        if (lastVersionId != null)
            syncInfo = this.launcher.getVersionManager().getVersionSyncInfo(lastVersionId);
        else {
            syncInfo = (VersionSyncInfo) this.launcher.getVersionManager().getVersions().get(0);
        }

        if (syncInfo == null) {
            Launcher.getInstance().println("Tried to launch a version without a version being selected...");
            setWorking(false);
            return;
        }

        synchronized (this.lock) {
            this.launcher.println("Queueing library & version downloads");
            try {
                this.version = this.launcher.getVersionManager().getLatestCompleteVersion(syncInfo);
            } catch (IOException e) {
                Launcher.getInstance().println("Couldn't get complete version info for " + syncInfo.getLatestVersion(), e);
                setWorking(false);
                return;
            }

            if (this.version.getMinimumLauncherVersion() > 3) {
                Launcher.getInstance().println("An update to your launcher is available and is required to play " + this.version.getId() + ". Please restart your launcher.");
                setWorking(false);
                return;
            }

            if (!syncInfo.isInstalled()) {
                try {
                    VersionList localVersionList = this.launcher.getVersionManager().getLocalVersionList();
                    if ((localVersionList instanceof LocalVersionList)) {
                        ((LocalVersionList) localVersionList).saveVersion(this.version);
                        Launcher.getInstance().println("Installed " + syncInfo.getLatestVersion());
                    }
                } catch (IOException e) {
                    Launcher.getInstance().println("Couldn't save version info to install " + syncInfo.getLatestVersion(), e);
                    setWorking(false);
                    return;
                }
            }
            try {
                DownloadJob job = new DownloadJob("Version & Libraries", false, this);
                addJob(job);
                this.launcher.getVersionManager().downloadVersion(syncInfo, job);
                job.startDownloading(this.launcher.getVersionManager().getExecutorService());
            } catch (IOException e) {
                Launcher.getInstance().println("Couldn't get version info for " + syncInfo.getLatestVersion(), e);
                setWorking(false);
                return;
            }
        }
    }

    protected void launchGame() {
        this.launcher.println("Launching game");
        Profile selectedProfile = this.launcher.getProfileManager().getSelectedProfile();

        if (this.version == null) {
            Launcher.getInstance().println("Aborting launch; version is null?");
            return;
        }

        cleanOldNatives();

        this.nativeDir = new File(this.launcher.getWorkingDirectory(), "versions/" + this.version.getId() + "/" + this.version.getId() + "-natives-" + System.nanoTime());
        if (!this.nativeDir.isDirectory()) this.nativeDir.mkdirs();
        this.launcher.println("Unpacking natives to " + this.nativeDir);
        try {
            unpackNatives(this.version, this.nativeDir);
        } catch (IOException e) {
            Launcher.getInstance().println("Couldn't unpack natives!", e);
            return;
        }

        File gameDirectory = selectedProfile.getGameDir() == null ? this.launcher.getWorkingDirectory() : selectedProfile.getGameDir();
        Launcher.getInstance().println("Launching in " + gameDirectory);

        if (!gameDirectory.exists()) {
            if (!gameDirectory.mkdirs()) {
                Launcher.getInstance().println("Aborting launch; couldn't create game directory");
            }
        } else if (!gameDirectory.isDirectory()) {
            Launcher.getInstance().println("Aborting launch; game directory is not actually a directory");
            return;
        }

        JavaProcessLauncher processLauncher = new JavaProcessLauncher(selectedProfile.getJavaPath(), new String[0]);
        processLauncher.directory(gameDirectory);

        File assetsDirectory = new File(this.launcher.getWorkingDirectory(), "assets");

        if (OperatingSystem.getCurrentPlatform().equals(OperatingSystem.OSX)) {
            processLauncher.addCommands(new String[]{"-Xdock:icon=" + new File(assetsDirectory, "icons/minecraft.icns").getAbsolutePath(), "-Xdock:name=Minecraft"});
        }

        String profileArgs = selectedProfile.getJavaArgs();

        if (profileArgs != null)
            processLauncher.addSplitCommands(profileArgs);
        else {
            processLauncher.addSplitCommands("-Xmx1G");
        }

        processLauncher.addCommands(new String[]{"-Djava.library.path=" + this.nativeDir.getAbsolutePath()});
        processLauncher.addCommands(new String[]{"-cp", constructClassPath(this.version)});
        processLauncher.addCommands(new String[]{this.version.getMainClass()});

        String[] args = getMinecraftArguments(this.version, selectedProfile, gameDirectory, assetsDirectory);
        if (args == null) return;
        processLauncher.addCommands(args);

        Proxy proxy = this.launcher.getProxy();
        PasswordAuthentication proxyAuth = this.launcher.getProxyAuth();
        if (!proxy.equals(Proxy.NO_PROXY)) {
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            processLauncher.addCommands(new String[]{"--proxyHost", address.getHostName()});
            processLauncher.addCommands(new String[]{"--proxyPort", Integer.toString(address.getPort())});
            if (proxyAuth != null) {
                processLauncher.addCommands(new String[]{"--proxyUser", proxyAuth.getUserName()});
                processLauncher.addCommands(new String[]{"--proxyPass", new String(proxyAuth.getPassword())});
            }

        }

        processLauncher.addCommands(this.launcher.getAdditionalArgs());


        if (selectedProfile.getResolution() != null) {
            processLauncher.addCommands(new String[]{"--width", String.valueOf(selectedProfile.getResolution().getWidth())});
            processLauncher.addCommands(new String[]{"--height", String.valueOf(selectedProfile.getResolution().getHeight())});
        }
        try {
            List parts = processLauncher.getFullCommands();
            StringBuilder full = new StringBuilder();
            boolean first = true;

            for (String part : (List<String>) parts) {
                if (!first) full.append(" ");
                full.append(part);
                first = false;
            }

            Launcher.getInstance().println("Running " + full.toString());
            JavaProcess process = processLauncher.start();
            process.safeSetExitRunnable(this);
            Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
            Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
            Launcher.getInstance().println("---- YOU CAN CLOSE THIS LAUNCHER IF THE GAME STARTED OK ----");
            Launcher.getInstance().println("---- (We'll do this automatically later ;D) ----");
        } catch (IOException e) {
            Launcher.getInstance().println("Couldn't launch game", e);
            setWorking(false);
            return;
        }
    }

    private String[] getMinecraftArguments(CompleteVersion version, Profile selectedProfile, File gameDirectory, File assetsDirectory) {
        if (version.getMinecraftArguments() == null) {
            Launcher.getInstance().println("Can't run version, missing minecraftArguments");
            setWorking(false);
            return null;
        }

        Map map = new HashMap();
        StrSubstitutor substitutor = new StrSubstitutor(map);
        String[] split = version.getMinecraftArguments().split(" ");
        AuthenticationService authentication = selectedProfile.getAuthentication();

        map.put("auth_username", authentication.getUsername());
        map.put("auth_session", (authentication.getSessionToken() == null) && (authentication.canPlayOnline()) ? "-" : authentication.getSessionToken());

        if (authentication.getSelectedProfile() != null) {
            map.put("auth_player_name", authentication.getSelectedProfile().getName());
            map.put("auth_uuid", authentication.getSelectedProfile().getId());
        } else {
            map.put("auth_player_name", "Player");
            map.put("auth_uuid", new UUID(0L, 0L).toString());
        }

        map.put("profile_name", selectedProfile.getName());
        map.put("version_name", version.getId());

        map.put("game_directory", gameDirectory.getAbsolutePath());
        map.put("game_assets", assetsDirectory.getAbsolutePath());

        for (int i = 0; i < split.length; i++) {
            split[i] = substitutor.replace(split[i]);
            launcher.getInstance().println("[Custom Debug log] " + split[i]);

        }

        return split;
    }

    private void cleanOldNatives() {
        File root = new File(this.launcher.getWorkingDirectory(), "versions/");
        this.launcher.println("Looking for old natives to clean up...");
        IOFileFilter ageFilter = new AgeFileFilter(System.currentTimeMillis() - 3600L);

        for (File version : root.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY))
            for (File folder : version.listFiles((FileFilter) FileFilterUtils.and(new IOFileFilter[]{new PrefixFileFilter(version.getName() + "-natives-"), ageFilter}))) {
                Launcher.getInstance().println("Deleting " + folder);

                FileUtils.deleteQuietly(folder);
            }
    }

    private void unpackNatives(CompleteVersion version, File targetDir) throws IOException {
        OperatingSystem os = OperatingSystem.getCurrentPlatform();
        Collection libraries = version.getRelevantLibraries(os);

        for (Library library : (Collection<Library>) libraries) {
            Map nativesPerOs = library.getNatives();

            if ((nativesPerOs != null) && (nativesPerOs.get(os) != null)) {
                File file = new File(this.launcher.getWorkingDirectory(), library.getArtifactPath((String) nativesPerOs.get(os)));
                ZipFile zip = new ZipFile(file);
                ExtractRules extractRules = library.getExtractRules();
                try {
                    Enumeration entries = zip.entries();

                    while (entries.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry) entries.nextElement();

                        if ((extractRules == null) || (extractRules.shouldExtract(entry.getName()))) {
                            File targetFile = new File(targetDir, entry.getName());
                            if (targetFile.getParentFile() != null) targetFile.getParentFile().mkdirs();

                            if (!entry.isDirectory()) {
                                BufferedInputStream inputStream = new BufferedInputStream(zip.getInputStream(entry));

                                byte[] buffer = new byte[2048];
                                FileOutputStream outputStream = new FileOutputStream(targetFile);
                                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
                                try {
                                    int length;
                                    while ((length = inputStream.read(buffer, 0, buffer.length)) != -1)
                                        bufferedOutputStream.write(buffer, 0, length);
                                } finally {
                                    Downloadable.closeSilently(bufferedOutputStream);
                                    Downloadable.closeSilently(outputStream);
                                    Downloadable.closeSilently(inputStream);
                                }
                            }
                        }
                    }
                } finally {
                    zip.close();
                }
            }
        }
    }

    private String constructClassPath(CompleteVersion version) {
        StringBuilder result = new StringBuilder();
        Collection classPath = version.getClassPath(OperatingSystem.getCurrentPlatform(), this.launcher.getWorkingDirectory());
        String separator = System.getProperty("path.separator");

        for (File file : (Collection<File>) classPath) {
            if (!file.isFile()) throw new RuntimeException("Classpath file not found: " + file);
            if (result.length() > 0) result.append(separator);
            result.append(file.getAbsolutePath());
        }

        return result.toString();
    }

    public void onJavaProcessEnded(JavaProcess process) {
        int exitCode = process.getExitCode();

        if (exitCode == 0) {
            Launcher.getInstance().println("Game ended with no troubles detected (exit code " + exitCode + ")");
        } else {
            Launcher.getInstance().println("Game ended with bad state (exit code " + exitCode + ")");

            String errorText = null;
            String[] sysOut = (String[]) process.getSysOutLines().getItems();

            for (int i = sysOut.length - 1; i >= 0; i--) {
                String line = sysOut[i];
                String crashIdentifier = "#@!@#";
                int pos = line.lastIndexOf(crashIdentifier);

                if ((pos >= 0) && (pos < line.length() - crashIdentifier.length() - 1)) {
                    errorText = line.substring(pos + crashIdentifier.length()).trim();
                    break;
                }
            }

            if (errorText != null) {
                File file = new File(errorText);

                if (file.isFile()) {
                    Launcher.getInstance().println("Crash report detected, opening: " + errorText);
                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (result.length() > 0) result.append("\n");
                            result.append(line);
                        }

                        reader.close();

                        this.launcher.getLauncherPanel().getTabPanel().setCrashReport(new CrashReportTab(this.version, file, result.toString()));
                    } catch (IOException e) {
                        Launcher.getInstance().println("Couldn't open crash report", e);
                    } finally {
                        Downloadable.closeSilently(inputStream);
                    }
                } else {
                    Launcher.getInstance().println("Crash report detected, but unknown format: " + errorText);
                }
            }
        }

        setWorking(false);
    }

    public void onDownloadJobFinished(DownloadJob job) {
        updateProgressBar();
        synchronized (this.lock) {
            if (job.getFailures() > 0) {
                this.launcher.println("Job '" + job.getName() + "' finished with " + job.getFailures() + " failure(s)!");
                setWorking(false);
            } else {
                this.launcher.println("Job '" + job.getName() + "' finished successfully");

                if ((isWorking()) && (!hasRemainingJobs()))
                    try {
                        launchGame();
                    } catch (Throwable ex) {
                        Launcher.getInstance().println("Fatal error launching game. Report this to http://mojang.atlassian.net please!", ex);
                    }
            }
        }
    }

    public void onDownloadJobProgressChanged(DownloadJob job) {
        updateProgressBar();
    }

    protected void updateProgressBar() {
        final float progress = getProgress();
        final boolean hasTasks = hasRemainingJobs();

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                GameLauncher.this.launcher.getLauncherPanel().getProgressBar().setVisible(hasTasks);
                GameLauncher.this.launcher.getLauncherPanel().getProgressBar().setValue((int) (progress * 100.0F));
            }
        });
    }

    protected float getProgress() {
        synchronized (this.lock) {
            float max = this.jobs.size();
            float result = 0.0F;

            for (DownloadJob job : this.jobs) {
                result += job.getProgress();
            }

            return result / max;
        }
    }

    public boolean hasRemainingJobs() {
        synchronized (this.lock) {
            for (DownloadJob job : this.jobs) {
                if (!job.isComplete()) return true;
            }
        }

        return false;
    }

    public void addJob(DownloadJob job) {
        synchronized (this.lock) {
            this.jobs.add(job);
        }
    }
}


