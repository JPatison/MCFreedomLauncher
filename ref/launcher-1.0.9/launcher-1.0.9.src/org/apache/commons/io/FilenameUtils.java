package org.apache.commons.io;

import java.io.File;

public class FilenameUtils
{
  public static final String EXTENSION_SEPARATOR_STR = Character.toString('.');

  private static final char SYSTEM_SEPARATOR = File.separatorChar;
  private static final char OTHER_SEPARATOR;

  static boolean isSystemWindows()
  {
    return SYSTEM_SEPARATOR == '\\';
  }

  static
  {
    if (isSystemWindows())
      OTHER_SEPARATOR = '/';
    else
      OTHER_SEPARATOR = '\\';
  }
}