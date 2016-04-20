package model.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import model.Constants;
import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;

/*
 * Interface to the sqlite database. There is an individual sqlite database for each domain. 
 * 
 * TODO: there are many cases where a query returns a result to check for existence. Probably could do that smarter
 * to cut down on needless queries and do more transactions.
 */

public class Domain {

	protected Connection conn;
	protected Statement stat;
	
	private int maxWordID = 0;
	private TreeMap<String,Integer> cachedWords = new TreeMap<String,Integer>();
	private TreeSet<String> cachedIntents = new TreeSet<String>();
	private TreeSet<String> cachedConcepts = new TreeSet<String>();
	private TreeSet<String> cachedProperties = new TreeSet<String>();
	private TreeSet<String> cachedConceptIntentAttachments = new TreeSet<String>();
	private TreeSet<String> cachedPropertyConceptAttachments = new TreeSet<String>();
	
	public List<String> getDomains() {
		
		ArrayList<String> domains = new ArrayList<String>();
		for (File f : new File(Constants.BASE_FILE_PATH).listFiles()) {
			if (f.isDirectory()) domains.add(f.getName());
		}
		return domains;
	}
	
	public List<String> getIntents() throws SQLException {
		if (!cachedIntents.isEmpty())
			return new ArrayList<String>(cachedIntents);
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct intent FROM intent"));
		List<String> intents = new ArrayList<String>();
		while (result.next()) {
			intents.add(result.getString("intent"));
		}
		cachedIntents.addAll(intents);
		return intents;
	}
	
	public List<String> getConcepts() throws SQLException {
		if (!cachedConcepts.isEmpty())
			return new ArrayList<String>(cachedConcepts);		
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct concept FROM concept"));
		List<String> concepts = new ArrayList<String>();
		while (result.next()) {
			concepts.add(result.getString("concept"));
		}
		cachedConcepts.addAll(concepts);
		return concepts;
	}
	
	public List<Integer> getConceptIDs() throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct cid FROM concept"));
		List<Integer> concepts = new ArrayList<Integer>();
		while (result.next()) {
			concepts.add(result.getInt("cid"));
		}
		return concepts;
	}
	
	public String getConcept(Integer cid) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT concept FROM concept WHERE cid=%d", cid));
		return result.getString("concept");
	}
	
	public void addNewConcept(String concept) throws SQLException {
		Statement stat = createStatement();
		concept = concept.replace(" ", "_");
		stat.execute(String.format("INSERT INTO concept (concept) VALUES ('%s')", concept));
	}
	
	public void addNewIntent(String intent) throws SQLException {
		Statement stat = createStatement();
		intent = intent.replace(" ", "_");
		stat.execute(String.format("INSERT INTO intent (intent) VALUES ('%s')", intent));
	}
	
	public void createNewDomain(String s) throws SQLException {
		File dirs = new File(Constants.BASE_FILE_PATH + s);
		dirs.mkdirs();
		createDB(s);
	}

	private void createDB(String s) throws SQLException {
		setDomain(s);
		createEmptyTables(s);
	}

	private void createEmptyTables(String s) throws SQLException {
		Statement stat = createStatement();
		stat.execute(String.format("CREATE TABLE intent (iid INTEGER PRIMARY KEY AUTOINCREMENT, intent TEXT)")); // an intent is an abstraction over concepts
		stat.execute(String.format("CREATE TABLE intent_seuqence (left INTEGER, right INTEGER)")); // intents that go together (e.g., food+time+place)
		stat.execute(String.format("CREATE TABLE concept (cid INTEGER PRIMARY KEY AUTOINCREMENT, concept TEXT)")); // concepts abstract over chunks of utterances
		stat.execute(String.format("CREATE TABLE concept_intent (cid INTEGER, iid INTEGER)")); // concepts group to make intents
		stat.execute(String.format("CREATE TABLE property (pid INTEGER PRIMARY KEY AUTOINCREMENT, property TEXT)")); // concepts abstract over chunks of utterances
		stat.execute(String.format("CREATE TABLE property_concept (pid INTEGER, cid INTEGER)")); // concepts group to make intents		
		stat.execute(String.format("CREATE TABLE word (wid INTEGER PRIMARY KEY AUTOINCREMENT, word TEXT)")); // words represent the utterances
		stat.execute(String.format("CREATE TABLE sequence (left INTEGER, right INTEGER, cid INTEGER)")); // word sequences map to concepts (via properties, but that's not represented here)
	}

	public int getNumberOfConceptsForIntent(int intentID) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT count(*) c FROM concept, concept_intent i WHERE i.cid = concept.cid AND i.iid = %d", intentID));
		return result.getInt("c");
	}

	public void setDomain(String d) throws SQLException {
		createConnection(Constants.BASE_FILE_PATH + d + "/" + d + ".db");
	}
	
	public void createConnection(String path) throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		conn =  DriverManager.getConnection("jdbc:sqlite:" + path);
	}
	
	public Statement createStatement() throws SQLException {
		if (stat == null)
			stat = conn.createStatement();
		return stat;
	}
	
	public void closeConnection() throws SQLException {
		conn.close();
	}
	
	private int getConceptID(String concept) throws SQLException {
		Statement stat = createStatement();
        concept = concept.replace(" " , "_");
		ResultSet result = stat.executeQuery(String.format("SELECT cid FROM concept WHERE concept='%s'", concept));
		return result.getInt("cid");
	}

	public void addExampleForConcept(String concept, String example) throws SQLException {
		int cid = getConceptID(concept);
		example = Constants.S_TAG + " " + example + " " + Constants.S_TAG;
		ArrayList<String> splitExample = new ArrayList<String>(Arrays.asList(example.split("\\s+")));
		for (int i=0; i<splitExample.size()-1; i++) {
			addWordPair(splitExample.get(i), splitExample.get(i+1), cid);
		}
	}

	public void addWordPair(String word1, String word2, int cid) throws SQLException {
		int w1 = offerWord(word1);
		int w2 = offerWord(word2);
		cachedWords.put(word1, w1);
		cachedWords.put(word2, w2);
		addSequence(w1, w2, cid);
	}

	public int offerWord(String word) throws SQLException {
		if (!cachedWords.isEmpty()) {
			if (cachedWords.containsKey(word))
				return cachedWords.get(word);
		}
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT wid FROM word WHERE word='%s'", word));
		if (result.isAfterLast()) {
			stat.execute(String.format("INSERT INTO word (word) VALUES ('%s')", word));
			return getMaxWordID();
		}
		else {
			return result.getInt("wid");	
		}
		
	}
	
	private int getMaxWordID() throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT max(wid) w FROM word"));
		return result.getInt("w");
//		return maxWordID++;
	}	

	private void addSequence(int w1, int w2, int cid) throws SQLException {
		Statement stat = createStatement();
		stat.execute(String.format("INSERT INTO sequence (left, right, cid) VALUES (%d, %d, %d)", w1, w2, cid));
	}
	
	public String getWordFromID(int id) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT word FROM word WHERE wid=%d", id));
		return result.getString("word");		
	}

	public List<LingEvidence> getUtterancesForConcept(int cid) throws SQLException {
//		TODO: FIX THIS TO WORK VIA PROPERTIES
		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		Statement stat = createStatement();
		String query = "SELECT w1.word as word1, w2.word as word2 FROM sequence, word w1, word w2 WHERE w1.wid = sequence.left AND w2.wid = sequence.right AND cid = %d";
		ResultSet result = stat.executeQuery(String.format(query, cid));
		
		String prev = Constants.S_TAG;
		
		while (result.next()) {
			LingEvidence l = new LingEvidence();
			String w1 = result.getString("word1");
			l.addEvidence("w1", prev);
			l.addEvidence("w2", w1);
			l.addEvidence("w3", result.getString("word2"));
			ling.add(l);
			prev = w1;
		}
		
		return ling;
	}

	public int getNumberOfIntents() throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT count(*) c FROM intent"));
		return result.getInt("c");
	}

	public void attachConceptToIntent(String c, String i) throws SQLException {
		int cid = getConceptID(c);
		int iid = getIntentID(i);
		Statement stat = createStatement();
		stat.execute(String.format("INSERT INTO concept_intent (cid, iid) VALUES (%d, %d)", cid, iid));
	}

	public int getIntentID(String intent) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT iid FROM intent WHERE intent='%s'", intent));
		return result.getInt("iid");
	}

	public List<String> getConceptsForIntent(String intent) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct concept FROM intent i, concept c, concept_intent ci where c.cid = ci.cid AND i.iid = ci.iid and i.intent = '%s'", intent));
		List<String> concepts = new ArrayList<String>();
		while (result.next()) {
			concepts.add(result.getString("concept"));
		}
		return concepts;
	}

	public void offerNewConcept(String slotValue) throws SQLException {
		List<String> concepts = this.getConcepts();
		if (concepts.contains(slotValue)) return;
		cachedConcepts.add(slotValue);
		this.addNewConcept(slotValue);
	}
	
	public void offerNewIntent(String intent) throws SQLException {
		List<String> intents = this.getIntents();
		if (intents.contains(intent)) return;
		cachedIntents.add(intent);
		this.addNewIntent(intent);
	}
	
	public void offerNewConceptIntentAttachment(String concept, String intent) throws SQLException {
		if (!checkConceptIntentAttacmentExistence(concept, intent)) {
			cachedConceptIntentAttachments.add(concept + "_" + intent);
			attachConceptToIntent(concept, intent);
		}
	}

	private boolean checkConceptIntentAttacmentExistence(String concept, String intent) throws SQLException {
		if (!cachedConceptIntentAttachments.isEmpty())
			return cachedConceptIntentAttachments.contains(concept +"_" + intent);
		int cid = getConceptID(concept);
		int iid = getIntentID(intent);
		Statement stat = createStatement();
		ResultSet res = stat.executeQuery(String.format("SELECT * FROM concept_intent WHERE cid=%d AND iid=%d", cid, iid));
		if (res.isAfterLast()) return false;
		return true;
	}
	
	private boolean checkPropertyConceptAttacmentExistence(String property, String concept) throws SQLException {
		if (!cachedPropertyConceptAttachments.isEmpty()) {
			return cachedPropertyConceptAttachments.contains(property + "_" + concept);
		}
		int pid = getPropertyID(property);
		int cid = getConceptID(concept);
		Statement stat = createStatement();
		ResultSet res = stat.executeQuery(String.format("SELECT * FROM property_concept WHERE pid=%d AND cid=%d", pid, cid));
		if (res.isAfterLast()) return false;
		return true;
	}	

	public void offerNewProperty(String property) throws SQLException {
		List<String> properties = this.getProperties();
		if (properties.contains(property)) return;
		cachedProperties.add(property);
		this.addNewProperty(property);
	}

	private void addNewProperty(String property) throws SQLException {
		Statement stat = createStatement();
		property = property.replace(" ", "_");
		stat.execute(String.format("INSERT INTO property (property) VALUES ('%s')", property));		
	}

	private List<String> getProperties() throws SQLException {
		if (!cachedProperties.isEmpty())
			return new ArrayList<String>(cachedProperties);	
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct property FROM property"));
		List<String> properties = new ArrayList<String>();
		while (result.next()) {
			String prop = result.getString("property");
			properties.add(prop);
			cachedProperties.add(prop);
		}
		return properties;
	}

	private void attachPropertyToConcept(String p, String c) throws SQLException {
		int cid = getConceptID(c);
		int pid = getPropertyID(p);
		Statement stat = createStatement();
		stat.execute(String.format("INSERT INTO property_concept (pid, cid) VALUES (%d, %d)", pid, cid));
	}

	private int getPropertyID(String property) throws SQLException {
		Statement stat = createStatement();
		property = property.replace(" " , "_");
		ResultSet result = stat.executeQuery(String.format("SELECT pid FROM property WHERE property='%s'", property));
		return result.getInt("pid");
	}

	public void offerNewPropertyConceptAttachment(String p, String c) throws SQLException {
		if (!checkPropertyConceptAttacmentExistence(p, c)) {
			cachedPropertyConceptAttachments.add(p + "_" + c);
			attachPropertyToConcept(p, c);
		}
	}

	public Properties<Property<String>> getPropertiesForConcept(String concept) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT distinct property FROM property p, concept c, property_concept pc where c.cid = pc.cid AND p.pid = pc.pid and c.concept = '%s'", concept));
		Properties<Property<String>> properties =  new Properties<Property<String>>();
		while (result.next()) {
			properties.add(new Property<String>(result.getString("property")));
		}
		return properties;
	}

	public String getIntentForConcept(String concept) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("SELECT intent FROM intent i, concept c, concept_intent ci where c.cid = ci.cid AND i.iid = ci.iid and c.concept = '%s'", concept));
		return result.getString("intent");
		
	}

}
