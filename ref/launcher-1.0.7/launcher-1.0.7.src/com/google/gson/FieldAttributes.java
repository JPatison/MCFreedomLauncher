package com.google.gson;

import com.google.gson.internal..Gson.Preconditions;
import java.lang.reflect.Field;

public final class FieldAttributes
{
  private final Field field;

  public FieldAttributes(Field f)
  {
    .Gson.Preconditions.checkNotNull(f);
    this.field = f;
  }
}