package com.google.gson.internal;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public final class StringMap<V> extends AbstractMap<String, V>
{
  private LinkedEntry<V> header;
  private static final Map.Entry[] EMPTY_TABLE = new LinkedEntry[2];
  private LinkedEntry<V>[] table;
  private int size;
  private int threshold;
  private Set<String> keySet;
  private Set<Map.Entry<String, V>> entrySet;
  private Collection<V> values;
  private static final int seed = new Random().nextInt();

  public StringMap()
  {
    this.table = ((LinkedEntry[])EMPTY_TABLE);
    this.threshold = -1;
    this.header = new LinkedEntry();
  }

  public int size() {
    return this.size;
  }

  public boolean containsKey(Object key) {
    return ((key instanceof String)) && (getEntry((String)key) != null);
  }

  public V get(Object key) {
    if ((key instanceof String)) {
      LinkedEntry entry = getEntry((String)key);
      return entry != null ? entry.value : null;
    }
    return null;
  }

  private LinkedEntry<V> getEntry(String key)
  {
    if (key == null) {
      return null;
    }

    int hash = hash(key);
    LinkedEntry[] tab = this.table;
    for (LinkedEntry e = tab[(hash & tab.length - 1)]; e != null; e = e.next) {
      String eKey = e.key;
      if ((eKey == key) || ((e.hash == hash) && (key.equals(eKey)))) {
        return e;
      }
    }
    return null;
  }

  public V put(String key, V value) {
    if (key == null) {
      throw new NullPointerException("key == null");
    }

    int hash = hash(key);
    LinkedEntry[] tab = this.table;
    int index = hash & tab.length - 1;
    for (LinkedEntry e = tab[index]; e != null; e = e.next) {
      if ((e.hash == hash) && (key.equals(e.key))) {
        Object oldValue = e.value;
        e.value = value;
        return oldValue;
      }

    }

    if (this.size++ > this.threshold) {
      tab = doubleCapacity();
      index = hash & tab.length - 1;
    }
    addNewEntry(key, value, hash, index);
    return null;
  }

  private void addNewEntry(String key, V value, int hash, int index) {
    LinkedEntry header = this.header;

    LinkedEntry oldTail = header.prv;
    LinkedEntry newTail = new LinkedEntry(key, value, hash, this.table[index], header, oldTail);
    LinkedEntry tmp52_49 = (header.prv = newTail); oldTail.nxt = tmp52_49; this.table[index] = tmp52_49;
  }

  private LinkedEntry<V>[] makeTable(int newCapacity)
  {
    LinkedEntry[] newTable = (LinkedEntry[])new LinkedEntry[newCapacity];
    this.table = newTable;
    this.threshold = ((newCapacity >> 1) + (newCapacity >> 2));
    return newTable;
  }

  private LinkedEntry<V>[] doubleCapacity()
  {
    LinkedEntry[] oldTable = this.table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == 1073741824) {
      return oldTable;
    }
    int newCapacity = oldCapacity * 2;
    LinkedEntry[] newTable = makeTable(newCapacity);
    if (this.size == 0) {
      return newTable;
    }

    for (int j = 0; j < oldCapacity; j++)
    {
      LinkedEntry e = oldTable[j];
      if (e != null)
      {
        int highBit = e.hash & oldCapacity;
        LinkedEntry broken = null;
        newTable[(j | highBit)] = e;
        for (LinkedEntry n = e.next; n != null; n = n.next) {
          int nextHighBit = n.hash & oldCapacity;
          if (nextHighBit != highBit) {
            if (broken == null)
              newTable[(j | nextHighBit)] = n;
            else {
              broken.next = n;
            }
            broken = e;
            highBit = nextHighBit;
          }
          e = n;
        }

        if (broken != null)
          broken.next = null;
      }
    }
    return newTable;
  }

  public V remove(Object key) {
    if ((key == null) || (!(key instanceof String))) {
      return null;
    }
    int hash = hash((String)key);
    LinkedEntry[] tab = this.table;
    int index = hash & tab.length - 1;
    LinkedEntry e = tab[index]; LinkedEntry prev = null;
    for (; e != null; e = e.next) {
      if ((e.hash == hash) && (key.equals(e.key))) {
        if (prev == null)
          tab[index] = e.next;
        else {
          prev.next = e.next;
        }
        this.size -= 1;
        unlink(e);
        return e.value;
      }
      prev = e;
    }

    return null;
  }

  private void unlink(LinkedEntry<V> e) {
    e.prv.nxt = e.nxt;
    e.nxt.prv = e.prv;
    e.nxt = (e.prv = null);
  }

  public void clear() {
    if (this.size != 0) {
      Arrays.fill(this.table, null);
      this.size = 0;
    }

    LinkedEntry header = this.header;
    for (LinkedEntry e = header.nxt; e != header; ) {
      LinkedEntry nxt = e.nxt;
      e.nxt = (e.prv = null);
      e = nxt;
    }

    header.nxt = (header.prv = header);
  }

  public Set<String> keySet() {
    Set ks = this.keySet;
    return this.keySet = new KeySet(null);
  }

  public Collection<V> values() {
    Collection vs = this.values;
    return this.values = new Values(null);
  }

  public Set<Map.Entry<String, V>> entrySet() {
    Set es = this.entrySet;
    return this.entrySet = new EntrySet(null);
  }

  private boolean removeMapping(Object key, Object value)
  {
    if ((key == null) || (!(key instanceof String))) {
      return false;
    }

    int hash = hash((String)key);
    LinkedEntry[] tab = this.table;
    int index = hash & tab.length - 1;
    LinkedEntry e = tab[index]; for (LinkedEntry prev = null; e != null; e = e.next) {
      if ((e.hash == hash) && (key.equals(e.key))) {
        if (value == null ? e.value != null : !value.equals(e.value)) {
          return false;
        }
        if (prev == null)
          tab[index] = e.next;
        else {
          prev.next = e.next;
        }
        this.size -= 1;
        unlink(e);
        return true;
      }
      prev = e;
    }

    return false;
  }

  private static int hash(String key)
  {
    int h = seed;
    for (int i = 0; i < key.length(); i++) {
      int h2 = h + key.charAt(i);
      int h3 = h2 + h2 << 10;
      h = h3 ^ h3 >>> 6;
    }

    h ^= h >>> 20 ^ h >>> 12;
    return h ^ h >>> 7 ^ h >>> 4;
  }

  private final class EntrySet extends AbstractSet<Map.Entry<String, V>>
  {
    private EntrySet()
    {
    }

    public Iterator<Map.Entry<String, V>> iterator()
    {
      // Byte code:
      //   0: new 34	com/google/gson/internal/StringMap$EntrySet$1
      //   3: dup
      //   4: aload_0
      //   5: invokespecial 74	com/google/gson/internal/StringMap$EntrySet$1:<init>	(Lcom/google/gson/internal/StringMap$EntrySet;)V
      //   8: areturn
    }

    public boolean contains(Object o)
    {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry e = (Map.Entry)o;
      Object mappedValue = StringMap.this.get(e.getKey());
      return (mappedValue != null) && (mappedValue.equals(e.getValue()));
    }

    public boolean remove(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry e = (Map.Entry)o;
      return StringMap.this.removeMapping(e.getKey(), e.getValue());
    }

    public int size() {
      return StringMap.this.size;
    }

    public void clear() {
      StringMap.this.clear();
    }
  }

  private final class KeySet extends AbstractSet<String>
  {
    private KeySet()
    {
    }

    public Iterator<String> iterator()
    {
      // Byte code:
      //   0: new 26	com/google/gson/internal/StringMap$KeySet$1
      //   3: dup
      //   4: aload_0
      //   5: invokespecial 56	com/google/gson/internal/StringMap$KeySet$1:<init>	(Lcom/google/gson/internal/StringMap$KeySet;)V
      //   8: areturn
    }

    public int size()
    {
      return StringMap.this.size;
    }

    public boolean contains(Object o) {
      return StringMap.this.containsKey(o);
    }

    public boolean remove(Object o) {
      int oldSize = StringMap.this.size;
      StringMap.this.remove(o);
      return StringMap.this.size != oldSize;
    }

    public void clear() {
      StringMap.this.clear();
    }
  }

  static class LinkedEntry<V>
    implements Map.Entry<String, V>
  {
    final String key;
    V value;
    final int hash;
    LinkedEntry<V> next;
    LinkedEntry<V> nxt;
    LinkedEntry<V> prv;

    LinkedEntry()
    {
      this(null, null, 0, null, null, null);
      this.prv = this; this.nxt = this;
    }

    LinkedEntry(String key, V value, int hash, LinkedEntry<V> next, LinkedEntry<V> nxt, LinkedEntry<V> prv)
    {
      this.key = key;
      this.value = value;
      this.hash = hash;
      this.next = next;
      this.nxt = nxt;
      this.prv = prv;
    }

    public final String getKey() {
      return this.key;
    }

    public final V getValue() {
      return this.value;
    }

    public final V setValue(V value) {
      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public final boolean equals(Object o) {
      if (!(o instanceof Map.Entry)) {
        return false;
      }
      Map.Entry e = (Map.Entry)o;
      Object eValue = e.getValue();
      return (this.key.equals(e.getKey())) && (this.value == null ? eValue == null : this.value.equals(eValue));
    }

    public final int hashCode()
    {
      return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
    }

    public final String toString() {
      return this.key + "=" + this.value;
    }
  }

  private abstract class LinkedHashIterator<T>
    implements Iterator<T>
  {
    StringMap.LinkedEntry<V> next = StringMap.this.header.nxt;
    StringMap.LinkedEntry<V> lastReturned = null;

    private LinkedHashIterator() {  } 
    public final boolean hasNext() { return this.next != StringMap.this.header; }

    final StringMap.LinkedEntry<V> nextEntry()
    {
      StringMap.LinkedEntry e = this.next;
      if (e == StringMap.this.header) {
        throw new NoSuchElementException();
      }
      this.next = e.nxt;
      return this.lastReturned = e;
    }

    public final void remove() {
      if (this.lastReturned == null) {
        throw new IllegalStateException();
      }
      StringMap.this.remove(this.lastReturned.key);
      this.lastReturned = null;
    }
  }

  private final class Values extends AbstractCollection<V>
  {
    private Values()
    {
    }

    public Iterator<V> iterator()
    {
      // Byte code:
      //   0: new 23	com/google/gson/internal/StringMap$Values$1
      //   3: dup
      //   4: aload_0
      //   5: invokespecial 50	com/google/gson/internal/StringMap$Values$1:<init>	(Lcom/google/gson/internal/StringMap$Values;)V
      //   8: areturn
    }

    public int size()
    {
      return StringMap.this.size;
    }

    public boolean contains(Object o) {
      return StringMap.this.containsValue(o);
    }

    public void clear() {
      StringMap.this.clear();
    }
  }
}