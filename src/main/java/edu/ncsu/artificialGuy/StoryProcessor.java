package edu.ncsu.artificialGuy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryProcessor {
	
	private String path;
	private String orgText;
	private String entityText;
	private String coRefText;
	
	private NLP nlp;

	@SuppressWarnings("unused")
	private StoryProcessor() {

	}

	public StoryProcessor(String filePath) {
		
		// story file path
		path = new String(filePath);

		// read story from file
		orgText = new String();
		String line;
		try {
			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(path));

			while ((line = bufferedReader.readLine()) != null) {
				orgText += line;
			}

			// Close file
			bufferedReader.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Unable to open file '" + filePath + "'");
		} catch (IOException ex) {
			System.out.println("Error reading file '" + filePath + "'");
		}
		
		// perform all NLP on text
		nlp = NLP.getInstance();
		performNLP();
	}

	public String getText() {
		return orgText;
	}
	
	public Set<String> getEntities() {
		
		Set<String> entities = new HashSet<String>();
		
		Pattern pattern = Pattern.compile("<.*>(.*?)</.*>");
		Matcher matcher = pattern.matcher(entityText);
		while (matcher.find()) {
			entities.add(matcher.group(1));
		}
		
		return entities;
	}
	
	private void performNLP() {
		// TODO : resolve co reference
		coRefText = new String(orgText);
		
		// entity extraction
		entityText = new String(nlp.tagEntities(coRefText));
	}
}
