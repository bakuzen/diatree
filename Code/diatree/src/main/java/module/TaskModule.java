package module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.SlotIU;
import model.Constants;
import model.Frame;
import sium.nlu.stat.Distribution;
import util.TaskUtils;

public class TaskModule extends IUModule {
	
	@S4Component(type = INLUModule.class)
	public final static String INLU_MODULE = "inlu";
	protected INLUModule inlu;
	private boolean isAdaptive;
	private TaskUtils tasks;
	private LinkedList<String> toConfirm;
	
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		tasks = new TaskUtils(this);
		inlu = (INLUModule) ps.getComponent(INLU_MODULE);
		setAdaptive(false);
		tasks.nextTask();
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		if (!this.isAdaptive()) return;
		// 	the task begins after they start the initial intent (e.g., essen or nachricht)
		
//		we may need to send these one at a time, but for now put everything in the newEdits
		List<EditMessage<? extends IU>> newEdits = new ArrayList<EditMessage<? extends IU>>();
		
		for (EditMessage<? extends IU> edit : edits){
			SlotIU decisionIU = (SlotIU) edit.getIU();
			SlotIU slotIU = (SlotIU) edit.getIU().groundedIn().get(0);
			String concept = slotIU.getDistribution().getArgMax().getEntity();
			String intent = slotIU.getName();
			Double confidence = slotIU.getConfidence();
			Distribution<String> dist = slotIU.getDistribution();
			if (intent.equals(Constants.INTENT)) {
				HashMap<String, LinkedList<String>> progression = tasks.predictProgression(concept);
				System.out.println("PROGRESSION: " + progression);
				if (progression == null) continue;
//				first, get the stuff to be filled, and fill everything
				for (String tofill : progression.get("fill")) {
					addEdit(tofill, "select", newEdits);
				}
				toConfirm = progression.get("confirm");
				if (toConfirm != null && !toConfirm.isEmpty()) {
					String tofill = toConfirm.pop();
					addEdit(tofill, "confirm", newEdits);
				}
			}
			
			if (intent.equals(Constants.CONFIRM) && concept.equals(Constants.YES)) {
				if (toConfirm != null && !toConfirm.isEmpty()) {
					String tofill = toConfirm.pop();
					addEdit(tofill, "confirm", newEdits);
				}
			}
			
			
			break; // to hack this, only show one expansion at a time 
		}
		
		if (!newEdits.isEmpty()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		rightBuffer.setBuffer(newEdits);
		
	}


	private void addEdit(String tofill, String string, List<EditMessage<? extends IU>> newEdits) {
		String[] ic = tofill.split(":");
		String i = ic[0];
		String c = ic[1];
		Distribution<String> inluDist = new Distribution<String>();
		inluDist.addProbability(c, 1.0);
		SlotIU inluIU = new SlotIU();
		inluIU.setName(i);
		inluIU.setDistribution(inluDist);
		
		Distribution<String> decDist = new Distribution<String>();
		decDist.addProbability(string, 1.0);
		SlotIU decIU = new SlotIU();
		decIU.setName("decision");
		decIU.setDistribution(decDist);
		decIU.groundIn(inluIU);
		newEdits.add(new EditMessage<SlotIU>(EditType.ADD, decIU));
		
	}

	public void taskComplete() {
//		need to get the final slots that were filled
		Frame frame = inlu.getFilledFrame();
		tasks.registerFrame(frame);
//		TODO: need to log the task, the updates to the tree, and the final filled slots
		inlu.resetSession();
		tasks.nextTask();
	}

	public void logResponses(HashMap<String, String> responses) {
		System.out.println("RESPONSES:" + responses);
		
	}

	public boolean isAdaptive() {
		return isAdaptive;
	}

	public void setAdaptive(boolean isAdaptive) {
		this.isAdaptive = isAdaptive;
	}

	public boolean isInFunction() {
		return inlu.isInFunction();
	}
	
}
