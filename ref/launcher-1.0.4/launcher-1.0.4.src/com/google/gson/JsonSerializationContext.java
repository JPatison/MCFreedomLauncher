package com.google.gson;

public abstract interface JsonSerializationContext
{
  public abstract JsonElement serialize(Object paramObject);
}