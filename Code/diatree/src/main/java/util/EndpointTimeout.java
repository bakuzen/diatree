package util;

import module.TreeModule;

public class EndpointTimeout extends Thread {
	
	
	private static EndpointTimeout timeoutThread;
	private static TreeModule tree;
	private static int duration;
	
	
	public static void setVariables(TreeModule m, int d) {
		duration = d;
		tree = m;
	}
	
//	singleton
	public static EndpointTimeout getInstance() {
		
		if (timeoutThread == null) {
			timeoutThread = new EndpointTimeout();
		}
		
		return timeoutThread;
	}
	
	public void reset() {
		timeoutThread.interrupt();
		timeoutThread = new EndpointTimeout();
		timeoutThread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(duration);
			if (tree != null)
				tree.update();
		} 
		catch (InterruptedException e) {
		}
	}
	
}
