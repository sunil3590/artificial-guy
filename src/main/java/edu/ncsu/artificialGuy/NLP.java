package edu.ncsu.artificialGuy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefChain.CorefMention;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterIdAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentenceIndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
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
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, depparse, parse, dcoref");
		props.put("enforceRequirements", false);
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
				// this is the lemma of the token
				String lemma = token.get(LemmaAnnotation.class);

				tagged.add(word + "/" + pos + "/" + ne + "/" + lemma);
			}

		}

		return tagged;
	}

	public String resolveCoRef(String text) {

		// TODO :
		//		maintain spacing
		//		maintain capitalization

		String resolved = new String();

		Annotation document = runPipeline(text);

		Map<Integer, CorefChain> corefs = document.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {

			int curSentIdx = sentence.get(SentenceIndexAnnotation.class);
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);

			for (CoreLabel token : tokens) {

				Integer corefClustId = token.get(CorefClusterIdAnnotation.class);
				CorefChain chain = corefs.get(corefClustId);

				// if there is no chain to replace
				if (chain == null || chain.getMentionsInTextualOrder().size() == 1) {
					resolved += token.word() + " ";
				} else {

					int sentIndx = chain.getRepresentativeMention().sentNum - 1;
					CoreMap corefSent = sentences.get(sentIndx);
					SemanticGraph dep = corefSent.get(BasicDependenciesAnnotation.class);
					List<IndexedWord> topSortWords = dep.topologicalSort();

					CorefMention reprMent = chain.getRepresentativeMention();
					String rootWord = null;
					for (IndexedWord word : topSortWords) {
						if (word.index() >= reprMent.startIndex && word.index() <= reprMent.endIndex) {
							rootWord = word.originalText();
							break;
						}
					}

					if (curSentIdx != sentIndx || token.index() < reprMent.startIndex
							|| token.index() > reprMent.endIndex) {
						resolved += rootWord + " ";
					} else {
						resolved += token.word() + " ";
					}
				}
			}
		}

		return resolved;
	}
	
	public List<SemanticGraph> getDependencies(String text) {

		Annotation document = runPipeline(text);
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		List<SemanticGraph> depGraphs = new ArrayList<SemanticGraph>();
		for (CoreMap sentence : sentences) {
			depGraphs.add(sentence.get(BasicDependenciesAnnotation.class));
		}
		return depGraphs;
	}
}
