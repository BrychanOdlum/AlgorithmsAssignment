package net.brychan.Drawing.Compression;

import net.brychan.Drawing.Coordinate;

import java.util.ArrayList;

public class CompressionLine {
	private ArrayList<Coordinate> coordinates;
	private int colour;

	private Coordinate head;
	private Coordinate tail;

	private CompressionLine headChild; // line connected at head
	private CompressionLine tailChild; // line connected at tail

	public CompressionLine(ArrayList<Coordinate> coordinates, int colour) {
		this.coordinates = coordinates;
		this.colour = colour;

		head = coordinates.get(0);
		tail = coordinates.get(coordinates.size() - 1);
	}

	public DrawDirection getEntryDirection() {
		if (coordinates.size() == 1) {
			return DrawDirection.ANY;
		}

		if (coordinates.get(0).getX() != coordinates.get(1).getX()) {
			if (headChild == null && tailChild == null) {
				return DrawDirection.HORIZONTAL; // Any horizontal is fine
			} else if (headChild == null) {
				return DrawDirection.HORIZONTAL; // Left side is free, lets start there.
			} else {
				return DrawDirection.HORIZONTAL; // Right side must be free
			}
		}

		if (coordinates.get(0).getY() != coordinates.get(1).getY()) {
			if (headChild == null && tailChild == null) {
				return DrawDirection.VERTICAL; // Any horizontal is fine
			} else if (headChild == null) {
				return DrawDirection.VERTICAL; // Top is free, lets go downwards
			} else {
				return DrawDirection.VERTICAL; // Bottom is free, lets go upwards
			}
		}

		return DrawDirection.UNKNOWN;
	}

	public void setHeadChild(CompressionLine line) {
		headChild = line;
	}

	public void setTailChild(CompressionLine line) {
		tailChild = line;
	}

	public void setHead(Coordinate coordinate) {
		head = coordinate;
	}

	public void setTail(Coordinate coordinate) {
		tail = coordinate;
	}

	public ArrayList<Coordinate> getCoordinates() {
		return coordinates;
	}

	public int getColour() {
		return colour;
	}

	public Coordinate getHead() {
		return head;
	}

	public Coordinate getTail() {
		return tail;
	}

	public CompressionLine getHeadChild() {
		return headChild;
	}

	public CompressionLine getTailChild() {
		return tailChild;
	}

	public int length() {
		return coordinates.size();
	}
}
