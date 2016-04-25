package model;

import edu.cmu.sphinx.util.props.Configurable;
import module.TreeModule;

public interface CustomFunction extends Configurable {
	
	public void run(TreeModule treeModule);

}
