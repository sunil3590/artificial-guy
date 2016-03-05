package edu.ncsu.artificialGuy;

import java.util.Scanner;
import java.util.Set;

public class ArtificialGuy {

	// TODO : mid-term submission
	// Knowledge Representation
	//		NLP
	//			co-reference resolution
	//			POS
	//			NER tagging
	//			dependency parse
	//		Build KR
	//			Nouns
	//			Verbs
	//			Relationships
	//			Attributes (adjectives, adverbs, etc)
	
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
		System.out.println("Here's how the story goes");
		System.out.println("\n##################################################################");
		System.out.println(storyProc.getText().replace(". ", "\n"));
		System.out.println("##################################################################\n");

		// class to build a knowledge graph
		KnowledgeGraph kr = new KnowledgeGraph("jdbc:neo4j://localhost:7474/", "neo4j", "megaton119");

		// extract from story and build a KR
		Set<String> entities = storyProc.getEntities();
		
		// add entities to KR
		for (String entity : entities) {
			kr.addNode(entity);
		}
		
		// question answering session
		Scanner sc = new Scanner(System.in);
		while (true) {
			// User question
			System.out.print("Enter question (or \"quit\") > ");
			String question = sc.nextLine();
			if (question.equals("quit")) {
				sc.close();
				break;
			}
		}
		
		// terminate before exit
		kr.terminate();
		
		System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println("RIP ArtificialGuy");
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");
	}
}
