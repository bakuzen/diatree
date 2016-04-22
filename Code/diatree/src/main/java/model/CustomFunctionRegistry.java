package model;

import java.util.HashMap;

import model.functions.MessageFunction;

public class CustomFunctionRegistry {
	
	private static HashMap<String,Class<?>> functions;
	
	private static void init() {
//		TODO be able to load functions in some other way, like from a file
		functions = new HashMap<String,Class<?>>();
		functions.put("message", MessageFunction.class);
	}
	
	public static CustomFunction getNewFunction(String function) throws InstantiationException, IllegalAccessException {
		if (functions == null) init();
		return (CustomFunction) functions.get(function).newInstance();
	}

}
