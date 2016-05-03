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
	//			Nouns						DONE
	//			Verbs						DONE
	//			Relationships				DONE
	//			Attributes (ADJ, ADV)		DONE
	// post mid-term
	// Q/A
	//		Accept question					DONE
	//		NLP								DONE
	//		Query KR						DONE
	//		NLG								TODO
	
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.out.println("Usage : artificial-guy STORY_FILE NEO4J_DATA_FOLDER");			
		}

		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("                                  Artificial Guy");
		System.out.println("Parses a story using Stanford CoreNLP and builds a knowledge graph using Neo4J");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

		System.out.println("Using story file : " + args[0]);
		Path path = FileSystems.getDefault().getPath("input", args[0]);
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

		System.out.println("\n################## ADDING RELATIONSHIPS TO KR ####################");
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
				if (srcPos.matches("(NN|VB|JJ|RB).*") && dstPos.matches("(NN|VB|JJ|RB).*")) {
					boolean status = false;
					status = kr.addRelation(srcToken, srcPos, srcNer, dstToken, dstPos, dstNer, reln, Integer.toString(sentId));
					if (status == false) {
						System.out.println("Failed to add relation - " + srcToken + "(" + srcPos + ")" + " -" + reln
								+ "-> " + dstToken + "(" + dstPos + ")");
					} else {
						System.out.println(srcToken + "(" + srcPos + ")" + " -" + reln
								+ "-> " + dstToken + "(" + dstPos + ")");
						numEdges++;
					}
				}
			}
			sentId++;
		}
		
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SUMMARY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("Number of relationships added : " + numEdges);
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

		// question answering session
		Scanner sc = new Scanner(System.in);
		while (true) {
			// user question
			System.out.print("\n*** Question (\"quit\") > ");
			String question = sc.nextLine();
			if (question.equals("quit")) {
				sc.close();
				break;
			}
			
			// parse the question
			List<SemanticGraph> qDepGraphs = nlp.getDependencies(question);
			
			// is the question too complex to answer?
			if (qDepGraphs.size() > 1) {
				System.out.println("*** I don't know");
				continue;
			}
			
			// parts of the parsed question
			String verb = null;
			String subj = null;
			String obj = null;
			String questWord = null;
			String questReln = null;
			
			// extract subj, verb, obj triplet from question
			// and also the question word
			SemanticGraph qDepGraph = qDepGraphs.get(0);
			
			// root verb
			if (qDepGraph.getFirstRoot().tag().startsWith("VB")) {
				if (!qDepGraph.getFirstRoot().lemma().matches("(be|do|have)")) {
					verb = qDepGraph.getFirstRoot().lemma();
				}
			}
			
			Iterable<SemanticGraphEdge> edges = qDepGraph.edgeIterable();
			for (SemanticGraphEdge edge : edges) {
				String srcToken = edge.getSource().lemma();
				String dstToken = edge.getTarget().lemma();
				String srcPos = edge.getSource().tag();
				String dstPos = edge.getTarget().tag();
				String reln = edge.getRelation().getShortName();
				
				// question word
				if (srcPos.startsWith("W")) {
					questWord = srcToken;
					questReln = reln;
				} else if (dstPos.startsWith("W")) {
					questWord = dstToken;
					questReln = reln;
				}
				
				// subj and obj
				if (reln == "nsubj" && !dstPos.startsWith("W")) {
					subj = dstToken;
				} else if (reln == "dobj" && !dstPos.startsWith("W")) {
					obj = dstToken;					
				}
			}
			
			// check if question word exists
			if (questWord == null) {
				System.out.println("*** Is this a question?");
				continue;
			}
			
			// ask the KR
			// TODO : there has to be a cleaner way to do this
			List<String> answer = null;
			if (verb == null) {
				if (obj == null && questReln.equals("advmod")) {
					answer = kr.getDesc(subj);
				}
			} else {
				if (subj == null && obj == null) {
					answer = kr.getSubj(verb);
				} else if (obj == null) {
					answer = kr.getObj(subj, verb);
				} else if (subj == null) {
					answer = kr.getSubj(verb, obj);	
				}
			}
			
			if (answer == null) {
				System.out.println("*** I don't know");
				continue;				
			} else {
				System.out.println("*** Answer > " + answer);
			}
		}

		// terminate before exit
		kr.terminate();
		
		// time to say bye
		System.out.println("\nTo visualize the knowledge graph :");
		System.out.println("		./neo4j console");
		System.out.println("		http://localhost:7474/");
		System.out.println("\nYour friendly bot, artificial-guy\n");
	}
}
