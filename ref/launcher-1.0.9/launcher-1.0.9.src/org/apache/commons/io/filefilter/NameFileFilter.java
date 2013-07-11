package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import org.apache.commons.io.IOCase;

public class NameFileFilter extends AbstractFileFilter
  implements Serializable
{
  private final String[] names;
  private final IOCase caseSensitivity;

  public NameFileFilter(String name)
  {
    this(name, null);
  }

  public NameFileFilter(String name, IOCase caseSensitivity)
  {
    if (name == null) {
      throw new IllegalArgumentException("The wildcard must not be null");
    }
    this.names = new String[] { name };
    this.caseSensitivity = (caseSensitivity == null ? IOCase.SENSITIVE : caseSensitivity);
  }

  public boolean accept(File file)
  {
    String name = file.getName();
    for (String name2 : this.names) {
      if (this.caseSensitivity.checkEquals(name, name2)) {
        return true;
      }
    }
    return false;
  }

  public boolean accept(File dir, String name)
  {
    for (String name2 : this.names) {
      if (this.caseSensitivity.checkEquals(name, name2)) {
        return true;
      }
    }
    return false;
  }

  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(super.toString());
    buffer.append("(");
    if (this.names != null) {
      for (int i = 0; i < this.names.length; i++) {
        if (i > 0) {
          buffer.append(",");
        }
        buffer.append(this.names[i]);
      }
    }
    buffer.append(")");
    return buffer.toString();
  }
}