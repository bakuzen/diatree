package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;

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
	        		q1.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q1.setText("<html>Die Darstellung war nützlich<br> und einfach zu vertehen.</html>");
	        		frame.getContentPane().add(q1);
	        		getButtonGroup(".p1q1", frame);
	        		
	        		JLabel q2 = new JLabel();
	        		q2.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q2.setText("<html>Der Assistent war einfach <br>und intuitiv zu benutzen.</html>");
	        		frame.getContentPane().add(q2);
	        		getButtonGroup(".p1q2", frame);
	        		
	        		JLabel q3 = new JLabel();
	        		q3.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q3.setText("<html>Der Assistent hat verstanden,<br> was ich sagen wollte.</html>");
	        		frame.getContentPane().add(q3);
	        		getButtonGroup(".p1q3", frame);
	        		
	        		JLabel q4 = new JLabel();
	        		q4.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q4.setText("<html>Ich habe immer verstanden, <br>was der Assistent von mir wollte.</html>");
	        		frame.getContentPane().add(q4);
	        		getButtonGroup(".p1q4", frame);
	        		
	        		JLabel q5 = new JLabel();
	        		q5.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q5.setText("<html>Der Assistent hat <br>viele Fehler gemacht.</html>");
	        		frame.getContentPane().add(q5);
	        		getButtonGroup(".p1q5", frame);	   
	        		
	        		JLabel q6 = new JLabel();
	        		q6.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q6.setText("<html>Der Assistent hat nicht<br> geantwortet, während ich redete.</html>");
	        		frame.getContentPane().add(q6);
	        		getButtonGroup(".p1q6", frame);	 	
	        		
	        		JLabel q7 = new JLabel();
	        		q7.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q7.setText("<html>Manchmal war ich mir nicht sicher,<br> ob der Assistent mich verstanden hat.</html>");
	        		frame.getContentPane().add(q7);
	        		getButtonGroup(".p1q7", frame);	 
	        		
	        		JLabel q8 = new JLabel();
	        		q8.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q8.setText("<html>Der Assistent hat geantwortet,<br> während ich redete.</html>");
	        		frame.getContentPane().add(q8);
	        		getButtonGroup(".p1q8", frame);	 	
	        		
	        		JLabel q9 = new JLabel();
	        		q9.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q9.setText("<html>Der Assistent tat manchmal Dinge,<br> die ich nicht erwartet habe.</html>");
	        		frame.getContentPane().add(q9);
	        		getButtonGroup(".p1q9", frame);	 	 
	        		
	        		JLabel q10 = new JLabel();
	        		q10.setBorder(BorderFactory.createLineBorder(Color.BLACK));
	        		q10.setText("<html><font size=\"3\">Wenn der Assistent einen Fehler gemacht hat,<br> war es für mich einfach, ihn zu korrigieren.</font></html>");
	        		frame.getContentPane().add(q10);
	        		getButtonGroup(".p1q10", frame);		        		
	        		
	        		
	        		
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
//	        		frame.setLocationRelativeTo(null);
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
