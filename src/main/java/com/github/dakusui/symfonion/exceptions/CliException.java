package com.github.dakusui.symfonion.exceptions;

import java.io.Serial;

public class CliException extends SymfonionException {
	/**
	 * Serial version UID.
	 */
	@Serial
	private static final long serialVersionUID = 952596486373752642L;

	public CliException(String msg, Throwable e) {
		super(msg, e);
	}

	public CliException(String msg) {
		super(msg);
	}

}
