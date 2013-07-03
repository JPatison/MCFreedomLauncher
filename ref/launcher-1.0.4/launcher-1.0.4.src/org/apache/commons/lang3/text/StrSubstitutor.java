package org.apache.commons.lang3.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StrSubstitutor
{
  public static final StrMatcher DEFAULT_PREFIX = StrMatcher.stringMatcher("${");

  public static final StrMatcher DEFAULT_SUFFIX = StrMatcher.stringMatcher("}");
  private char escapeChar;
  private StrMatcher prefixMatcher;
  private StrMatcher suffixMatcher;
  private StrLookup<?> variableResolver;
  private boolean enableSubstitutionInVariables;

  public StrSubstitutor()
  {
    this((StrLookup)null, DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
  }

  public <V> StrSubstitutor(Map<String, V> valueMap)
  {
    this(StrLookup.mapLookup(valueMap), DEFAULT_PREFIX, DEFAULT_SUFFIX, '$');
  }

  public StrSubstitutor(StrLookup<?> variableResolver, StrMatcher prefixMatcher, StrMatcher suffixMatcher, char escape)
  {
    setVariableResolver(variableResolver);
    setVariablePrefixMatcher(prefixMatcher);
    setVariableSuffixMatcher(suffixMatcher);
    setEscapeChar(escape);
  }

  public String replace(String source)
  {
    if (source == null) {
      return null;
    }
    StrBuilder buf = new StrBuilder(source);
    if (!substitute(buf, 0, source.length())) {
      return source;
    }
    return buf.toString();
  }

  protected boolean substitute(StrBuilder buf, int offset, int length)
  {
    return substitute(buf, offset, length, null) > 0;
  }

  private int substitute(StrBuilder buf, int offset, int length, List<String> priorVariables)
  {
    StrMatcher prefixMatcher = getVariablePrefixMatcher();
    StrMatcher suffixMatcher = getVariableSuffixMatcher();
    char escape = getEscapeChar();

    boolean top = priorVariables == null;
    boolean altered = false;
    int lengthChange = 0;
    char[] chars = buf.buffer;
    int bufEnd = offset + length;
    int pos = offset;
    while (pos < bufEnd) {
      int startMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd);

      if (startMatchLen == 0) {
        pos++;
      }
      else if ((pos > offset) && (chars[(pos - 1)] == escape))
      {
        buf.deleteCharAt(pos - 1);
        chars = buf.buffer;
        lengthChange--;
        altered = true;
        bufEnd--;
      }
      else {
        int startPos = pos;
        pos += startMatchLen;
        int endMatchLen = 0;
        int nestedVarCount = 0;
        while (pos < bufEnd) {
          if ((isEnableSubstitutionInVariables()) && ((endMatchLen = prefixMatcher.isMatch(chars, pos, offset, bufEnd)) != 0))
          {
            nestedVarCount++;
            pos += endMatchLen;
          }
          else
          {
            endMatchLen = suffixMatcher.isMatch(chars, pos, offset, bufEnd);

            if (endMatchLen == 0) {
              pos++;
            }
            else {
              if (nestedVarCount == 0) {
                String varName = new String(chars, startPos + startMatchLen, pos - startPos - startMatchLen);

                if (isEnableSubstitutionInVariables()) {
                  StrBuilder bufName = new StrBuilder(varName);
                  substitute(bufName, 0, bufName.length());
                  varName = bufName.toString();
                }
                pos += endMatchLen;
                int endPos = pos;

                if (priorVariables == null) {
                  priorVariables = new ArrayList();
                  priorVariables.add(new String(chars, offset, length));
                }

                checkCyclicSubstitution(varName, priorVariables);
                priorVariables.add(varName);

                String varValue = resolveVariable(varName, buf, startPos, endPos);

                if (varValue != null)
                {
                  int varLen = varValue.length();
                  buf.replace(startPos, endPos, varValue);
                  altered = true;
                  int change = substitute(buf, startPos, varLen, priorVariables);

                  change = change + varLen - (endPos - startPos);

                  pos += change;
                  bufEnd += change;
                  lengthChange += change;
                  chars = buf.buffer;
                }

                priorVariables.remove(priorVariables.size() - 1);

                break;
              }
              nestedVarCount--;
              pos += endMatchLen;
            }
          }
        }
      }
    }

    if (top) {
      return altered ? 1 : 0;
    }
    return lengthChange;
  }

  private void checkCyclicSubstitution(String varName, List<String> priorVariables)
  {
    if (!priorVariables.contains(varName)) {
      return;
    }
    StrBuilder buf = new StrBuilder(256);
    buf.append("Infinite loop in property interpolation of ");
    buf.append((String)priorVariables.remove(0));
    buf.append(": ");
    buf.appendWithSeparators(priorVariables, "->");
    throw new IllegalStateException(buf.toString());
  }

  protected String resolveVariable(String variableName, StrBuilder buf, int startPos, int endPos)
  {
    StrLookup resolver = getVariableResolver();
    if (resolver == null) {
      return null;
    }
    return resolver.lookup(variableName);
  }

  public char getEscapeChar()
  {
    return this.escapeChar;
  }

  public void setEscapeChar(char escapeCharacter)
  {
    this.escapeChar = escapeCharacter;
  }

  public StrMatcher getVariablePrefixMatcher()
  {
    return this.prefixMatcher;
  }

  public StrSubstitutor setVariablePrefixMatcher(StrMatcher prefixMatcher)
  {
    if (prefixMatcher == null) {
      throw new IllegalArgumentException("Variable prefix matcher must not be null!");
    }
    this.prefixMatcher = prefixMatcher;
    return this;
  }

  public StrMatcher getVariableSuffixMatcher()
  {
    return this.suffixMatcher;
  }

  public StrSubstitutor setVariableSuffixMatcher(StrMatcher suffixMatcher)
  {
    if (suffixMatcher == null) {
      throw new IllegalArgumentException("Variable suffix matcher must not be null!");
    }
    this.suffixMatcher = suffixMatcher;
    return this;
  }

  public StrLookup<?> getVariableResolver()
  {
    return this.variableResolver;
  }

  public void setVariableResolver(StrLookup<?> variableResolver)
  {
    this.variableResolver = variableResolver;
  }

  public boolean isEnableSubstitutionInVariables()
  {
    return this.enableSubstitutionInVariables;
  }
}