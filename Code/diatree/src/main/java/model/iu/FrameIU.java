package model.iu;

import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import model.Frame;

public class FrameIU extends IU {
	
	private Frame frame;
	
	public FrameIU(Frame frame) {
		this.setFrame(frame);
	}

	@Override
	public String toPayLoad() {
		return "frame: " + this.getID();
	}

	public Frame getFrame() {
		return frame;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

	public ConfidenceIU getConfidenceIUForIntent(String intent) {
		SlotIU slotIU = new SlotIU(intent, this.getFrame().getDistributionForIntent(intent));
		ConfidenceIU confIU = new ConfidenceIU(this.getFrame().getConfidenceforIntent(intent));
		confIU.groundIn(slotIU);
		slotIU.ground(confIU);
		
		return confIU;
	}

	public SlotIU getSlotIUForIntent(String intent) {
		return new SlotIU(intent, this.getFrame().getDistributionForIntent(intent));
	}

}
