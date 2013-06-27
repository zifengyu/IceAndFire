package com.sina.dfh98.iceandfire;

public class BookMark {

	public int vol;
	public int chapter;
	public int character;

	private static int[] maxChapter = {73, 69, 81, 45, 72}; 

	public BookMark(int vol, int chapter, int character) {
		this.vol = vol;
		this.chapter = chapter;
		this.character = character;		
	}

	public void toNextChapter() {
		if (chapter < maxChapter[vol - 1] ) {
			++chapter;
			character = 0;
		}
	}

	public void toPreviousChapter() {
		if (chapter > 0) {
			--chapter;		
			character = -1;
		}
	}

}
