package edu.ncsu.artificialGuy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;

public class StoryProcessor {
	
	// mid-term submission
	// Knowledge Representation
	//		NLP
	//			co-reference resolution		DONE
	//			POS							DONE
	//			NER tagging					DONE
	//			stemming					DONE
	//			dependency parse			DONE
	//		Build KR
	//			Remove stop words			TODO
	//			Nouns						DONE
	//			Verbs						DONE
	//			Relationships				DONE
	//			Attributes (ADJ, ADV)		DONE

	private String filePath;
	private String orgText;
	private String coRefText;
	private List<String> tokens;
	private List<SemanticGraph> depGraphs;
	
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
	
	public String getCorefText() {
		return coRefText;
	}
	
	public List<String> getTokens() {
		return tokens;
	}
	
	public List<SemanticGraph> getDepGraphs() {
		return depGraphs;
	}
	
	private void nlpPreprocess() {
		// resolve co reference
		coRefText = new String(nlp.resolveCoRef(orgText));
		
		// token processor
		tokens = new ArrayList<String>(nlp.tagTokens(coRefText));
		
		// dependency parser
		depGraphs = nlp.getDependencies(coRefText);
	}
}
