package app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.catalina.LifecycleException;

import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.apps.SimpleReco;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.PushBuffer;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.SphinxASR;
import servlet.DiaTreeServlet;
import tomcat.EmbeddedTomcat;

public class Main {
	
	@S4Component(type = SphinxASR.class)
	public final static String PROP_CURRENT_HYPOTHESIS = "currentASRHypothesis";	
	
	@S4Component(type = DiaTreeServlet.class)
	public final static String DIATREE_SERVLET = "diatree";
	
	
	private static PropertySheet ps;
	private List<PushBuffer> hypListeners;
	
	private void run() throws LifecycleException, InterruptedException, PropertyException, MalformedURLException {
		
		EmbeddedTomcat tomcat = new EmbeddedTomcat();
		
		ConfigurationManager cm = new ConfigurationManager(new File("src/main/java/config/config.xml").toURI().toURL());
		ps = cm.getPropertySheet(PROP_CURRENT_HYPOTHESIS);
		
		tomcat.addServlet("diatree", (DiaTreeServlet) cm.lookup(DIATREE_SERVLET));
		tomcat.start();
		
		SphinxASR webSpeech = (SphinxASR) cm.lookup(PROP_CURRENT_HYPOTHESIS);
//		GoogleASR webSpeech = (GoogleASR) cm.lookup("googleASR");
		
//		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M", "-G", "AIzaSyDXOjOCiM7v0mznDF1AWXXoR1ehqLeIB18"});
		RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M"});	
		
		new Thread(){ 
			public void run() {
				
				try {
					SimpleReco simpleReco = new SimpleReco(cm, rclp);
					simpleReco.recognizeInfinitely();
				} 
				catch (PropertyException e) {
					e.printStackTrace();
				} 
				catch (IOException e) {
					e.printStackTrace();
				} 
				catch (UnsupportedAudioFileException e) {
					e.printStackTrace();
				}
			}
		}.start();
		
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
		}
	}

}
