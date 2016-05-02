package util;

import module.INLUModule;

public class SessionTimeout extends Thread {
	
	
	private static SessionTimeout timeoutThread;
	private static INLUModule inlu;
	private static int duration;
	
	
	public static void setVariables(INLUModule m, int d) {
		duration = d;
		inlu = m;
	}
	
//	singleton
	public static SessionTimeout getInstance() {
		
		if (timeoutThread == null) {
			timeoutThread = new SessionTimeout();
		}
		
		return timeoutThread;
	}
	
	public void reset() {
		timeoutThread.interrupt();
		timeoutThread = new SessionTimeout();
		timeoutThread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(duration);
			inlu.resetSession();
		} 
		catch (InterruptedException e) {
		}
	}
	
}
