package net.brychan.Drawing.Compression;

import net.brychan.Drawing.Coordinate;
import net.brychan.Drawing.Direction;
import net.brychan.Drawing.Drawing;
import net.brychan.Drawing.DrawingCommand;
import net.brychan.Image;

import java.util.*;

public class CompressionAttemptRLE {

	private Image image;

	private boolean searching;

	private int score; // lowest wins
	private HashSet<CompressionAttempt> children;

	public CompressionAttemptRLE(Image image) {
		this.image = image;

		searching = true;
		children = new HashSet<>();


		/*
		 *	STEP 0 - BACKGROUND COLOUR
		 * 	------------------------------
		 * 	Find most likely background colour based on colour frequency.
		 *
		 */

		HashMap<Integer, Integer> colourCount = new HashMap<>();

		for (int y = 0; y < image.getPixels().length; y++) {
			for (int x = 0; x < image.getPixels()[y].length; x++) {
				if (!colourCount.containsKey(image.getPixels()[y][x])) {
					colourCount.put(image.getPixels()[y][x], 0);
				} else {
					Integer val = colourCount.get(image.getPixels()[y][x]);
					colourCount.put(image.getPixels()[y][x], val + 1);
				}
			}
		}

		int bgColour = 0, wCount = 0;
		for (Map.Entry<Integer, Integer> colour : colourCount.entrySet()) {
			System.out.println("colour " + colour.getKey() + ": " + colour.getValue());
			if (colour.getValue() > wCount) {
				bgColour = colour.getKey();
				wCount = colour.getValue();
			}
		}

		System.out.println("BG: " + bgColour);


		/*
		 *	STEP 1 - FIND LINES
		 * 	------------------------------
		 * 	Find all full lines which make up the image. Longest lines,
		 * 	no clipping is done. Should consider this!!!
		 *
		 */

		ArrayList<DrawingCommand> commands = new ArrayList<>();

		Coordinate currentCoordinate = new Coordinate(0, 0);
		int lastColour = bgColour;



		int emptyRows = 0;
		Direction xDir = Direction.RIGHT;

		if (image.getPixel(currentCoordinate) != bgColour) {
			commands.add(new DrawingCommand(Direction.DOWN, 0, true, image.getPixel(currentCoordinate)));
		}

		/*

		while (currentCoordinate.getY() < image.getHeight()) {

			int lColour = bgColour;
			int lCount = 0;

			int lastCMDCounts = commands.size();

			while (currentCoordinate.getX() < image.getWidth() && currentCoordinate.getX() >= 0) {

				int pColour = image.getPixel(currentCoordinate);

				if (pColour != lColour) {
					commands.add(new DrawingCommand(xDir, lCount, lColour != bgColour, lColour != bgColour ? lColour : 0));
					lCount = 0;
				}

				lColour = pColour;
				lCount++;

				currentCoordinate.setX(currentCoordinate.getX() + (xDir == Direction.RIGHT ? 1 : -1));
			}

			if (lCount != 0) {
				commands.add(new DrawingCommand(xDir, lCount, lColour != bgColour, lColour != bgColour ? lColour : 0));
			}

			if (commands.size() == lastCMDCounts) {
				emptyRows++;
			}

			xDir = xDir == Direction.RIGHT ? Direction.LEFT : Direction.RIGHT;

			currentCoordinate.setX(xDir == Direction.RIGHT ? 0 : image.getWidth() - 1);
			currentCoordinate.setY(currentCoordinate.getY() + 1);

			if (currentCoordinate.getY() < image.getHeight()) {
				if (image.getPixel(currentCoordinate) != bgColour) {
					commands.add(new DrawingCommand(Direction.DOWN, 1, true, image.getPixel(currentCoordinate)));
				}
			}

		}

		System.out.println("Done...");

		for (DrawingCommand cmd : commands) {
			System.out.println(cmd.toString());
		}
		*/


	}

}