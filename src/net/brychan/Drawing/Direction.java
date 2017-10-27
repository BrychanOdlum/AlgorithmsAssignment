package net.brychan.Drawing;

public enum Direction {
	UP {
		public String toString() {
			return "up";
		}

		public Coordinate relative(Coordinate c) {
			return new Coordinate(c.getX(), c.getY() - 1);
		}

		public Direction opposite() {
			return DOWN;
		}
	},
	DOWN {
		public String toString() {
			return "down";
		}

		public Coordinate relative(Coordinate c) {
			return new Coordinate(c.getX(), c.getY() + 1);
		}

		public Direction opposite() {
			return UP;
		}
	},
	LEFT {
		public String toString() {
			return "left";
		}

		public Coordinate relative(Coordinate c) {
			return new Coordinate(c.getX() - 1, c.getY());
		}

		public Direction opposite() {
			return RIGHT;
		}
	},
	RIGHT {
		public String toString() {
			return "right";
		}

		public Coordinate relative(Coordinate c) {
			return new Coordinate(c.getX() + 1, c.getY());
		}

		public Direction opposite() {
			return LEFT;
		}
	};

	public abstract Coordinate relative(Coordinate c);

	public abstract Direction opposite();
}