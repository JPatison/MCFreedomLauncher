package org.apache.commons.io.filefilter;

import java.util.ArrayList;
import java.util.List;

public class FileFilterUtils
{
  private static final IOFileFilter cvsFilter = notFileFilter(and(new IOFileFilter[] { directoryFileFilter(), nameFileFilter("CVS") }));

  private static final IOFileFilter svnFilter = notFileFilter(and(new IOFileFilter[] { directoryFileFilter(), nameFileFilter(".svn") }));

  public static IOFileFilter nameFileFilter(String name)
  {
    return new NameFileFilter(name);
  }

  public static IOFileFilter directoryFileFilter()
  {
    return DirectoryFileFilter.DIRECTORY;
  }

  public static IOFileFilter and(IOFileFilter[] filters)
  {
    return new AndFileFilter(toList(filters));
  }

  public static List<IOFileFilter> toList(IOFileFilter[] filters)
  {
    if (filters == null) {
      throw new IllegalArgumentException("The filters must not be null");
    }
    List list = new ArrayList(filters.length);
    for (int i = 0; i < filters.length; i++) {
      if (filters[i] == null) {
        throw new IllegalArgumentException("The filter[" + i + "] is null");
      }
      list.add(filters[i]);
    }
    return list;
  }

  public static IOFileFilter notFileFilter(IOFileFilter filter)
  {
    return new NotFileFilter(filter);
  }
}