package net.brychan.Drawing;

// A single drawing command. Which direction to go in, how far to move, and
// whether to paint all of the spaces in-between, or leave them as-is. Also
// indicate which colour if painting.
public class DrawingCommand {
	private Direction dir;
	private int distance;
	private boolean paint;
	private int colour;

	// Read in a Drawing commands from a string
	// The format should be "direction distance colour" or "direction distance"
	// if moving without painting, for example
	// left 10 3
	// up 1
	// up 2 c
	public DrawingCommand(String s) {
		// Split the string by whitespace
		String[] elems = s.split("\\s");

		if (elems.length != 3 && elems.length != 2) {
			System.out.println("Bad command (should have 2 or 3 parts): " + s);
			System.exit(1);
		}

		if (elems[0].equals("up")) {
			dir = Direction.UP;
		} else if (elems[0].equals("down")) {
			dir = Direction.DOWN;
		} else if (elems[0].equals("left")) {
			dir = Direction.LEFT;
		} else if (elems[0].equals("right")) {
			dir = Direction.RIGHT;
		} else {
			System.out.println("Bad direction (should be up, down, left, or right): " + elems[0]);
			System.exit(1);
		}

		try {
			distance = Integer.parseInt(elems[1]);
		}
		catch(NumberFormatException e) {
			System.out.println("Bad distance (should be a number): " + elems[1]);
			System.exit(1);
		}
		// Check for the optional colour
		if (elems.length == 2) {
			paint = false;
		} else {
			paint = true;
			try {
				colour = Integer.parseInt(elems[2], 16);
				if (colour < 0 || colour > 15)
					throw new NumberFormatException();
			}
			catch(NumberFormatException e) {
				System.out.println("Bad colour (should be a hex number betweeen 0 and f): " + elems[2]);
				System.exit(1);
			}
		}
	}

	public DrawingCommand(Direction dir, int distance, boolean paint, int colour) {
		this.dir = dir;
		this.distance = distance;
		this.paint = paint;
		this.colour = colour;
	}

	public Direction getDirection() {
		return dir;
	}

	public int getDistance() {
		return distance;
	}

	public boolean shouldPaint() {
		return paint;
	}

	public int getColour() {
		return colour;
	}


	public String toString() {
		return (dir.toString() + " " + distance + " " + (paint?Integer.toHexString(colour):""));
	}
}