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
import inpro.apps.SimpleText;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.PushBuffer;
import inpro.incremental.processor.TextBasedFloorTracker;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.IUDocument;
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
import module.TaskModule;
import util.ClientUtils;
import util.GoogleASRUtil;
import util.SigDial2IncrementalTimeout;

public class SigDial2Incremental {
	
//	@S4Component(type = SphinxASR.class)
//	public final static String PROP_CURRENT_HYPOTHESIS = "currentASRHypothesis";
	
//	@S4ComponentList(type = PushBuffer.class)
//	public final static String PROP_HYP_CHANGE_LISTENERS = SphinxASR.PROP_HYP_CHANGE_LISTENERS;
	
	@S4Component(type = DiaTreeSocket.class)
	public final static String DIATREE_SOCKET = "diatree";
	
	@S4Component(type = TextBasedFloorTracker.class)
	public final static String PROP_FLOOR_MANAGER = "textBasedFloorTracker";
	
	GoogleASR webSpeech;
	private static PropertySheet ps;
//	private List<PushBuffer> hypListeners;
	List<EditMessage<IU>> edits = new ArrayList<EditMessage<IU>>();
	
	private static IUDocument iuDocument;
	private List<PushBuffer> hypListeners;
	
	private void run() throws InterruptedException, PropertyException, IOException, UnsupportedAudioFileException {
		
//		EmbeddedTomcat tomcat = new EmbeddedTomcat();
		
		
		ConfigurationManager cm = new ConfigurationManager(new File("src/main/java/config/config.xml").toURI().toURL());
		cm.setGlobalProperty("isIncremental", "true");
//		ps = cm.getPropertySheet(PROP_CURRENT_HYPOTHESIS);
//		hypListeners = ps.getComponentList(PROP_HYP_CHANGE_LISTENERS, PushBuffer.class);
		TaskModule task = (TaskModule) cm.lookup("task");
		SigDial2IncrementalTimeout.setVariables(task, 5 * (60 * 1000));
		SigDial2IncrementalTimeout.getInstance().reset(); // start the timer
//		INLUModule nlu = (INLUModule) cm.lookup("inlu");
		
		AdvancedDiaTreeCreator creator = new AdvancedDiaTreeCreator((DiaTreeSocket) cm.lookup(DIATREE_SOCKET));
		JettyServer jetty = new JettyServer(creator);
		
		CustomFunctionRegistry cfr = (CustomFunctionRegistry) cm.lookup("registry");
		
		
//		for Sphinx ASR
//		SphinxASR webSpeech = (SphinxASR) cm.lookup(PROP_CURRENT_HYPOTHESIS);
//		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M"});
		
		
//		for Google ASR
		webSpeech = (GoogleASR) cm.lookup("googleASR");
		

//		TextBasedFloorTracker textBasedFloorTracker = (TextBasedFloorTracker) cm.lookup(PROP_FLOOR_MANAGER);
//		iuDocument = new IUDocument();
//		iuDocument.setListeners(webSpeech.iulisteners);
//		SimpleText.createAndShowGUI(webSpeech.iulisteners, textBasedFloorTracker);
		
		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M", "-G", "INSERT_GOOGLE_ASR_KEY_HERE"});
		GoogleASRUtil.setVars(cm, rclp, webSpeech);
		GoogleASRUtil.startGoogleASR();		
//		ClientUtils.openNewClient();
		
		WordIU wiu = new WordIU("phase:incr", null, null);
		edits.add(new EditMessage<IU>(EditType.ADD, wiu));
		notifyListeners(new ArrayList<PushBuffer>(webSpeech.iulisteners));
		
//		Or, one can send words individually with a 500 ms pause between them
//		String[] uwords = {"message", "peter", "nimm", "das", "kreuz"};
//		List<String> words = Arrays.asList(uwords);
//		Thread.sleep(2000);
//		
//		WordIU prev = WordIU.FIRST_WORD_IU;
//		for (String word : words) {
//			WordIU wiu = new WordIU(word, prev, null);
//			edits.add(new EditMessage<IU>(EditType.ADD, wiu));
//			Thread.sleep(400);
//			notifyListeners(new ArrayList<PushBuffer>(webSpeech.iulisteners));
//			prev = wiu;
//		}
	}
	

	public static void main (String[] args) {
		try {
			new SigDial2Incremental().run();
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
