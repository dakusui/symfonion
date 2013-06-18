package com.github.dakusui.symfonion.core;

public class FractionFormatException extends Exception {

	/**
	 * Serial verision UID.
	 */
	private static final long serialVersionUID = -8791776177337740280L;
	private String fraction;
	
	
	public FractionFormatException(String fraction) {
		super(String.format("'{}' is an invalid fraction", fraction));
		this.fraction = fraction;
	}
	
	public String getFractionString() {
		return this.fraction;
	}

}
