package model;

import java.util.HashMap;
import java.util.Set;

import sium.nlu.stat.Distribution;

public class Frame {
	
	private HashMap<String,Distribution<String>> frame;
	
	public Frame() {
		frame = new HashMap<String,Distribution<String>>();
	}
	
	public void offerIntent(String intent) {
		if (!frame.containsKey(intent)) 
			frame.put(intent, new Distribution<String>());
	}
	
	public void add(String intent, String concept, double prob) {
		offerIntent(intent);
		frame.get(intent).addProbability(concept, prob);
	}
	
	public void normalizeAll() {
		for (String intent: frame.keySet()) {
			frame.get(intent).normalize();
		}
	}
	
	public Frame getArgMaxFrame() {
		Frame f = new Frame();
		for (String intent: frame.keySet()) {
			if (frame.get(intent).getEntropy() < Constants.CUTOFF)
				f.add(intent, frame.get(intent).getArgMax().getEntity(), frame.get(intent).getArgMax().getProbability());
		}
		return f;
	}
	
	public Set<String> getIntents() {
		return frame.keySet();
	}
	
	public String getValueForIntent(String intent) {
		return frame.get(intent).getArgMax().getEntity();
	}
	
	public double getConfidenceforIntent(String intent) {
		return frame.get(intent).getArgMax().getProbability();
	}
	
	public int size() {
		return frame.size();
	}
	
	public String toString() {
		return frame.toString();
	}

}
