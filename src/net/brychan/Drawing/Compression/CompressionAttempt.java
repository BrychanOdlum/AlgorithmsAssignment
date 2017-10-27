package net.brychan.Drawing.Compression;

import net.brychan.Drawing.Coordinate;
import net.brychan.Drawing.Direction;
import net.brychan.Drawing.Drawing;
import net.brychan.Drawing.DrawingCommand;
import net.brychan.Image;

import java.util.*;

public class CompressionAttempt {

	private Image image;

	private boolean searching;
	private ArrayList<String> commands;

	private int score; // lowest wins
	private HashSet<CompressionAttempt> children;

	public CompressionAttempt(Image image) {
		this.image = image;


		searching = true;
		children = new HashSet<>();
		commands = new ArrayList<>();

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

		HashSet<CompressionLine> lines = new HashSet<>();

		// Scan vertical
		for (int x = 0; x < image.getWidth(); x++) {
			ArrayList<Coordinate> activeCoords = new ArrayList<>();
			int lastColour = -1;
			for (int y = 0; y < image.getHeight(); y++) {
				Coordinate coord = new Coordinate(x, y);
				int pixelColour = image.getPixel(coord);
				if (lastColour != pixelColour && activeCoords.size() > 0) {
					CompressionLine line = new CompressionLine((ArrayList<Coordinate>) activeCoords.clone(), lastColour);
					lines.add(line);
					activeCoords.clear();
				}
				if (pixelColour != bgColour) {
					activeCoords.add(coord);
				}
				lastColour = pixelColour;
			}
			if (activeCoords.size() > 0) {
				CompressionLine line = new CompressionLine(activeCoords, lastColour);
				lines.add(line);
			}
		}

		// Scan horizontal
		for (int y = 0; y < image.getHeight(); y++) {
			ArrayList<Coordinate> activeCoords = new ArrayList<>();
			int lastColour = -1;
			for (int x = 0; x < image.getWidth(); x++) {
				Coordinate coord = new Coordinate(x, y);
				int pixelColour = image.getPixel(coord);
				if (lastColour != pixelColour && activeCoords.size() > 0) {
					CompressionLine line = new CompressionLine((ArrayList<Coordinate>) activeCoords.clone(), lastColour);
					lines.add(line);
					activeCoords.clear();
				}
				if (pixelColour != bgColour) {
					activeCoords.add(coord);
				}
				lastColour = pixelColour;
			}
			if (activeCoords.size() > 0) {
				CompressionLine line = new CompressionLine(activeCoords, lastColour);
				lines.add(line);
			}
		}

		// Remove unnecessary smaller lines which fall within larger lines
		ArrayList<CompressionLine> oList = new ArrayList(lines);
		oList.sort((line1, line2) -> Integer.compare(line2.length(), line1.length()));

		for (int i = oList.size() - 1; i >= 0; i--) {
			ArrayList<CompressionLine> pList = (ArrayList<CompressionLine>) oList.clone();
			pList.remove(pList.get(i));
			if (coversImage(bgColour, new HashSet<>(pList))) {
				oList = pList;
			}
		}

		for (CompressionLine line : oList) {
			System.out.println(line.length());
			for (Coordinate c : line.coordinates) {
				System.out.println("x: " + c.getX() + ", y: " + c.getY());
			}
		}

		lines = (HashSet<CompressionLine>) new HashSet(oList);



		/*
		 *	STEP 2 - LINKING NODES
		 * 	------------------------------
		 * 	Each node needs to find it's next logical node.
		 * 	Tip: think about distance between, one axis > two axis.
		 * 	Even better if we have to travel no distance!
		 *
		 */

		System.out.println(oList.size());
		System.out.println(coversImage(bgColour, lines));

		System.out.println("totalLines: " + lines.size());

		for (int i = oList.size() - 1; i >= 0; i--) {
			CompressionLine lineP = oList.get(i);

			ArrayList<CompressionLine> potentialNodes = new ArrayList(oList);
			potentialNodes.remove(lineP);

			for (int j = potentialNodes.size() - 1; j >= 0; j--) {
				CompressionLine lineC = potentialNodes.get(j);

				if (lineP.headChild == null && lineC.headChild == null && lineP.head.equals(lineC.head)) {
					System.out.println("head matched to head");
					lineP.headChild = lineC;
					lineC.headChild = lineP;
				}
				if (lineP.headChild == null && lineC.tailChild == null && lineP.head.equals(lineC.tail)) {
					System.out.println("head matched to tail");
					lineP.headChild = lineC;
					lineC.tailChild = lineP;
				}
				if (lineP.tailChild == null && lineC.headChild == null && lineP.tail.equals(lineC.head)) {
					System.out.println("tail matched to head");
					lineP.tailChild = lineC;
					lineC.headChild = lineP;
				}
				if (lineP.tailChild == null && lineC.tailChild == null && lineP.tail.equals(lineC.tail)) {
					System.out.println("tail matched to tail");
					lineP.tailChild = lineC;
					lineC.tailChild = lineP;
				}
			}
		}

		ArrayList<CompressionLine> entryLines = new ArrayList(oList);

		for (int i = entryLines.size() - 1; i >= 0; i--) {
			CompressionLine line = entryLines.get(i);
			if (line.tailChild != null && line.headChild != null) {
				entryLines.remove(line);
			} else {

				if (line.headChild != null && entryLines.contains(line.headChild)) {
					System.out.println("removing head" + line.head);
					entryLines.remove(line.headChild);
				}

				if (line.tailChild != null && entryLines.contains(line.tailChild)) {
					System.out.println("removing tail" + line.tailChild);
					entryLines.remove(line.tailChild);
				}

			}
		}


		System.out.println("entryLines: " + entryLines.size());

		ArrayList<DrawingCommand> allCmds = new ArrayList<>();
		Coordinate currentCoordinate = new Coordinate(0, 0);

		//HashMap<CompressionLine, DrawingCommand>




		// TODO: FIRST PASS ONLY LINK LINES WHICH MATCH BY 1 COMMAND
		// TODO: NEXT PASS ONLY LINK LINES WHICH MATCH BY 2 COMMANDS
		// TODO: NEXT PASS ONLY LINK LINES WHICH MATCH 3 COMMANDS

			while (entryLines.size() > 0) {

				CompressionLine bestLine = null;
				PathResponse bestRes = null;
				int bestCount = 0;

				ArrayList<CompressionLine> possibleLines = new ArrayList<>(entryLines);

				for (int j = 0; j < possibleLines.size(); j++) {

					CompressionLine line = entryLines.get(j);
					PathResponse res = commandsFromToDraw(currentCoordinate, line);

					if (bestLine == null || res.commands.size() < bestCount) {
						bestLine = line;
						bestRes = res;
						bestCount = res.commands.size();
					}

				}

				if (bestRes == null) {
					continue;
				}

				allCmds.addAll(bestRes.commands);

				currentCoordinate = bestRes.link == bestLine.head ? bestLine.tail : bestLine.head;


				if (bestRes.section == Section.HEAD) {
					bestLine.head = bestRes.link;
				} else {
					bestLine.tail = bestRes.link;
				}

				entryLines.remove(bestLine);


				CompressionLine lastLine = bestLine;
				CompressionLine nextLine = bestLine.headChild != null ? bestLine.headChild : bestLine.tailChild;
				Coordinate connectionCoordinate = null;
				if (nextLine != null) {
					if (nextLine.head.equals(lastLine.head) || nextLine.head.equals(lastLine.tail)) {
						connectionCoordinate = nextLine.head;
					} else if (nextLine.tail.equals(lastLine.head) || nextLine.tail.equals(lastLine.tail)) {
						connectionCoordinate = nextLine.tail;
					}
				}


				ArrayList<CompressionLine> alreadyDrawn = new ArrayList<>();

				while (nextLine != null) {

					if (alreadyDrawn.contains(nextLine)) {
						nextLine = null;;
						continue;
					}
					alreadyDrawn.add(nextLine);

					Coordinate distantCoordinate = nextLine.head.equals(connectionCoordinate) ? nextLine.tail : nextLine.head;

					System.out.println("in here" + connectionCoordinate.toString() + ", " + distantCoordinate.toString());

					if (connectionCoordinate.getX() != distantCoordinate.getX()) {
						if (connectionCoordinate.getX() < distantCoordinate.getX()) {
							// If first x is to the left of the last one
							allCmds.add(new DrawingCommand(Direction.RIGHT, nextLine.length() - 1, true, nextLine.colour));
							if (new DrawingCommand(Direction.RIGHT, nextLine.length() - 1, true, nextLine.colour).toString().equals("left 11 1")) {
								System.out.println("OURERROR::: here1");
							}
						} else {
							allCmds.add(new DrawingCommand(Direction.LEFT, nextLine.length() - 1, true, nextLine.colour));
							if (new DrawingCommand(Direction.LEFT, nextLine.length() - 1, true, nextLine.colour).toString().equals("left 11 1")) {
								System.out.println("OURERROR::: here2");
							}
						}
					} else if (connectionCoordinate.getY() != distantCoordinate.getY()) {
						if (connectionCoordinate.getY() < distantCoordinate.getY()) {
							// If first y is above of the last one
							allCmds.add(new DrawingCommand(Direction.DOWN, nextLine.length() - 1, true, nextLine.colour));
							if (new DrawingCommand(Direction.DOWN, nextLine.length() - 1, true, nextLine.colour).toString().equals("left 11 1")) {
								System.out.println("OURERROR::: here3");
							}
						} else {
							allCmds.add(new DrawingCommand(Direction.UP, nextLine.length() - 1, true, nextLine.colour));
							if (new DrawingCommand(Direction.UP, nextLine.length() - 1, true, nextLine.colour).toString().equals("left 11 1")) {
								System.out.println("OURERROR::: here4");
							}
						}
					}
					currentCoordinate = distantCoordinate;

					entryLines.remove(nextLine);


					if (nextLine.headChild != null && nextLine.headChild.equals(lastLine)) {
						nextLine.headChild = null;
					}
					if (nextLine.tailChild != null && nextLine.tailChild.equals(lastLine)) {
						nextLine.tailChild = null;
					}


					lastLine = nextLine;
					nextLine = nextLine.headChild != null ? nextLine.headChild : nextLine.tailChild;
					if (nextLine != null) {
						if (nextLine.head.equals(lastLine.head) || nextLine.head.equals(lastLine.tail)) {
							connectionCoordinate = nextLine.head;
						} else if (nextLine.tail.equals(lastLine.head) || nextLine.tail.equals(lastLine.tail)) {
							connectionCoordinate = nextLine.tail;
						}
					}


					System.out.println("-----");
				}


			}





		/*

		for (int i = 0; i < entryLines.size(); i++) {

			CompressionLine line = entryLines.get(i);

			PathResponse res2 = commandsFromToDraw(currentCoordinate, line);

			allCmds.addAll(res2.commands);
			currentCoordinate = res2.link == line.head ? line.tail : line.head;

			if (res2.section == Section.HEAD) {
				entryLines.get(i).head = res2.link;
			} else if (res2.section == Section.TAIL) {
				entryLines.get(i).tail = res2.link;
			}


		}

		*/




		System.out.println("------------");

		this.commands.add(Integer.toString(image.getHeight()));
		this.commands.add(Integer.toString(image.getWidth()));
		this.commands.add(bgColour < 10 ? Integer.toString(bgColour) : bgColour == 10 ? "a" : bgColour == 11 ? "b" : bgColour == 12 ? "c" : bgColour == 13 ? "d" : bgColour == 14 ? "e" : "f");

		System.out.println(image.getHeight());
		System.out.println(image.getWidth());

		System.out.println(bgColour < 10 ? bgColour : bgColour == 10 ? "a" : bgColour == 11 ? "b" : bgColour == 12 ? "c" : bgColour == 13 ? "d" : bgColour == 14 ? "e" : "f");
		for (DrawingCommand cmd : allCmds) {
			System.out.println(cmd);
			this.commands.add(cmd.toString());
		}
		System.out.println("-> CMD COUNT: " + allCmds.size());



		// TODO: First find those adjacent
		// TODO: Then find those one command away
		// TODO: Then find those two commands away

		System.out.println(entryLines.size());



		System.out.println("COMPRESSION COMPLETE.");
	}


	private PathResponse commandsFromToDraw(Coordinate origin, CompressionLine line) {

		ArrayList<DrawingCommand> commands = new ArrayList<>();
		Coordinate link = null;
		Section section = null;

		if (line.tailChild == null) {
			System.out.println("here");
			for (Coordinate coordinate : line.coordinates) {
				System.out.println(coordinate.toString());
			}
			System.out.println(line.getEntryDirection());
			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.HORIZONTAL) {
				System.out.println("here2");
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.coordinates.get(line.coordinates.size() - 1).relative(Direction.RIGHT);

				boolean isAdjacentY = Math.abs(origin.getY() - line.tail.getY()) == 1 && origin.getX() == line.tail.getX();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, false, line.colour);
					if (cmdX.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here5");
					}
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, isAdjacentY, line.colour);
					if (cmdY.toString().equals("left 11 2")) {
						System.out.println("OURERROR::: here6");
					}
				}

				cmdLine = new DrawingCommand(Direction.LEFT, line.length() - (isAdjacentY ? 1 : 0), true, line.colour);


				if (cmdLine.toString().equals("left 11 1")) {
					System.out.println("OURERROR::: here7: " + entryCoord.toString());
					System.out.println("OURERROR::: here7: " + cmdLine.toString());
					System.out.println("OURERROR::: here7: " + line.coordinates.get(0).toString());
					System.out.println("OURERROR::: here7: " + line.coordinates.get(line.length() - 1).toString());
				}

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.tail;
					section = Section.TAIL;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}


			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.VERTICAL) {
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.tail.relative(Direction.DOWN);

				boolean isAdjacentX = Math.abs(origin.getX() - line.tail.getX()) == 1 && origin.getY() == line.tail.getY();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, isAdjacentX, line.colour);
					if (cmdX.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here8");
					}
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, false, line.colour);
					if (cmdY.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here9");
					}
				}

				cmdLine = new DrawingCommand(Direction.UP, line.length() - (isAdjacentX ? 1 : 0), true, line.colour);
				if (cmdLine.toString().equals("left 11 1")) {
					System.out.println("OURERROR::: here10");
				}

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.tail;
					section = Section.TAIL;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}
		}

		if (line.headChild == null) {
			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.HORIZONTAL) {
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.head.relative(Direction.LEFT);

				boolean isAdjacentY = Math.abs(origin.getY() - line.head.getY()) == 1 && origin.getX() == line.head.getX();

				// If they're not in the same row...
				if (!isAdjacentY && origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, false, line.colour);
					if (cmdX.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here1");
					}
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, isAdjacentY, line.colour);
					if (cmdY.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here1");
					}
				}

				cmdLine = new DrawingCommand(Direction.RIGHT, line.length() - (isAdjacentY ? 1 : 0), true, line.colour);
				if (cmdLine.toString().equals("left 11 1")) {
					System.out.println("OURERROR::: here1");
				}

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.head;
					section = Section.HEAD;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}


			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.VERTICAL) {
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.head.relative(Direction.UP);

				boolean isAdjacentX = Math.abs(origin.getX() - line.head.getX()) == 1 && origin.getY() == line.head.getY();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, isAdjacentX, line.colour);
					if (cmdX.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here1");
					}
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, false, line.colour);
					if (cmdY.toString().equals("left 11 1")) {
						System.out.println("OURERROR::: here1");
					}
				}

				cmdLine = new DrawingCommand(Direction.DOWN, line.length() - (isAdjacentX ? 1 : 0), true, line.colour);
				if (cmdLine.toString().equals("left 11 1")) {
					System.out.println("OURERROR::: here1");
				}

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.head;
					section = Section.HEAD;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}
		}

		return new PathResponse(section, link, commands);
	}

	public ArrayList<String> getCommands() {
		return commands;
	}

	private boolean coversImage(int bgColour, HashSet<CompressionLine> lines) {
		HashSet<Coordinate> pixels = new HashSet<>();
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				if (image.getPixel(new Coordinate(x, y)) != bgColour) {
					pixels.add(new Coordinate(x, y));
				}
			}
		}
		for (CompressionLine line : lines) {
			for (Coordinate coordinate : line.coordinates) {
				pixels.remove(coordinate);
			}
		}
		return pixels.size() == 0;
	}

	class PathResponse {

		private Section section;
		private Coordinate link;
		private ArrayList<DrawingCommand> commands;

		private PathResponse(Section section, Coordinate link, ArrayList<DrawingCommand> commands) {
			this.section = section;
			this.link = link;
			this.commands = commands;
		}

	}

	class CompressionLine {
		private ArrayList<Coordinate> coordinates;
		private int colour;

		private Coordinate head;
		private Coordinate tail;

		private CompressionLine headChild; // line connected at head
		private CompressionLine tailChild; // line connected at tail

		private CompressionLine(ArrayList<Coordinate> coordinates, int colour) {
			this.coordinates = coordinates;
			this.colour = colour;

			head = coordinates.get(0);
			tail = coordinates.get(coordinates.size() - 1);
		}

		private DrawDirection getEntryDirection() {
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

		public int length() {
			return coordinates.size();
		}
	}




	enum Section {
		HEAD,
		TAIL,
		MIDDLE,
		UNKNOWN
	}

	enum DrawDirection {
		LEFT,
		RIGHT,
		UP,
		DOWN,
		VERTICAL,
		HORIZONTAL,
		ANY,
		UNKNOWN
	}


}
