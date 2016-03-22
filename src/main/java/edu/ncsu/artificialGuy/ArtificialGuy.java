package edu.ncsu.artificialGuy;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class ArtificialGuy {

	// TODO : post mid-term
	//		Q/A
	//		Accept question
	//		NLP
	//		Query KR
	//		NLG

	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.out.println("Usage : artificial-guy STORY_FILE_PATH NEO4J_DATA_FOLDER");			
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("                                  Artificial Guy");
		System.out.println("Parses a story using Stanford CoreNLP and builds a knowledge graph using Neo4J");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

		System.out.println("Using story file : " + args[0] + "\n");

		// build a Story object
		StoryProcessor storyProc = new StoryProcessor(args[0]);

		// display the original story
		System.out.println("\n######################## ORIGINAL STORY ##########################");
		System.out.println(storyProc.getText().replace(". ", ".\n"));
		System.out.println("##################################################################\n");

		// story after co reference resolution
		System.out.println("\n################# AFTER COREFERENCE RESOLUTION ###################");
		System.out.println(storyProc.getCorefText().replace(". ", ".\n"));
		System.out.println("##################################################################\n");
		
		// class to build a knowledge graph
		KnowledgeGraph kr = new KnowledgeGraph(args[1]);

		// extract tokens from story
		List<String> tokens = storyProc.getTokens();

		// tokens to be added to KR
		int numNodes = 0;
		List<String> nodeTokens = new ArrayList<String>();
		for (String token : tokens) {
			String parts[] = token.split("/");
			// TODO : figure out which POS make a node
			// all types of nouns, verbs, adjectives, adverbs
			if (parts[1].matches("N.*") || parts[1].matches("V.*") || parts[1].matches("JJ.*")
					|| parts[1].matches("RB.*")) {
				nodeTokens.add(token);
			}
		}

		// add tokens to KR
		numNodes = kr.addTokens(nodeTokens);

		// relationships to be added to KR
		List<SemanticGraph> depGraphs = storyProc.getDepGraphs();

		// add relationships to KR
		int numEdges = 0;
		for (SemanticGraph depGraph : depGraphs) {
			Iterable<SemanticGraphEdge> edges = depGraph.edgeIterable();
			for (SemanticGraphEdge edge : edges) {
				String srcToken = edge.getSource().lemma();
				String dstToken = edge.getTarget().lemma();
				String srcPos = edge.getSource().tag();
				String dstPos = edge.getTarget().tag();
				String srcType = edge.getSource().ner();
				String dstType = edge.getTarget().ner();
				String reln = edge.getRelation().getShortName();
				if ((srcPos.matches("N.*") || srcPos.matches("V.*") || srcPos.matches("JJ.*") || srcPos.matches("RB.*"))
						&& (dstPos.matches("N.*") || dstPos.matches("V.*") || dstPos.matches("JJ.*")
								|| dstPos.matches("RB.*"))) {
					boolean status = false;
					status = kr.addRelation(srcToken, srcPos, srcType, dstToken, dstPos, dstType, reln);
					if (status == false) {
						System.out.println("Failed to add relation - " + srcToken + "(" + srcPos + ")" + " - " + reln
								+ " -> " + dstToken + "(" + dstPos + ")");
					} else {
						numEdges++;
					}
				}
			}
		}

		// TODO : question answering session

		// terminate before exit
		kr.terminate();

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SUMMARY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Number of nodes added : " + numNodes);
		System.out.println("Number of edges added : " + numEdges);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
}
