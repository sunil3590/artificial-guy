package edu.ncsu.artificialGuy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
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

		Annotation document = runPipeline(text);

		List<String> tagged = new ArrayList<String>();

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

		Annotation doc = runPipeline(text);

		Map<Integer, CorefChain> corefs = doc.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

		String resolved = new String();

		for (CoreMap sentence : sentences) {

			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
			int curSentIdx = sentence.get(CoreAnnotations.SentenceIndexAnnotation.class);

			for (CoreLabel token : tokens) {

				Integer corefClustId = token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
				CorefChain chain = corefs.get(corefClustId);

				if (chain == null || chain.getMentionsInTextualOrder().size() == 1) {
					resolved += token.word() + " ";
				} else {

					int sentIndx = chain.getRepresentativeMention().sentNum - 1;
					CoreMap corefSentence = sentences.get(sentIndx);
					List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);

					System.out.println(token.word() + " --> corefClusterID = " + corefClustId);
					System.out.println("matched chain = " + chain);

					String newwords = new String();
					CorefMention reprMent = chain.getRepresentativeMention();
					System.out.println(reprMent);
					System.out.println(token.index());
					if (curSentIdx != sentIndx || token.index() < reprMent.startIndex
							|| token.index() > reprMent.endIndex) {

						for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
							CoreLabel matchedLabel = corefSentenceTokens.get(i - 1);
							resolved += matchedLabel.word() + " ";
							newwords += matchedLabel.word() + " ";
						}

						System.out.println("converting " + token.word() + " to " + newwords + "\n");
					} else {
						resolved += token.word() + " ";
					}
				}
			}
		}

		return resolved;

	}
}
