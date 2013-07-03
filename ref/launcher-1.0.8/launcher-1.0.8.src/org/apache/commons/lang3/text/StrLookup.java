package org.apache.commons.lang3.text;

import java.util.Map;

public abstract class StrLookup<V>
{
  private static final StrLookup<String> NONE_LOOKUP = new MapStrLookup(null);

  private static final StrLookup<String> SYSTEM_PROPERTIES_LOOKUP = lookup;

  public static <V> StrLookup<V> mapLookup(Map<String, V> map)
  {
    return new MapStrLookup(map);
  }

  public abstract String lookup(String paramString);

  static
  {
    StrLookup lookup = null;
    try {
      Map propMap = System.getProperties();

      Map properties = propMap;
      lookup = new MapStrLookup(properties);
    } catch (SecurityException ex) {
      lookup = NONE_LOOKUP;
    }
  }

  static class MapStrLookup<V> extends StrLookup<V>
  {
    private final Map<String, V> map;

    MapStrLookup(Map<String, V> map)
    {
      this.map = map;
    }

    public String lookup(String key)
    {
      if (this.map == null) {
        return null;
      }
      Object obj = this.map.get(key);
      if (obj == null) {
        return null;
      }
      return obj.toString();
    }
  }
}