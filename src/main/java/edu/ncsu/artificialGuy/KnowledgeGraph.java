package edu.ncsu.artificialGuy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
			case "csubj":
				relnType = KRRelnTypes.CSUBJ;
				break;
			case "csubjpass":
				relnType = KRRelnTypes.CSUBJPASS;
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
			case "rcmod":
				relnType = KRRelnTypes.RCMOD;
				break;
			case "tmod":
				relnType = KRRelnTypes.TMOD;
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
	
	private List<String> queryKR(String match, String where, String ret) {
		
		if (match == null || ret == null) {
			System.out.println("Invalid input to query");
			return null;
		}
		
		// query the KR
		String query = match;
		if (where != null) {
			query += " " + where;
		}
		query += " " + ret;
		System.out.println(query);
		Result result = graphDb.execute(query);
		
		// if there was no result
		if (result.hasNext() == false) {
			return null;
		}
		
		// process all rows of query result
		List<String> answer = new ArrayList<String>();
		while (result.hasNext()) {
			Node node = (Node) result.next().get("answer");
			try (Transaction tx = graphDb.beginTx()) {
				String token = (String) node.getProperty("token");
				if (!answer.contains(token)) {
					answer.add(token);
				}
				tx.success();
			}
		}
		
		// return null if no description found
		if (answer.size() == 0) {
			answer = null;
		}
		
		return answer;
	}
	/**
	 * Returns words that describe the subj 
	 * <p>
	 * @param subj whose description is needed, not null
	 * @return words that describe the subj, may be null
	 */
	public List<String> getDesc(String subj) {
		
		// check arguments
		if (subj == null) {
			return null;
		}
		
		// convert the English question to Cypher query
		String match = null;
		String where = null;
		String ret = null;
		
		// build the MATCH part of query
		match = "MATCH ";
		match += "({token:'" + subj + "'})";
		match += "-[:ACOMP|:ADVMOD|:AMOD|:NMOD|:NPADVMOD|:RCMOD|:TMOD]-"; // TODO
		match += "(answer)";
		
		// expected POS of the answer is the WHERE condition
		where = "WHERE answer.pos STARTS WITH 'JJ' OR answer.pos STARTS WITH 'RB'";
		
		// what needs to be RETURNed from the query
		ret = "RETURN answer";
		
		// query the KR
		List<String> answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		
		// relaxed query
		match = "MATCH ";
		match += "({token:'" + subj + "'})";
		match += "--";
		match += "(answer)";
		answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		answer = queryKR(match, null, ret);
		
		return answer;
	}
	
	/**
	 * Returns subject for a verb 
	 * <p>
	 *
	 * @return answer words for the question
	 */	
	public List<String> getSubj(String verb) {
		
		// check arguments
		if (verb == null) {
			return null;
		}
		
		// convert the English question to Cypher query
		String match = null;
		String where = null;
		String ret = null;
		
		// build the MATCH part of query
		match = "MATCH ";
		match += "(answer)";
		match += "-[:CSUBJ|:CSUBJPASS|:NSUBJ|:NSUBJPASS|:XSUBJ]-"; // TODO
		match += "({token:'" + verb + "'})";
		
		// expected POS of the answer is the WHERE condition
		where = "WHERE answer.pos STARTS WITH 'NN'";
		
		// what needs to be RETURNed from the query
		ret = "RETURN answer";
		
		// query the KR
		List<String> answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		
		// relaxed query
		match = "MATCH ";
		match += "(answer)";
		match += "--";
		match += "({token:'" + verb + "'})";
		answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		answer = queryKR(match, null, ret);
		
		return answer;
	}
	
	public List<String> getSubj(String verb, String obj) {
		
		// check arguments
		if (verb == null || obj == null) {
			return null;
		}
		
		// convert the English question to Cypher query
		String match = null;
		String where = null;
		String ret = null;
		
		// build the MATCH part of query
		match = "MATCH ";
		match += "(answer)";
		match += "-[:CSUBJ|:CSUBJPASS|:NSUBJ|:NSUBJPASS|:XSUBJ]-"; // TODO
		match += "({token:'" + verb + "'})";
		match += "-[:DOBJ|:IOBJ|:POBJ]-"; // TODO
		match += "({token:'" + obj + "'})";
		
		// expected POS of the answer is the WHERE condition
		where = "WHERE answer.pos STARTS WITH 'NN'";
		
		// what needs to be RETURNed from the query
		ret = "RETURN answer";
		
		// query the KR
		List<String> answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		
		// relaxed query
		match = "MATCH ";
		match += "(answer)";
		match += "--";
		match += "({token:'" + verb + "'})";
		match += "--";
		match += "({token:'" + obj + "'})";
		answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		answer = queryKR(match, null, ret);
		
		return answer;
	}
	
	public List<String> getObj(String subj, String verb) {
		
		// check arguments
		if (subj == null || verb == null) {
			return null;
		}
		
		// convert the English question to Cypher query
		String match = null;
		String where = null;
		String ret = null;
		
		// build the MATCH part of query
		match = "MATCH ";
		match += "({token:'" + subj + "'})";
		match += "-[:CSUBJ|:CSUBJPASS|:NSUBJ|:NSUBJPASS|:XSUBJ]-"; // TODO
		match += "({token:'" + verb + "'})";
		match += "-[:DOBJ|:IOBJ|:POBJ]-"; // TODO
		match += "(answer)";
		
		// expected POS of the answer is the WHERE condition
		where = "WHERE answer.pos STARTS WITH 'NN'";
		
		// what needs to be RETURNed from the query
		ret = "RETURN answer";
		
		// query the KR
		List<String> answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		
		// relaxed query
		match = "MATCH ";
		match += "({token:'" + subj + "'})";
		match += "--";
		match += "({token:'" + verb + "'})";
		match += "--";
		match += "(answer)";
		answer = queryKR(match, where, ret);
		if (answer != null) {
			return answer;
		}
		answer = queryKR(match, null, ret);
		
		return answer;
	}

	public void terminate() {
		System.out.println("\nShutting down database ...");
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
		CSUBJ,
		CSUBJPASS,
		DOBJ,
		IOBJ,
		NEG,
		NMOD,
		NPADVMOD,
		NSUBJ,
		NSUBJPASS,
		POBJ,
		RCMOD,
		TMOD,
		XSUBJ,
		UNKNOWN
	}
}
