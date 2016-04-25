package model.functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
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

public class MessageFunction extends IUModule  implements CustomFunction {
	
	@S4Component(type = GoogleASR.class)
	public final static String ASR = "asr";
	
	@S4String(defaultValue = "complete")
	public final static String KEYWORD = "keyword";
	
	private GoogleASR recognizer;
	private TreeModule treeModule;
	ArrayList<PushBuffer> listeners;
	LinkedList<String> wordStack;

	private String keyword;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		recognizer = (GoogleASR) ps.getComponent(ASR);
		keyword = ps.getString(KEYWORD);
		wordStack = new LinkedList<String>();
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
		System.out.println("EDITS: " +edits);
		for (EditMessage<? extends IU> edit : edits){
			switch (edit.getType()) {
			case ADD:
				String word = edit.getIU().toPayLoad();
				if (word.equals(keyword)) {
					recognizer.iulisteners.remove(this);
					recognizer.iulisteners.addAll(listeners);
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
		
		String message = "message:" + Constants.DELIMITER;
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
//		top.addChild(new Node(""));
		System.out.println(message);
		top.setName(message);

		treeModule.update();
	}

}
