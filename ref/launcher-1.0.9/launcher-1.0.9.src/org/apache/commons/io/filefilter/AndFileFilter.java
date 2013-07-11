package org.apache.commons.io.filefilter;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AndFileFilter extends AbstractFileFilter
  implements Serializable
{
  private final List<IOFileFilter> fileFilters;

  public AndFileFilter()
  {
    this.fileFilters = new ArrayList();
  }

  public AndFileFilter(List<IOFileFilter> fileFilters)
  {
    if (fileFilters == null)
      this.fileFilters = new ArrayList();
    else
      this.fileFilters = new ArrayList(fileFilters);
  }

  public boolean accept(File file)
  {
    if (this.fileFilters.isEmpty()) {
      return false;
    }
    for (IOFileFilter fileFilter : this.fileFilters) {
      if (!fileFilter.accept(file)) {
        return false;
      }
    }
    return true;
  }

  public boolean accept(File file, String name)
  {
    if (this.fileFilters.isEmpty()) {
      return false;
    }
    for (IOFileFilter fileFilter : this.fileFilters) {
      if (!fileFilter.accept(file, name)) {
        return false;
      }
    }
    return true;
  }

  public String toString()
  {
    StringBuilder buffer = new StringBuilder();
    buffer.append(super.toString());
    buffer.append("(");
    if (this.fileFilters != null) {
      for (int i = 0; i < this.fileFilters.size(); i++) {
        if (i > 0) {
          buffer.append(",");
        }
        Object filter = this.fileFilters.get(i);
        buffer.append(filter == null ? "null" : filter.toString());
      }
    }
    buffer.append(")");
    return buffer.toString();
  }
}