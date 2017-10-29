package net.brychan;

import net.brychan.Drawing.Drawing;

public class Main {

    public static void main(String[] args) {
	    // write your code here



		String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-images/test-image3";
		Image im1 = new Image(file);

		try {
			Drawing drawing = im1.compress();
			//im1.toPNG("/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-images/test-imag4");
		} catch (Exception e) {

		}

		/*
		boolean compression = true;

		if (compression) {

			//String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-images/test-image3";
			String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/pixel-art/pixel-art2";

			int successes = 0;
			for (int i = 0; i < 500; i++) {
				Image im1 = new Image(file);
				CompressionAttempt ca = new CompressionAttempt(im1);

				try {
					new File("/Users/brychan/Documents/School/AlgorithmAssignment1/Images/tempoutput").delete();
					BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/brychan/Documents/School/AlgorithmAssignment1/Images/tempoutput"));

					for (String line : ca.getCommands()) {
						writer.write(line);
						writer.newLine();
						writer.flush();
					}

					Drawing d = new Drawing("/Users/brychan/Documents/School/AlgorithmAssignment1/Images/tempoutput");

					Image im2 = d.draw();

					boolean matches = true;

					for (int y = 0; y < im1.getPixels().length; y++) {
						for (int x = 0; x < im1.getPixels().length; x++) {
							if (im1.getPixel(new Coordinate(y, x)) != im2.getPixel(new Coordinate(y, x))) {
								matches = false;
							}
						}
					}
					System.out.println(matches + " on pass " + i);
					if (matches) {
						successes++;
					}

				} catch (Exception e) {


				}

				//Drawing d = new Drawing(file);
			}

			System.out.println(successes + " / 100");


		} else {

			String file = "/Users/brychan/Documents/School/AlgorithmAssignment1/Images/test-instructions";


			Drawing d = new Drawing(file);


			try {

				Image i = d.draw();
				System.out.println(i.toString());

			} catch (Exception ex) {
				System.out.println(ex);
			}

			System.out.print(d.toString());

		}

		*/


    }
}
