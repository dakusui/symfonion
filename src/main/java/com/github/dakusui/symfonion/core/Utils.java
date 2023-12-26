package com.github.dakusui.symfonion.core;

import com.github.dakusui.symfonion.core.exceptions.FractionFormatException;
import com.github.dakusui.symfonion.core.exceptions.SymfonionException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.dakusui.symfonion.core.exceptions.ExceptionThrower.*;
import static java.lang.String.format;


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
		if (str == null ) {
			return null;
		}
		Matcher m = fractionPattern.matcher(str);
		if (!m.matches()) {
			throw throwFractionFormatException(str);
		}
		Fraction ret = new Fraction(
							   Integer.parseInt(m.group(1)), 
							   Integer.parseInt(m.group(2))
					   );
		return ret;
	}

	public static String loadResource(String resourceName) throws SymfonionException {
		StringBuffer b = new StringBuffer(4096);
		try {
			InputStream is = new BufferedInputStream(ClassLoader.getSystemResourceAsStream(resourceName));
			loadFromInputStream(b, is);
		} catch (IOException e) {
			throw loadResourceException(resourceName, e);
		}
		return b.toString();
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

	private static void loadFromInputStream(StringBuffer b, InputStream is)
			throws UnsupportedEncodingException, IOException {
		Reader r = new InputStreamReader(is, "utf-8");
		int c;
		while ((c = r.read()) != -1) {
			b.append((char)c);
		}
	}

	public static Fraction parseNoteLength(String length) throws SymfonionException {
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
		} else if ("0".equals(length)) {
			ret = new Fraction(0, 1);
		} else {
			ret = null;
		}
		return ret;
	}
	
	public static final byte[] getIntBytes(int input) {
		byte[] retval = new byte[3];
		
		retval[0] = (byte)(input >> 16 & 0xff);
		retval[1] = (byte)(input >> 8 & 0xff);
		retval[2] = (byte)(input & 0xff);
		
		return retval;
	}

	public static void dump(int[] arr) {
		System.out.print(Arrays.toString(arr));
	}
	
	public static int toint(Object o) {
		if (o == null) {
			throw new NullPointerException("null cannot be converted to int.");
		}
		if (o instanceof Number) {
			return ((Number)o).intValue();
		}
		if (o instanceof String) {
			return Integer.parseInt((String)o);
		}
		return Integer.parseInt(o.toString());
	}

}
