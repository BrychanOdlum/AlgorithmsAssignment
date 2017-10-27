package net.brychan;

import net.brychan.Drawing.Compression.CompressionAttempt;
import net.brychan.Drawing.Coordinate;
import net.brychan.Drawing.Direction;
import net.brychan.Drawing.Drawing;

public class Main {

    public static void main(String[] args) {
	    // write your code here

		boolean compression = true;

		if (compression) {


			String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-images/test-image6";
			//String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/pixel-art/pixel-art4";

			Image i = new Image(file);
			CompressionAttempt ca = new CompressionAttempt(i);

		} else {

			String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-instructions";

			Direction dir = Direction.LEFT;
			dir = dir.opposite();
			System.out.println(dir.toString());

			Coordinate cor = new Coordinate(3, 1);
			System.out.println(cor.relative(dir));

			Drawing d = new Drawing(file);


			try {

				Image i = d.draw();
				System.out.println(i.toString());

			} catch (Exception ex) {
				System.out.println(ex);
			}

			System.out.print(d.toString());

		}


    }
}
