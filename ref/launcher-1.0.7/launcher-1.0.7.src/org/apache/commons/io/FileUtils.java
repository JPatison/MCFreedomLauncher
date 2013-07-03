package org.apache.commons.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;

public class FileUtils
{
  public static final BigInteger ONE_KB_BI = BigInteger.valueOf(1024L);

  public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

  public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

  public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

  public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

  public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

  public static final BigInteger ONE_ZB = BigInteger.valueOf(1024L).multiply(BigInteger.valueOf(1152921504606846976L));

  public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

  public static final File[] EMPTY_FILE_ARRAY = new File[0];

  private static final Charset UTF8 = Charset.forName("UTF-8");

  public static FileInputStream openInputStream(File file)
    throws IOException
  {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (!file.canRead())
        throw new IOException("File '" + file + "' cannot be read");
    }
    else {
      throw new FileNotFoundException("File '" + file + "' does not exist");
    }
    return new FileInputStream(file);
  }

  public static FileOutputStream openOutputStream(File file, boolean append)
    throws IOException
  {
    if (file.exists()) {
      if (file.isDirectory()) {
        throw new IOException("File '" + file + "' exists but is a directory");
      }
      if (!file.canWrite())
        throw new IOException("File '" + file + "' cannot be written to");
    }
    else {
      File parent = file.getParentFile();
      if ((parent != null) && 
        (!parent.mkdirs()) && (!parent.isDirectory())) {
        throw new IOException("Directory '" + parent + "' could not be created");
      }
    }

    return new FileOutputStream(file, append);
  }

  public static void deleteDirectory(File directory)
    throws IOException
  {
    if (!directory.exists()) {
      return;
    }

    if (!isSymlink(directory)) {
      cleanDirectory(directory);
    }

    if (!directory.delete()) {
      String message = "Unable to delete directory " + directory + ".";

      throw new IOException(message);
    }
  }

  public static boolean deleteQuietly(File file)
  {
    if (file == null)
      return false;
    try
    {
      if (file.isDirectory())
        cleanDirectory(file);
    }
    catch (Exception localException1)
    {
    }
    try {
      return file.delete(); } catch (Exception ignored) {
    }
    return false;
  }

  public static void cleanDirectory(File directory)
    throws IOException
  {
    if (!directory.exists()) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    File[] files = directory.listFiles();
    if (files == null) {
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (File file : files) {
      try {
        forceDelete(file);
      } catch (IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception)
      throw exception;
  }

  public static String readFileToString(File file, Charset encoding)
    throws IOException
  {
    InputStream in = null;
    try {
      in = openInputStream(file);
      return IOUtils.toString(in, Charsets.toCharset(encoding));
    } finally {
      IOUtils.closeQuietly(in);
    }
  }

  public static String readFileToString(File file)
    throws IOException
  {
    return readFileToString(file, Charset.defaultCharset());
  }

  public static void writeStringToFile(File file, String data, Charset encoding, boolean append)
    throws IOException
  {
    OutputStream out = null;
    try {
      out = openOutputStream(file, append);
      IOUtils.write(data, out, encoding);
      out.close();
    } finally {
      IOUtils.closeQuietly(out);
    }
  }

  public static void writeStringToFile(File file, String data)
    throws IOException
  {
    writeStringToFile(file, data, Charset.defaultCharset(), false);
  }

  public static void forceDelete(File file)
    throws IOException
  {
    if (file.isDirectory()) {
      deleteDirectory(file);
    } else {
      boolean filePresent = file.exists();
      if (!file.delete()) {
        if (!filePresent) {
          throw new FileNotFoundException("File does not exist: " + file);
        }
        String message = "Unable to delete file: " + file;

        throw new IOException(message);
      }
    }
  }

  public static void forceDeleteOnExit(File file)
    throws IOException
  {
    if (file.isDirectory())
      deleteDirectoryOnExit(file);
    else
      file.deleteOnExit();
  }

  private static void deleteDirectoryOnExit(File directory)
    throws IOException
  {
    if (!directory.exists()) {
      return;
    }

    directory.deleteOnExit();
    if (!isSymlink(directory))
      cleanDirectoryOnExit(directory);
  }

  private static void cleanDirectoryOnExit(File directory)
    throws IOException
  {
    if (!directory.exists()) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    File[] files = directory.listFiles();
    if (files == null) {
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (File file : files) {
      try {
        forceDeleteOnExit(file);
      } catch (IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception)
      throw exception;
  }

  public static boolean isFileNewer(File file, long timeMillis)
  {
    if (file == null) {
      throw new IllegalArgumentException("No specified file");
    }
    if (!file.exists()) {
      return false;
    }
    return file.lastModified() > timeMillis;
  }

  public static boolean isSymlink(File file)
    throws IOException
  {
    if (file == null) {
      throw new NullPointerException("File must not be null");
    }
    if (FilenameUtils.isSystemWindows()) {
      return false;
    }
    File fileInCanonicalDir = null;
    if (file.getParent() == null) {
      fileInCanonicalDir = file;
    } else {
      File canonicalDir = file.getParentFile().getCanonicalFile();
      fileInCanonicalDir = new File(canonicalDir, file.getName());
    }

    if (fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile())) {
      return false;
    }
    return true;
  }
}