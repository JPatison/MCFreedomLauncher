package org.apache.commons.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import org.apache.commons.io.output.StringBuilderWriter;

public class IOUtils
{
  public static final char DIR_SEPARATOR = File.separatorChar;
  public static final String LINE_SEPARATOR;

  public static void closeQuietly(InputStream input)
  {
    closeQuietly(input);
  }

  public static void closeQuietly(OutputStream output)
  {
    closeQuietly(output);
  }

  public static void closeQuietly(Closeable closeable)
  {
    try
    {
      if (closeable != null)
        closeable.close();
    }
    catch (IOException localIOException)
    {
    }
  }

  public static String toString(InputStream input, Charset encoding)
    throws IOException
  {
    StringBuilderWriter sw = new StringBuilderWriter();
    copy(input, sw, encoding);
    return sw.toString();
  }

  public static void write(String data, OutputStream output, Charset encoding)
    throws IOException
  {
    if (data != null)
      output.write(data.getBytes(Charsets.toCharset(encoding)));
  }

  public static void copy(InputStream input, Writer output, Charset encoding)
    throws IOException
  {
    InputStreamReader in = new InputStreamReader(input, Charsets.toCharset(encoding));
    copy(in, output);
  }

  public static int copy(Reader input, Writer output)
    throws IOException
  {
    long count = copyLarge(input, output);
    if (count > 2147483647L) {
      return -1;
    }
    return (int)count;
  }

  public static long copyLarge(Reader input, Writer output)
    throws IOException
  {
    return copyLarge(input, output, new char[4096]);
  }

  public static long copyLarge(Reader input, Writer output, char[] buffer)
    throws IOException
  {
    long count = 0L;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  static
  {
    StringBuilderWriter buf = new StringBuilderWriter(4);
    PrintWriter out = new PrintWriter(buf);
    out.println();
    LINE_SEPARATOR = buf.toString();
    out.close();
  }
}