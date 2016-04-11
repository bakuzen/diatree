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
	
	public double getEntropyForIntent(String intent) {
		return frame.get(intent).getEntropy();
	}
	
	public Frame getArgMaxFrame() {
		return getArgMaxFrame(Constants.CUTOFF);
	}
	
	public Frame getArgMaxFrame(double cutoff) {
		Frame f = new Frame();
		for (String intent: frame.keySet()) {
			if (frame.get(intent).getConfidence() > cutoff)
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

	public Distribution<String> getDistributionForIntent(String intent) {
		return this.frame.get(intent);
	}

}
