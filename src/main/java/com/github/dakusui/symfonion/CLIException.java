package com.github.dakusui.symfonion;

import com.github.dakusui.symfonion.core.exceptions.SymfonionException;

public class CLIException extends SymfonionException {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 952596486373752642L;

	public CLIException(String msg, Throwable e) {
		super(msg, e);
	}

	public CLIException(String msg) {
		super(msg);
	}

}
