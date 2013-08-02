package net.minecraft.launcher.updater;

import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public abstract class FileBasedVersionList extends VersionList
{
  protected String getContent(String path)
    throws IOException
  {
    return IOUtils.toString(getFileInputStream(path)).replaceAll("\\r\\n", "\r").replaceAll("\\r", "\n");
    }

    protected abstract InputStream getFileInputStream(String paramString)
            throws FileNotFoundException;
}
