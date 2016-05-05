package module;

import java.util.Collection;
import java.util.List;

import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.incremental.IUModule;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;
import util.TaskUtils;

public class TaskModule extends IUModule {
	
	@S4Component(type = INLUModule.class)
	public final static String INLU_MODULE = "inlu";
	protected INLUModule inlu;

	private TaskUtils tasks;
	
	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		super.newProperties(ps);
		tasks = new TaskUtils(this);
		inlu = (INLUModule) ps.getComponent(INLU_MODULE);
		tasks.nextTask();
	}
	
	@Override
	protected void leftBufferUpdate(Collection<? extends IU> ius, List<? extends EditMessage<? extends IU>> edits) {

	}

	public void taskComplete() {
		inlu.resetSession();
		tasks.nextTask();
	}

}
