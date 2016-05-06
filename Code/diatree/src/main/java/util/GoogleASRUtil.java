package util;

import java.io.IOException;

import javax.sound.sampled.UnsupportedAudioFileException;

import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import inpro.apps.SimpleReco;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.source.GoogleASR;

public class GoogleASRUtil {
	
	static ConfigurationManager cm;
	static RecoCommandLineParser rclp;
	static GoogleASR webSpeech;
	
	static Thread speechThread;
	static SimpleReco simpleReco;
	
	public static void setVars(ConfigurationManager c, RecoCommandLineParser r, GoogleASR w) {
		cm = c;
		rclp = r;
		webSpeech = w;
	}
	
	
	
	public static void startGoogleASR() {
		
		if (speechThread != null && speechThread.isAlive()) {
			webSpeech.shutdown();
//			if (simpleReco != null) simpleReco.shutdownMic();
			speechThread.interrupt();
			
		}
		
		speechThread = new Thread() {
		public void run() {
			
			try {
				

				simpleReco = new SimpleReco(cm, rclp);
			while (true) {
				try {
					

					new Thread(){ 
						public void run() {
							try {
								simpleReco.recognizeOnce();
							} 
							catch (PropertyException e) {
								e.printStackTrace();
							} 
							
						}
					}.start();
					
					Thread.sleep(10000);
					webSpeech.shutdown();
//					simpleReco.shutdownMic();
				}
					
				catch (InterruptedException e) {
//					e.printStackTrace();
				}
			}
			
		}  catch (PropertyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}
		catch (RuntimeException e) {
			
		}
	}
	};
	speechThread.start();
	
	}

}
