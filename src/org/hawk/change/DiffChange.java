package org.hawk.change;

import java.util.Collection;

public class DiffChange {
	private int add;
	private int delete;
	private int change;
	private int move;
	private String type;
	private Collection diff;

	public DiffChange() {
		add=delete=change=move=0;
		type="modify";
	}
	public void setAdd(int x) {
		add=x;
	}
	public int getAdd() {
		return add;
	}
	public void setDelete(int x) {
		delete =x;
	}
	public int getDelete() {
		return delete;
	}
	public void setChange(int x) {
		change = x;
	}
	public int getChange() {
		return change;
	}
	public void setMove(int x) {
		move=x;
	}
	public int getMove() {
		return move;
	}
	public void setType(String x ) {
		type=x;
	}
	public String getType() {
		return type;
	}
	public void setDiff(Collection x ) {
		diff=x;
	}
	public Collection getDiff() {
		return diff;
	}
}
