package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher.OperatingSystem;

public class Library
{
  private static final String LIBRARY_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
  private String name;
  private List<OperatingSystem> os;
  private Map<OperatingSystem, String> natives;
  private ExtractRules extract;
  private String url;

  public Library()
  {
  }

  public Library(String name)
  {
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Library name cannot be null or empty");
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public Library addRestriction(OperatingSystem[] operatingSystems) {
    if (this.os == null) this.os = new ArrayList();
    if (operatingSystems != null) Collections.addAll(this.os, operatingSystems);
    return this;
  }

  public Library addNative(OperatingSystem operatingSystem, String name) {
    if ((operatingSystem == null) || (!operatingSystem.isSupported())) throw new IllegalArgumentException("Cannot add native for unsupported OS");
    if ((name == null) || (name.length() == 0)) throw new IllegalArgumentException("Cannot add native for null or empty name");
    if (this.natives == null) this.natives = new EnumMap(OperatingSystem.class);
    this.natives.put(operatingSystem, name);
    return this;
  }

  public List<OperatingSystem> getRestrictedOperatingSystems() {
    return this.os;
  }

  public Map<OperatingSystem, String> getNatives() {
    return this.natives;
  }

  public ExtractRules getExtractRules() {
    return this.extract;
  }

  public Library setExtractRules(ExtractRules rules) {
    this.extract = rules;
    return this;
  }

  public String getArtifactBaseDir() {
    if (this.name == null) throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
    String[] parts = this.name.split(":", 3);
    return String.format("%s/%s/%s", new Object[] { parts[0].replaceAll("\\.", "/"), parts[1], parts[2] });
  }

  public String getArtifactPath() {
    if (this.name == null) throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
    return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename() });
  }

  public String getArtifactPath(String classifier) {
    if (this.name == null) throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
    return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename(classifier) });
  }

  public String getArtifactFilename() {
    if (this.name == null) throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
    String[] parts = this.name.split(":", 3);
    return String.format("%s-%s.jar", new Object[] { parts[1], parts[2] });
  }

  public String getArtifactFilename(String classifier) {
    if (this.name == null) throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
    String[] parts = this.name.split(":", 3);
    return String.format("%s-%s-%s.jar", new Object[] { parts[1], parts[2], classifier });
  }

  public String toString()
  {
    return "Library{name='" + this.name + '\'' + ", os=" + this.os + ", natives=" + this.natives + ", extract=" + this.extract + '}';
  }

  public String getDownloadUrl()
  {
    if (this.url != null) return this.url;
    return "https://s3.amazonaws.com/Minecraft.Download/libraries/";
  }
}