package util;

import util.TaskUtils.Task;
import java.util.LinkedList;

import model.Constants;
import model.Frame;

import java.util.HashMap;

public class AdaptiveTask {
	
	HashMap<String,HashMap<String,LinkedList<String>>> completed;
	
	public AdaptiveTask() {
		completed = new HashMap<String,HashMap<String,LinkedList<String>>>();
	}
	
	public void registerFrame(Frame frame) {
		String domain = frame.getValueForIntent(Constants.INTENT);
		if (!completed.containsKey(domain)) 
			completed.put(domain, new HashMap<String,LinkedList<String>>());
		HashMap<String,LinkedList<String>> current = completed.get(domain);
		for (String intent : frame.getIntents()) {
			if (intent.equals(Constants.INTENT)) continue; // we've dealt with this
			if (!current.containsKey(intent)) current.put(intent, new LinkedList<String>());
			
			String concept = frame.getValueForIntent(intent);
			if (!current.get(intent).contains(concept))
				current.get(intent).push(concept);
		}
	}
	
	public HashMap<String,LinkedList<String>> predictProgression(String domain) {
		if (!completed.containsKey(domain)) return null;
		
		HashMap<String, LinkedList<String>> prediction = new HashMap<String, LinkedList<String>>();
		prediction.put("fill", new LinkedList<String>());
		prediction.put("confirm", new LinkedList<String>());
		
		HashMap<String,LinkedList<String>> current = completed.get(domain);
		
		for (String intent : current.keySet()) {
			if (current.get(intent).size() == 1)
				prediction.get("fill").add(intent+":"+current.get(intent).peek());
			else if (current.get(intent).size() > 1)
				prediction.get("confirm").add(intent+":"+current.get(intent).peek());
		}
		
		return prediction;
	}
	
//	public void pushTask(Task task) {
//		if (!completed.containsKey(task.domain))
//			completed.put(task.domain, new HashMap<String,LinkedList<String>>());
//		for (String intent : task.intentsConcepts.keySet())  {
//			if (!completed.get(task.domain).containsKey(intent))
//				completed.get(task.domain).put(intent, new LinkedList<String>());
//			LinkedList<String> values = task.intentsConcepts.get(intent);
////			if (!completed.get(task.domain).get(intent).contains(o)
//		}
//	}
	

}
