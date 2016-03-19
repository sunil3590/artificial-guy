package edu.ncsu.artificialGuy;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class KnowledgeGraph {

	private static final String db_path = "/media/windows/Users/Sunil/Everything/EOS"
			+ "/neo4j-community-2.3.2/data/graph.db";
	private GraphDatabaseService graphDb;

	@SuppressWarnings("unused")
	private KnowledgeGraph() {

	}

	@SuppressWarnings("deprecation")
	public KnowledgeGraph(String url, String user, String pass) {

		// create a neo4j database
		try {
			FileUtils.deleteRecursively(new File(db_path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(db_path);
		
		registerShutdownHook(graphDb);
	}

	public boolean addNode(String entity) {
		return addNode(entity, null, null);
	}
	
	public boolean addNode(String entity, String pos) {
		return addNode(entity, pos, null);
	}
	
	public boolean addNode(String entity, String pos, String type) {
		
		if (entity == null) {
			return false;
		}
		
		// TODO : check for duplicate nodes
		Node node;
		try (Transaction tx = graphDb.beginTx()) {
			// Database operations go here
			node = graphDb.createNode();
			node.setProperty("entity", entity);
			if(pos != null) {
				node.addLabel(DynamicLabel.label(pos));	
			}
			if(type != null) {
				node.addLabel(DynamicLabel.label(type));	
			}			

			// transaction complete
			tx.success();
		}
		
		return true;
	}

	public void terminate() {
		System.out.println();
		System.out.println("Shutting down database ...");
		graphDb.shutdown();
	}

	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}
}
