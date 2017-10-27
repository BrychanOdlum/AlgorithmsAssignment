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

		Coordinate currentCoordinate = new Coordinate(0, 0);
		int lastColour = bgColour;


		while (currentCoordinate.getX() >= 0 && currentCoordinate.getX() > 0) {



		}


	}

}