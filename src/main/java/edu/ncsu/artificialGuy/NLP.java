package edu.ncsu.artificialGuy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLP {

	// singleton instance
	private static NLP nlp = null;

	private static StanfordCoreNLP pipeline;

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
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}

	private Annotation runPipeline(String text) {
		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run pipeline on this text
		pipeline.annotate(document);

		return document;
	}

	public List<String> tagTokens(String text) {

		List<String> tagged = new ArrayList<String>();

		Annotation document = runPipeline(text);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys
		// and has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				tagged.add(word + "/" + pos + "/" + ne);
			}

		}

		return tagged;
	}

	public String resolveCoRef(String text) {

		String resolved = new String();

		Annotation document = runPipeline(text);

		Map<Integer, CorefChain> corefs = document.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			int curSentIdx = sentence.get(SentenceIndexAnnotation.class);

			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {

				Integer corefClustId = token.get(CorefClusterIdAnnotation.class);
				CorefChain chain = corefs.get(corefClustId);

				// if there is no chain to replace
				if (chain == null || chain.getMentionsInTextualOrder().size() == 1) {
					resolved += token.word() + " ";
				} else {

					int sentIndx = chain.getRepresentativeMention().sentNum - 1;
					CoreMap corefSent = sentences.get(sentIndx);
					List<CoreLabel> corefSentToks = corefSent.get(TokensAnnotation.class);

					System.out.println(token.word() + " --> corefClusterID = " + corefClustId);
					System.out.println("Matched chain = " + chain);

					String newwords = new String();
					CorefMention reprMent = chain.getRepresentativeMention();
					boolean replaced = false;
					if (curSentIdx != sentIndx || token.index() < reprMent.startIndex
							|| token.index() > reprMent.endIndex) {

						for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
							CoreLabel matchedLabel = corefSentToks.get(i - 1);
							String pos = matchedLabel.get(PartOfSpeechAnnotation.class);
							if (pos.matches("N.*")) {
								resolved += matchedLabel.word() + " ";
								newwords += matchedLabel.word() + " ";
								replaced = true;
							}
						}

						if (replaced == false) {
							resolved += token.word() + " ";
							System.out.println("\n");
						} else {
							System.out.println("Converting " + token.word() + " TO " + newwords + "\n");	
						}
					} else {
						resolved += token.word() + " ";
						System.out.println("\n");
					}
				}
			}
		}

		return resolved;

	}
}
