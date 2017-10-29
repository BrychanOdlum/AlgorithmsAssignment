package net.brychan.Drawing.Compression;

import net.brychan.Drawing.Coordinate;
import net.brychan.Drawing.Direction;
import net.brychan.Drawing.DrawingCommand;
import net.brychan.Image;

import java.util.*;

public class CompressionAttempt {

	private Image image;

	private ArrayList<String> commands;
	private ArrayList<DrawingCommand> drawingCommands;
	private int bgColour;

	public CompressionAttempt(Image image) {
		this.image = image;

		commands = new ArrayList<>();

		for (int bgColour = 0; bgColour <= 15; bgColour++) {


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

			lines = (HashSet<CompressionLine>) new HashSet(oList);



		/*
		 *	STEP 2 - LINKING NODES
		 * 	------------------------------
		 * 	Each node needs to find it's next logical node.
		 * 	Tip: think about distance between, one axis > two axis.
		 * 	Even better if we have to travel no distance!
		 *
		 */

			for (int i = oList.size() - 1; i >= 0; i--) {
				CompressionLine lineP = oList.get(i);

				ArrayList<CompressionLine> potentialNodes = new ArrayList(oList);
				potentialNodes.remove(lineP);

				for (int j = potentialNodes.size() - 1; j >= 0; j--) {
					CompressionLine lineC = potentialNodes.get(j);

					if (lineP.getHeadChild() == null && lineC.getHeadChild() == null && lineP.getHead().equals(lineC.getHead())) {
						lineP.setHeadChild(lineC);
						lineC.setHeadChild(lineP);
					}
					if (lineP.getHeadChild() == null && lineC.getTailChild() == null && lineP.getHead().equals(lineC.getTail())) {
						lineP.setHeadChild(lineC);
						lineC.setTailChild(lineP);
					}
					if (lineP.getTailChild() == null && lineC.getHeadChild() == null && lineP.getTail().equals(lineC.getHead())) {
						lineP.setTailChild(lineC);
						lineC.setHeadChild(lineP);
					}
					if (lineP.getTailChild() == null && lineC.getTailChild() == null && lineP.getTail().equals(lineC.getTail())) {
						lineP.setTailChild(lineC);
						lineC.setTailChild(lineP);
					}
				}
			}

			// Remove linked lines, not needed to search both
			ArrayList<CompressionLine> entryLines = new ArrayList(oList);

			for (int i = entryLines.size() - 1; i >= 0; i--) {
				CompressionLine line = entryLines.get(i);
				if (line.getTailChild() != null && line.getHeadChild() != null) {
					entryLines.remove(line);
				} else {

					if (line.getHeadChild() != null && entryLines.contains(line.getHeadChild())) {
						entryLines.remove(line.getHeadChild());
					}

					if (line.getTailChild() != null && entryLines.contains(line.getTailChild())) {
						entryLines.remove(line.getTailChild());
					}

				}
			}

			ArrayList<DrawingCommand> allCmds = new ArrayList<>();
			Coordinate currentCoordinate = new Coordinate(0, 0);

			// TODO: FIRST PASS ONLY LINK LINES WHICH MATCH BY 1 COMMAND
			// TODO: NEXT PASS ONLY LINK LINES WHICH MATCH BY 2 COMMANDS
			// TODO: NEXT PASS ONLY LINK LINES WHICH MATCH 3 COMMANDS

			while (entryLines.size() > 0) {

				CompressionLine bestLine = null;
				CompressionPathResponse bestRes = null;
				int bestCount = 0;

				ArrayList<CompressionLine> possibleLines = new ArrayList<>(entryLines);

				for (int j = 0; j < possibleLines.size(); j++) {

					CompressionLine line = entryLines.get(j);
					CompressionPathResponse res = commandsFromToDraw(currentCoordinate, line);

					if (bestLine == null || res.getCommands().size() < bestCount) {
						bestLine = line;
						bestRes = res;
						bestCount = res.getCommands().size();
					}

				}

				if (bestRes == null) {
					continue;
				}

				allCmds.addAll(bestRes.getCommands());

				currentCoordinate = bestRes.getLink() == bestLine.getHead() ? bestLine.getTail() : bestLine.getHead();

				if (bestRes.getSection() == Section.HEAD) {
					bestLine.setHead(bestRes.getLink());
				} else {
					bestLine.setTail(bestRes.getLink());
				}

				entryLines.remove(bestLine);


				CompressionLine lastLine = bestLine;
				CompressionLine nextLine = bestLine.getHeadChild() != null ? bestLine.getHeadChild() : bestLine.getTailChild();
				Coordinate connectionCoordinate = null;
				if (nextLine != null) {
					if (nextLine.getHead().equals(lastLine.getHead()) || nextLine.getHead().equals(lastLine.getTail())) {
						connectionCoordinate = nextLine.getHead();
					} else if (nextLine.getTail().equals(lastLine.getHead()) || nextLine.getTail().equals(lastLine.getTail())) {
						connectionCoordinate = nextLine.getTail();
					}
				}

				ArrayList<CompressionLine> alreadyDrawn = new ArrayList<>();

				while (nextLine != null) {

					if (alreadyDrawn.contains(nextLine)) {
						nextLine = null;;
						continue;
					}
					alreadyDrawn.add(nextLine);

					Coordinate distantCoordinate = nextLine.getHead().equals(connectionCoordinate) ? nextLine.getTail() : nextLine.getHead();

					if (connectionCoordinate.getX() != distantCoordinate.getX()) {
						if (connectionCoordinate.getX() < distantCoordinate.getX()) {
							allCmds.add(new DrawingCommand(Direction.RIGHT, nextLine.length() - 1, true, nextLine.getColour()));
						} else {
							allCmds.add(new DrawingCommand(Direction.LEFT, nextLine.length() - 1, true, nextLine.getColour()));
						}
					} else if (connectionCoordinate.getY() != distantCoordinate.getY()) {
						if (connectionCoordinate.getY() < distantCoordinate.getY()) {
							allCmds.add(new DrawingCommand(Direction.DOWN, nextLine.length() - 1, true, nextLine.getColour()));
						} else {
							allCmds.add(new DrawingCommand(Direction.UP, nextLine.length() - 1, true, nextLine.getColour()));
						}
					}

					currentCoordinate = distantCoordinate;

					entryLines.remove(nextLine);

					if (nextLine.getHeadChild() != null && nextLine.getHeadChild().equals(lastLine)) {
						nextLine.setHeadChild(null);
					}
					if (nextLine.getTailChild() != null && nextLine.getTailChild().equals(lastLine)) {
						nextLine.setTailChild(null);
					}

					lastLine = nextLine;
					nextLine = nextLine.getHeadChild() != null ? nextLine.getHeadChild() : nextLine.getTailChild();
					if (nextLine != null) {
						if (nextLine.getHead().equals(lastLine.getHead()) || nextLine.getHead().equals(lastLine.getTail())) {
							connectionCoordinate = nextLine.getHead();
						} else if (nextLine.getTail().equals(lastLine.getHead()) || nextLine.getTail().equals(lastLine.getTail())) {
							connectionCoordinate = nextLine.getTail();
						}
					}
				}
			}

			ArrayList<String> attemptedLines = new ArrayList<>();
			attemptedLines.add(Integer.toString(image.getHeight()));
			attemptedLines.add(Integer.toString(image.getWidth()));
			attemptedLines.add(bgColour < 10 ? Integer.toString(bgColour) : bgColour == 10 ? "a" : bgColour == 11 ? "b" : bgColour == 12 ? "c" : bgColour == 13 ? "d" : bgColour == 14 ? "e" : "f");

			for (DrawingCommand cmd : allCmds) {
				attemptedLines.add(cmd.toString());
			}

			if (this.commands.size() == 0 || attemptedLines.size() < this.commands.size()) {
				this.commands = attemptedLines;
				this.drawingCommands = allCmds;
				this.bgColour = bgColour;
			}

		}

		/*
		System.out.println("-> CMD COUNT: " + (this.commands.size() - 3)); // Subtract headers
		System.out.println("---------------------------");
		System.out.println("COMPRESSION COMPLETE.");
		System.out.println("---------------------------");
		*/
	}


	private CompressionPathResponse commandsFromToDraw(Coordinate origin, CompressionLine line) {

		ArrayList<DrawingCommand> commands = new ArrayList<>();
		Coordinate link = null;
		Section section = null;

		if (line.getTailChild() == null) {
			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.HORIZONTAL) {
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.getCoordinates().get(line.getCoordinates().size() - 1).relative(Direction.RIGHT);

				boolean isAdjacentY = Math.abs(origin.getY() - line.getTail().getY()) == 1 && origin.getX() == line.getTail().getX();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, false, line.getColour());
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, isAdjacentY, line.getColour());
				}

				cmdLine = new DrawingCommand(Direction.LEFT, line.length() - (isAdjacentY ? 1 : 0), true, line.getColour());

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.getTail();
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
				Coordinate entryCoord = line.getTail().relative(Direction.DOWN);

				boolean isAdjacentX = Math.abs(origin.getX() - line.getTail().getX()) == 1 && origin.getY() == line.getTail().getY();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, isAdjacentX, line.getColour());
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, false, line.getColour());
				}

				cmdLine = new DrawingCommand(Direction.UP, line.length() - (isAdjacentX ? 1 : 0), true, line.getColour());

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.getTail();
					section = Section.TAIL;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}
		}

		if (line.getHeadChild() == null) {
			if (line.getEntryDirection() == DrawDirection.ANY || line.getEntryDirection() == DrawDirection.HORIZONTAL) {
				ArrayList<DrawingCommand> cCmd = new ArrayList<>();

				DrawingCommand cmdLine = null;
				DrawingCommand cmdX = null;
				DrawingCommand cmdY = null;

				// Move along X axis to entry point...
				Coordinate entryCoord = line.getHead().relative(Direction.LEFT);

				boolean isAdjacentY = Math.abs(origin.getY() - line.getHead().getY()) == 1 && origin.getX() == line.getHead().getX();

				// If they're not in the same row...
				if (!isAdjacentY && origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, false, line.getColour());
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, isAdjacentY, line.getColour());
				}

				cmdLine = new DrawingCommand(Direction.RIGHT, line.length() - (isAdjacentY ? 1 : 0), true, line.getColour());

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.getHead();
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
				Coordinate entryCoord = line.getHead().relative(Direction.UP);

				boolean isAdjacentX = Math.abs(origin.getX() - line.getHead().getX()) == 1 && origin.getY() == line.getHead().getY();

				// If they're not in the same row...
				if (origin.getX() != entryCoord.getX()) {
					Direction cmdXDirection = entryCoord.getX() > origin.getX() ? Direction.RIGHT : Direction.LEFT;
					int cmdXDistance = Math.abs(entryCoord.getX() - origin.getX());
					cmdX = new DrawingCommand(cmdXDirection, cmdXDistance, isAdjacentX, line.getColour());
				}

				// If they're not in the same column...
				if (origin.getY() != entryCoord.getY()){
					Direction cmdYDirection = entryCoord.getY() > origin.getY() ? Direction.DOWN : Direction.UP;
					int cmdYDistance = Math.abs(entryCoord.getY() - origin.getY());
					cmdY = new DrawingCommand(cmdYDirection, cmdYDistance, false, line.getColour());
				}

				cmdLine = new DrawingCommand(Direction.DOWN, line.length() - (isAdjacentX ? 1 : 0), true, line.getColour());

				cCmd.add(cmdX);
				cCmd.add(cmdY);
				cCmd.add(cmdLine);
				if ((commands.size() == 0) || (commands.size() > cCmd.size())) {
					link = line.getHead();
					section = Section.HEAD;
					for (DrawingCommand cmd : cCmd) {
						if (cmd != null) {
							commands.add(cmd);
						}
					}
				}
			}
		}

		return new CompressionPathResponse(section, link, commands);
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
			for (Coordinate coordinate : line.getCoordinates()) {
				pixels.remove(coordinate);
			}
		}
		return pixels.size() == 0;
	}

	public Image getImage() {
		return image;
	}

	public ArrayList<DrawingCommand> getDrawingCommands() {
		return drawingCommands;
	}

	public int getBgColour() {
		return bgColour;
	}


}
