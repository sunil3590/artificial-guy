package edu.ncsu.artificialGuy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StoryProcessor {
	
	// TODO : mid-term submission
	// Knowledge Representation
	//		NLP
	//			co-reference resolution
	//			POS
	//			NER tagging
	//			stemming
	//			dependency parse
	//		Build KR
	//			Nouns
	//			Verbs
	//			Relationships
	//			Attributes (adjectives, adverbs, etc)

	private String filePath;
	private String orgText;
	private String coRefText;
	private List<String> entities;
	
	private NLP nlp;

	@SuppressWarnings("unused")
	private StoryProcessor() {

	}

	public StoryProcessor(String filePath) {
		
		// story file path
		this.filePath = new String(filePath);

		// read story from file
		orgText = new String();
		String line;
		try {
			// Wrap FileReader in BufferedReader
			BufferedReader bufferedReader = new BufferedReader(new FileReader(this.filePath));

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
		nlpPreprocess();
	}

	public String getText() {
		return orgText;
	}
	
	public List<String> getEntities() {
		return entities;
	}
	
	private void nlpPreprocess() {
		// resolve co reference
		coRefText = new String(nlp.resolveCoRef(orgText));
		System.out.println(coRefText.replace(". ", ".\n"));
		
		// entity extraction
		entities = new ArrayList<String>(nlp.tagTokens(coRefText));
		System.out.println(entities);
	}
}
