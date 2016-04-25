package model;

import java.util.HashMap;
import java.util.List;

import edu.cmu.sphinx.util.props.Configurable;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4ComponentList;
import edu.cmu.sphinx.util.props.S4StringList;
import model.functions.MessageFunction;
import module.opendial.ConfigurableModule;

public class CustomFunctionRegistry implements Configurable {
	
	@S4ComponentList(type = CustomFunction.class)
	public final static String FUNCTION_COMPONENTS = "customFunctionComponents";
	
	@S4StringList()
	public final static String FUNCTION_STRINGS = "customFunctionNames";
	
	private static HashMap<String,CustomFunction> functions;
	

	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		List<String> names = ps.getStringList(FUNCTION_STRINGS);
		List<CustomFunction> f = ps.getComponentList(FUNCTION_COMPONENTS, CustomFunction.class);
		functions = new HashMap<String,CustomFunction>();
		for (int i=0; i<names.size(); i++) {
			functions.put(names.get(i), f.get(i));
		}
		
	}
	
	public static CustomFunction getFunction(String function) throws InstantiationException, IllegalAccessException {
		return (CustomFunction) functions.get(function);
	}


}
