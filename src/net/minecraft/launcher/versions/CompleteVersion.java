package net.minecraft.launcher.versions;

import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.updater.download.Downloadable;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.*;

public class CompleteVersion
        implements Version {
    private String id;
    private Date time;
    private Date releaseTime;
    private ReleaseType type;
    private String minecraftArguments;
    private List<Library> libraries;
    private String mainClass;
    private int minimumLauncherVersion;

    public CompleteVersion() {
    }

    public CompleteVersion(String id, Date releaseTime, Date updateTime, ReleaseType type, String mainClass, String minecraftArguments) {
        if ((id == null) || (id.length() == 0)) throw new IllegalArgumentException("ID cannot be null or empty");
        if (releaseTime == null) throw new IllegalArgumentException("Release time cannot be null");
        if (updateTime == null) throw new IllegalArgumentException("Update time cannot be null");
        if (type == null) throw new IllegalArgumentException("Release type cannot be null");
        if ((mainClass == null) || (mainClass.length() == 0))
            throw new IllegalArgumentException("Main class cannot be null or empty");
        if (minecraftArguments == null) throw new IllegalArgumentException("Process arguments cannot be null or empty");

        this.id = id;
        this.releaseTime = releaseTime;
        this.time = updateTime;
        this.type = type;
        this.mainClass = mainClass;
        this.libraries = new ArrayList();
        this.minecraftArguments = minecraftArguments;
    }

    public CompleteVersion(CompleteVersion version) {
        this(version.getId(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), version.getMainClass(), version.getMinecraftArguments());
    }

    public CompleteVersion(Version version, String mainClass, String minecraftArguments) {
        this(version.getId(), version.getReleaseTime(), version.getUpdatedTime(), version.getType(), mainClass, minecraftArguments);
    }

    public String getId() {
        return this.id;
    }

    public ReleaseType getType() {
        return this.type;
    }

    public Date getUpdatedTime() {
        return this.time;
    }

    public Date getReleaseTime() {
        return this.releaseTime;
    }

    public Collection<Library> getLibraries() {
        return this.libraries;
    }

    public String getMainClass() {
        return this.mainClass;
    }

    public void setUpdatedTime(Date time) {
        if (time == null) throw new IllegalArgumentException("Time cannot be null");
        this.time = time;
    }

    public void setReleaseTime(Date time) {
        if (time == null) throw new IllegalArgumentException("Time cannot be null");
        this.releaseTime = time;
    }

    public void setType(ReleaseType type) {
        if (type == null) throw new IllegalArgumentException("Release type cannot be null");
        this.type = type;
    }

    public void setMainClass(String mainClass) {
        if ((mainClass == null) || (mainClass.length() == 0))
            throw new IllegalArgumentException("Main class cannot be null or empty");
        this.mainClass = mainClass;
    }

    public Collection<Library> getRelevantLibraries(OperatingSystem os) {
        List result = new ArrayList();

        for (Library library : this.libraries) {
            List restrictedOperatingSystems = library.getRestrictedOperatingSystems();
            if ((restrictedOperatingSystems == null) || (restrictedOperatingSystems.contains(os))) {
                result.add(library);
            }
        }

        return result;
    }

    public Collection<File> getClassPath(OperatingSystem os, File base) {
        Collection libraries = getRelevantLibraries(os);
        Collection result = new ArrayList();

        for (Library library : (Collection<Library>) libraries) {
            if (library.getNatives() == null) {
                result.add(new File(base, library.getArtifactPath()));
            }
        }

        result.add(new File(base, "versions/" + getId() + "/" + getId() + ".jar"));

        return result;
    }

    public Collection<String> getExtractFiles(OperatingSystem os) {
        Collection libraries = getRelevantLibraries(os);
        Collection result = new ArrayList();

        for (Library library : (Collection<Library>) libraries) {
            Map natives = library.getNatives();

            if ((natives != null) && (natives.containsKey(os))) {
                result.add(library.getArtifactPath((String) natives.get(os)));
            }
        }

        return result;
    }

    public Set<String> getRequiredFiles(OperatingSystem os) {
        Set neededFiles = new HashSet();

        for (Library library : getRelevantLibraries(os)) {
            if (library.getNatives() != null) {
                String natives = (String) library.getNatives().get(os);
                if (natives != null) neededFiles.add(library.getArtifactPath(natives));
            } else {
                neededFiles.add(library.getArtifactPath());
            }

        }

        return neededFiles;
    }

    public Set<Downloadable> getRequiredDownloadables(OperatingSystem os, Proxy proxy, File targetDirectory, boolean ignoreLocalFiles) throws MalformedURLException {
        Set neededFiles = new HashSet();

        for (Library library : getRelevantLibraries(os)) {
            String file = null;

            if (library.getNatives() != null) {
                String natives = (String) library.getNatives().get(os);
                if (natives != null)
                    file = library.getArtifactPath(natives);
            } else {
                file = library.getArtifactPath();
            }

            if (file != null) {
                URL url = new URL(library.getDownloadUrl() + file);
                neededFiles.add(new Downloadable(proxy, url, new File(targetDirectory, file), ignoreLocalFiles));
            }
        }

        return neededFiles;
    }

    public String toString() {
        return "CompleteVersion{id='" + this.id + '\'' + ", time=" + this.time + ", type=" + this.type + ", libraries=" + this.libraries + ", mainClass='" + this.mainClass + '\'' + ", minimumLauncherVersion=" + this.minimumLauncherVersion + '}';
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        if (minecraftArguments == null) throw new IllegalArgumentException("Process arguments cannot be null or empty");
        this.minecraftArguments = minecraftArguments;
    }

    public int getMinimumLauncherVersion() {
        return this.minimumLauncherVersion;
    }

    public void setMinimumLauncherVersion(int minimumLauncherVersion) {
        this.minimumLauncherVersion = minimumLauncherVersion;
    }
}


