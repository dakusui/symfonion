package com.github.dakusui.symfonion.utils;

import com.github.dakusui.symfonion.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.valid8j_pcond.forms.Printables;

import java.io.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.exceptions.ExceptionThrower.*;
import static com.github.dakusui.valid8j.Requires.require;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.isNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;


public class Utils {
  public static final Pattern fractionPattern = Pattern.compile("([0-9]+)/([1-9][0-9]*)");
  public static final java.util.regex.Pattern lengthPattern = java.util.regex.Pattern.compile("([1-9][0-9]*)(\\.*)");

  public static int count(char ch, String s) {
    int ret = 0;
    for (int i = s.indexOf(ch); i >= 0; i = s.indexOf(ch, i + 1)) {
      ret++;
    }
    return ret;
  }

  public static Fraction parseFraction(String str) throws FractionFormatException {
    if (str == null) {
      return null;
    }
    Matcher m = fractionPattern.matcher(str);
    if (!m.matches()) {
      throw throwFractionFormatException(str);
    }
    return new Fraction(
        Integer.parseInt(m.group(1)),
        Integer.parseInt(m.group(2))
    );
  }

  public static String loadResource(String resourceName) throws SymfonionException {
    StringBuffer b = new StringBuffer(4096);
    try {
      InputStream is = new BufferedInputStream(require(ClassLoader.getSystemResourceAsStream(resourceName), resourceIsNotNull(resourceName)));
      loadFromInputStream(b, is);
    } catch (IOException e) {
      throw loadResourceException(resourceName, e);
    }
    return b.toString();
  }

  private static Predicate<InputStream> resourceIsNotNull(String resourceName) {
    return Printables.predicate(() -> "isNotNull[resourceLoadedFrom[" + resourceName + "]]", isNotNull());
  }

  public static String loadFile(String fileName) throws SymfonionException {
    StringBuffer b = new StringBuffer(4096);
    File f = new File(fileName);
    try {

      InputStream is = new BufferedInputStream(new FileInputStream(f));
      loadFromInputStream(b, is);
    } catch (FileNotFoundException e) {
      throw fileNotFoundException(f, e);
    } catch (IOException e) {
      throw loadFileException(f, e);
    }
    return b.toString();
  }

  private static void loadFromInputStream(StringBuffer b, InputStream is) throws IOException {
    Reader r = new InputStreamReader(is, UTF_8);
    int c;
    while ((c = r.read()) != -1) {
      b.append((char) c);
    }
  }

  public static Fraction parseNoteLength(String length) {
    Matcher m = lengthPattern.matcher(length);
    Fraction ret = null;
    if (m.matches()) {
      int l = Integer.parseInt(m.group(1));
      ret = new Fraction(1, l);
      int dots = Utils.count('.', m.group(2));
      for (int i = 0; i < dots; i++) {
        l *= 2;
        ret = Fraction.add(ret, new Fraction(1, l));
      }
    } else if (!"0".equals(length)) {
    } else {
      ret = new Fraction(0, 1);
    }
    return ret;
  }

  public static byte[] getIntBytes(int input) {
    byte[] ret = new byte[3];

    ret[0] = (byte) (input >> 16 & 0xff);
    ret[1] = (byte) (input >> 8 & 0xff);
    ret[2] = (byte) (input & 0xff);

    return ret;
  }
}
