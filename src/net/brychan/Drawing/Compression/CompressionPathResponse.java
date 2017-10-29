package net.brychan.Drawing.Compression;

import net.brychan.Drawing.Coordinate;
import net.brychan.Drawing.DrawingCommand;

import java.util.ArrayList;

public class CompressionPathResponse {
	private Section section;
	private Coordinate link;
	private ArrayList<DrawingCommand> commands;

	public CompressionPathResponse(Section section, Coordinate link, ArrayList<DrawingCommand> commands) {
		this.section = section;
		this.link = link;
		this.commands = commands;
	}

	public Section getSection() {
		return section;
	}

	public Coordinate getLink() {
		return link;
	}

	public ArrayList<DrawingCommand> getCommands() {
		return commands;
	}
}