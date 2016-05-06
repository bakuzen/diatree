package util;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import module.TaskModule;


public class SigDial2IncrementalTimeout extends Thread {
	
	
	private static SigDial2IncrementalTimeout timeoutThread;
	private static int duration;
	private HashMap<String, String> responses = new HashMap<String,String>();
	static TaskModule taskModule;
	
	public static void setVariables(TaskModule task, int d) {
		duration = d;
		taskModule = task;
	}
	
//	singleton
	public static SigDial2IncrementalTimeout getInstance() {
		
		if (timeoutThread == null) {
			timeoutThread = new SigDial2IncrementalTimeout();
		}
		
		return timeoutThread;
	}
	
	public void reset() {
		timeoutThread.interrupt();
		timeoutThread = new SigDial2IncrementalTimeout();
		timeoutThread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(duration);
			display();
		} 
		catch (InterruptedException e) {
		}
	}
	
	private void display() {
		 EventQueue.invokeLater(new Runnable() {
		        
	            @Override
	            public void run() {
	        		JFrame frame = new JFrame();
	        		frame.setLayout( new GridLayout(0,5));

	        		frame.setSize(1000, 1000);
	        		
	        		String utt = "<html>Phase B Fertig!</html>";
	        		JLabel text = new JLabel(utt);
	        		text.setFont(new Font(text.getFont().getName(), Font.PLAIN, 36));
	        		frame.getContentPane().add(text);
	        		frame.getContentPane().add(new JLabel(""));
	        		frame.getContentPane().add(new JLabel(""));
	        		frame.getContentPane().add(new JLabel(""));
	        		frame.getContentPane().add(new JLabel(""));
	        		
	        		JLabel q1 = new JLabel();
	        		q1.setText("how did you like it?");
	        		frame.getContentPane().add(q1);
	        		getButtonGroup(".p1q1", frame);
	        		
	        		JLabel q2 = new JLabel();
	        		q2.setText("was it everything you hoped for?");
	        		frame.getContentPane().add(q2);
	        		getButtonGroup(".p1q2", frame);
	        		
	        		JButton done = new JButton("Fertig!");
	        		done.addActionListener(new ActionListener() {
	                    @Override
	                    public void actionPerformed(ActionEvent e) {
	                    	frame.setVisible(false);
	                    	frame.dispose();
	                    	taskModule.logResponses(responses);
	                    	System.exit(0);
	                    }
	                });
	        		frame.getContentPane().add(done);
	        		
	        		
	        		frame.pack();
	        		frame.setLocationRelativeTo(null);
	        		frame.setVisible(true);
	        		frame.requestFocus();
	            }

				private ButtonGroup getButtonGroup(String string, JFrame frame) {
					
					Listener l = new Listener();
					
					JRadioButton phaseA = new JRadioButton("Phase A");
					phaseA.setActionCommand("PhaseA"+string);
					phaseA.setSelected(false);
					phaseA.addActionListener(l);
					
					JRadioButton both = new JRadioButton("Beide");
					both.setActionCommand("Beide"+string);
					both.setSelected(false);
					both.addActionListener(l);
					
					JRadioButton neither = new JRadioButton("Keiner");
					neither.setActionCommand("Keiner"+string);
					neither.setSelected(false);
					neither.addActionListener(l);
					
					JRadioButton phaseB = new JRadioButton("Phase B");
					phaseB.setActionCommand("PhaseB"+string);
					phaseB.setSelected(false);
					phaseB.addActionListener(l);
					
					ButtonGroup group = new ButtonGroup();
				    group.add(phaseA);
				    group.add(both);
				    group.add(neither);
				    group.add(phaseB);
				    
				    frame.add(phaseA);
				    frame.add(both);
				    frame.add(neither);
				    frame.add(phaseB);
				    
				    return group;
				}

	        });
		
	}
	
	private class Listener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			String[] a = e.getActionCommand().split("\\.");
			responses.put(a[1], a[0]);
		}
	
	}
	
	
	
}
