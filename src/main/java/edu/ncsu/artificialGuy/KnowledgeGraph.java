package edu.ncsu.artificialGuy;

import java.io.File;
import java.io.IOException;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.io.fs.FileUtils;

public class KnowledgeGraph {

	private String db_path = null;
	private GraphDatabaseService graphDb;

	@SuppressWarnings("deprecation")
	public KnowledgeGraph(String db_path) {
		
		this.db_path = new String(db_path);

		// delete data from previous runs
		try {
			FileUtils.deleteRecursively(new File(this.db_path));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// create a neo4j database
		this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(this.db_path);

		registerShutdownHook(graphDb);
	}

	private Node addNode(String token, String pos, String ner, String sentId) throws Exception {

		if (token == null || pos == null || ner == null) {
			throw new Exception("Invalid arguments");
		}
 
		// check for duplicate nodes
		Node node = getNode(token, pos, ner, sentId);
		if (node != null) {
			return node;
		}
		
		try (Transaction tx = graphDb.beginTx()) {
			// Database operations go here
			node = graphDb.createNode();
			node.setProperty("token", token);
			node.setProperty("pos", pos);
			node.setProperty("ner", ner);
			node.addLabel(DynamicLabel.label("S_" + sentId));

			// transaction complete
			tx.success();
		}
		
		return node;
	}
	
	private Node getNode(String token, String pos, String ner, String sentId) {
		Node node = null;

		String query = "MATCH (entity:" + "S_" + sentId + 
				" { token:'" + token + "' , pos:'" + pos + "' , ner:'" + ner + "' })"
						+ " RETURN entity";
		Result result = graphDb.execute(query);
		while (result.hasNext()) {
			node = (Node) result.next().get("entity");
		}

		return node;
	}

	public boolean addRelation(String srcToken, String srcPos, String srcNer, 
			String dstToken, String dstPos, String dstNer, String reln, String sentId) {

		if (srcToken == null || dstToken == null) {
			return false;
		}

		try (Transaction tx = graphDb.beginTx()) {
			Node srcNode = this.addNode(srcToken, srcPos, srcNer, sentId);
			if (srcNode == null) {
				return false;
			}

			Node dstNode = this.addNode(dstToken, dstPos, dstNer, sentId);
			if (dstNode == null) {
				return false;
			}

			// TODO : handle all types of relations
			KRRelnTypes relnType;
			switch (reln) {
			case "acomp":
				relnType = KRRelnTypes.ACOMP;
				break;
			case "advmod":
				relnType = KRRelnTypes.ADVMOD;
				break;
			case "amod":
				relnType = KRRelnTypes.AMOD;
				break;
			case "conj":
				relnType = KRRelnTypes.CONJ;
				break;
			case "dobj":
				relnType = KRRelnTypes.DOBJ;
				break;
			case "iobj":
				relnType = KRRelnTypes.IOBJ;
				break;
			case "neg":
				relnType = KRRelnTypes.NEG;
				break;
			case "nmod":
				relnType = KRRelnTypes.NMOD;
				break;
			case "npadvmod":
				relnType = KRRelnTypes.NPADVMOD;
				break;
			case "nsubj":
				relnType = KRRelnTypes.NSUBJ;
				break;
			case "nsubjpass":
				relnType = KRRelnTypes.NSUBJPASS;
				break;
			case "pobj":
				relnType = KRRelnTypes.POBJ;
				break;
			case "poss":
				relnType = KRRelnTypes.POSS;
				break;
			case "rcmod":
				relnType = KRRelnTypes.RCMOD;
				break;
			case "xsubj":
				relnType = KRRelnTypes.XSUBJ;
				break;
			default:
				relnType = KRRelnTypes.UNKNOWN;
				break;
			}
			
			srcNode.createRelationshipTo(dstNode, relnType);	

			// transaction complete
			tx.success();
		} catch (Exception e) {
			System.out.println("Error adding relationship");
			e.printStackTrace();
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

	private enum KRRelnTypes implements RelationshipType {
		ACOMP,
		ADVMOD,
		AMOD,
		CONJ,
		DOBJ,
		IOBJ,
		NEG,
		NMOD,
		NPADVMOD,
		NSUBJ,
		NSUBJPASS,
		POBJ,
		POSS,
		RCMOD,
		XSUBJ,
		UNKNOWN
	}
}
