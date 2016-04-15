package util;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class ClientUtils {

	private static Thread clientStartThread;
	
	public static void openNewClient() {
		
		if (clientStartThread != null && clientStartThread.isAlive()) return;
		
		clientStartThread  = new Thread(){ 
			public void run() {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
	
		clientStartThread.start();
		
		
		new Thread(){ 
				public void run() {
			
					if(Desktop.isDesktopSupported())
					{
					  try {
						Desktop.getDesktop().browse(new URI("http://localhost:8080"));
					} catch (IOException e) {
						e.printStackTrace();
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
					}
				}
		}.start();
		
	}

}
