package org.apache.commons.io;

import java.io.Serializable;

public final class IOCase
  implements Serializable
{
  public static final IOCase SENSITIVE = new IOCase("Sensitive", true);

  public static final IOCase INSENSITIVE = new IOCase("Insensitive", false);

  public static final IOCase SYSTEM = new IOCase("System", !FilenameUtils.isSystemWindows());
  private final String name;
  private final transient boolean sensitive;

  private IOCase(String name, boolean sensitive)
  {
    this.name = name;
    this.sensitive = sensitive;
  }

  public boolean checkEquals(String str1, String str2)
  {
    if ((str1 == null) || (str2 == null)) {
      throw new NullPointerException("The strings must not be null");
    }
    return this.sensitive ? str1.equals(str2) : str1.equalsIgnoreCase(str2);
  }

  public boolean checkStartsWith(String str, String start)
  {
    return str.regionMatches(!this.sensitive, 0, start, 0, start.length());
  }

  public String toString()
  {
    return this.name;
  }
}