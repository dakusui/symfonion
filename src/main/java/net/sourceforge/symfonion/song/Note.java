package net.sourceforge.symfonion.song;

public class Note {
	int key;
	int accent;
	public Note(int key, int accent) {
		this.key = key;
		this.accent = accent;
	}
	public int key() {
		return this.key;
	}
	public int accent() {
		return this.accent;
	}
}