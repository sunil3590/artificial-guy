package edu.ncsu.artificialGuy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Story {

	private String orgText;

	@SuppressWarnings("unused")
	private Story() {

	}

	public Story(File file) {

		// read story from file
		orgText = new String();
		String line;
		try {
			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

			while ((line = bufferedReader.readLine()) != null) {
				orgText += line;
			}

			// Close file
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + file + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + file + "'");
		}
	}

	public String getText() {
		return orgText;
	}
}
