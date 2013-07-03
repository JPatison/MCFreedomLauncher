package org.apache.commons.lang3;

import java.util.regex.Pattern;

public class StringUtils
{
  private static final Pattern WHITESPACE_BLOCK = Pattern.compile("\\s+");

  public static boolean isEmpty(CharSequence cs)
  {
    return (cs == null) || (cs.length() == 0);
  }

  public static boolean isBlank(CharSequence cs)
  {
    int strLen;
    if ((cs == null) || ((strLen = cs.length()) == 0))
      return true;
    int strLen;
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(cs.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  public static boolean isNotBlank(CharSequence cs)
  {
    return !isBlank(cs);
  }

  public static boolean containsIgnoreCase(CharSequence str, CharSequence searchStr)
  {
    if ((str == null) || (searchStr == null)) {
      return false;
    }
    int len = searchStr.length();
    int max = str.length() - len;
    for (int i = 0; i <= max; i++) {
      if (CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) {
        return true;
      }
    }
    return false;
  }
}