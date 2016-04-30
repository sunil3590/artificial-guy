package edu.ncsu.artificialGuy;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class ArtificialGuy {

	// mid-term
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
	// post mid-term
	// Q/A
	//		Accept question					DONE
	//		NLP								TODO
	//		Query KR						TODO
	//		NLG								TODO
	
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.out.println("Usage : artificial-guy STORY_FILE NEO4J_DATA_FOLDER");			
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("                                  Artificial Guy");
		System.out.println("Parses a story using Stanford CoreNLP and builds a knowledge graph using Neo4J");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

		System.out.println("Using story file : " + args[0] + "\n");
		Path path = FileSystems.getDefault().getPath("input", "owl.txt");
		File file = path.toFile();

		// build a Story object
		Story story = new Story(file);

		// display the original story
		System.out.println("\n######################## ORIGINAL STORY ##########################");
		System.out.println(story.getText().replace(". ", ".\n"));
		System.out.println("##################################################################\n");

		// to perform any NLP operations required
		NLP nlp = NLP.getInstance();
		
		// resolve co reference
		String coRefText = nlp.resolveCoRef(story.getText());

		// story after co reference resolution
		System.out.println("\n################# AFTER COREFERENCE RESOLUTION ###################");
		System.out.println(coRefText.replace(". ", ".\n"));
		System.out.println("##################################################################\n");
		
		// class to build a knowledge graph
		KnowledgeGraph kr = new KnowledgeGraph(args[1]);

		// relationships to be added to KR
		List<SemanticGraph> depGraphs = nlp.getDependencies(coRefText);

		// add relationships and nodes to KR
		int numEdges = 0;
		int sentId = 0;
		for (SemanticGraph depGraph : depGraphs) {
			Iterable<SemanticGraphEdge> edges = depGraph.edgeIterable();
			for (SemanticGraphEdge edge : edges) {
				String srcToken = edge.getSource().lemma();
				String dstToken = edge.getTarget().lemma();
				String srcPos = edge.getSource().tag();
				String dstPos = edge.getTarget().tag();
				String srcNer = edge.getSource().ner();
				String dstNer = edge.getTarget().ner();
				String reln = edge.getRelation().getShortName();
				
				// TODO : figure out which POS make a node
				// all types of nouns, verbs, adjectives, adverbs
				if ((srcPos.matches("N.*") || srcPos.matches("V.*") || srcPos.matches("JJ.*") || srcPos.matches("RB.*"))
						&& (dstPos.matches("N.*") || dstPos.matches("V.*") || dstPos.matches("JJ.*")
								|| dstPos.matches("RB.*"))) {
					boolean status = false;
					status = kr.addRelation(srcToken, srcPos, srcNer, dstToken, dstPos, dstNer, reln, Integer.toString(sentId));
					if (status == false) {
						System.out.println("Failed to add relation - " + srcToken + "(" + srcPos + ")" + " - " + reln
								+ " -> " + dstToken + "(" + dstPos + ")");
					} else {
						numEdges++;
					}
				}
			}
			sentId++;
		}
		
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SUMMARY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Number of relationships added : " + numEdges);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

		// question answering session
		Scanner sc = new Scanner(System.in);
		while (true) {
			// user question
			System.out.print("Enter question (or \"quit\") > ");
			String question = sc.nextLine();
			if (question.equals("quit")) {
				sc.close();
				break;
			}
			
			
		}

		// terminate before exit
		kr.terminate();
	}
}
