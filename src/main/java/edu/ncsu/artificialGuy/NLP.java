package edu.ncsu.artificialGuy;

import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFBiasedClassifier;
import edu.stanford.nlp.ling.CoreLabel;

public class NLP {
	
	// singleton instance
	private static NLP nlp = null;
	
	private static CRFBiasedClassifier<CoreLabel> ner;
	
	private static String nerPath = "models/english.all.3class.caseless.distsim.crf.ser.gz";
	
	private NLP() {
		// load all required models
		loadModels();
	}
	
	public static NLP getInstance() {
		if (nlp == null) {
			nlp = new NLP();
		}
		
		return nlp;
	}
	
	private static void loadModels() {

		Properties props = new Properties();

		ner = new CRFBiasedClassifier<CoreLabel>(props);
		ner.loadClassifierNoExceptions(nerPath, props);
		ner.setBiasWeight("LOCATION", 1.3d);
		ner.setBiasWeight("ORGANIZATION", 1.3d);
		ner.setBiasWeight("PERSON", 1.3d);
		ner.setBiasWeight("O", 0.8d);
	}
	
	public String tagEntities(String text) {
		return ner.classifyWithInlineXML(text);
	}
}
