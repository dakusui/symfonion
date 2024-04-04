package com.github.dakusui.symfonion.utils;

import com.github.dakusui.symfonion.exceptions.SymfonionException;
import com.github.dakusui.valid8j_pcond.forms.Printables;

import java.io.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.stream.Collector;

import static com.github.dakusui.symfonion.exceptions.CompatExceptionThrower.*;
import static com.github.dakusui.valid8j.Requires.require;
import static com.github.dakusui.valid8j_pcond.forms.Predicates.isNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;


public enum Utils {
  ;
  public static final java.util.regex.Pattern lengthPattern = java.util.regex.Pattern.compile("([1-9][0-9]*)(\\.*)");

  /**
   * Count occurrences of a given character {@code ch} in a string {@code s}.
   *
   * @param ch A character to count its occurrences.
   * @param s A string in which the number of {@code ch} should be counted.
   * @return The number of occurrences of {@code ch} in string {@code s}.
   */
  public static int count(char ch, String s) {
    int ret = 0;
    for (int i = s.indexOf(ch); i >= 0; i = s.indexOf(ch, i + 1)) {
      ret++;
    }
    return ret;
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

  public static String loadFile(String fileName) throws SymfonionException {
    StringBuffer b = new StringBuffer(4096);
    File f = new File(fileName);
    try (InputStream is = new BufferedInputStream(new FileInputStream(f))){
      loadFromInputStream(b, is);
    } catch (FileNotFoundException e) {
      throw fileNotFoundException(f, e);
    } catch (IOException e) {
      throw loadFileException(e);
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

  /**
   * This method was copied from <a href="https://stackoverflow.com/questions/22694884/filter-java-stream-to-1-and-only-1-element/22695424#22695424">stackoverflow.com</a> and renamed.
   *
   * @param <E> Type of the element to be collected.
   * @return A collector
   */
  public static <E> Collector<E, ?, Optional<E>> onlyElement() {
    return onlyElement((e1, e2) -> {
      throw new IllegalArgumentException("Multiple values are found in the stream: <" + e1 + "> and <" + e2 + ">");
    });
  }


  public static <E> Collector<E, AtomicReference<E>, Optional<E>> onlyElement(BiFunction<E, E, ? extends RuntimeException> multipleElements) {
    return Collector.of(
        AtomicReference::new,
        (ref, e) -> {
          if (!ref.compareAndSet(null, e)) {
            throw multipleElements.apply(ref.get(), e);
          }
        },
        (ref1, ref2) -> {
          if (ref1.get() == null) {
            return ref2;
          } else if (ref2.get() != null) {
            throw multipleElements.apply(ref1.get(), ref2.get());
          } else {
            return ref1;
          }
        },
        ref -> Optional.ofNullable(ref.get()),
        Collector.Characteristics.UNORDERED);
  }

  private static Predicate<InputStream> resourceIsNotNull(String resourceName) {
    return Printables.predicate(() -> "isNotNull[resourceLoadedFrom[" + resourceName + "]]", isNotNull());
  }

}
