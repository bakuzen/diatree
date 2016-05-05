package model;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import model.db.Domain;
import sium.nlu.context.Context;
import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.CoocurrenceMapping;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;
import util.LevenshteinDistance;

/*
 * This class is an interface between an user interface and SINLU. The idea is to be able to do some simple NLU
 * with a minimal amount of training data, but still scalable to using larger amounts of data. 
 */

public class DomainModel {
	
	private Mapping<String> mapping;
	private Domain db;
	private String domain;
	private LingEvidence ling;
	private Grounder<String,String> grounder;
	private Context<String, String> context;
	private boolean trainingComplete;
	
	public DomainModel(Domain db, String domain) {
		setTrainingComplete(false);
		grounder = new Grounder<String,String>(); // the grounder is part of SIUM
		ling = new LingEvidence(); // also part of SIUM
		mapping = new MaxEntMapping(Constants.BASE_FILE_PATH + domain + "/" + domain + ".txt"); // the thing that maps between language (in this case, ngrams) and high-level concepts
		this.setDB(db);
		this.setDomain(domain);
		updateContext(Constants.ROOT_NAME);
	}
	
	public void updateContext(String intent) {
		Context<String,String> context = new Context<String,String>();
		System.out.println("CURRENT INTENT " + intent);
		try {
			
//			intent for confirmations or aborts should always be there
			for (String concept : db.getConceptsForIntent(Constants.CONFIRM)) {
				Properties<Property<String>> properties = db.getPropertiesForConcept(concept);
				context.setEntity(concept, properties);
			}			
				
			for (String childIntent: db.getChildIntentsForIntent(intent)) {
				context.addPropertyToEntity(childIntent, childIntent);
				for (Property<String> prop : db.getPropertiesForConcept(childIntent))
					context.addPropertyToEntity(childIntent, prop.getProperty());
				for (String concept : db.getConceptsForIntent(childIntent)) {
					Properties<Property<String>> properties = db.getPropertiesForConcept(concept);
					context.setEntity(concept, properties);
				}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		setContext(context);
	}

	/*
	 * This takes information out of the database and trains a maxent model. The class labels are the properties of the concepts. The features
	 * are a bunch of ngrams. Essentially, we are training propery-level language models. 
	 */
	public void train() throws SQLException {
		for (Integer cid : db.getConceptIDs()) {
			Context<String,String> context = new Context<String,String>();
			String concept = db.getConcept(cid);
			Properties<Property<String>> properties = db.getPropertiesForConcept(concept);
			context.setEntity(concept, properties);
			for (LingEvidence ling : db.getUtterancesForConcept(cid)) {
				mapping.addEvidenceToTrain(ling, context.getPropertiesForEntity(concept));
			}
		}
		mapping.train();
		setTrainingComplete(true);
	}
	
	public void newUtterance() {
		grounder.clear();
		ling = new LingEvidence();
//		this.addIncrement(Constants.S_TAG);
	}
	
	public void setContext(Context<String,String> c) {
		this.context = c;
	}
	
	/*
	 * The Context object is from SIUM. It holds information about what can be predicted. In this case, 
	 * we want a distribution over all the concepts (from their properties). 
	 */
	public Context<String,String> getContext() {
		return context;
	}
	
	/*
	 * Words are evaluated individually; this keeps track of bigrams for features. 
	 */
	public Distribution<String> addIncrement(String word) {
		
		ling.addEvidence("w1", word);
//		Distribution<String> groundedResult = new Distribution<String>();
		Distribution<String> groundedResult = mapping.applyEvidenceToContext(ling);
//		did someone say a word that is the same spelling as a property? Give that property some credit. 
		if (getContext().getPropertiesSet().contains(word)) { 
			groundedResult.setProbabilityForItem(word, 1.0);
		}
		else {
//			try a probability derived from the Lev distance. This has to step through each of the properties, though. 
			for (String concept : getContext().getPropertiesSet()) {
				double lprob = LevenshteinDistance.getProbability(word, concept);
				if (lprob > Constants.WORD_DISTANCE_THRESHOLD) {
					groundedResult.setProbabilityForItem(concept, lprob);
				}
			}
		}
		grounder.groundIncrement(getContext(), groundedResult);
		return grounder.getPosterior();
		
	}
	
	public void revokeIncrement(String word) {
		grounder.undoStep();
	}
	
	

	public Frame getPredictedFrame() throws SQLException {
		
		Distribution<String> frameDist = new Distribution<String>(getPosterior());
		frameDist.normalize();
		
		
//		We take all the concepts and find what intent they belong to, then we return
//		a frame of intents (above a threshold) with a distribution (over concepts) for each intent value
		Frame frame = new Frame();
		
		for (DistRow<String> row : frameDist.getDistribution()) {
			List<String> intents = db.getIntentsForConcept(row.getEntity());
			for (String intent : intents)
				frame.add(intent, row.getEntity(), row.getProbability());
		}
		
		frame.normalizeAll();
		
		return frame;
		
	}
	
	public List<String> getPossibleIntents() {
		try {
			List<String> intents = this.db.getIntents();
			if (intents.contains(Constants.CONFIRM)) {
				intents.remove(Constants.CONFIRM);
			}
			return intents;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}



	public void close() {
		mapping.clear();
	}
	
	
	public Domain getDB() {
		return db;
	}
	
	public void setDB(Domain db) {
		this.db = db;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String d) {
		this.domain = d;
	}
	
	public Distribution<String> getPosterior() {
		return grounder.getPosterior();
	}
	
	public boolean isTrainingComplete() {
		return trainingComplete;
	}
	
	public void setTrainingComplete(boolean trainingComplete) {
		this.trainingComplete = trainingComplete;
	}
	
	public Distribution<String> getCombinedProperties() {
		return this.grounder.getCombinedProperties();
	}
//	
//	public Distribution<String> endUtterance() {
//		return addIncrement(Constants.S_TAG);
//	}

	public List<String> getPossibleIntentsForConcept(String intent) {
		try {
			return this.getDB().getConceptsForIntent(intent);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void getChildIntentsForIntent(String rootName) {
		// TODO Auto-generated method stub
		
	}

	

}
