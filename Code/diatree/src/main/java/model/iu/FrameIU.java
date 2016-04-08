package model.iu;

import inpro.incremental.unit.IU;
import model.Frame;

public class FrameIU extends IU {
	
	private Frame frame;
	
	public FrameIU(Frame frame) {
		this.setFrame(frame);
	}

	@Override
	public String toPayLoad() {
		return "frame:+ " + this.getID();
	}

	public Frame getFrame() {
		return frame;
	}

	public void setFrame(Frame frame) {
		this.frame = frame;
	}

}
