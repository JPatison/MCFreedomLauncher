package net.minecraft.launcher.updater;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import net.minecraft.launcher.Http;
import net.minecraft.launcher.OperatingSystem;
import net.minecraft.launcher.versions.CompleteVersion;

public class RemoteVersionList extends VersionList
{
  private final Proxy proxy;

  public RemoteVersionList(Proxy proxy)
  {
    this.proxy = proxy;
  }

  public boolean hasAllFiles(CompleteVersion version, OperatingSystem os)
  {
    return true;
  }

  protected String getUrl(String uri) throws IOException
  {
    return Http.performGet(new URL("https://s3.amazonaws.com/Minecraft.Download/" + uri), this.proxy);
  }

  public Proxy getProxy() {
    return this.proxy;
  }
}