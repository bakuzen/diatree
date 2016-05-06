package model.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4Integer;
import edu.cmu.sphinx.util.props.S4String;
import inpro.incremental.IUModule;
import inpro.incremental.PushBuffer;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.SphinxASR;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import model.Constants;
import model.CustomFunction;
import model.Node;
import module.TreeModule;
import util.EndpointTimeout;
import util.SessionTimeout;

public class MessageFunction extends IUModule  implements CustomFunction {
	
	@S4Component(type = GoogleASR.class)
	public final static String ASR = "asr";
	
	@S4String(defaultValue = "complete")
	public final static String KEYWORD = "keyword";
	
	@S4Integer (defaultValue = 1200)
	public final static String TIMEOUT = "timeout";
	
	private GoogleASR recognizer;
	private TreeModule treeModule;
	ArrayList<PushBuffer> listeners;
	LinkedList<String> wordStack;
	private MessageTimeout timeoutThread;
	private int timeoutDuration;

	private String keyword;
	
	
	private class MessageTimeout extends Thread {

		@Override
		public void run() {
			try {
				Thread.sleep(timeoutDuration);
				recognizer.iulisteners.clear();
				recognizer.iulisteners.addAll(listeners);
				treeModule.returnFromCustomFunction();
				treeModule.update();
			} 
			catch (InterruptedException e) {
			}
		}
		
	}
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		timeoutDuration = ps.getInt(TIMEOUT);
		recognizer = (GoogleASR) ps.getComponent(ASR);
		keyword = ps.getString(KEYWORD);
		wordStack = new LinkedList<String>();
		timeoutThread = new MessageTimeout();
	}

	@Override
	public void run(TreeModule treeModule) {
			wordStack.clear();
			this.treeModule = treeModule;
			listeners = new ArrayList<PushBuffer>(recognizer.iulisteners);
			recognizer.iulisteners.clear();
			recognizer.iulisteners.add(this);
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		
		SessionTimeout.getInstance().reset();
		if (!treeModule.isIncremental()) EndpointTimeout.getInstance().reset();
		
		if (timeoutThread != null) {
			timeoutThread.interrupt();
			timeoutThread = new MessageTimeout();
			timeoutThread.start();
		}
			
		
		for (EditMessage<? extends IU> edit : edits){
			switch (edit.getType()) {
			case ADD:
				String word = edit.getIU().toPayLoad();
				if (word.equals(keyword)) {
					recognizer.iulisteners.remove(this);
					recognizer.iulisteners.addAll(listeners);
					if (treeModule == null) return;
					treeModule.returnFromCustomFunction();
					treeModule.update();
					return;
				}
				wordStack.addLast(word);
				break;
			case COMMIT:
				break;
			case REVOKE:
				if (wordStack.isEmpty()) continue;
				wordStack.pollLast();
				break;
			default:
				break;
			}
		}
		
		String message = Constants.MESSAGE + ":" + Constants.DELIMITER;
		int numWords = 0;
		for (String word : wordStack) {
			String delim = " ";
			if (numWords > Constants.MAX_NUM_WORDS){
				numWords = 0;
				delim = Constants.DELIMITER;
			}
			numWords++;
			message += word + delim;
		}
		
		Node top = treeModule.getTopNode();
		top.setIden(Constants.MESSAGE);
//		top.addChild(new Node(""));
		top.setName(message);

		if (!EndpointTimeout.getInstance().isAlive() && treeModule.isIncremental())
			treeModule.update();
	}

}
