package com.github.dakusui.symfonion.exceptions;

import java.io.Serial;

import static java.lang.String.format;

public class FractionFormatException extends Exception {

	/**
	 * A serial version UID string.
	 */
	@Serial
	private static final long serialVersionUID = -8791776177337740280L;
	private final String fraction;
	
	
	public FractionFormatException(String fraction) {
		super(format("'%s' is an invalid fraction", fraction));
		this.fraction = fraction;
	}
	
	public String getFractionString() {
		return this.fraction;
	}

}
