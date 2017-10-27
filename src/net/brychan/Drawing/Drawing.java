package net.brychan.Drawing;

import net.brychan.Drawing.Exceptions.BadCommand;
import net.brychan.Drawing.Exceptions.PixelOutOfBounds;
import net.brychan.Image;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Drawing {

	private int height;
	private int width;
	private int background;
	private ArrayList<DrawingCommand> commands;

	// Read in an ArrayList of drawing commands from a file. There should be
	// exactly 1 command per line. The first two lines should be 2 numbers for
	// the height and width rather than commands. The third line is the
	// background colour.
	public Drawing(String filename) {
		commands = new ArrayList<>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			String s = in.readLine();
			try {
				height = Integer.parseInt(s);
			}
			catch(NumberFormatException e) {
				System.out.println("Expected the height on the first line: " + s);
				System.exit(1);
			}

			s = in.readLine();
			try {
				width = Integer.parseInt(s);
			}
			catch(NumberFormatException e) {
				System.out.println("Expected the width on the second line: " + s);
				System.exit(1);
			}

			s = in.readLine();
			try {
				background = Integer.parseInt(s,16);
			}
			catch(NumberFormatException e) {
				System.out.println("Expected the background colour on the third line: " + s);
				System.exit(1);
			}

			for (s = in.readLine(); s != null; s = in.readLine()) {
				commands.add(new DrawingCommand(s));
			}
		}
		catch (FileNotFoundException e) {
			System.out.println("File not found: " + filename);
			System.exit(1);
		}
		catch (IOException e) {
			System.exit(2);
		}
	}

	// create an empty drawing of the given dimensions
	public Drawing(int h, int w, int b) {
		height = h;
		width = w;
		assert (b >= 0 && b <= 15);
		background = b;
		commands = new ArrayList<DrawingCommand>();
	}

	public void addCommand(DrawingCommand c) {
		commands.add(c);
	}

	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append(height + "\n");
		s.append(width + "\n");
		s.append(Integer.toHexString(background) + "\n");
		for (DrawingCommand command : commands) {
			s.append(command.toString() + "\n");
		}
		return s.toString();
	}

	// Task 1: Implement the draw method to create and return an image by
	// executing all of the drawing commands in the commands field.
	// Throw a BadCommand exception if any command tries to paint outside of the
	// picture's dimensions, as given by the height and width field.
	// It is ok for the position to leave the dimensions, as long it no attempt
	// is made to paint outside of the picture.
	// (5 marks)
	public Image draw() throws BadCommand {
		Image image = new Image(height, width, background);

		Coordinate coordinate = new Coordinate(0, 0);

		for (DrawingCommand command : commands) {
			Direction direction = command.getDirection();
			int distance = command.getDistance();
			int colour = command.getColour();
			boolean shouldPaint = command.shouldPaint();

			// Normalise direction / distance
			if (distance < 0) {
				direction = direction.opposite();
				distance = 0 - distance;
			}

			try {
				if (distance == 0 && shouldPaint) {
					image.setPixel(coordinate, colour);
				}

				while (distance > 0) {
					coordinate = coordinate.relative(direction);

					if (shouldPaint) {
						image.setPixel(coordinate, colour);
					}

					distance--;
				}
			} catch (PixelOutOfBounds ex) {
				throw new BadCommand();
			}


		}

		return image;
	}

	public static void main(String[] args) {
		// A simple test to read in an file of drawing commands and print it out.
		Drawing d = new Drawing(args[0]);
		System.out.print(d.toString());
	}
}
