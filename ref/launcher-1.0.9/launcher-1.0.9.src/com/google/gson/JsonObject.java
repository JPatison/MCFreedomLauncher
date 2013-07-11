package com.google.gson;

import com.google.gson.internal..Gson.Preconditions;
import com.google.gson.internal.StringMap;
import java.util.Map.Entry;
import java.util.Set;

public final class JsonObject extends JsonElement
{
  private final StringMap<JsonElement> members = new StringMap();

  public void add(String property, JsonElement value)
  {
    if (value == null) {
      value = JsonNull.INSTANCE;
    }
    this.members.put((String).Gson.Preconditions.checkNotNull(property), value);
  }

  public Set<Map.Entry<String, JsonElement>> entrySet()
  {
    return this.members.entrySet();
  }

  public boolean equals(Object o)
  {
    return (o == this) || (((o instanceof JsonObject)) && (((JsonObject)o).members.equals(this.members)));
  }

  public int hashCode()
  {
    return this.members.hashCode();
  }
}