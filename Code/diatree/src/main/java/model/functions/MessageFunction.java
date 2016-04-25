package model.functions;

import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import model.CustomFunction;
import module.TreeModule;

public class MessageFunction extends IUModule  implements CustomFunction {
	
	@S4Component(type = GoogleASR.class)
	public final static String ASR = "asr";
	
	GoogleASR recognizer;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		recognizer = (GoogleASR) ps.getComponent(ASR);
	}

	@Override
	public void run(TreeModule treeModule) {
		System.out.println("MADE IT TO ASR MESSAGE FUNCTION");
	}

	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {
		System.out.println("LEFT BUFFER UPDATE FROM MESSAGE FUNCTION " + edits);
		
	}

}
