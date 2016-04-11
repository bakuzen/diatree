package model.iu;

import inpro.incremental.unit.IU;

public class ConfidenceIU extends IU {

	private double confidence;
	
	public ConfidenceIU(double c) {
		this.setConfidence(c);
	}
	
	@Override
	public String toPayLoad() {
		return getConfidence() + "";
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

}
