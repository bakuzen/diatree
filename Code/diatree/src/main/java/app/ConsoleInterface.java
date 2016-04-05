package app;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.varia.NullAppender;

import model.Constants;
import model.DomainModel;
import model.db.Domain;
import sium.nlu.stat.DistRow;


/*
 * This class is a simple console/terminal interface for creating, training, and evaluating simple NLU models. The main idea
 * is that it can yield respectable, or rather, useful results with very minimal training data. The underlying model works
 * incrementally (i.e., word by word) though this interface doesn't make use of that. 
 */

public class ConsoleInterface {
	
	private static DomainModel model;
	
	public static void main (String[] args) {
		
		// some of the supporting code needs this otherwise some warnings are thrown
		org.apache.log4j.BasicConfigurator.configure(new NullAppender());
		while (true) {
			try {
				topUserInput();
			} 
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Top level for a quaint console interface. Hopefully the method names are sufficient to
	 * understand what is going on.
	 */
	private static void topUserInput() throws SQLException {
		System.out.println("Pick a domain by typing its name, create (c) new domain, or exit (x).");
		
		Domain db = new Domain();
		
		List<String> domains = db.getDomains();
		System.out.println("Existing domains:");
		for (String domain : domains) System.out.println(domain);
		
		String s = getUserInput();
		
		if ("x".equals(s)) {
			System.exit(0);
		}
		
		if ("c".equals(s)) {
			domainCreationInterface(domains, db);
		}
		
		else if (domains.contains(s)) {
			domainInterface(s, db);
		}	
		
		else {
			System.out.println("Unknown command: " + s);
		}
	}

	/*
	 * Called when a specific domain is chosen to work with. 
	 */
	private static void domainInterface(String d, Domain db) throws SQLException {
		while (true) {
			
			db.setDomain(d);
			model = new DomainModel(db, d);
			int numIntents = 0;
			try { 
				numIntents = db.getNumberOfIntents();
			} 
			catch (SQLException e) {
				
			}
			System.out.println("Using domain: " + d);
			System.out.println("Number of intents: " + numIntents);
			System.out.println("add new intent (i) \nadd new concept (c) \nattach concept to intent (a)\nadd example utterance to concept (u) \ntrain domain (t),\nevaluate domain (e) \nreturn to top menu (r)"
					+ "\nrun batch evaluation (b)");
			
			switch (getUserInput()) {
			case "r":
				return;
			case "i":
				addNewIntent(db);
				break;
			case "c":
				addNewConcept(db);
				break;
			case "a":
				attachConceptToIntent(db);
				break;
			case "u":
				addNewUtterance(db);
				break;
			case "t":
				train(db, d);
				break;
			case "e":
				evaluate(db, d);
				break;
			case "b":
				try {
					batch(db, d);
				} 
				catch (FileNotFoundException e) {
					System.out.println("Test file not found for domain " + d);
				}
				break;
			}
		
		}
	}

	private static void attachConceptToIntent(Domain db) throws SQLException {
		List<String> intents = printIntents(db);
		System.out.println("Type intent to attach concept to:");
		String i = getUserInput();
		while (!intents.contains(i)) {
			System.out.println(i + " does not exist as an intent. See above intents.");
			i = getUserInput();
		}
		
		List<String> concepts = printConcepts(db);
		System.out.println("Type concept to attach to intent " + i + ":");
		String c = getUserInput();
		while (!concepts.contains(c)) {
			System.out.println(c + " does not exist as a concept. See above concepts.");
			c = getUserInput();
		}	
		
		db.attachConceptToIntent(c, i);
		
		
	}
	
	private static List<String> printIntents(Domain db) throws SQLException {
		List<String> intents = db.getIntents();
		System.out.println("Existing intents:");
		for (String concept : intents) System.out.println(concept);
		return intents;
	}

	private static void addNewIntent(Domain db) throws SQLException {
		List<String> intents = printIntents(db);
		System.out.println("Type new intent name.");
		String c = getUserInput();
		if (intents.contains(c)) {
			System.out.println("Intent " + c +  " already exists.");
			return;
		}
		db.addNewIntent(c);		
	}

	/*
	 * Called when someone wants to run a batch test. The batch test assumes that there is a file called
	 * "test" within the domain folder (i.e., domains/<nameofdomain>/test) where the data are in two columns, 
	 * the first being a concept, the second being an utterance, delimited by a \t, e.g. in the food domain:
	 * japanese I like ramen
	 * thai	I am looking for some thai food in this area
	 */
	private static void batch(Domain db, String d) throws FileNotFoundException {
		
		Scanner scan = new Scanner(new File(Constants.BASE_FILE_PATH + d + "/test"));
		double correct = 0.0;
		double total = 0.0;
		System.out.println("Running batch evaluation.");
		while (scan.hasNext()) {
			ArrayList<String> t = new ArrayList<String>(Arrays.asList(scan.nextLine().split("\\t")));
			String guess = printAndGetArgMax(t.get(1), false);
			if (t.get(0).equals(guess)) correct++;
			total++;
		}
		
		System.out.println(String.format("%.0f correct out of %.0f, %.2f accuracy.\n\n", correct, total, correct/total*100.0));
		scan.close();
	}

	/*
	 * This adds a new utterance that is actually used for training a model for this domain. 
	 */
	private static void addNewUtterance(Domain db) throws SQLException {
		System.out.println("Please choose a concept for adding example utterances:");
		List<String> concepts = db.getConcepts();
		if (concepts.isEmpty()) {
			System.out.println("Please create a concept first.");
			return;
		}
		System.out.println("Existing concepts:");
		for (String concept : concepts) System.out.println(concept);
		String c = getUserInput();
		while (!concepts.contains(c)) {
			System.out.println(c + " doesn't exist. Please choose an existing concept or return (r).");
			c = getUserInput();
			if ("r".equals(c) || "".equals(c)) return;
		}
		System.out.println("Using concept: " + c);
		
		while (true) {
			System.out.println("Add new utterance (please don't use puncutation) or return (r).");
			String utt = getUserInput();
			if ("r".equals(utt) || "".equals(utt)) return;
			db.addExampleForConcept(c, utt.replaceAll("[^a-zA-Z ]", "").toLowerCase());
		}
		
	}

	/*
	 * This displays a distribution over concepts given an utterance that a user wants to evaluate. 
	 */
	private static void evaluate(Domain db, String d) {
//		check to make sure the model file works before moving on
		if (!(new File(Constants.BASE_FILE_PATH + d + "/" + d + "Model.txt").exists())) {
			System.out.println("Training model before evaluation...");
			train(db, d);
		}
		System.out.println("Please type in an utterance to evaluate (please do not use punctuation!).");
		String c = getUserInput().replaceAll("[^a-zA-Z ]", "").toLowerCase();
		if ("".equals(c)) {
			System.out.println("Can't do anything with an empy string.");
			return;
		}
		printAndGetArgMax(c, true);
	}

	/*
	 * Supporting method to print distirbution and return the argmax
	 */
	private static String printAndGetArgMax(String c, boolean print) {
		if (print) System.out.println("Distribution over concepts:");
		model.newUtterance();
		ArrayList<String> utt = new ArrayList<String>(Arrays.asList(c.split("\\s+")));
		try {
		   for (String word : utt) model.addIncrement(word);
		} 
		catch (Exception e) {
			System.out.println("Something is wrong with the model file. Try training again.");
			return null;
		}
		model.getPosterior().normalize();
		for (DistRow<String> row : model.getPosterior().getDistribution()) {
			if (print) System.out.println(row.getEntity() + " " + row.getProbability());
		}
		try {
			System.out.println(model.getPredictedFrame());
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		if (print) System.out.println("\n\n");
		return model.getPosterior().getArgMax().getEntity();
	}

	/*
	 * trains the model
	 */
	private static void train(Domain db, String d) {
		model = new DomainModel(db, d);
		try {
			model.train();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Training done.");
	}

	/*
	 * Inserts a new concept.
	 */
	private static void addNewConcept(Domain db) throws SQLException {
		List<String> concepts = printConcepts(db);
		System.out.println("Type new concept name.");
		String c = getUserInput();
		if (concepts.contains(c)) {
			System.out.println("Concept " + c + " already exists.");
			return;
		}
		db.addNewConcept(c);
	}

	private static List<String> printConcepts(Domain db) throws SQLException {
		List<String> concepts = db.getConcepts();
		System.out.println("Existing concepts:");
		for (String concept : concepts) System.out.println(concept);
		return concepts;
	}

	/*
	 * Creates a new domain by making a subfolder in the domains folder and adds a new
	 * sqlite database.
	 */
	private static void domainCreationInterface(List<String> domains, Domain db) throws SQLException {
		
		System.out.println("Please choose a name for your domain:");
		String s = getUserInput();
		
		while (domains.contains(s) || "".equals(s)) {
			System.out.println(s + "Please choose another name.");
			s = getUserInput();
		}
		db.createNewDomain(s);
		
	}
	
	/*
	 * Gets the user input from the console; used by a number of methods here.
	 */
	private static String getUserInput() {
		Scanner in = new Scanner(System.in);
		String s = in.nextLine();
		//in.close();
		return s;
	}

}
