package edu.ncsu.artificialGuy;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class ArtificialGuy {
	
	// TODO : post mid-term
	// Q/A
	//		Accept question
	//		NLP
	//		Query KR
	//		NLG
	
	public static void main(String[] args) {

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Give me a story and I will build a knowledge graph");
		System.out.println("Psst. You can even peak into my brain (thanks to Neo4j)");
		System.out.println("Post mid term, I can answer your questions on the story!");
		System.out.println("I ROCK!! ....... right? :-/ pleaseeee say yes.... Nevermind");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
		
		System.out.println("Using story file : " + args[0] + "\n");
		
		// build a Story object
		StoryProcessor storyProc = new StoryProcessor(args[0]);
		
		// display the story
		System.out.println("\n######################## ORIGINAL STORY ##########################");
		System.out.println(storyProc.getText().replace(". ", "\n"));
		System.out.println("##################################################################\n");

		// class to build a knowledge graph
		KnowledgeGraph kr = new KnowledgeGraph();

		// extract tokens from story
		List<String> tokens = storyProc.getTokens();
		
		// tokens to be added to KR
		List<String> nodeTokens = new ArrayList<String>();
		for (String token : tokens) {
			String parts[] = token.split("/");
			// TODO : which POS needs to be a node in KR?
			// all types of noun and verb
			if (parts[1].matches("N.*") || parts[1].matches("V.*")) {
				nodeTokens.add(token);
			}
		}
		
		// add tokens to KR
		kr.addTokens(nodeTokens);
		
		// relationships to be added to KR
		List<SemanticGraph> depGraphs = storyProc.getDepGraphs();
		
		// TODO : add relationships to KR
		for (SemanticGraph depGraph : depGraphs) {
			Iterable<SemanticGraphEdge> edges = depGraph.edgeIterable();
			for (SemanticGraphEdge edge : edges) {
				String srcToken = edge.getSource().lemma();
				String dstToken = edge.getTarget().lemma();
				String srcPos = edge.getSource().tag();
				String dstPos = edge.getTarget().tag();
				String srcType = edge.getSource().ner();
				String dstType = edge.getTarget().ner();
				if ((srcPos.matches("N.*") || srcPos.matches("V.*")) && 
						(dstPos.matches("N.*") || dstPos.matches("V.*"))) {
					kr.addRelation(srcToken, srcPos, srcType, dstToken, dstPos, dstType);
				}
			}
		}
		
		// TODO : question answering session
		
		// terminate before exit
		kr.terminate();
		
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("RIP ArtificialGuy");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
}
