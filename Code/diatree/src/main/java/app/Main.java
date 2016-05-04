package app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;


import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.apps.SimpleReco;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.PushBuffer;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.SphinxASR;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;
import jetty.AdvancedDiaTreeCreator;
import jetty.DiaTreeSocket;
import jetty.JettyServer;
import model.CustomFunctionRegistry;
import module.INLUModule;
import util.ClientUtils;

public class Main {
	
//	@S4Component(type = SphinxASR.class)
//	public final static String PROP_CURRENT_HYPOTHESIS = "currentASRHypothesis";
	
//	@S4ComponentList(type = PushBuffer.class)
//	public final static String PROP_HYP_CHANGE_LISTENERS = SphinxASR.PROP_HYP_CHANGE_LISTENERS;
	
	@S4Component(type = DiaTreeSocket.class)
	public final static String DIATREE_SOCKET = "diatree";
	
	GoogleASR webSpeech;
	private static PropertySheet ps;
//	private List<PushBuffer> hypListeners;
	List<EditMessage<IU>> edits = new ArrayList<EditMessage<IU>>();
	
	private void run() throws InterruptedException, PropertyException, IOException, UnsupportedAudioFileException {
		
//		EmbeddedTomcat tomcat = new EmbeddedTomcat();
		
		
		ConfigurationManager cm = new ConfigurationManager(new File("src/main/java/config/config.xml").toURI().toURL());
//		ps = cm.getPropertySheet(PROP_CURRENT_HYPOTHESIS);
//		hypListeners = ps.getComponentList(PROP_HYP_CHANGE_LISTENERS, PushBuffer.class);
		
//		INLUModule nlu = (INLUModule) cm.lookup("inlu");
//		tomcat.addServlet("diatree", (DiaTreeServlet) cm.lookup(DIATREE_SERVLET));
//		tomcat.start();
		AdvancedDiaTreeCreator creator = new AdvancedDiaTreeCreator((DiaTreeSocket) cm.lookup(DIATREE_SOCKET));
		JettyServer jetty = new JettyServer(creator);
		
		CustomFunctionRegistry cfr = (CustomFunctionRegistry) cm.lookup("registry");
		
		
//		for Sphinx ASR
//		SphinxASR webSpeech = (SphinxASR) cm.lookup(PROP_CURRENT_HYPOTHESIS);
//		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M"});
		
		
//		for Google ASR
		webSpeech = (GoogleASR) cm.lookup("googleASR");
		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M", "-G", "AIzaSyCDOkjKVuIKkMM_oic5gty7t91CWw8Y6ws"});
//		startGoogleASR(cm, rclp);
		ClientUtils.openNewClient();
		
		String[] uwords = {"route", "nein", "ehm", "ich", "route", "ehm", "ich",  "von", "ehm", "ich", "bielefeld", "ehm", "ich", "nein", "von", "hier", "ehm", "ich",  "nach", "berlin"};
//		String[] uwords = {"essen", "typ", "franzözisch","ja","wo", "stadtmitte", "rücksetzen", "anruf", "name", "michael",
//		String[] uwords = {"route", "von", "bielefeld", "nach", "trier"};
//		String[] uwords = {"essen", "typ", "franzözisch","ja","preis", "günstig", "wo", "stadtmitte", "rücksetzen", "anruf", "name", "michael",
//				"rücksetzen","nachricht", "jana", "rücksetzen"};
//		String[] uwords = {"nachricht",  "message", "nimm", "das", "rote", "kreuz","neben","dem","blauen","t", "ferkel", "name", "jana"};
//		String[] uwords = {"hunger", "nein", "essen", "stadtmitte", "teuer", "nein", 
//				           "teuer", "nein", "preis", "teuer","typ", "thai", 
//				           "nein", "nein", "nein", "nein", "nein", "handy", "nein"};
		List<String> words = Arrays.asList(uwords);
		Thread.sleep(2000);
//		
		WordIU prev = WordIU.FIRST_WORD_IU;
		for (String word : words) {
			WordIU wiu = new WordIU(word, prev, null);
			edits.add(new EditMessage<IU>(EditType.ADD, wiu));
			Thread.sleep(900);
			notifyListeners(new ArrayList<PushBuffer>(webSpeech.iulisteners));
			prev = wiu;
		}
//		List<String> words = Arrays.asList(uwords);
//		Thread.sleep(2000);
////		
//		WordIU prev = WordIU.FIRST_WORD_IU;
//		for (String word : words) {
//			WordIU wiu = new WordIU(word, prev, null);
//			edits.add(new EditMessage<IU>(EditType.ADD, wiu));
//			Thread.sleep(400);
//			notifyListeners(new ArrayList<PushBuffer>(webSpeech.iulisteners));
//			prev = wiu;
//		}
	}
	
	
	private void startGoogleASR(ConfigurationManager cm, RecoCommandLineParser rclp) {
		new Thread() {
		public void run() {
			
			try {
				SimpleReco simpleReco = new SimpleReco(cm, rclp);
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
					
					Thread.sleep(1000);
					webSpeech.shutdown();
//					simpleReco.shutdownMic();
				}
					
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			
		} catch (PropertyException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (UnsupportedAudioFileException e1) {
			e1.printStackTrace();
		}
	}
	}.start();
	
	}

	public static void main (String[] args) {
		try {
			new Main().run();
		} 
		catch (InterruptedException e) {
			e.printStackTrace();
		} 
		catch (PropertyException e) {
			e.printStackTrace();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} 
	}
	
	public void notifyListeners(List<PushBuffer> listeners) {
		if (edits != null && !edits.isEmpty()) {
			//logger.debug("notifying about" + edits);
			for (PushBuffer listener : listeners) {
				listener.hypChange(null, edits);
			}
			edits = new ArrayList<EditMessage<IU>>();
		}
	}

}
