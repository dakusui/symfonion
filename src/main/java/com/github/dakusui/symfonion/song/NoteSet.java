package com.github.dakusui.symfonion.song;

import java.util.LinkedList;

import com.github.dakusui.symfonion.core.Fraction;

public class NoteSet extends LinkedList<Note> {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -6221696604950143374L;
	
	Fraction length;
	
	public void setLength(Fraction length) {
		this.length = length;
	}
	
	public Fraction getLength() {
		return this.length;
	}

}
