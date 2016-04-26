package app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.catalina.LifecycleException;

import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import edu.cmu.sphinx.util.props.S4ComponentList;
import inpro.apps.SimpleReco;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.PushBuffer;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.SphinxASR;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.EditType;
import inpro.incremental.unit.IU;
import inpro.incremental.unit.WordIU;
import jetty.JettyServer;
import model.CustomFunctionRegistry;
import servlet.DiaTreeServlet;
import tomcat.EmbeddedTomcat;
import util.ClientUtils;

public class Main {
	
//	@S4Component(type = SphinxASR.class)
//	public final static String PROP_CURRENT_HYPOTHESIS = "currentASRHypothesis";
	
//	@S4ComponentList(type = PushBuffer.class)
//	public final static String PROP_HYP_CHANGE_LISTENERS = SphinxASR.PROP_HYP_CHANGE_LISTENERS;
	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "diatree";
	
	GoogleASR webSpeech;
	private static PropertySheet ps;
//	private List<PushBuffer> hypListeners;
	List<EditMessage<IU>> edits = new ArrayList<EditMessage<IU>>();
	
	private void run() throws LifecycleException, InterruptedException, PropertyException, IOException, UnsupportedAudioFileException {
		
//		EmbeddedTomcat tomcat = new EmbeddedTomcat();
		JettyServer jetty = new JettyServer();
		
		ConfigurationManager cm = new ConfigurationManager(new File("src/main/java/config/config.xml").toURI().toURL());
//		ps = cm.getPropertySheet(PROP_CURRENT_HYPOTHESIS);
//		hypListeners = ps.getComponentList(PROP_HYP_CHANGE_LISTENERS, PushBuffer.class);
		
//		tomcat.addServlet("diatree", (DiaTreeServlet) cm.lookup(DIATREE_SERVLET));
//		tomcat.start();
		
		CustomFunctionRegistry cfr = (CustomFunctionRegistry) cm.lookup("registry");
		


		
//		for Sphinx ASR
//		SphinxASR webSpeech = (SphinxASR) cm.lookup(PROP_CURRENT_HYPOTHESIS);
//		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M"});
		
		
//		for Google ASR
		
		webSpeech = (GoogleASR) cm.lookup("googleASR");
		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M", "-G", "AIzaSyDXOjOCiM7v0mznDF1AWXXoR1ehqLeIB18"});

		ClientUtils.openNewClient();
		
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
						
						Thread.sleep(10000);
						webSpeech.shutdown();
//						simpleReco.shutdownMic();
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
		
		

		
//		String[] uwords = {"i", "want", "some", "cheap", "thai", "yes", "food", "around", "downtown"};
//		String[] uwords = {"food", "indian", "no", "thai", "yes", "cheap", "downtown"};
//		String[] uwords = {"essen",  "günstig", "wo", "stadtmitte","typ", "franzözisch","ja", "rücksetzen", "anruf", "name", "michael",
//				"rücksetzen","nachricht", "jana", "rücksetzen"};
//		String[] uwords = {"nachricht",  "message", "nimm", "das", "rote", "kreuz","neben","dem","blauen","t", "ferkel", "name", "jana"};
//		List<String> words = Arrays.asList(uwords);
//		
//		WordIU prev = WordIU.FIRST_WORD_IU;
//		for (String word : words) {
//			WordIU wiu = new WordIU(word, prev, null);
//			edits.add(new EditMessage<IU>(EditType.ADD, wiu));
//			Thread.sleep(1000);
//			notifyListeners(new ArrayList<PushBuffer>(webSpeech.iulisteners));
//
//			prev = wiu;
//		}
		
	}
	
	public static void main (String[] args) {
		try {
			new Main().run();
		} 
		catch (LifecycleException e) {
			e.printStackTrace();
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
