package edu.ncsu.artificialGuy;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class KnowledgeGraph {

	private static final String db_path = "/media/windows/Users/Sunil/Everything/EOS"
			+ "/neo4j-community-2.3.2/data/graph.db";
	private GraphDatabaseService graphDb;

	@SuppressWarnings("deprecation")
	public KnowledgeGraph() {

		// create a neo4j database
		try {
			FileUtils.deleteRecursively(new File(db_path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(db_path);

		registerShutdownHook(graphDb);
	}

	private boolean addNode(String token, String pos, String type) {

		if (token == null || pos == null || type == null) {
			return false;
		}

		Node node;
		try (Transaction tx = graphDb.beginTx()) {
			// check for duplicate nodes
			Node oldNode = getNode(token, pos, type);
			if (oldNode != null) {
				return true;
			}

			// Database operations go here
			node = graphDb.createNode();
			node.setProperty("entity", token);
			if (pos != null) {
				node.addLabel(DynamicLabel.label(pos));
			}
			if (type != null) {
				node.addLabel(DynamicLabel.label(type));
			}

			// transaction complete
			tx.success();
		}

		return true;
	}

	public void addTokens(List<String> tokens) {
		for (String token : tokens) {
			String parts[] = token.split("/");

			if (parts[1].matches("N.*") || parts[1].matches("V.*")) {
				boolean status = this.addNode(parts[3].toLowerCase(), parts[1], parts[2]);
				if (status == false) {
					System.out.println("Failed to add node : " + token);
				}
			}
		}
	}
	
	public Node getNode(String token, String pos, String type) {
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("otherLabels", Arrays.asList(type));
//		String query = "MATCH (n:" + pos + ") WHERE ALL(x IN {otherLabels} WHERE x IN LABELS(n)) RETURN n";
//		Result result = graphDb.execute(query, params);
//		System.out.println(result.toString());
		
		// TODO : both labels to be used
		Node node = graphDb.findNode(DynamicLabel.label(pos), "entity", token.toLowerCase());
		return node;
	}

	public boolean addRelation(String srcToken, String srcPos, String srcType, 
			String dstToken, String dstPos, String dstType) {

		if (srcToken == null || dstToken == null) {
			return false;
		}

		try (Transaction tx = graphDb.beginTx()) {
			Node srcNode = this.getNode(srcToken, srcPos, srcType);
			if (srcNode == null) {
				return false;
			}

			Node dstNode = this.getNode(dstToken, dstPos, dstType);
			if (dstNode == null) {
				return false;
			}

			srcNode.createRelationshipTo(dstNode, KRRelnTypes.UNKNOWN);

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

	enum KRRelnTypes implements RelationshipType {
		UNKNOWN
	}
}
