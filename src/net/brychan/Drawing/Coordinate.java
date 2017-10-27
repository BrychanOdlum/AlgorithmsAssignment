package net.brychan.Drawing;

public class Coordinate {

	private int x;
	private int y;

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate relative(Direction d) {
		return d.relative(this);
	}

	public int getX() {

		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {

		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	public String toString() {
		return "{ x: " + getX() + ", y: " + getY() + " }"; // TODO: String interpolation?!
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Coordinate that = (Coordinate) o;

		if (getX() != that.getX()) return false;
		return y == that.y;
	}

	@Override
	public int hashCode() {
		int result = getX();
		result = 31 * result + y;
		return result;
	}


}
