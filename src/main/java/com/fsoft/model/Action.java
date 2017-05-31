package com.fsoft.model;

public class Action implements Comparable<Action>{
	
	private int index;
	
	private String type;
	
	private String start;
	
	private String fileName;
	
	private String end;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	@Override
	public int compareTo(Action o) {
		return new Integer(this.index).compareTo(new Integer(o.index));
	}
}
