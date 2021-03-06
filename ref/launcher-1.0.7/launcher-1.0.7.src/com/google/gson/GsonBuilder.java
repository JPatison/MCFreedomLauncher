package com.google.gson;

import com.google.gson.internal..Gson.Preconditions;
import com.google.gson.internal.Excluder;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GsonBuilder
{
  private Excluder excluder = Excluder.DEFAULT;
  private LongSerializationPolicy longSerializationPolicy = LongSerializationPolicy.DEFAULT;
  private FieldNamingStrategy fieldNamingPolicy = FieldNamingPolicy.IDENTITY;
  private final Map<Type, InstanceCreator<?>> instanceCreators = new HashMap();

  private final List<TypeAdapterFactory> factories = new ArrayList();

  private final List<TypeAdapterFactory> hierarchyFactories = new ArrayList();
  private boolean serializeNulls;
  private String datePattern;
  private int dateStyle = 2;
  private int timeStyle = 2;
  private boolean complexMapKeySerialization;
  private boolean serializeSpecialFloatingPointValues;
  private boolean escapeHtmlChars = true;
  private boolean prettyPrinting;
  private boolean generateNonExecutableJson;

  public GsonBuilder enableComplexMapKeySerialization()
  {
    this.complexMapKeySerialization = true;
    return this;
  }

  public GsonBuilder setPrettyPrinting()
  {
    this.prettyPrinting = true;
    return this;
  }

  public GsonBuilder registerTypeAdapter(Type type, Object typeAdapter)
  {
    .Gson.Preconditions.checkArgument(((typeAdapter instanceof JsonSerializer)) || ((typeAdapter instanceof JsonDeserializer)) || ((typeAdapter instanceof InstanceCreator)) || ((typeAdapter instanceof TypeAdapter)));

    if ((typeAdapter instanceof InstanceCreator)) {
      this.instanceCreators.put(type, (InstanceCreator)typeAdapter);
    }
    if (((typeAdapter instanceof JsonSerializer)) || ((typeAdapter instanceof JsonDeserializer))) {
      TypeToken typeToken = TypeToken.get(type);
      this.factories.add(TreeTypeAdapter.newFactoryWithMatchRawType(typeToken, typeAdapter));
    }
    if ((typeAdapter instanceof TypeAdapter)) {
      this.factories.add(TypeAdapters.newFactory(TypeToken.get(type), (TypeAdapter)typeAdapter));
    }
    return this;
  }

  public GsonBuilder registerTypeAdapterFactory(TypeAdapterFactory factory)
  {
    this.factories.add(factory);
    return this;
  }

  public Gson create()
  {
    List factories = new ArrayList();
    factories.addAll(this.factories);
    Collections.reverse(factories);
    factories.addAll(this.hierarchyFactories);
    addTypeAdaptersForDate(this.datePattern, this.dateStyle, this.timeStyle, factories);

    return new Gson(this.excluder, this.fieldNamingPolicy, this.instanceCreators, this.serializeNulls, this.complexMapKeySerialization, this.generateNonExecutableJson, this.escapeHtmlChars, this.prettyPrinting, this.serializeSpecialFloatingPointValues, this.longSerializationPolicy, factories);
  }

  private void addTypeAdaptersForDate(String datePattern, int dateStyle, int timeStyle, List<TypeAdapterFactory> factories)
  {
    DefaultDateTypeAdapter dateTypeAdapter;
    if ((datePattern != null) && (!"".equals(datePattern.trim()))) {
      dateTypeAdapter = new DefaultDateTypeAdapter(datePattern);
    }
    else
    {
      DefaultDateTypeAdapter dateTypeAdapter;
      if ((dateStyle != 2) && (timeStyle != 2))
        dateTypeAdapter = new DefaultDateTypeAdapter(dateStyle, timeStyle);
      else
        return;
    }
    DefaultDateTypeAdapter dateTypeAdapter;
    factories.add(TreeTypeAdapter.newFactory(TypeToken.get(java.util.Date.class), dateTypeAdapter));
    factories.add(TreeTypeAdapter.newFactory(TypeToken.get(Timestamp.class), dateTypeAdapter));
    factories.add(TreeTypeAdapter.newFactory(TypeToken.get(java.sql.Date.class), dateTypeAdapter));
  }
}